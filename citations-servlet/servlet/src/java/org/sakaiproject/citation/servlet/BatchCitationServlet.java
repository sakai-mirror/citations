/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.citation.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

import net.sf.json.*;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.cover.ConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * 
 *
 */
public class BatchCitationServlet extends CitationServlet
{

	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
	    super.doGet(req, res);
	}

	/**
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin(req, res, null);
		}

		else if (req.getParameter("batch_urls") == null)
		{
		    // There is no POST handling at the base, so just throw here
		    // If there was some single-URL handling, we could call super.doPost
		    sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
		else
		{
			setupResponse(req, res);
			ContentResource resource = null;
			try {
				ParameterParser paramParser = (ParameterParser) req
					.getAttribute(ATTR_PARAMS);
				resource = findResource(paramParser, option);

				ArrayList<Citation> citations = new ArrayList<Citation>();
				ArrayList<String> failures = new ArrayList<String>();
				String[] urls = req.getParameterValues("url[]");
				if (urls != null && urls.length > 0) {
					for (String url : urls) {
						//decode POSTed URL
						String decodedUrl = URLDecoder.decode(url);
						Map<String, String[]> params = getUrlParameters(decodedUrl);
						OpenUrlRequest wrappedReq = new OpenUrlRequest(req, params);
						Citation citation = findOpenUrlCitation(wrappedReq);
						if (citation != null) {
							citations.add(citation);
						}
						else {
							failures.add(url);
						}
					}
				}

				Citation citation = null;
				if (citations.size() > 0) {
					citation = citations.get(0);
				}

				// set the success flag
				setVmReference("success", citation != null, req);

				if (citation != null) {
					addCitation(resource, citation);
					setVmReference( "citation", citation, req );
					setVmReference("topRefresh", Boolean.TRUE, req ); // TODO
				} else {
					// return failure
					setVmReference("error", rb.getString("error.notfound"), req);
				}
			} catch (IdUnusedException iue) {
				setVmReference("error", rb.getString("error.noid"), req);
			} catch (ServerOverloadException e) {
				setVmReference("error", rb.getString("error.unavailable"), req);
			} catch (PermissionException e) {
				setVmReference("error", rb.getString("error.permission"), req);
			}
			// Set near end so we always have something
			setVmReference( "titleArgs",  new String[]{ getCollectionTitle(resource) }, req );
			// return the servlet template
			includeVm( SERVLET_TEMPLATE, req, res );
		}
	}

	public class OpenUrlRequest extends HttpServletRequestWrapper {
		private Map<String, String[]> openUrlParams;

		public OpenUrlRequest(HttpServletRequest request, Map<String, String[]> openUrlParams) {
			super(request);
			this.openUrlParams = openUrlParams;
		}

		public Map<String, String[]> getParameterMap() {
			return Collections.unmodifiableMap(openUrlParams);
		}

		public String getParameter(String name) {
			String[] vals = openUrlParams.get(name);
			if (vals != null && vals.length > 0) {
				return vals[0];
			}
			return null;
		}

		public Enumeration<String> getParameterNames() {
			return Collections.enumeration(openUrlParams.keySet());
		}

		public String[] getParameterValues(String name) {
			String[] val = openUrlParams.get(name);
			if (val != null) {
				ArrayList list = new ArrayList(Arrays.asList(val));
				ArrayList copy = new ArrayList(list);
				return (String[]) copy.toArray(new String[] {});
			}
			return null;
		}
	}
	
	public static Map<String, String[]> getUrlParameters(String url) throws UnsupportedEncodingException {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		String[] urlParts = url.split("\\?");
		if (urlParts.length > 1) {
			String query = urlParts[1];
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				String key = URLDecoder.decode(pair[0], "UTF-8");
				String value = "";
				if (pair.length > 1) {
					value = URLDecoder.decode(pair[1], "UTF-8");
				}
				List<String> values = params.get(key);
				if (values == null) {
					values = new ArrayList<String>();
					params.put(key, values);
				}
				values.add(value);
			}
		}

		Map<String, String[]> ret = new HashMap<String, String[]>();
		for (String k : params.keySet()) {
			ret.put(k, (String[]) params.get(k).toArray(new String[] {}));
		}
		return ret;
	}

}
