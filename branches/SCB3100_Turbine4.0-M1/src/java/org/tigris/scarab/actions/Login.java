package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;

// Turbine Stuff 
import org.apache.velocity.context.Context;
import org.apache.turbine.util.RunData;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.modules.Action;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.turbine.util.security.DataBackendException;
import org.apache.turbine.util.security.UnknownEntityException;
import org.apache.turbine.util.security.PasswordMismatchException;
import org.apache.turbine.util.security.TurbineSecurityException;

// Scarab Stuff
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.actions.base.ScarabTemplateAction;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for dealing with the Login
 * Action.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class Login extends ScarabTemplateAction
{

    public static void simpleLogin(RunData data, ScarabUser user)
    {
        if(user.getUserName().equals("")) //is it a TurbineAnonymousUser?
        {
        	user.setHasLoggedIn(Boolean.FALSE);
        }
        else
        {
        	user.setHasLoggedIn(Boolean.TRUE);
            try
            {
                user.updateLastLogin();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        data.setUser(user);
        data.save();
    }

    public static void anonymousLogin(RunData data)
    {
    	ScarabUser anonymous = null;
    	try
        {
        	anonymous = ScarabUserManager.getAnonymousUser();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        simpleLogin(data, anonymous);
    }


    /**
     * This manages clicking the Login button
     */
    public void doLogin(RunData data, Context context)
        throws Exception
    {
        data.setACL(null);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid() && checkUser(data, context))
        {
            ScarabUser user = (ScarabUser)data.getUser();
            List userModules = user.getModules();
            if (userModules != null)
            {
                Module uniqueModule = null;
                if (userModules.size() == 2)
                {
                    Module module1 = (Module)userModules.get(0);
                    Module module2 = (Module)userModules.get(1);
                    if (module1.isGlobalModule())
                    {
                        uniqueModule = module2;
                    }
                    else if (module2.isGlobalModule())
                    {
                        uniqueModule = module1;
                    }
                }
                else if (userModules.size() == 1)
                {
                    uniqueModule = (Module)userModules.get(0);
                    if (uniqueModule.isGlobalModule())
                    {
                        uniqueModule = null;
                    }
                }

                if (uniqueModule != null )
                {
                    getScarabRequestTool(context).setCurrentModule(uniqueModule);
                    data.getParameters().remove(ScarabConstants.CURRENT_MODULE);
                    data.getParameters().add(ScarabConstants.CURRENT_MODULE,
                                             uniqueModule.getQueryKey());

                    if ("SelectModule.vm".equals(data.getParameters()
                            .getString(ScarabConstants.NEXT_TEMPLATE, "SelectModule.vm"))
                        && user.hasPermission(ScarabSecurity.ISSUE__ENTER, uniqueModule )) 
                    {
                        data.getParameters().remove(ScarabConstants.NEXT_TEMPLATE);
                        
                        Query defaultQuery = QueryPeer.getDefaultQuery(uniqueModule, user.getUserId());
                        ParameterParser pp = data.getParameters();
                        if(defaultQuery != null)
                        {
                            String qkey = defaultQuery.getQueryKey();
                            pp.add(ScarabConstants.NEXT_TEMPLATE, "IssueList.vm");
                            //pp.add("queryId", qkey);
                            //pp.add("query", qkey);
                            //data.setAction("Search");
                            pp.setString("eventSubmit_doSelectquery", "foo");
                            pp.setString("go",qkey);
                            Search action = new Search();
                            action.doSelectquery(data, context);
                            
                            MITList mitList = defaultQuery.getMITList();
                            user.setCurrentMITList(mitList);
                            String mitListId = Long.toString(mitList.getListId());
                            pp.setString(ScarabConstants.CURRENT_MITLIST_ID, mitListId);
                        }
                        else
                        {
                            pp.add(ScarabConstants.NEXT_TEMPLATE, "home,EnterNew.vm");
                        }
                    }
                }
            }
            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                           "SelectModule.vm");
            data.setScreenTemplate(template);
        }
    }

    /**
     * Used from LoginValve only
     * Checks that the user exists and has been confirmed.
     * If all checks have passed, setup the login-data. The user is now logged in.
     * If checks fail, the user is not logged in, but a failure action has been 
     * prepared for further activity (return to login page, not confirmed page, password renew page, etc..)
     */
    public boolean checkUser(RunData data, Context context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);

        Group login = intake.get("Login", IntakeTool.DEFAULT_KEY);
        String username = login.get("Username").toString();
        String password = login.get("Password").toString();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        return authentifyWithCredentials(data, scarabR, username, password);
    }
        
    /**
     * More generic authentification, used for automatic login (parameter based).
     * Note: This is a highly unsecure method and it bears some potential vulnerability.
     * So it should be used with care. the calling method is responsible to ensure safe usage!
     * 
     * The method checks that the user exists and has been confirmed.
     * If all checks have passed, setup the login-data. The user is now logged in.
     * If checks fail, the user is not logged in.
     * If a ScarabRequestTool instance is provided (not null) then a failure action will be 
     * prepared for further activity (return to login page, not confirmed page, password renew page, etc..)
     * @throws Exception 
     */
    public static boolean authentifyWithCredentials(RunData data, ScarabRequestTool scarabR, String username, String password) throws Exception
    {
        
        ScarabUser user = authenticateUser(data, scarabR, username, password);
        if(user == null)
        {
            // Could not authenticate the user
            return false;
        }

        try
        {
            if (user.getConfirmed().equals(ScarabUser.DELETED))
            {
                setAlertMessage(scarabR,L10NKeySet.UserIsDeleted);
                Log.get().error("Deleted user attempting to log in");
                failAction(data, "Login.vm");
                return false;
            }
            // check the CONFIRM_VALUE
            if (!user.isConfirmed())
            {
                user = (ScarabUser) TurbineSecurity.getUserInstance();
                setUser(scarabR, user);
                setAlertMessage(scarabR,L10NKeySet.UserIsNotConfirmed);

                failAction(data, "Confirm.vm");
                return false;
            }


            // store the user object
            data.setUser(user);
            // mark the user as being logged in
            user.setHasLoggedIn(Boolean.TRUE);
            // set the last_login date in the database
            user.updateLastLogin();

            // check if the password is expired
            boolean userPasswordExpired = user.isPasswordExpired();
            if (userPasswordExpired)
            {
                user = (ScarabUser) TurbineSecurity.getUserInstance();
                setUser(scarabR, user);
                setAlertMessage(scarabR,L10NKeySet.YourPasswordHasExpired);

                data.setScreenTemplate("ChangePassword.vm");
                //change next screen to allow password reset.
                data.save();
                return false;
            }

            // update the password expire
            user.setPasswordExpire();
            // this only happens if the user is valid
            // otherwise, we will get a valueBound in the User
            // object when we don't want to because the username is
            // not set yet.
                       
            // save the User object into the session
            data.save();

        }
        catch (TurbineSecurityException e)
        {
            Localizable msg = new L10NMessage(L10NKeySet.ExceptionTurbineGeneric,e);
            setAlertMessage(scarabR,msg);
            failAction(data, "Login.vm");
            return false;
        }
        return true;
    }

    private static void setUser(ScarabRequestTool scarabR, ScarabUser user) 
    {
        scarabR.setUser(user);
    }

    private static void setAlertMessage(ScarabRequestTool scarabR, Localizable key) 
    {
        if(scarabR != null)
        {
            scarabR.setAlertMessage(key);
        }
    }

    /**
     * Perform a simple authentication for given user/password.
     * returns the ScarabUser instance on success.
     * returns a null pointer on failure.
     * @param data
     * @param scarabR
     * @param username
     * @param password
     * @return
     */
    private static ScarabUser authenticateUser(RunData data, ScarabRequestTool scarabR, String username, String password) 
    {        
        ScarabUser user = null;  
        try
        {
            // Authenticate the user and get the object.
            user = (ScarabUser) TurbineSecurity
                .getAuthenticatedUser(username, password);
        }
        catch (UnknownEntityException e)
        {
            setAlertMessage(scarabR, L10NKeySet.InvalidUsernameOrPassword);
            Log.get().info("Invalid login attempted: " + e.getMessage());
            failAction(data, "Login.vm");            
            return null;
        }
        catch (PasswordMismatchException e)
        {
            setAlertMessage(scarabR, L10NKeySet.InvalidUsernameOrPassword);
            Log.get().debug("Password mis-match during login attempt: "
                           + e.getMessage());
            failAction(data, "Login.vm");            
            return null;
        }
        catch (DataBackendException e)
        {
            setAlertMessage(scarabR, L10NKeySet.ExceptionDatabaseGenericError);
            Log.get().error("Error while attempting to log in", e);
            failAction(data, "Login.vm");
            return null;
        }
        return user;
    }

    /**
     * sets an anonymous user
     * sets the template to the passed in template
     */
    private static void failAction(RunData data, String template)
    {
        anonymousLogin(data);
    	data.setScreenTemplate(template);
    }
    
    /**
     * calls doLogin()
     */
    public void doPerform(RunData data, Context context)
        throws Exception
    {
        doLogin(data, context);
    }    
}
