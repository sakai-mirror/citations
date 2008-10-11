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

package org.sakaiproject.citations.helper.renderers;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.cover.ConfigurationService;
import org.sakaiproject.citations.helper.api.CitationHelperRenderer;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.srg.tool.renderers.CitationsItemRenderer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;

/**
 * CollectionRenderer 
 *
 */
public class CollectionRenderer implements CitationHelperRenderer
{
	private static Log logger = LogFactory.getLog(CollectionRenderer.class);
	/*
	 * Temporary: the default "related link" text.  It seems like this should come
	 * from the Citations configuration service, but it doesn't seem to be
	 * implemented yet(?).
	 */
	public static final String DEFAULT_RELATED_LINK_LABEL = "Related Link";

	public static final String COMPONENT_ID = "CitationsItem:";
	

	/* (non-Javadoc)
	 * @see org.sakaiproject.citations.helper.api.CitationHelperRenderer#getRendererType()
	 */
	public String getRendererType()
    {
	    // TODO method stub for getRendererType
	    return null;
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.citations.helper.api.CitationHelperRenderer#make(uk.org.ponder.rsf.components.UIContainer, java.lang.Object)
	 */
	public UIJointContainer make(UIContainer parent, Object item)
    {
		UIJointContainer element = null;
		CitationCollection citationCollection = null;
		if(item instanceof CitationCollection)
		{
			citationCollection = (CitationCollection) item;
		}
		
	    if(citationCollection == null)
	    {
	    	// TODO: error?  Display message?
	    }
	    else
	    {
	    	String localId = String.valueOf(citationCollection.getId());
	    	element = new UIJointContainer(parent, rsf_id, COMPONENT_ID, localId);
	    	
	    	UIBranchContainer allCitations = UIBranchContainer.make(parent, "citations:");
	    	for(Citation citation : (List<Citation>) citationCollection.getCitations())
	    	{
	    		// the "main" link - use the preferred URL if available, an OpenURL if not

	    		UIBranchContainer oneCitation = UIBranchContainer.make(allCitations, "citation:");
	    		UILink.make(oneCitation, "title-link", citation.getDisplayName(), getPreferredUrl(citation));

	    		UIOutput.make(oneCitation, "creator", citation.getCreator());
	    		UIOutput.make(oneCitation, "source", citation.getSource());

	    		UIBranchContainer citationActions = UIBranchContainer.make(oneCitation, "citationActions:");

	    		// next the OpenURL

	    		UIBranchContainer openUrlAction = UIBranchContainer.make(citationActions, "citationAction:");
	    		UILink.make(openUrlAction, "action-link",
	    				ConfigurationService.getSiteConfigOpenUrlLabel(),
	    				citation.getOpenurl());

	    		// then the custom URLs

	    		boolean separatorDisplayed  = false;
	    		List<String> customUrlIds   = citation.getCustomUrlIds();

	    		for(Iterator iterator = customUrlIds.iterator(); iterator.hasNext(); )
	    		{
	    			String customUrlId = (String) iterator.next();
	    			String label;

	    			try
	    			{
	    				label = citation.getCustomUrlLabel(customUrlId);
	    				if ((label == null) || (label.trim().length() == 0))
	    				{
	    					label = DEFAULT_RELATED_LINK_LABEL;
	    				}
	    			}
	    			catch(Exception e)
	    			{
	    				label = DEFAULT_RELATED_LINK_LABEL;
	    			}
	    			try
	    			{
	    				String link = citation.getCustomUrl(customUrlId);

	    				// display the OpenURL | Related Link separator?

	    				if (!separatorDisplayed)
	    				{
	    					UIOutput.make(openUrlAction, "separator", " | ");
	    					separatorDisplayed = true;
	    				}

	    				UIBranchContainer citationAction = UIBranchContainer.make(citationActions, "citationAction:");
	    				UILink.make(citationAction, "action-link", label, link);

	    				// if we have another Related Link, add the | separator

	    				if (iterator.hasNext())
	    				{
	    					UIOutput.make(citationAction, "separator", " | ");
	    				}
	    			}
	    			catch(Exception e)
	    			{
	    				// TODO: ignore this custom URL, but log the error
	    			}
	    		}

	    		// and finally the view Citation
	    		/***
	    		UIBranchContainer viewCitationAction = UIBranchContainer.make(citationActions, "citationAction:");
	    		UILink.make(viewCitationAction, "action-link", "view-citation-label", "view-citation-link");
	    		UIOutput.make(viewCitationAction, "separator", "");
	    		 ***/
	    	}
	    }
	    return element;
    }

	protected String getPreferredUrl(Citation citation)
    {
		String url;
		/*
		 * Custom URL?
		 */
		if (citation.hasPreferredUrl())
		{
			String id = citation.getPreferredUrlId();

			try
			{
				return citation.getCustomUrl(id);
			}
			catch (IdUnusedException exception)
			{
				logger.warn("No matching URL for ID: "
						+   id
						+   ", returning an OpenURL");
			}
		}
		/*
		 * Use an OpenURL
		 */
		return citation.getOpenurl();
    }

}
