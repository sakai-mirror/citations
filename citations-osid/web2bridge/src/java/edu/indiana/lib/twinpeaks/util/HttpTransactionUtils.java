/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * HTTP utilites
 */
public class HttpTransactionUtils
{
	private static org.apache.commons.logging.Log	_log = LogUtils.getLog(HttpTransactionUtils.class);

  private HttpTransactionUtils() {
  }
  /**
   * Default HTTP character set
   */
  public static final String DEFAULTCS	= "ISO-8859-1";

	/*
	 * Parameter handling
	 */

	/**
	 * Format one HTTP parameter
	 * @param name Parameter name
	 * @param value Parameter value (URLEncoded using default chracter set)
	 * @return Parameter text (ampersand+name=url-encoded-value)
	 */
  public static String formatParameter(String name, String value) {
  	return formatParameter(name, value, "&", DEFAULTCS);
  }

	/**
	 * Format one HTTP parameter
	 * @param name Parameter name
	 * @param value Parameter value (will be URLEncoded)
	 * @param separator Character to separate parameters
	 * @param cs Character set specification (utf-8, etc)
	 * @return Parameter text (separator+name=url-encoded-value)
	 */
  public static String formatParameter(String name, String value,
  																		 String separator, String cs) {
  	StringBuffer parameter = new StringBuffer();

    if (!StringUtils.isNull(value)) {

	   	parameter.append(separator);
 		 	parameter.append(name);
 			parameter.append('=');

			try {
  	 		parameter.append(URLEncoder.encode(value, cs));
  	 	} catch (UnsupportedEncodingException exception) {
  	 		throw new IllegalArgumentException("Invalid character set: \""
  	 																			 + cs
  	 																			 + "\"");
  		}
  	}
  	return parameter.toString();
  }

	/*
   * HTTP status values
   */

 	/**
 	 * Informational status?
 	 * @return true if so
 	 */
	public static boolean isHttpInfo(int status) {
		return ((status / 100) == 1);
	}

 	/**
 	 * HTTP redirect?
 	 * @return true if so
 	 */
	public static boolean isHttpRedirect(int status) {
		return ((status / 100) == 3);
	}

 	/**
 	 * Success status?
 	 * @return true if so
 	 */
	public static boolean isHttpSuccess(int status) {
		return ((status / 100) == 2);
	}

 	/**
 	 * Error in request?
 	 * @return true if so
 	 */
	public static boolean isHttpRequestError(int status) {
		return ((status / 100) == 4);
	}

 	/**
 	 * Server error?
 	 * @return true if so
 	 */
	public static boolean isHttpServerError(int status) {
		return ((status / 100) == 5);
	}

 	/**
 	 * General "did an error occur"?
 	 * @return true if so
 	 */
	public static boolean isHttpError(int status) {
		return isHttpRequestError(status) || isHttpServerError(status);
	}

	/**
	 * Set up a simple Map of HTTP request parameters (assumes no duplicate names)
	 * @param request HttpServletRequest object
	 * @return Map of name=value pairs
	 */
	public static Map getAttributesAsMap(HttpServletRequest request) {
		Enumeration enumeration		= request.getParameterNames();
		HashMap			map						= new HashMap();

		while (enumeration.hasMoreElements()) {
			String name	= (String) enumeration.nextElement();

			map.put(name, request.getParameter(name));
		}
		return map;
	}

  /**
   * Format a base URL string ( protocol://server[:port] )
   * @param url URL to format
   * @return URL string
   */
  public static String formatUrl(URL url) throws MalformedURLException {
  	return formatUrl(url, false);
  }

  /**
   * Format a base URL string ( protocol://server[:port][/file-specification] )
   * @param url URL to format
   * @param preserveFile Keep the /directory/filename portion of the URL?
   * @return URL string
   */
  public static String formatUrl(URL url, boolean preserveFile)
  																				throws MalformedURLException {
    StringBuffer	result;
		int						port;

		result = new StringBuffer(url.getProtocol());

    result.append("://");
    result.append(url.getHost());

    if ((port = url.getPort()) != -1) {
    	result.append(":");
    	result.append(String.valueOf(port));
   	}

   	if (preserveFile) {
   		String file = url.getFile();

   		if (file != null) {
   			result.append(file);
   		}
   	}
    return result.toString();
	}

