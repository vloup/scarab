/**
 * 
 */
package org.tigris.scarab.services.security.ldap;

import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.test.BaseScarabTestCase;

import junit.framework.TestCase;

/**
 * @author pti
 *
 */
public class LDAPIteratorTest extends BaseScarabTestCase {

	private LDAPSynchronizer syncer;
	private LDAPSynchronizer emptySyncer;
	
	final static String TESTUSER = "u001";
	
	public void setUp() throws Exception {
		super.setUp();

		Configuration config = new PropertiesConfiguration();
		config.setProperty("providerFactory", "com.sun.jndi.ldap.LdapCtxFactory");
		config.setProperty("providerURL", "ldap://localhost/");
		config.setProperty("baseDN", "dc=crowsnest,dc=be");
		config.setProperty("loginAttribute", "uid");

		config.setProperty("query", "objectClass=person");
		syncer = new LDAPSynchronizer(config);
		
		if (TurbineSecurity.accountExists(TESTUSER)) {
			TurbineSecurity.removeUser(TurbineSecurity.getUser(TESTUSER));			
		}

	}
	
	/**
	 * Test the update from LDAP to scarab user
	 * 
	 * @throws Exception
	 */
	public void testSyncer() throws Exception {
		SearchResult sr;
		Attributes attribs;
		User user;
		
		attribs = new BasicAttributes();
		attribs.put("uid",TESTUSER);
		attribs.put("sn","Testeur");
		attribs.put("givenName","Ted");
		attribs.put("mail","ted@testeur.org");
		
		sr = new SearchResult("dn=testuser,dc=testeur,dc=org",null,attribs);
		syncer.synchUser(sr);
		
		// test creation of new account
		user = TurbineSecurity.getUser(TESTUSER);
		assertNotNull(user);
		assertEquals("ted@testeur.org",user.getEmail());
		assertEquals("Ted",user.getFirstName());
		assertEquals("Testeur",user.getLastName());

		attribs.put("givenName","Tom");
		attribs.put("mail","tom@testeur.org");

		// test update of existing account
		sr = new SearchResult("dn=testuser,dc=testeur,dc=org",null,attribs);
		syncer.synchUser(sr);
		user = TurbineSecurity.getUser(TESTUSER);
		assertEquals("tom@testeur.org",user.getEmail());
		assertEquals("Tom",user.getFirstName());

	}
	
}
