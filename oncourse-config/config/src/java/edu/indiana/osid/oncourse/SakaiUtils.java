/*******************************************************************************
 * $URL:
 * $Id:
 * **********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 ******************************************************************************/
package edu.indiana.osid.oncourse;

import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;


/**
 * Sakai Helper methods
 */
public class SakaiUtils
{
 	private static Log _log = LogFactory.getLog(SakaiUtils.class);


	/**
	 * Fetch session attribute names
	 * @return A List of attribute names (null if none)
	 */
	public static List<String> getAttributeNames()
	{
		Session     session;
    List        nameList;
    Enumeration enumeration;

		session = SessionManager.getCurrentSession();
		if (session == null)
		{
		  _log.debug("Unable to lookup attribute names; no current session");
		  return null;
		}

		enumeration = session.getAttributeNames();
		nameList    = null;

		while (enumeration.hasMoreElements())
		{
		  if (nameList == null) nameList = new ArrayList();

		  nameList.add((String) enumeration.nextElement());
		}
		return nameList;
	}

	/**
	 * Fetch a session attribute by name
	 * @param name The attribute to lookup
	 * @return The attribute value (null if none)
	 */
	public static Object getAttribute(String name)
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
		  _log.debug("Unable to lookup attribute; no current session");
		  return null;
		}
		return session.getAttribute(name);
	}

	/**
	 * Get the current user id
	 * @return User ID (null if none)
	 */
	public static String getUserId()
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
		  _log.debug("Unable to lookup user id; no current session");
		  return null;
		}
		return session.getUserId();
	}

	/**
	 * Get the current user
	 * @return User EID (null if none)
	 */
	public static String getUser()
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
		  _log.debug("Unable to lookup user id; no current session");
		  return null;
		}
		return session.getUserEid();
	}

	/**
	 * Get the current site id
	 * @return Site id (GUID)
	 */
	public static String getSiteId()
	{
		Placement placement = ToolManager.getCurrentPlacement();

		if (placement == null)
		{
			_log.debug("No current tool placement");
      _log.debug("Attributes defined: " + getAttributeNames());
			return null;
		}
		_log.debug("Site ID: " + placement.getContext());
		return placement.getContext();
	}

	/**
	 * Get a site property by name
	 *
	 * @param name Property name
	 * @return The property value (null if none)
	 */
	public static String getSiteProperty(String name)
	{
	  String  siteId;
		Site    site;

    if ((siteId = getSiteId()) == null)
    {
      return null;
    }

    try
    {
		  site = SiteService.getSite(getSiteId());
  		return site.getProperties().getProperty(name);
		}
    catch (Exception exception)
    {
      _log.error("Failed to get site property " + name + ": " + exception);
      return null;
    }
	}

	/**
	 * Fetch the user role in the current site
	 * @return Role (null if none)
	 */
	public static String getUserRole()
	{
		AuthzGroup 	group;
		Role 				role;

    try
    {
		  group = AuthzGroupService.getAuthzGroup("/site/" + getSiteId());
		  if (group == null)
		  {
		    _log.debug("No group getAuthzGroup() for site id: \"/site/" + getSiteId() + "\"");
		    return null;
		  }
		}
		catch (Exception exception)
		{
			_log.debug("Failed to get group, " + exception);
			return null;
		}

    _log.debug("role for " + getUserId() + " = " + group.getUserRole(getUserId()));

		role = group.getUserRole(getUserId());
		if (role == null)
		{
			_log.debug("No current role for user id: \"" + getUserId() + "\"");
			return null;
		}
		return role.getId();
	}
}