	/**
	 * Pull the server [and port] from a URL specification
	 * @param url URL string
	 * @return server[:port]
	 */
	public static String getServer(String url) {
    String  server  = url;
    int     protocol, slash;

    if ((protocol = server.indexOf("//")) != -1) {
      if ((slash = server.substring(protocol + 2).indexOf("/")) != -1) {
        server = server.substring(0, protocol + 2 + slash);
      }
    }
 		return server;
 	}

	/*
	 * urlEncodeParameters(): URL component specifications
	 */

	/**
	 * protocol://server
	 */
	public static final String	SERVER				= "server";
	/**
	 * /file/specification
	 */
	public static final String	FILE					= "file";
	/**
	 * ?parameter1=value1&parameter2=value2
	 */
	public static final String	PARAMETERS		= "parameters";
	/**
	 * /file/specification?parameter1=value1&parameter2=value2
	 */
	public static final String	FILEANDPARAMS	= "fileandparameters";

  /**
   * Fetch a component from a URL string
   * @param url URL String
   * @param component name (one of server, file, parameters, fileandparameters)
   * @return URL component string (null if none)
   */
  public static String getUrlComponent(String url, String component)
  																		 throws MalformedURLException {
		String	file;
		int			index;

		if (component.equalsIgnoreCase(SERVER)) {
			return getServer(url);
		}

		if (!component.equalsIgnoreCase(FILE) &&
		    !component.equalsIgnoreCase(PARAMETERS) &&
		    !component.equalsIgnoreCase(FILEANDPARAMS)) {
			throw new IllegalArgumentException(component);
		}

		file = new URL(url).getFile();
		if (file == null) {
			return null;
		}
		/*
		 * Fetch file and parameters?
		 */
		if (component.equalsIgnoreCase(FILEANDPARAMS)) {
			return file;
		}
		/*
		 * File portion only?
		 */
		index	= file.indexOf('?');

		if (component.equalsIgnoreCase(FILE)) {
			switch (index) {
				case -1:				// No parameters
					return file;
				case 0:					// Only parameters (no file)
					return null;
				default:
					return file.substring(0, index);
			}
		}
		/*
		 * Isolate parameters
		 */
		return (index == -1) ? null : file.substring(index);
	}

	/**
	 * URLEncode parameter names and values
	 * @param original Full URL specification (http://example.com/xxx?a=b&c=d)
	 * @return Original URL with (possibly) encoded parameters
	 */
	public static String urlEncodeFullUrl(String original) {
		StringBuffer 	encoded;
		String				base, file, params;

		try {
			base		= getUrlComponent(original, SERVER);
			file		= getUrlComponent(original, FILE);
			params	= getUrlComponent(original, PARAMETERS);

		} catch (MalformedURLException exception) {
			_log.warn("Invalid URL provided: " + original);
			return original;
		}

		if (StringUtils.isNull(params)) {
			return original;
		}

		encoded = new StringBuffer();
		encoded.append(base);

		if (!StringUtils.isNull(file)) {
			encoded.append(file);
		}
		encoded.append(urlEncodeParameters(params));
		return encoded.toString();
	}


	/**
	 * URLEncode parameter names and values
	 * @param original Original parameter list (?a=b&c=d)
	 * @return Possibly encoded parameter list
	 */
	public static String urlEncodeParameters(String original) {
		StringBuffer 	encoded	= new StringBuffer();

		for (int i = 0; i < original.length(); i++) {
			String c = original.substring(i, i + 1);

			if (!c.equals("&") && !c.equals("=") && !c.equals("?")) {
				c = URLEncoder.encode(c);
			}
			encoded.append(c);
		}
		return encoded.toString();
	}


  /*
   * Test
   */
  public static void main(String[] args) throws Exception {
  	String u = "http://example.com/dir1/dir2/file.html?parm1=1&param2=2";

  	System.out.println("Server: " + getUrlComponent(u, "server"));
  	System.out.println("File: " + getUrlComponent(u, "file"));
  	System.out.println("Parameters: " + getUrlComponent(u, "parameters"));
  	System.out.println("File & Parameters: " + getUrlComponent(u, "fileandparameters"));
  	System.out.println("Bad: " + getUrlComponent(u, "bad"));
  }
}
