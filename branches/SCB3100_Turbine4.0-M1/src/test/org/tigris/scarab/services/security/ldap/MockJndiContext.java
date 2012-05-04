package org.tigris.scarab.services.security.ldap;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

public class MockJndiContext implements Context {
	/**
 * Logger for this class
 */
private static final Logger logger = Logger.getLogger(MockJndiContext.class);

	static HashMap map = new HashMap();

	static public void clear() {
		map.clear();
	}
	
	public MockJndiContext() throws NamingException {
		super();
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	public Object lookup(String name) throws NamingException {
		Object o = map.get(name);
		if (o == null) {
			throw new NamingException("Name " + name
					+ " not found in MockJndiContext");
		}
		return o;
	}

	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}

	public void bind(String name, Object obj) throws NamingException {
		map.put(name, obj);
	}

	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}

	public void rebind(String name, Object obj) throws NamingException {
		map.put(name, obj);
	}

	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}

	public void unbind(String name) throws NamingException {
		map.remove(name);
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}

	public void rename(String oldName, String newName) throws NamingException {
		bind(newName, lookup(oldName));
		unbind(oldName);
	}

	public NamingEnumeration list(Name name) throws NamingException {
		return null;
	}

	public NamingEnumeration list(String name) throws NamingException {
		return null;
	}

	public NamingEnumeration listBindings(Name name) throws NamingException {
		return null;
	}

	public NamingEnumeration listBindings(String name) throws NamingException {
		return null;
	}

	public void destroySubcontext(Name name) throws NamingException {

	}

	public void destroySubcontext(String name) throws NamingException {

	}

	public Context createSubcontext(Name name) throws NamingException {
		return null;
	}

	public Context createSubcontext(String name) throws NamingException {
		return null;
	}

	public Object lookupLink(Name name) throws NamingException {
		return null;
	}

	public Object lookupLink(String name) throws NamingException {
		return null;
	}

	public NameParser getNameParser(Name name) throws NamingException {
		return null;
	}

	public NameParser getNameParser(String name) throws NamingException {
		return null;
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		return null;
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		return null;
	}

	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		return null;
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		return null;
	}

	public Hashtable getEnvironment() throws NamingException {
		return null;
	}

	public void close() throws NamingException {

	}

	public String getNameInNamespace() throws NamingException {
		return null;
	}

//	static {
//		System.getProperties().setProperty("java.naming.factory.initial",
//				"com.melexis.viiper.shopfloor.ejb.MockJndiContext");
//
//		try {
//			if (!NamingManager.hasInitialContextFactoryBuilder()) {
//			   NamingManager
//					.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {
//
//						public InitialContextFactory createInitialContextFactory(
//								Hashtable environment)
//								throws NamingException {
//							return new MockJndiContext();
//						}
//					});
//			}
//		} catch (IllegalStateException e) {
//			logger.error("Illegal State Exception", e);
//		} catch (NamingException e) {
//			logger.error("NamingException", e);
//		}
//	}

}
