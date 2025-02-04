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
 * software developed by CollabNet <http://www.Collab.Net/>."
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

import java.util.Iterator;
import java.util.List;

import org.apache.turbine.util.parser.ParameterParser;
import org.apache.turbine.modules.screens.TemplateScreen;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.util.security.AccessControlList;
import org.apache.turbine.util.security.DataBackendException;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.RunData;
import org.apache.turbine.Turbine;
import org.apache.velocity.context.Context;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.IssueTemplateInfo;
import org.tigris.scarab.om.IssueTemplateInfoPeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.PendingGroupUserRole;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.SecurityAdminTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

/**
 * This class is responsible for managing the approval process.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class Approval extends RequireLoginFirstAction
{
    private static final String REJECT = "reject";
    private static final String APPROVE = "approve";
    private static final String COMMENT = "comment";

    private static final Integer QUERY = new Integer(0);
    private static final Integer ISSUE_ENTRY_TEMPLATE = new Integer(1);
    private static final Integer COMMENTED = new Integer(2);

    private static final Integer REJECTED = QUERY;
    private static final Integer APPROVED = ISSUE_ENTRY_TEMPLATE;

    /**
     * This action only handles events, so this method does nothing.
     */
    public void doPerform(RunData data, Context context) throws Exception
    {
    }

    public void doSubmit(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Module module = scarabR.getCurrentModule();
        String globalComment = data.getParameters().getString("global_comment");
       
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String action = null;
        Integer actionWord = null;
        Integer artifact = null;
        String artifactName = null;
        String comment = null;
        ScarabUser toUser = null;
        String userId;
        boolean success = true;

        for (int i =0; i<keys.length; i++)
        {
            action="none";
            key = keys[i].toString();
            if (key.startsWith("query_id_"))
            {
               String queryId = key.substring(9);
               Query query = QueryPeer.retrieveByPK(new NumberKey(queryId));

               action = params.getString("query_action_" + queryId);
               comment = params.getString("query_comment_" + queryId);

               userId = params.getString("query_user_" + queryId);
               toUser = scarabR.getUser(userId);
               artifact = QUERY;
               artifactName = query.getName();

               if (query.getApproved())
               {
                   success = false;
                   boolean isApproved = Scope.MODULE__PK.equals(query.getScopeId());
                   LocalizationKey l10nKey = (isApproved) ?
                       L10NKeySet.ItemAlreadyApproved:L10NKeySet.ItemAlreadyRejected;
                   L10NMessage l10nMessage = new L10NMessage(l10nKey,artifactName);
                   scarabR.setAlertMessage(l10nMessage);
               }
               else
               {
                   if (action.equals(REJECT))
                   {
                       try
                       {
                           query.approve(user, false);
                       }
                       catch (ScarabException e)
                       {
                           L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                           scarabR.setAlertMessage(msg);
                       }
                       actionWord = REJECTED;
                   } 
                   else if (action.equals(APPROVE))
                   {
                       try
                       {
                           query.approve(user, true);
                       }
                       catch(ScarabException e)
                       {
                           L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                           scarabR.setAlertMessage(msg);
                       }
                       actionWord = APPROVED;
                       }
                       else if (action.equals(COMMENT))
                       {
                           actionWord = COMMENTED; 
                       }
                   }
               }
               else if (key.startsWith("template_id_"))
               {
                   String templateId = key.substring(12);
                   IssueTemplateInfo info = IssueTemplateInfoPeer
                                     .retrieveByPK(new NumberKey(templateId));

               action = params.getString("template_action_" + templateId);
               comment = params.getString("template_comment_" + templateId);

               userId = params.getString("template_user_" + templateId);
               toUser = scarabR.getUser(userId);
               artifact = ISSUE_ENTRY_TEMPLATE;
               artifactName = info.getName();

               if (info.getApproved())
               {
                   success = false;
                   boolean isApproved = Scope.MODULE__PK.equals(info.getScopeId());
                   LocalizationKey l10nKey = (isApproved) ?
                       L10NKeySet.ItemAlreadyApproved:L10NKeySet.ItemAlreadyRejected;
                   L10NMessage l10nMessage = new L10NMessage(l10nKey,artifactName);
                   scarabR.setAlertMessage(l10nMessage);
               }
               else
               {
                   if (action.equals(REJECT))
                   {
                       try
                       {
                           info.approve(user, false);
                       }
                       catch(ScarabException e)
                       {
                           L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                           scarabR.setAlertMessage(msg);
                       }
                       actionWord = REJECTED;
                   } 
                   else if (action.equals(APPROVE))
                   {
                       try
                       {
                           info.approve(user, true);
                       }
                       catch(ScarabException e)
                       {
                           L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                           scarabR.setAlertMessage(msg);
                       }
                       actionWord = APPROVED;
                   }
                   else if (action.equals(COMMENT))
                   {
                       actionWord = COMMENTED;
                   }
                }
                if (!action.equals("none") && success)
                {
                    // send email
                    EmailContext ectx = new EmailContext();
                    ectx.setUser(user);
                    // add specific data to context for email template
                    ectx.put("artifactIndex", artifact);
                    ectx.put("artifactName", artifactName);
                    ectx.put("actionIndex", actionWord);
                    ectx.put("comment", comment);
                    ectx.put("globalComment", globalComment);

                    String template = Turbine.getConfiguration().
                        getString("scarab.email.approval.template",
                                  "Approval.vm");
                    try
                    {
                        Email.sendEmail(ectx, module, user, 
                                        module.getSystemEmail(), 
                                        toUser, template);
                    }
                    catch (Exception e)
                    {
                        L10NMessage l10nMessage =new L10NMessage(EMAIL_ERROR,e);
                        scarabR.setAlertMessage(l10nMessage);
                    }
                }
            }
        }
    }

    public void doApproveroles(RunData data, Context context)
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Module module = scarabR.getCurrentModule();
        if (((ScarabUser)data.getUser())
            .hasPermission(ScarabSecurity.USER__APPROVE_ROLES, module)) 
        {
            SecurityAdminTool scarabA = getSecurityAdminTool(context);
            List pendings = scarabA.getPendingGroupUserRoles(module);
            Iterator i = pendings.iterator();
            while (i.hasNext()) 
            {
                PendingGroupUserRole pending = (PendingGroupUserRole)i.next();
                ScarabUser user = 
                    ScarabSecurity.getUserById(pending.getUserId().intValue());

                String checked = data.getParameters()
                .getString("user_id_"+user.getName());

                if(checked != null && checked.equals("on"))
                {
                    String role = data.getParameters()
                        .getString(user.getName());
                    if (role != null && role.length() > 0) 
                    {
                        if (role.equalsIgnoreCase(l10n.get(L10NKeySet.Deny)))
                        {
                            pending.delete();
                        }
                        else 
                        {
                            try
                            {
                                TurbineSecurity.grant(user, 
                                  (org.apache.turbine.om.security.Group)module,
                                  TurbineSecurity.getRole(role));
                            }
                            catch (DataBackendException e)
                            {
                                // maybe the role request was approved 
                                // by another admin?
                                AccessControlList acl = TurbineSecurity
                                        .getACL(user);
                                if (acl
                                        .hasRole(
                                                TurbineSecurity.getRole(role),
                                                (org.apache.turbine.om.security.Group) module))
                                {
                                    String[] args = {role,
                                            user.getName(),
                                            module.getRealName()};
                                    String msg = l10n
                                            .format(
                                                    "RolePreviouslyApprovedForUserInModule",
                                                    args);
                                    String info = (String) scarabR
                                            .getInfoMessage();
                                    if (info == null)
                                    {
                                        info = msg;
                                    }
                                    else
                                    {
                                        info += " " + msg;
                                    }

                                    scarabR.setInfoMessage(info);
                                }
                                else
                                {
                                    throw e; //EXCEPTION
                                }
                            }
                            pending.delete();
                        }
                    }
                }
            }
            scarabR.setConfirmMessage(L10NKeySet.AllRolesProcessed);
        }
        TemplateScreen.setTemplate(data, nextTemplate);
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    private SecurityAdminTool getSecurityAdminTool(Context context)
    {
        return (SecurityAdminTool)context
            .get(ScarabConstants.SECURITY_ADMIN_TOOL);
    }
}
