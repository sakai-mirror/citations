package edu.indiana.lib.twinpeaks.util;

import java.lang.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class StatusUtils
{

private static org.apache.commons.logging.Log	_log = LogUtils.getLog(StatusUtils.class);
	/**
	 * Set up initial status information (done before LOGON)
	 */
	public static void initialize(SessionContext sessionContext, String targets)
	{
		StringTokenizer parser 		= new StringTokenizer(targets);
		ArrayList				dbList		= new ArrayList();
		HashMap					targetMap	= getNewStatusMap(sessionContext);

		/*
		 * Establish the DB list and initial (pre-LOGON) status
		 */
		while (parser.hasMoreTokens())
		{
			String 	db 				= parser.nextToken();
			HashMap emptyMap 	= new HashMap();

			/*
			 * Empty status entry
			 */
			emptyMap.put("STATUS", "INACTIVE");
			emptyMap.put("STATUS_MESSAGE", "<none>");

			emptyMap.put("HITS", "0");
			emptyMap.put("ESTIMATE", "0");
			emptyMap.put("MERGED", "0");
			/*
			 * Save
			 */
      dbList.add(db);
      targetMap.put(db, emptyMap);
		}

		sessionContext.put("TARGETS", dbList);
		sessionContext.putInt("active", 0);

		sessionContext.put("STATUS", "INACTIVE");
		sessionContext.put("STATUS_MESSAGE", "<none>");
	}

	/**
	 * Get an iterator into the system status map
	 * @param sessionContext Active SessionContext
	 * @return Status map Iterator
	 */
	public static Iterator getStatusMapEntrySetIterator(SessionContext sessionContext)
	{
		HashMap statusMap = (HashMap) sessionContext.get("searchStatus");
		Set			entrySet	= Collections.EMPTY_SET;

		if (statusMap != null)
		{
			entrySet = statusMap.entrySet();
		}
		return entrySet.iterator();
	}

	/**
	 * Get the status entry for a specified target database
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Status Map for this target (null if none)
	 */
	public static HashMap getStatusMapForTarget(SessionContext sessionContext,
																					    String target)
	{
		HashMap statusMap = (HashMap) sessionContext.get("searchStatus");

		return (statusMap == null) ? null : (HashMap) statusMap.get(target);
	}

	/**
	 * Create a new status map
	 * @param sessionContext Active SessionContext
	 * @return Status Map for this target
	 */
	public static HashMap getNewStatusMap(SessionContext sessionContext)
	{
		HashMap statusMap = new HashMap();

		sessionContext.remove("searchStatus");
		sessionContext.put("searchStatus", statusMap);

		return statusMap;
	}

	/**
	 * Set global status (effects all target databases)
	 * @param sessionContext Active SessionContext
	 * @param status One of ERROR | DONE
	 * @param message Status text
	 */
	public static void setGlobalStatus(SessionContext sessionContext,
											 						   String status, String message)
	{
		/*
		 * Set global status
		 */
		sessionContext.put("STATUS", status);
		sessionContext.put("STATUS_MESSAGE", message);
		/*
		 * Per-target status
		 */
		for (Iterator iterator = StatusUtils.getStatusMapEntrySetIterator(sessionContext); iterator.hasNext(); )
		{
			Map.Entry entry 			= (Map.Entry) iterator.next();
			HashMap		targetMap 	= (HashMap) entry.getValue();

			targetMap.put("STATUS", status);
			targetMap.put("STATUS_MESSAGE", message);
		}
	}

	/**
	 * Set global error status (effects all target databases)
	 * @param sessionContext Active SessionContext
	 * @param error Error number
	 * @param message Expanded error text (null to omit expanded message)
	 */
	public static void setGlobalError(SessionContext sessionContext,
											 						  String error, String message)
	{
		String	statusMessage 	= "Error " + error;

		if (!StringUtils.isNull(message))
		{
			statusMessage += ": " + message;
		}
		setGlobalStatus(sessionContext, "ERROR", statusMessage);
	}

	/**
	 * Set all status value to "search complete" (effects all target databases)
	 * @param sessionContext Active SessionContext
	 */
	public static void setAllComplete(SessionContext sessionContext)
	{
		setGlobalStatus(sessionContext, "DONE", "Search complete");
	}

	/**
	 * Update the hit count for this target (database)
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Updated hit count
	 */
	public static int updateHits(SessionContext sessionContext,
															 String target)
	{
		Map				targetMap;
		String		hits;
		int				total, estimate;

		if (StringUtils.isNull(target))
		{
			throw new SearchException("No target database to update");
		}

		if ((targetMap = getStatusMapForTarget(sessionContext, target)) == null)
		{
			throw new SearchException("No status map for target database " + target);
		}
  	_log.debug("Map for target " + target + ": " + targetMap);
		/*
		 * Update total hits from this search source
		 */
		hits 	= (String) targetMap.get("HITS");
		total = Integer.parseInt(hits) + 1;

		targetMap.put("HITS", String.valueOf(total));
		/*
		 * Have we collected all available results?
		 */
		estimate = Integer.parseInt((String) targetMap.get("ESTIMATE"));
		if (estimate == total)
		{
			int active = sessionContext.getInt("active");
			/*
			 * If this is the last active source, mark everything DONE
			 */
			if (--active <= 0)
			{
				setAllComplete(sessionContext);
			}
			else
			{	/*
				 * Just this source is finished
				 */
				targetMap.put("STATUS", "DONE");
				targetMap.put("STATUS_MESSAGE", "Search complete");
			}
			sessionContext.putInt("active", active);
		}
		return total;
	}

	/**
	 * Fetch the estimated hiots for a specified target (database)
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Updated hit count
	 */
	public static int getEstimatedHits(SessionContext sessionContext,
															 			 String target)
	{
		Map			targetMap;
		String	estimate;


		if ((targetMap = getStatusMapForTarget(sessionContext, target)) == null)
		{
			throw new SearchException("No status map for target database " + target);
		}

		estimate = (String) targetMap.get("ESTIMATE");
		return Integer.parseInt(estimate);
	}
}