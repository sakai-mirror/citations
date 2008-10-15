/**
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

package org.sakaiproject.citations.helper.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citations.helper.params.CitationsViewParams;
import org.sakaiproject.exception.IdUnusedException;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.content.ContentTypeReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * CitationCollectionProducer 
 *
 */
public class CitationCollectionProducer implements ViewComponentProducer, ViewParamsReporter, ContentTypeReporter
{
	public static final String VIEW_ID = "CitationCollection";
	
	private Log logger = LogFactory.getLog(CitationCollectionProducer.class);
	
	protected CitationService citationService;
	public void setCitationService(CitationService citationService)
	{
		this.citationService = citationService;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public String getViewID()
	{
		// TODO method stub for getViewID
		return VIEW_ID;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
	        ComponentChecker checker)
	{
		String citationCollectionId = null;
		CitationCollection collection = null;
		int citationCount = 0;
		if(viewparams instanceof CitationsViewParams)
		{
			citationCollectionId = ((CitationsViewParams) viewparams).citationCollectionId;
			if(citationCollectionId != null)
			{
				try
                {
	                collection = this.citationService.getCollection(citationCollectionId);
	                citationCount = collection.size();
                }
                catch (IdUnusedException e)
                {
	                logger.warn("CitationCollectionProducer.fillComponents --> IdUnusedException: " + citationCollectionId + " " + e);
                }
			}
		}
		UIBranchContainer citationCollection = UIBranchContainer.make(tofill, "citationCollection:", citationCollectionId);
		UIBranchContainer citationContext = UIBranchContainer.make(citationCollection, "citationContext:");
		
		UIBranchContainer titleBar = UIBranchContainer.make(citationContext, "titleBar:");
		UIMessage.make(titleBar, "title", "citations.title");
		UIBranchContainer actions = UIBranchContainer.make(titleBar, "actions:");
		UIMessage.make(actions, "citationCount", "citations.count", new Object[]{ Integer.valueOf(citationCount) });
		
		UIBranchContainer citationNavBar = UIBranchContainer.make(citationContext, "citationNavBar:");
		UIBranchContainer chooseView = UIBranchContainer.make(citationNavBar, "chooseView:");
		
		
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters()
    {
	    return new CitationsViewParams();
    }

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.content.ContentTypeReporter#getContentType()
	 */
	public String getContentType()
    {
	    return ContentTypeInfoRegistry.HTML_FRAGMENT;
    }

}
