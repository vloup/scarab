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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;


public class DummyCallbackHandler implements CallbackHandler {
	private String username;
    private LDAPUserInfoCallback ldapUserInfo;

	char[] password;

	public DummyCallbackHandler() {
	};

	/**
	 * <p>
	 * Creates a callback handler with the give username and password.
	 */
	public DummyCallbackHandler(String user, String pass) {
		this.username = user;
		this.password = pass.toCharArray();
	}

	public void setUsername(String user) {
		this.username = user;
	}

	public void setPassword(String pass) {
		this.password = pass.toCharArray();
	}
    
    

	/**
     * @return Returns the ldapUserInfo.
     */
    public LDAPUserInfoCallback getLdapUserInfo() {
        return ldapUserInfo;
    }

   

    public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				((NameCallback) callbacks[i]).setName(username);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(password);
            } else if (callbacks[i] instanceof LDAPUserInfoCallback) {
                ldapUserInfo = (LDAPUserInfoCallback) callbacks[i];
			} else {
				throw new UnsupportedCallbackException(callbacks[i],
						"Callback class not supported");
			}
		}
	}

	/**
	 * Clears out password state.
	 */
	public void clearPassword() {
		if (password != null) {
			for (int i = 0; i < password.length; i++)
				password[i] = ' ';
			password = null;
		}
	}

}
