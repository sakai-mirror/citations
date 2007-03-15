/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osid.repository.RepositoryIterator;
import org.sakaiproject.citation.api.ActiveSearch;
import org.sakaiproject.citation.util.api.SearchException;

/**
 * 
 */
public interface SearchManager
{
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int DEFAULT_START_RECORD = 1;
	public static final int MIN_START_RECORD = 1;
	public static final String DEFAULT_SORT_BY = "rank";


	public static final String ASSET_NOT_FETCHED = "An Asset is available, but has not yet been fetched.";

	public static final String METASEARCH_ERROR = "Metasearch error has occured.";

	public static final String SESSION_TIMED_OUT = "Metasearch session has timed out.";

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 */
	public ActiveSearch doNextPage(ActiveSearch search)
	        throws SearchException;

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 */
	public ActiveSearch doPrevPage(ActiveSearch search)
	        throws SearchException;

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 */
	public ActiveSearch doSearch(ActiveSearch search)
	        throws SearchException;

	/**
	 * @return The SearchDatabaseHierarchy for this search.
	 * @throws SearchException
	 * @see SearchDatabaseHierarchy
	 */
	public SearchDatabaseHierarchy getSearchHierarchy() throws SearchException;
	
	/**
	 * @return
	 */
	public ActiveSearch newSearch();

	/**
     * @param savedResults
     * @return
     */
    public ActiveSearch newSearch(CitationCollection savedResults);

	/**
     * @param resourceId
     * @return
     */
    public String getGoogleScholarUrl(String resourceId);
    
    /**
     * Sets the databases that have been selected to search.
     * 
     * @param databaseIds String array of database ids that have been
     * selected to search
     */
    public void setDatabaseIds( String[] databaseIds );

}