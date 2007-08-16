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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.citation.api.SiteOsidConfiguration;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Oncourse CL Repository OSID configuration
 */
public class OncourseOsidConfiguration implements SiteOsidConfiguration, CampusNames
{
 	private static Log _log = LogFactory.getLog(OncourseOsidConfiguration.class);

  /*
 	 * Configuration parameter names
 	 *
   * IUB configuration
   */
  public final static String  IUB_CONFIG        = "sakaibrary.iub.configuration";
  public final static String  IUB_DB            = "sakaibrary.iub.database";
  /*
   * Other configuration
   */
  public final static String  IUPUI_CONFIG      = "sakaibrary.iupui.configuration";
  public final static String  IPFW_CONFIG       = "sakaibrary.ipfw.configuration";
  public final static String  IUE_CONFIG        = "sakaibrary.iue.configuration";
  public final static String  IUK_CONFIG        = "sakaibrary.iuk.configuration";
  public final static String  IUNW_CONFIG       = "sakaibrary.iunw.configuration";
  public final static String  IUSB_CONFIG       = "sakaibrary.iusb.configuration";
  public final static String  IUSE_CONFIG       = "sakaibrary.iuse.configuration";
  /*
   * Default configuration
   */
  public final static String  DEFAULT_CONFIG    = "sakaibrary.default.configuration";
  public final static String  DEFAULT_DB        = "sakaibrary.default.database";
  /*
   * Sakai home
   */
  public final static String  SAKAI_HOME        = "${sakai.home}";
	/*
	 * Default campus for guests (used when all else fails)
	 *
	 * For Fall, 2007: this can be anything but IUB
	 */
	public final static String  GUEST_CAMPUS      = IUPUI;
  /*
   * Unknown user and/or site
   */
  public final static String  UNKNOWN           = "<<unknown>>";
  /*
   * Reserved configuration filename (means "no database for this user")
   */
  public static final String  NO_DATABASE       = "__no_database_hierarchy_available__";
  /*
   * IUB details
   */
  private String    _iubDatabase        = null;
  private String    _iubConfig          = null;
  /*
   * Other campus details
   */
  private String    _iupuiConfig        = null;
  private String    _ipfwConfig         = null;
  private String    _iueConfig          = null;
  private String    _iukConfig          = null;
  private String    _iunwConfig         = null;
  private String    _iusbConfig         = null;
  private String    _iuseConfig         = null;
  /*
   * Default details
   */
  private String    _defaultDatabase    = null;
  private String    _defaultConfig      = null;
  /*
   * kerberos username
   */
  private String    _kerberosUsername   = null;
  /*
   * Campus roles and affiliations
   */
  CampusAssociation _campusAssociation  = null;

  /**
   * Constructor
   */
  public OncourseOsidConfiguration() { }

  /*
   * Interface methods
   */

