/**
 * 
 */
package org.tigris.scarab.services.security.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * @author pti
 *
 */
public class MockInitialContextFactory implements InitialContextFactory {

	/* (non-Javadoc)
	 * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
	 */
	public Context getInitialContext(Hashtable arg0) throws NamingException {
		return new MockJndiContext();
	}

}
