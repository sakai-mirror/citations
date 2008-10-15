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

package org.sakaiproject.citations.helper;

import org.sakaiproject.citation.api.CitationsProvider;
import org.sakaiproject.citations.helper.params.CitationsViewParams;
import org.sakaiproject.citations.helper.producers.CitationCollectionProducer;
import org.sakaiproject.entitybroker.EntityReference;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * CitationsViewParamsInferrer 
 *
 */
public class CitationsViewParamsInferrer implements EntityViewParamsInferrer
{
	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference)
	{
		EntityReference ref = new EntityReference(reference);
		return new CitationsViewParams(CitationCollectionProducer.VIEW_ID, ref.getId());
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.PrefixHandler#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes()
	{
		return new String[] { CitationsProvider.ENTITY_PREFIX };

	}

}