  /**
   * Initialization
   */
  public void init() throws OsidConfigurationException
  {
    _log.debug("init()");

    _iubConfig        = getSakaiPropertiesValue(IUB_CONFIG);
    _iubDatabase      = getSakaiPropertiesValue(IUB_DB);

    _iupuiConfig      = getSakaiPropertiesValue(IUPUI_CONFIG);
    _ipfwConfig       = getSakaiPropertiesValue(IPFW_CONFIG);
    _iueConfig        = getSakaiPropertiesValue(IUE_CONFIG);
    _iukConfig        = getSakaiPropertiesValue(IUK_CONFIG);
    _iunwConfig       = getSakaiPropertiesValue(IUNW_CONFIG);
    _iusbConfig       = getSakaiPropertiesValue(IUSB_CONFIG);
    _iuseConfig       = getSakaiPropertiesValue(IUSE_CONFIG);


    _defaultConfig    = getSakaiPropertiesValue(DEFAULT_CONFIG);
    _defaultDatabase  = getSakaiPropertiesValue(DEFAULT_DB);
  }

  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML (eg file:///tomcat-home/sakai/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException
  {
    String campus, config;

    getCampusAssociation();

    campus = _campusAssociation.suggestCampus();


    config = _defaultConfig;

    if (IUB.equals(campus))       config = _iubConfig;
    if (IUPUI.equals(campus))     config = _iupuiConfig;
    if (IPFW.equals(campus))      config = _ipfwConfig;
    if (IUE.equals(campus))       config = _iueConfig;
    if (IUK.equals(campus))       config = _iukConfig;
    if (IUNW.equals(campus))      config = _iunwConfig;
    if (IUSB.equals(campus))      config = _iusbConfig;
    if (IUSE.equals(campus))      config = _iuseConfig;

    _log.debug("Configuration: " + config);
    return config;
  }

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException
  {
    String campus;

    getCampusAssociation();

    campus = _campusAssociation.suggestCampus();

    if (_campusAssociation.isGuest())
    {
      _log.debug("Guest gets hierarchy: " + NO_DATABASE);
      return NO_DATABASE;
    }

    _log.debug("Hierarchy: " + (IUB.equals(campus) ? _iubDatabase : _defaultDatabase));
    return IUB.equals(campus) ? _iubDatabase : _defaultDatabase;
  }

  /**
   * Fetch this user's group affiliations
   * @return A list of group IDs (empty if no IDs exist)
   */
  public List<String> getGroupIds() throws OsidConfigurationException
  {
    ArrayList<String> groupList = new ArrayList();
    String            campus;

    getCampusAssociation();

    if ((campus = _campusAssociation.suggestCampus()) != null)
    {
      CampusAssociation.CampusDetail campusDetail;

      campusDetail = _campusAssociation.getCampusDetail(campus);

      groupList.add(campus);
      groupList.addAll(campusDetail.getAffiliationList());
    }
    _log.debug("Group list: " + groupList);
    return groupList;
  }

  /*
   * Helpers
   */

  /**
   * Fetch (once) the campus affiliation details for this user
   */
  private void getCampusAssociation() throws OsidConfigurationException
  {
    String username = SakaiUtils.getUser();
    String siteId   = SakaiUtils.getSiteId();

/*  displaySessionAttributes();
 *  getToolAttributeNames();
 */
    if (username == null) username  = UNKNOWN;
    if (siteId   == null) siteId    = UNKNOWN;

    if (_campusAssociation != null)
    {
      if ((username.equals(_campusAssociation.getUsername())) &&
          (siteId.equals(_campusAssociation.getSiteId())))
      {
        _log.debug("Campus association for " + username + " cached - no action required");
        return;
      }
    }

   _log.debug("Getting campus associations for user " + username);
   _campusAssociation = new CampusAssociation(username, siteId);

   /*
    * Guest user?  Default to IUB guest access
    */
   if ((username.equals(UNKNOWN)) || (username.indexOf('@') != -1))
   {
      _campusAssociation.getGuestAssociations(getGuestCampusAssociation(username));
      return;
    }
    /*
     * Not a guest, try fetching this user from Oncourse
     */
    try
    {
      _campusAssociation.getUserAssociations();
    }
    catch (Exception exception)
    {
      _log.error("Providing GUEST access for \""
               + username
               + "\", unable to obtain Oncourse data: "
               + exception.toString());

      _campusAssociation.getGuestAssociations(getGuestCampusAssociation(username));
    }
  }

  /**
   * Select a campus (based on the guest's domain).  If the user's domain
   * is not known to us, return <code>GUEST_CAMPUS</code>
   *
   * @param guest Guest username
   * @return A best guess at the campus affiliation
   */
  protected String getGuestCampusAssociation(String guest)
  {
    String  domain;
    int     index;

/*  Guests get default access for now **************************

    index = guest.indexOf('@');
    if ((index == -1) || ((index + 1) == guest.length()))
    {
      return GUEST_CAMPUS;
    }

    domain = guest.substring(index + 1).trim().toLowerCase();

    for (Iterator i = DOMAIN_MAP.keySet().iterator(); i.hasNext(); )
    {
      String knownDomain = (String) i.next();

      if (domain.endsWith(knownDomain))
      {
        _log.debug("Guest campus returned: " + (String) DOMAIN_MAP.get(knownDomain));
        return (String) DOMAIN_MAP.get(knownDomain);
      }
    }
******************************************************************/
    return GUEST_CAMPUS;
  }

  /**
   * Expand the ${sakai.home} macro if found in the database or configuration
   * file specifications (the specification themselves are in sakai.properties)
   *
   * @param name The property name to lookup in sakai.properties
   * @return The original value (possibly with ${sakai.home} replaced by
   *                             the actual path specification), null if missing
   */
  protected String getSakaiPropertiesValue(String name)
  {
    String value = ServerConfigurationService.getString(name, null);

    if (value != null)
    {
      value = doMacroExpansion(value);
    }

    return value;
  }

	/**
	 * Lookup value for requested macro name
	 */
	private String getMacroValue(String macroName)
	{
		try
		{
			if (macroName.equals(SAKAI_HOME))
			{
				return ServerConfigurationService.getSakaiHomePath();
			}
		}
		catch (Throwable throwable)
		{
			return "";
		}
		/*
		 * An unsupported macro: use the original text "as is"
		 */
		return macroName;
	}

	/**
	 * Expand one macro reference
	 * @param text Expand macros found in this text
	 * @param macroName Macro name
	 */
	private void expand(StringBuffer sb, String macroName)
	{
		int index;

		/*
		 * Replace every occurance of the macro in the parameter list
		 */
		index = sb.indexOf(macroName);
		while (index != -1)
		{
			String  macroValue = getMacroValue(macroName);

			sb.replace(index, (index + macroName.length()), macroValue);
			index = sb.indexOf(macroName, (index + macroValue.length()));
		}
	}

	/**
	 * Expand macros, inserting session and site information
	 * @param originalText Expand macros found in this text
	 * @return [possibly] Updated text
	 */
	private String doMacroExpansion(String originalText)
	{
		StringBuffer  sb;

		/*
		 * Quit now if no macros are embedded in the text
		 */
		if (originalText.indexOf("${") == -1)
		{
			return originalText;
		}
		/*
		 * Expand each macro
		 */
		sb = new StringBuffer(originalText);

		expand(sb, SAKAI_HOME);

		return sb.toString();
	}

  /*
   * Debug
   */

  /**
   * Display Session attribute values
   */
  private void displaySessionAttributes()
  {
    List attributeList = SakaiUtils.getAttributeNames();

    for (Iterator i = attributeList.iterator(); i.hasNext(); )
    {
      String  name  = (String) i.next();
      Object  value = SakaiUtils.getAttribute(name);

      _log.debug("Session Attribute: " + name + "=" + value);
    }
  }

	/**
	 * Fetch tool session attribute names
	 * @return A List of attribute names (null if none)
	 */
	public static List<String> getToolAttributeNames()
	{
		ToolSession     session;
    List        nameList;
    java.util.Enumeration enumeration;

		session = SessionManager.getCurrentToolSession();
		if (session == null)
		{
		  _log.warn("Unable to lookup attribute names; no Tool session");
		  return null;
		}

		enumeration = session.getAttributeNames();
		nameList    = null;

		while (enumeration.hasMoreElements())
		{
		  String name = (String) enumeration.nextElement();

		  if (nameList == null) nameList = new ArrayList();

      _log.debug("Tool Attribute: " + name + "=" + session.getAttribute(name));
		  nameList.add(name);
		}
		return nameList;
	}

	/**
	 * Fetch a Tool session attribute by name
	 * @param name The attribute to lookup
	 * @return The attribute value (null if none)
	 */
	public static Object getAttribute(String name)
	{
		ToolSession session = SessionManager.getCurrentToolSession();

		if (session == null)
		{
		  _log.warn("Unable to lookup attribute; no current session");
		  return null;
		}
		return session.getAttribute(name);
	}
}
