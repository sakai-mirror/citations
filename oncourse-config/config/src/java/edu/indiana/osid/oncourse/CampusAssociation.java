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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.citation.util.api.OsidConfigurationException;

/**
 * Determine campus associations
 */
public class CampusAssociation implements CampusNames
{
 	private static Log _log = LogFactory.getLog(CampusAssociation.class);

  /*
   * Scores for each affilition type
   *
   * These are totaled up to produce an overall score for each campus.  The
   * campus with the high score is suggested as the primary campus.
   */
  public static final int   FACULTY_SCORE           = 3;
  public static final int   STAFF_SCORE             = 2;
  public static final int   ACTIVE_STUDENT_SCORE    = 1;
  public static final int   INACTIVE_STUDENT_SCORE  = 0;
  /*
   * No campus association
   */
  public static final int   NO_AFFILIATION          = -1;
  /*
   * Affiliations and total scores
   */
  private HashMap   _detailMap      = new HashMap();
  /*
   * Kerberos (CAS) username
   */
  private String    _kerberosUsername;
  /*
   * Sakai Site ID
   */
  private String    _sakaiSiteId;
  /**
   * Constructor
   * @param kerberosUsername Kerberos/CAS user
   */
  public CampusAssociation(String kerberosUsername, String siteId)
  {
    _kerberosUsername = kerberosUsername;
    _sakaiSiteId      = siteId;
  }

  /**
   * Determine campus information for the requested user, via the
   * Oncourse specific API (getCampusAffiliations(userEid))
   */
  protected void getUserAssociations() throws Exception
  {
    String[]  campusList;
    String    courseProperty;
    int       limit;
    /*
     * Look for the property
     */
/* Not for Fall 2007 **************************************************

    courseProperty = SakaiUtils.getSiteProperty("site_oncourse_course_id");
    if (courseProperty != null)
    {
      ArrayList affiliationList;
      String    campus  = courseProperty.substring(7, 9);
      String    role    = SakaiUtils.getUserRole();

      _log.debug("Found Oncourse property: " +  courseProperty
              +  ", campus is " + campus
              +  ", role is "   + role);

      affiliationList = new ArrayList();

      if ("instructor".equalsIgnoreCase(role)   ||
          "assistant".equalsIgnoreCase(role))
      {
        affiliationList.add(CATEGORY_FACULTY);
        affiliationList.add(CATEGORY_FULL);
      }
      else if ("student".equalsIgnoreCase(role))
      {
        affiliationList.add(CATEGORY_ACTIVE);
        affiliationList.add(CATEGORY_FULL);
      }
      else
      {
        affiliationList.add(CATEGORY_GUEST);
      }

      _log.debug("Affiliation list is " + affiliationList);
      addScore(new CampusDetail(campus, affiliationList));
      return;
    }
******************************************************************/
    /*
     * No property, try the API
     *
     * For Fall 2007 use only one campus from the list
     */
    campusList = edu.iu.oncourse.util.CampusAffiliationService.getCampusAffiliations(_kerberosUsername);
    if (campusList == null)
    {
      _log.info("No campus available for user " + _kerberosUsername);
      return;
    }

    limit = (campusList.length > 1) ? 1 : campusList.length;
    for (int i = 0; i < limit; i++)
    {
      String campus = campusList[i];

      addScore(new CampusDetail(campus, getCampusRoles(campus)));
    }
  }

  /**
   * Create associations for a guest user (existing associations are discarded)
   * @param campus Campus association for this guest
   */
  protected void getGuestAssociations(String campus)
  {
    ArrayList affiliationList;

    affiliationList = new ArrayList();
    affiliationList.add(CATEGORY_GUEST);

    _detailMap.clear();
    addScore(new CampusDetail(campus, affiliationList));
  }

  /**
   * Is this a guest?
   * @return true If so
   */
  protected boolean isGuest()
  {
    if (_kerberosUsername != null)
    {
      return (_kerberosUsername.indexOf('@') != -1);
    }
    return false;
  }

  /**
   * Helper: get campus roles (temporary implementation)
   * @param campus Campus id
   * @return List of campus roles
   */
  private List getCampusRoles(String campus)
  {
    ArrayList roleList = new ArrayList();

    roleList.add(CATEGORY_FULL);
    return roleList;
  }

  /**
   * Helper: save user data in our global map
   * @param CampusDetail Campus details (name, score, etc)
   */
  private void addScore(CampusDetail CampusDetail)
  {
    if (CampusDetail.getScore() != NO_AFFILIATION)
    {
      _detailMap.put(CampusDetail.getCampus(), CampusDetail);
    }
  }

  /**
   * Return an array of associated CampusDetail objects
   * @return CampusDetail array (sorted by score, zero length if none)
   */
  protected CampusDetail[] getCampusDetails()
  {
    CampusDetail[] CampusDetails = (CampusDetail[])
                                    _detailMap.values().toArray(new CampusDetail[_detailMap.size()]);

    Arrays.sort(CampusDetails);
    return CampusDetails;
  }

