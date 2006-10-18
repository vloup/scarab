
package org.tigris.scarab.services.security.ldap;
/* ====================================================================
*
* Copyright (c) 2006 CollabNet.
*
* Licensed under the
*
*     CollabNet/Tigris.org Apache-style license (the "License");
*
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://scarab.tigris.org/LICENSE
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
* implied. See the License for the specific language governing
* permissions and limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of CollabNet.
*
*/

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbineUser;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.security.ScarabDBSecurityService;
import org.tigris.scarab.util.PasswordGenerator;

/**
 * Iterate over the query result set of an LDAP query for users
 * 
 * @author pti
 * 
 */
public class LDAPSynchronizer {
	static Category logger = Logger.getInstance(LDAPSynchronizer.class);

	private String baseDn;

	private String providerFactory;

	private String providerUrl;

	private String query;

	private String loginAttribute;

	public LDAPSynchronizer(Configuration cfg) throws NamingException {
		providerFactory = cfg.getString(providerFactory,
				"com.sun.jndi.ldap.LdapCtxFactory");
		providerUrl = cfg.getString("providerUrl", "ldap://localhost/");
		baseDn = cfg.getString("baseDN", "");
		query = cfg.getString("query", "objectClass=inetOrgPerson");
		loginAttribute = cfg.getString("loginAttribute", "uid");
	}

	/**
	 * Synchronize a user from an LDAP repository with the database. Create new
	 * ScarabUsers for people found in LDAP, but not in Scarab.
	 * 
	 * TODO: remove/disable ScarabUsers not found in LDAP
	 * 
	 * @param sr
	 *            A search result returned from the JNDI query;
	 * @throws Exception 
	 */
	public void synchUser(SearchResult sr) throws Exception {
		Attributes attributes = null;
		ScarabUser user = null;

		attributes = sr.getAttributes();
		String uid = (String) attributes.get(loginAttribute).get(0);
		logger.info("uid : " + uid);
		user = ScarabUserManager.getInstance(uid);

		// if the user does not exist yet, create a new one
		if (user == null) {
			user = ScarabUserManager.getInstance();
			user.setName(uid);

			// Generate random password to avoid security hole.
			// This will be reset when the user logs in the first time
			user.setPassword(PasswordGenerator.generate());
			user.createNewUser();
		}

		user.setConfirmed("CONFIRMED");
		user.setEmail(getAttributeValue(attributes.get("mail"), "Unknown"));
		user.setLastName(getAttributeValue(attributes.get("sn"), "Unknown"));
		user.setFirstName(getAttributeValue(attributes.get("givenName"),
				"Unknown"));

		TurbineSecurity.saveUser(user);

	}

	/**
	 * @param attrib
	 *            Attribute to get the value from
	 * @param deflt
	 *            Default string returned when attrib is null.
	 * @throws NamingException
	 */
	private String getAttributeValue(Attribute attrib, String deflt)
			throws NamingException {
		String result = deflt;
		if (attrib != null) {
			result = (String) attrib.get(0);
		}
		return result;
	}

	/**
	 * Do a subtree search to get all accounts and synch them one by one.
	 * 
	 * @throws NamingException
	 */
	public void synchronize() throws NamingException {
		InitialDirContext ctx;
		NamingEnumeration results;

		Hashtable props = new Hashtable();
		props.put(Context.INITIAL_CONTEXT_FACTORY, providerFactory);
		props.put(Context.PROVIDER_URL, providerUrl);
		ctx = new InitialDirContext(props);

		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		results = ctx.search(baseDn, query, null, cons);

		while (results.hasMore()) {
			try {
				synchUser((SearchResult) results.next());
			} catch (NamingException e) {
				logger.error("NamingException caught: ", e);
			} catch (Exception e) {
				logger.error("Exception caught: ", e);
			}
		}

	}

}
