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

package org.sakaiproject.citation.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.CitationsProvider;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.exception.IdUnusedException;

/**
 * CitationsProviderImpl 
 *
 */
public class CitationsProviderImpl implements CitationsProvider, CoreEntityProvider,
        AutoRegisterEntityProvider, CRUDable
{
	private static Log logger = LogFactory.getLog(CitationsProviderImpl.class);
	
	protected CitationService citationService = null;
	public void setCitationService(CitationService citationService)
	{
		this.citationService = citationService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	public String getEntityPrefix()
    {
	    return ENTITY_PREFIX;
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id)
	{
		return this.citationService.collectionExists(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Createable#createEntity(org.sakaiproject.entitybroker.EntityReference, java.lang.Object, java.util.Map)
	 */
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params)
    {
		String collectionId = null;
		if(entity == null)
		{
			throw new IllegalArgumentException("error creating CitationCollection: entity is null");
		}
	    if(entity instanceof CitationCollection)
	    {
	    	CitationCollection collection = (CitationCollection) entity;
	    	this.citationService.save(collection);
	    	if(collection == null)
	    	{
	    		throw new IllegalArgumentException("error creating CitationCollection");
	    	}
	    	collectionId = collection.getId();
	    	if(collectionId == null)
	    	{
	    		throw new IllegalArgumentException("Error creating CitationCollection");
	    	}
	    }
	    return collectionId;
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable#getSampleEntity()
	 */
	public Object getSampleEntity()
    {
	    return this.citationService.addCollection();
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable#updateEntity(org.sakaiproject.entitybroker.EntityReference, java.lang.Object, java.util.Map)
	 */
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params)
    {
	    if(entity instanceof CitationCollection)
	    {
		    CitationCollection collection = (CitationCollection) entity;
			this.citationService.save(collection );
	    }
	    else
	    {
	    	throw new IllegalArgumentException("Error updating CitationCollection");
	    }
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
	 */
	public Object getEntity(EntityReference ref)
    {
		Object entity = null;
		String collectionId = ref.getId();
		if(collectionId == null)
		{
			entity = getSampleEntity();
		}
		else
		{
		    try
	        {
		    	entity = this.citationService.getCollection(collectionId);
	        }
	        catch (IdUnusedException e)
	        {
		        logger.debug("CitationsProviderImpl.getEntity --> IdUnusedException: " + collectionId + " " + e);
		        throw new IllegalArgumentException("Invalid id for CitationCollection ");
	        }
		}
        return entity;
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable#deleteEntity(org.sakaiproject.entitybroker.EntityReference, java.util.Map)
	 */
	public void deleteEntity(EntityReference ref, Map<String, Object> params)
    {
		String collectionId = ref.getId();
		if(collectionId == null)
		{
	        logger.debug("CitationsProviderImpl.deleteEntity --> null cellection id");
		}
		else
		{
		    try
	        {
		    	CitationCollection collection = this.citationService.getCollection(collectionId);
				this.citationService.removeCollection(collection );
	        }
	        catch (IdUnusedException e)
	        {
		        logger.debug("CitationsProviderImpl.deleteEntity --> IdUnusedException: " + collectionId + " " + e);
	        }
		}
    }

}