  /**
   * Return a CampusDetail object
   * @param campus Campus of interest
   * @return The appropriate CampusDetail object (null if none)
   */
  protected CampusDetail getCampusDetail(String campus)
  {
    return (CampusDetail) _detailMap.get(campus);
  }

  /**
   * Suggest one primary campus
   * @return Campus name (null if none)
   */
  protected String suggestCampus()
  {
    String[] suggestions = suggestCampuses();

    return (suggestions.length == 0) ? null : suggestions[0];
  }

  /**
   * Suggest one (or more) primary campus(es)
   * @return primary campus array (sorted by score, zero length if none)
   */
  protected String[] suggestCampuses()
  {
    CampusDetail[]  CampusDetails = getCampusDetails();
    String[]        suggestions   = new String[CampusDetails.length];

    for (int i = 0; i < CampusDetails.length; i++)
    {
      suggestions[i] = CampusDetails[i].getCampus();
    }
    return suggestions;
  }

  /**
   * Get the total affilition score for this campus
   * @param campus GDS campus abbreviation (BL, IN, EA, etc)
   * @return The campus score (NO_AFFILIATION if none)
   */
  protected int getScore(String campus)
  {
    CampusDetail[] CampusDetails  = getCampusDetails();

    for (int i = 0; i < CampusDetails.length; i++)
    {
      if (CampusDetails[i].getCampus().equalsIgnoreCase(campus))
      {
        return CampusDetails[i].getScore();
      }
    }
    return NO_AFFILIATION;
  }

  /**
   * Fetch our username
   * @return The kerberos username
   */
  protected String getUsername()
  {
    return _kerberosUsername;
  }
  /**
   * Fetch the Sakai Site ID
   * @return The site ID
   */
  protected String getSiteId()
  {
    return _sakaiSiteId;
  }

  /*
   * Campus details
   */

  /**
   * Campus details (name, affiliation list, overall affiliation score)
   */
  public static class CampusDetail implements Comparable
  {
    private List    cs_affiliationList;
    private String  cs_campus;
    private int     cs_score;

    /**
     * private constructor
     */
    private CampusDetail() { }

    /**
     * Constructor
     * @param campus Campus abbreviation (BL, IN, etc)
     * @param affiliationList List of campus affilitions (STUDENT, STAFF, etc)
     */
    public CampusDetail(String campus, List affiliationList)
    {
      cs_affiliationList  = affiliationList;
      cs_campus           = campus;
      cs_score            = NO_AFFILIATION;

      if (affiliationList != null)
      {
        cs_score   = getFacultyScore();
        cs_score  += getStaffScore();
        cs_score  += getStudentScore();
      }
    }

    /**
     * Fetch the campus affiliation list
     * @return The affiliation list
     */
    public List<String> getAffiliationList()
    {
      return cs_affiliationList;
    }

    /**
     * Fetch the overall affiliation score
     * @return The cumulative score
     */
    public int getScore()
    {
      return cs_score;
    }

    /**
     * Fetch the GDS campus name (an abbreviation: BL, IN, KO, etc)
     * @return The campus name
     */
    public String getCampus()
    {
      return cs_campus;
    }

    /**
     * Fetch the faculity affiliation score
     * @return the faculity score
     */
    public int getFacultyScore()
    {
      return cs_affiliationList.contains(CATEGORY_FACULTY) ? FACULTY_SCORE : 0;
    }

    /**
     * Is this a faculty member?
     * @return true if so
     */
    public boolean isFaculty()
    {
      return getFacultyScore() != 0;
    }

    /**
     * Fetch the staff affiliation score
     * @return the staff score
     */
    public int getStaffScore()
    {
      return cs_affiliationList.contains(CATEGORY_STAFF) ? STAFF_SCORE : 0;
    }

    /**
     * Is this a staff member?
     * @return true if so
     */
    public boolean isStaff()
    {
      return getStaffScore() != 0;
    }

    /**
     * Fetch the student affiliation score
     * @return the student score
     */
    public int getStudentScore()
    {
      int score;

      score = cs_affiliationList.contains(CATEGORY_STUDENT) ? ACTIVE_STUDENT_SCORE : 0;

      if (cs_affiliationList.contains(CATEGORY_INACTIVE))
      {
        score = INACTIVE_STUDENT_SCORE;
      }
      return score;
    }

    /**
     * Is this a student (any kind, enrolled or not)?
     * @return true if so
     */
    public boolean isStudent()
    {
      return cs_affiliationList.contains(CATEGORY_STUDENT);
    }

    /**
     * Is this student enrolled (or admitted)?
     * @return true if so
     */
    public boolean isActiveStudent()
    {
      return !cs_affiliationList.contains(CATEGORY_INACTIVE);
    }

    /**
     * Local compareTo() for sort
     * @param CampusDetail A CampusDetail object for comparison
     * @return 0 (scores are equal), or
     *        <0 (parameter score < this score), or
     *        >0 (parameter score > this score)
     */
    public int compareTo(Object CampusDetail) throws ClassCastException
    {
      int score = ((CampusDetail) CampusDetail).getScore();

      return score - this.cs_score;
    }
  }
}