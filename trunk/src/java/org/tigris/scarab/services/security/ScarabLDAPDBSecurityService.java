package org.tigris.scarab.services.security;
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
*
* Copyright (C) 2004 Core Software Foundation
*
*/
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.UnknownEntityException;

import org.apache.log4j.Logger;

import org.apache.torque.TorqueException;
import org.apache.turbine.Turbine;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.security.ldap.DummyCallbackHandler;
import org.tigris.scarab.services.security.ldap.LDAPSynchronizer;
import org.tigris.scarab.services.security.ldap.LDAPUserInfoCallback;


import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


/**
 * @author <a href="mailto:evrim@core.gen.tr">Evrim ULU</a>
 * @version 1.0
 */
public class ScarabLDAPDBSecurityService extends ScarabDBSecurityService {
    static Logger log = Logger.getLogger(ScarabLDAPDBSecurityService.class);
    DummyCallbackHandler cbh = new DummyCallbackHandler();
    LoginContext lc = null;

    public ScarabLDAPDBSecurityService() {
        super();
        initialize();
    }

	/* (non-Javadoc)
	 * @see org.apache.fulcrum.security.BaseSecurityService#init()
	 * 
	 * Initialize parent and synchronize accounts in configured to do so
	 */
	public void init() throws InitializationException {
		super.init();
        Configuration cfg = Turbine.getConfiguration().subset("scarab.login.ldap");
        if ((cfg != null) && 
        	(getConfiguration().getBoolean("synchronizeOnStartUp",false))) {
			try {
				LDAPSynchronizer syncher = new LDAPSynchronizer(cfg);
				syncher.synchronize(); 
			} catch (NamingException e) {
				log.error("NamingException caught synchronizing accounts: ",e);
			}
        }
	}
	

	/**
     * Prepare the login context.
     */
    private void initialize() {
        cbh.clearPassword();

        try {
            lc = new LoginContext("Scarab", cbh);
        } catch (LoginException e) {
            log.error(
                "Unable to create LDAP jaas login context. Forget to add jaas config to JVM? Try adding -Djava.security.auth.login.config=jaas.conf to servlet container.");
            log.error(e);
        }
    }

    /**
     * Authenticates a user, using the name and password present in the
     * parameter.
     *
     * @return true, if this is a valid UserProfile, false otherwise.
     */
    private LDAPUserInfoCallback authenticate(String user, String pass) {
        if (null == lc) {
            return null;
        }

        LDAPUserInfoCallback info = null;
        cbh.setUsername(user);
        cbh.setPassword(pass);

        try {
            lc.login();

            info = cbh.getLdapUserInfo();
            cbh.clearPassword();
        } catch (LoginException e) {
            cbh.clearPassword();
            log.info(e);

            return null;
        }

        return info;
    }

    /**
     * Authenticates an user, and constructs an User object to represent
     * him/her.
     *
     * @param username The user name.
     * @param password The user password.
     * @return An authenticated Turbine User.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's account does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     *
     * This function is overriden by LDAP module to enable LDAP
     * authentication.
     */
    public User getAuthenticatedUser(String username, String password)
        throws DataBackendException, UnknownEntityException, 
            PasswordMismatchException {
        LDAPUserInfoCallback info = authenticate(username, password);

        /* First try to auth from ldap */
        if (info != null) {
            /* Check if user exists */
            if (!super.userManager.accountExists(username)) {
                try {
                    super.userManager.createAccount(createUser(username,
                            password, info), password);
                } catch (EntityExistsException e) {
                }

                
            } else {
                User curruser = userManager.retrieve(username);
                copyProps(curruser, info);

                String encrypted = TurbineSecurity.encryptPassword(password);

                if (!encrypted.equals(curruser.getPassword())) {
                    curruser.setPassword(password);
                }

                userManager.store(curruser);
            }

            return userManager.retrieve(username);
        }

        return userManager.retrieve(username, password);
    }

    /**
     * Create a new user with the given username and password. Then copy
     * the useful information from the ldapUserInfo object.
     * 
     * @param  username
     * @param  password
     * @param  ldapUserInfo
     * @return the newly created user.
     */
    private User createUser(String username, String password,
        LDAPUserInfoCallback ldapUserInfo) {
        User user = null;

        try {
            user = ScarabUserManager.getInstance(); //ScarabUserManager.getInstance(sm.getOwnerId());
        } catch (TorqueException e) {
            log.error(e);

            return null;
        }

        user.setConfirmed("CONFIRMED");
        user.setUserName(username);
        user.setPassword(password);
        copyProps(user, ldapUserInfo);

        return user;
    }

    private void copyProps(User user, LDAPUserInfoCallback ldapUserInfo) {
        user.setEmail(ldapUserInfo.getEmail());
        user.setFirstName(ldapUserInfo.getGivenname());
        user.setLastName(ldapUserInfo.getSn());
    }


}
