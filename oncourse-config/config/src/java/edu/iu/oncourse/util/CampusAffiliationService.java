package edu.iu.oncourse.util;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.component.cover.ServerConfigurationService;



public class CampusAffiliationService {

	private static final Log LOG = LogFactory.getLog(CampusAffiliationService.class);


	/**
	 * @param args
	 * @throws Exception
	 */

	public static String[] getCampusAffiliations(String userEid) throws Exception
	{
		String ADSuser = ServerConfigurationService.getString("ads.user");
		String ADSpassword = ServerConfigurationService.getString("ads.pw");

		List<String>      attrs = new ArrayList<String>();
		NamingEnumeration namingEnumeration = null;

		String domain = "DC=ads,DC=iu,DC=edu"; // if your domain is domain.com

		Hashtable env = new Hashtable(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ads.iu.edu:636");
		env.put(Context.REFERRAL, "follow");
		env.put(Context.SECURITY_PRINCIPAL, "cn=" + ADSuser + ",ou=Accounts," + domain);
		env.put(Context.SECURITY_CREDENTIALS, ADSpassword);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PROTOCOL, "ssl");
		DirContext ctx = null;
		try
		{
			ctx = new InitialDirContext(env);
		} catch (NamingException e)
		{
			throw new RuntimeException("Cannot connect to Ldap server: " + e);
		}
		SearchControls constrains = new SearchControls();
		constrains.setSearchScope(SearchControls.SUBTREE_SCOPE);
		constrains.setReturningAttributes(new String[] {  "ou" });
		NamingEnumeration results = null;


		try
		{
			results = ctx.search("ou=Accounts," + domain, "(&(objectCategory=user)(cn="+userEid+"))", constrains);
			while (results.hasMore())
			{
		    SearchResult thisResult = (SearchResult) results.next();
		    Attributes attr = thisResult.getAttributes();

		    // add each value
		    namingEnumeration = (NamingEnumeration) attr.getAll();

		    while (namingEnumeration.hasMore())
		    {
		      Attribute thisAttribute = (Attribute) namingEnumeration.next();

          for (int i = 0; i < thisAttribute.size(); i++)
          {
            String attributeString = (String) thisAttribute.get(i);

		        attrs.add(attributeString);
		      }
		    }
		  }
		}
		finally
		{
      if (namingEnumeration != null)
      {
        try { namingEnumeration.close(); } catch (Exception ignore) { }
      }
      if (results != null)
      {
        try { results.close(); } catch (Exception ignore) { }
      }
      if (ctx != null)
      {
        try { ctx.close(); } catch (Exception ignore) { }
      }
		}
		/*
		 * Return null if no results available, populated String array otherwise
		 */
		return attrs.isEmpty() ? null : attrs.toArray(new String[attrs.size()]);
	}

}
