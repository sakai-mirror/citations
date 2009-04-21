/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.util.api;

import java.util.Set;

public interface SearchQuery {
	
	public Set<String> getKeywords();
	
	public void addKeywords(String keywords);
	
	public Set<String> getTitles();
	
	public void addTitle(String title);
	
	public Set<String> getYears();
	
	public void addYear(String year);
	
	public void addAuthor( String author );
	
	public Set<String> getAuthors();
	
	public void addSubject( String subject );
	
	public Set<String> getSubjects();
}
