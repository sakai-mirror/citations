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

import java.util.Map;
import java.util.HashMap;

/**
 * Common GDS names and terms
 */
public interface CampusNames
{
	/*
	 * Campus abbreviations (what about CO, SY, CS?)
	 */
	public final static String		IUB							  = "BL";		        // Bloomington
	public final static String		IUPUI						  = "IN";		        // Indianapolis
	public final static String		IPFW						  = "FW";		        // Fort Wayne
	public final static String		IUE							  = "EA";	        	// Richmond
	public final static String		IUK							  = "KO";		        // Kokomo
	public final static String		IUNW						  = "NW";		        // Gary
	public final static String		IUSB						  = "SB";		        // South Bend
	public final static String		IUSE						  = "SE";		        // New Albany
	/*
	 * Campus domains
	 */
	public final static String		DOMAIN_IUB			  = "indiana.edu";  // Bloomington
	public final static String		DOMAIN_IUB2		    = "iub.edu";      // Bloomington
	public final static String		DOMAIN_IUPUI			= "iupui.edu";    // Indianapolis
	public final static String		DOMAIN_IPFW				= "ipfw.edu";		  // Fort Wayne
	public final static String		DOMAIN_IUE				= "iue.edu";	    // Richmond
	public final static String		DOMAIN_IUK				= "iuk.edu";		  // Kokomo
	public final static String		DOMAIN_IUNW				= "iun.edu";		  // Gary
	public final static String		DOMAIN_IUSB				= "iusb.edu";		  // South Bend
	public final static String		DOMAIN_IUSE				= "ius.edu";		  // New Albany

	/*
	 * Affiliations (specific GDS names, general affiliation category)
	 */
	public final static String		UNDERGRADUATE		  = "UNDERGRADUATE";
	public final static String		GRADUATE				  = "GRADUATE";
	public final static String		PROFESSIONAL		  = "PROFESSIONAL";
	public final static String		CATEGORY_STUDENT  = "STUDENT";


	public final static String		ADMITTED				  = "ADMITTED";
	public final static String		ENROLLED				  = "ENROLLED";
	public final static String		CATEGORY_ACTIVE		= "ACTIVE-STUDENT";

	public final static String		FORMER_STUDENT	  = "FORMER STUDENT";
	public final static String		CATEGORY_INACTIVE	= "INACTIVE-STUDENT";

	public final static String		FACULTY					  = "FACULTY";
	public final static String		CATEGORY_FACULTY 	= "FACULTY";

	public final static String		STAFF						  = "STAFF";
	public final static String		ADDITIONAL_PAY	  = "ADDITIONAL PAY";
	public final static String		STUDENT_HOURLY	  = "STUDENT HOURLY";
	public final static String		REGULAR_HOURLY	  = "REGULAR HOURLY";
	public final static String		RETIRED_STAFF		  = "RETIRED STAFF";
	public final static String		CATEGORY_STAFF		= "STAFF";
  /*
   * Database access groups
   */
	public final static String		CATEGORY_FULL     = "FULL-ACCESS";
	public final static String		CATEGORY_GUEST    = "GUEST-ACCESS";
	/*
	 * All participating campuses
	 */
	public final static String[] 	CAMPUS_LIST				=	{	  IUB,
																											  IUPUI,
																											  IPFW,
																											  IUE,
																											  IUK,
																											  IUNW,
																											  IUSB,
																											  IUSE
																										};
  /*
   * All participating domains
   */
	public final static Map  	    DOMAIN_MAP			=	new HashMap()
	{
                                  {
                                  	  put(DOMAIN_IUB,    IUB);
                                  	  put(DOMAIN_IUB2,   IUB);
                                  		put(DOMAIN_IUPUI,  IUPUI);
                                  		put(DOMAIN_IPFW,   IPFW);
                                  	  put(DOMAIN_IUE,    IUE);
                                  		put(DOMAIN_IUK,    IUK);
                                  		put(DOMAIN_IUNW,   IUNW);
                                  		put(DOMAIN_IUSB,   IUSB);
                                  		put(DOMAIN_IUSE,   IUSE);
                                  }
  };
}