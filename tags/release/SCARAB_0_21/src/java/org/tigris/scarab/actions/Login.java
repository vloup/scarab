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
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.TurbineSecurityException;

// Scarab Stuff
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
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
    /**
     * This manages clicking the Login button
     */
    public void doLogin(RunData data, TemplateContext context)
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
                        data.getParameters().add(ScarabConstants.NEXT_TEMPLATE,
                                                 "home,EnterNew.vm");
                    }
                }
            }
            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                           "SelectModule.vm");
            setTarget(data, template);
        }
    }

    /**
     * Checks to make sure that the user exists, has been confirmed.
     */
    public boolean checkUser(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        Group login = intake.get("Login", IntakeTool.DEFAULT_KEY);
        String username = login.get("Username").toString();
        String password = login.get("Password").toString();
        
        ScarabUser user = null;

        try
        {
            // Authenticate the user and get the object.
            user = (ScarabUser) TurbineSecurity
                .getAuthenticatedUser(username, password);
        }
        catch (UnknownEntityException e)
        {
            scarabR.setAlertMessage(L10NKeySet.InvalidUsernameOrPassword);
            Log.get().info("Invalid login attempted: " + e.getMessage());
            return failAction(data, "Login.vm");            
        }
        catch (PasswordMismatchException e)
        {
            scarabR.setAlertMessage(L10NKeySet.InvalidUsernameOrPassword);
            Log.get().debug("Password mis-match during login attempt: "
                           + e.getMessage());
            return failAction(data, "Login.vm");            
        }
        catch (DataBackendException e)
        {
            scarabR.setAlertMessage(L10NKeySet.ExceptionDatabaseGenericError);
            Log.get().error("Error while attempting to log in", e);
            return failAction(data, "Login.vm");
        }

        try
        {
            if (user.getConfirmed().equals(ScarabUser.DELETED)){
                scarabR.setAlertMessage(L10NKeySet.UserIsDeleted);
                Log.get().error("Deleted user attempting to log in");
                return failAction(data, "Login.vm");
            }
            // check the CONFIRM_VALUE
            if (!user.isConfirmed())
            {
                if (scarabR != null)
                {
                    user = (ScarabUser) TurbineSecurity.getUserInstance();
                    scarabR.setUser(user);
                    scarabR.setAlertMessage(L10NKeySet.UserIsNotConfirmed);
                }

                return failAction(data, "Confirm.vm");
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
                if (scarabR != null)
                {
                    user = (ScarabUser) TurbineSecurity.getUserInstance();
                    scarabR.setUser(user);
                    scarabR.setAlertMessage(L10NKeySet.YourPasswordHasExpired);
                }


                setTarget(data, "ChangePassword.vm");
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
            scarabR.setAlertMessage(msg);
            return failAction(data, "Login.vm");
        }
        return true;
    }

    /**
     * sets an anonymous user
     * sets the template to the passed in template
     */
    private boolean failAction(RunData data, String template)
        throws DataBackendException, UnknownEntityException
    {
        // Retrieve an anonymous user
        AnonymousUserUtil.anonymousLogin(data);
        setTarget(data, template);
        return false;
    }
    
    /**
     * calls doLogin()
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doLogin(data, context);
    }
    
}
