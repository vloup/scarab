package org.tigris.scarab.actions.admin;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */


// JDK classes
import java.util.Calendar;
import java.util.List;

import org.apache.fulcrum.intake.model.Group;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.modules.screens.TemplateScreen;
import org.apache.turbine.om.security.Role;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.security.AccessControlList;
import org.apache.turbine.util.RunData;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.velocity.context.Context;
import org.tigris.scarab.actions.ForgotPassword;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.TurbineUserPeer;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.PasswordGenerator;
import org.tigris.scarab.util.ScarabConstants;


/**
 * This class is responsible for dealing with the user management
 * Action(s).
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @author <a href="mailto:mpoeschl@martmot.at">Martin Poeschl</a>
 * @version $Id$
 */
public class ManageUser extends RequireLoginFirstAction
{
    /**
     * This manages clicking the Add User button
     */
    public void doAdduser(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabUser su = null;
        
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Object user = data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register",
                                      ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                                      IntakeTool.DEFAULT_KEY, false);
            }
            
            su  = (ScarabUser) AnonymousUserUtil.getAnonymousUser();
            su.setName(register.get("UserName").toString());
            su.setFirstName(register.get("FirstName").toString());
            su.setLastName(register.get("LastName").toString());
            su.setEmail(register.get("Email").toString());
            su.setPassword(register.get("Password").toString().trim());
            
            if (TurbineSecurity.accountExists(su))
            {
                TemplateScreen.setTemplate(data, template);
                scarabR.setAlertMessage(L10NKeySet.UsernameExistsAlready);
                data.getParameters().setString("errorLast","true");
                data.getParameters().setString("state","showadduser");
                return;
            }
            
            // if we got here, then all must be good...
            try
            {
                su.createNewUser();
                ScarabUserImpl.confirmUser(register.get("UserName").toString());
                // force the user to change their password the first time they login
                su.setPasswordExpire(Calendar.getInstance());
                Localizable msg = new L10NMessage(L10NKeySet.UserCreated,register.get("UserName").toString());
                scarabR.setConfirmMessage(msg);
                data.getParameters().setString("state","showadduser");
                data.getParameters().setString("lastAction","addeduser");
                
                TemplateScreen.setTemplate(data, nextTemplate);
                return;
            }
            catch (Exception e)
            {
                TemplateScreen.setTemplate(data, template);
                data.getParameters().setString("lastAction","");
                Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                scarabR.setAlertMessage (msg);
                Log.get().error(e);
                data.getParameters().setString("state","showadduser");
                return;
            }
        }
        else
        {
            data.getParameters().setString("state","showadduser");
            data.getParameters().setString("lastAction","");
        }
    }
    
    public void doEdituser(RunData data, Context context) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabUser su = null;
        
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Object user = data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register",
                                          ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                                      IntakeTool.DEFAULT_KEY, false);
            }
            
            
            // if we got here, then all must be good...

            String username = data.getParameters().getString("username");
            su = (ScarabUser) TurbineSecurity.getUser(username);
            try
            {
                if ((su != null) && (register != null))
                {
                    // update the first name, last name, email
                    // Turbine's security service does not allow
                    // changing the username, this is considered the
                    // defining info of a particular user.  SCB197 is
                    // a request to make this information modifiable.
                    su.setFirstName(register.get("FirstName").toString());
                    su.setLastName(register.get("LastName").toString());
                    su.setEmail(register.get("Email").toString());
                    su.setConfirmed(data.getParameters().getString("accountStatus"));
                    TurbineSecurity.saveUser(su);
                    
                    //
                    // Fix: SCB1065
                    // I think this fix really belongs in Turbine, but
                    // I'm not going to touch that code. So here's a 
                    // workaround.
                    //
                    User userInSession = data.getUser(); 
                    if (userInSession.getName().equals(username))
                    {
                        //
                        // The current user is trying to modify their
                        // own details. Update the user object in the
                        // session with the new values otherwise the
                        // old ones will be saved back to the database
                        // when the user logs out, or the session times
                        // out.
                        //
                        userInSession.setFirstName(su.getFirstName());
                        userInSession.setLastName(su.getLastName());
                        userInSession.setEmail(su.getEmail());
                        userInSession.setConfirmed(su.getConfirmed());
                    }


                    
                    String password;
                    String passwordConfirm;

                    String generatePassword = data.getParameters().getString("generate-password");
                    if(generatePassword!=null && generatePassword.equalsIgnoreCase("on"))
                    {
                        password = passwordConfirm = PasswordGenerator.generate();
                    }
                    else
                    {
                        password = register.get("NPassword").toString();
                        passwordConfirm = register.get("NPasswordConfirm").toString();                        
                    }
                        
                    if (!password.equals(""))
                    {
                        if (password.equals(passwordConfirm))
                        {
                            TurbineSecurity.forcePassword(su, password);
                            su.setPasswordExpire(Calendar.getInstance());
                            TurbineSecurity.saveUser(su);
                            User me = data.getUser();
                            try
                            {
                                data.setUser(su);
                                ForgotPassword.sendNotificationEmail(context, su, password);
                            }
                            catch(Exception e)
                            {
                                Localizable msg = new L10NMessage(L10NKeySet.ExceptionEmailFailure,e);
                                scarabR.setAlertMessage(msg);
                            }
                            data.setUser(me);
                        }
                        else
                        /* !password.equals(passwordConfirm) */
                        {
                            scarabR.setAlertMessage(L10NKeySet.PasswordsDoNotMatch);
                            return;
                        }
                    }
                    
                    
                    
                    Localizable msg = new L10NMessage(L10NKeySet.UserChangesSaved, username);
                    scarabR.setConfirmMessage(msg);
                    data.getParameters().setString("state", "showedituser");
                    data.getParameters().setString("lastAction", "editeduser");
                    
                    TemplateScreen.setTemplate(data, nextTemplate);
                    return;
                }
                else
                {
                    Localizable msg = new L10NMessage(L10NKeySet.UserNotRetrieved, username);
                    scarabR.setAlertMessage(msg);
                    data.getParameters().setString("state", "showedituser");                    
                }
            }
            catch (Exception e)
            {
                TemplateScreen.setTemplate(data, template);
                data.getParameters().setString("lastAction","");
                Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                scarabR.setAlertMessage (msg);
                Log.get().error(e);
                data.getParameters().setString("state","showedituser");
                return;
            }   
        }
        else
        {
            data.getParameters().setString("state","showedituser");
            data.getParameters().setString("lastAction","");
        }
    }

    public void doDeleteuser(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        User user = null;
        String username = data.getParameters().getString("username");
        User userInSession = data.getUser(); 
        if (userInSession.getName().equals(username)){
            scarabR.setAlertMessage(L10NKeySet.UserCanNotDeleteSelf);
            return;
        }
        try
        {
            
            user =  TurbineSecurity.getUser(username);
            user.setConfirmed(ScarabUser.DELETED);
            TurbineSecurity.saveUser(user);
            List lista = (List)data.getUser().getTemp("userList");
            if (lista != null)
                lista.set(lista.indexOf(user), user);
            
            Localizable msg = new L10NMessage(L10NKeySet.UserDeleted, username);
            scarabR.setConfirmMessage(msg);
            data.getParameters().setString("state", "showedituser");
            data.getParameters().setString("lastAction", "editeduser");
            
            TemplateScreen.setTemplate(data, nextTemplate);
            return;
            
           
        }
        catch (Exception e)
        {
            TemplateScreen.setTemplate(data, template);
            data.getParameters().setString("lastAction","");
            Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
            scarabR.setAlertMessage (msg);
            Log.get().error(e);
            data.getParameters().setString("state","showedituser");
            return;
        }        
       
    }
    
    
    /**
     * This manages clicking the 'Update Roles' button
     */
    public void doRoles(RunData data, Context context)
        throws Exception
    {
        String username = data.getParameters().getString("username");
        User user = TurbineSecurity.getUser(username);
        
        AccessControlList acl = TurbineSecurity.getACL(user);
        
        // Grab all the Groups and Roles in the system.
        org.apache.turbine.om.security.Group[] groups = TurbineSecurity.getAllGroups().getGroupsArray();
        Role[] roles = TurbineSecurity.getAllRoles().getRolesArray();
        
        for (int i = 0; i < groups.length; i++)
        {
            String groupName = groups[i].getName();
            
            for (int j = 0; j < roles.length; j++)
            {
                String roleName = roles[j].getName();
                String groupRole = groupName + roleName;
                
                String formGroupRole = data.getParameters().getString(groupRole);
                
                if (formGroupRole != null && !acl.hasRole(roles[j], groups[i]))
                {
                    TurbineSecurity.grant(user, groups[i], roles[j]);
                }
                else if (formGroupRole == null && acl.hasRole(roles[j], groups[i]))
                {
                    TurbineSecurity.revoke(user, groups[i], roles[j]);
                }
            }
        }
    }
    
    // all the goto's (button redirects) are here
    
    /**
     * 
     */
    public void doGotoedituser(RunData data, Context context)
        throws Exception
    {
        String userName = data.getParameters().getString("username");
        if ((userName != null) && (userName.length() > 0))
        {
            data.getParameters().setString("state","showedituser");
            TemplateScreen.setTemplate(data, "admin,EditUser.vm");
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage(L10NKeySet.UserSelect);
        }
    }
    
    /**
     * 
     */
    public void doGotoeditroles(RunData data, Context context)
        throws Exception
    {
        String userName = data.getParameters().getString("username");
        if ((userName != null) && (userName.length() > 0))
        {
            TemplateScreen.setTemplate(data, "admin,EditUserRoles.vm");
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage(L10NKeySet.UserSelect);
        }
    }
    
    /**
     * 
     */
    public void doGotodeleteuser(RunData data, Context context)
        throws Exception
    {
        TemplateScreen.setTemplate(data, "admin,DeleteUser.vm");
    }
    
    /**
     * 
     */
    public void doGotoadduser(RunData data, Context context)
        throws Exception
    {
        TemplateScreen.setTemplate(data, "admin,AddUser.vm");
    }

    /**
     * This manages clicking the 'Search' button. Sets some data in context and delegates
     * to the page (that will make the real search).
     */
    public void doSearch(RunData data, Context context)
        throws Exception
    {
        String searchField = data.getParameters().getString("searchField");
        String searchCriteria = data.getParameters().getString("searchCriteria");
        String orderByField = data.getParameters().getString("orderByField");
        String ascOrDesc = data.getParameters().getString("ascOrDesc");
        String resultsPerPage= data.getParameters().getString("resultsPerPage");
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        scarabR.setGlobalUserSearchParam("searchField", searchField);
        scarabR.setGlobalUserSearchParam("searchCriteria", searchCriteria);
        scarabR.setGlobalUserSearchParam("orderByField", orderByField);
        scarabR.setGlobalUserSearchParam("ascOrDesc", ascOrDesc);
        scarabR.setGlobalUserSearchParam("resultsPerPage", resultsPerPage);
        
        TemplateScreen.setTemplate(data, "admin,ManageUserSearch.vm");
    }
    
    /**
     * calls doSearch()
     */
    public void doPerform(RunData data, Context context)
        throws Exception
    {
        doSearch(data, context);
    }
}
