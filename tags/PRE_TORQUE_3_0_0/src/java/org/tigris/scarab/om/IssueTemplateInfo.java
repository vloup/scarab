package org.tigris.scarab.om;

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

import java.util.Arrays;

import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;

import org.apache.torque.om.Persistent;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

/** 
 * This class represents the IssueTemplateInfo object.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public  class IssueTemplateInfo 
    extends org.tigris.scarab.om.BaseIssueTemplateInfo
    implements Persistent
{

    /**
     * A new IssueTemplateInfo object
     */
    public static IssueTemplateInfo getInstance() 
    {
        return new IssueTemplateInfo();
    }


    public boolean canDelete(ScarabUser user)
        throws Exception
    {
        // can delete a template if they have delete permission
        // Or if is their personal template
        return (user.hasPermission(ScarabSecurity.ITEM__DELETE, getIssue().getModule())
            || (user.getUserId().equals(getIssue().getCreatedBy().getUserId()) 
                && getScopeId().equals(Scope.PERSONAL__PK)));
    }

    public boolean canEdit(ScarabUser user)
        throws Exception
    {
        return canDelete(user);
    }

    public boolean saveAndSendEmail(ScarabUser user, Module module, 
                                    TemplateContext context)
        throws Exception
    {
        // If it's a module scoped template, user must have Item | Approve 
        //   permission, Or its Approved field gets set to false
        boolean success = true;
        Issue issue = IssuePeer.retrieveByPK(getIssueId());

        // If it's a module template, user must have Item | Approve 
        //   permission, or its Approved field gets set to false
        if (getScopeId().equals(Scope.PERSONAL__PK)
            || user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
        } 
        else
        {
            setApproved(false);
            issue.save();

            // Send Email to the people with module edit ability so
            // that they can approve the new template
            if (context != null)
            {
                String template = Turbine.getConfiguration().
                    getString("scarab.email.requireapproval.template",
                              "RequireApproval.vm");
                
                ScarabUser[] toUsers = module.getUsers(ScarabSecurity.MODULE__EDIT);
                // if the module doesn't have any users, then we need to add at 
                // least ourselves...
                if (toUsers.length == 0)
                {
                    toUsers = new ScarabUser[1];
                    toUsers[0] = user;
                }

                EmailContext ectx = new EmailContext();
                ectx.setLocalizationTool(
                    (ScarabLocalizationTool)context.get("l10n"));
                ectx.setLinkTool((ScarabLink)context.get("link"));
                ectx.setUser(user);
                ectx.setModule(module);
                ectx.setDefaultTextKey("NewTemplateRequiresApproval");

                String fromUser = "scarab.email.default";
                if (!Email.sendEmail(ectx, module, 
                    fromUser, module.getSystemEmail(), Arrays.asList(toUsers),
                    null, template))
                {
                    success = false;
                }
            }
        }
        save();
        return success;
    }

    /*
     * Checks permission and approves or rejects template. If template
     * is approved,template type set to "module", else set to "personal".
     */
    public void approve(ScarabUser user, boolean approved)
         throws Exception
    {                
        Module module = getIssue().getModule();

        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
            if (!approved)
            {
                setScopeId(Scope.PERSONAL__PK);
            }
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

}
