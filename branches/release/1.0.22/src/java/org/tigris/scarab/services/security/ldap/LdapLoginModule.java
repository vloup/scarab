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
* Copyright (C) 2004 Core Software Foundation
*
*/
import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


public class LdapLoginModule implements LoginModule {
	public static final String HOST_KEY = "host";
	public static final String USERNAME_KEY = "usernamefield";
	public static final String BASEDN_KEY = "basedn";
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	Properties options = new Properties();
	private String cbName;
	private String cbPassword;
	private boolean verification = false;
	private Hashtable props;
    private DirContext ctx;
    
	public void initialize(Subject subject,
			CallbackHandler callbackHandler, Map sharedState, Map options) {
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options.putAll(options);
	}

	public boolean abort() throws LoginException {
		return true;
	}

	public boolean commit() throws LoginException {
		return true;
	}

	public boolean login() throws LoginException {
		Callback[] calls = new Callback[3];
        LDAPUserInfoCallback ldapUserInfo =  new LDAPUserInfoCallback();
		calls[0] = new NameCallback("name");
		calls[1] = new PasswordCallback("Password", false);
        calls[2] =  ldapUserInfo;
		
		if (callbackHandler == null)
			throw new LoginException("callback is null");

		try {
			callbackHandler.handle(calls);
		} catch (IOException e) {
			throw new LoginException(e.toString());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(
					e.toString()
							+ "callbackHandler does not support name or password callback");
		}

		cbName = ((NameCallback) calls[0]).getName();
		if (cbName.equals(null))
			throw new LoginException("name must not be null");
		
		cbPassword = String.valueOf(((PasswordCallback) (calls[1])).getPassword());
		if (cbPassword.equals(null))
			throw new LoginException("password must not be null");
		
		try {
			props = new Hashtable();
			props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			props.put(Context.PROVIDER_URL, options.get(HOST_KEY));
            ctx = new InitialDirContext(props);
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search((String)options.get(BASEDN_KEY),"uid={0}",new Object[] { cbName },cons);
            Attributes attributes = null;
            String principal = null;
            while (results.hasMore()){
                SearchResult sr = (SearchResult)results.next();
                attributes = sr.getAttributes();
                principal = sr.getName() + "," + options.get(BASEDN_KEY);
            }
            if ( attributes == null || principal == null){
                throw new LoginException("No User Found");
            }
            ctx.close();
			props.put(Context.SECURITY_PRINCIPAL, principal);
			props.put(Context.SECURITY_CREDENTIALS, cbPassword);
			props.put(Context.SECURITY_AUTHENTICATION, "simple");
			ctx = new InitialDirContext(props);
			verification = true;
            ldapUserInfo.setEmail(attributes.get("mail").get().toString());
            ldapUserInfo.setGivenname(attributes.get("givenname").get().toString());
            ldapUserInfo.setSn(attributes.get("sn").get().toString());
			ctx.close();
		} catch (NamingException e) {
			throw new LoginException(e.toString() + "  " + e.getRootCause());
		}
		
		return verification;
	}

	public boolean logout() throws LoginException {
		return true;
	}
}
