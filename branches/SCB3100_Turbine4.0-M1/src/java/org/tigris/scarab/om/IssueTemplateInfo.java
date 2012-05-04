package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
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
import org.apache.torque.TorqueException;

import org.apache.velocity.context.Context;
import org.apache.turbine.Turbine;

import org.apache.torque.om.Persistent;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.ScarabException;

/** 
 * This class represents the IssueTemplateInfo object.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public  class IssueTemplateInfo 
    extends BaseIssueTemplateInfo
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
        throws TorqueException
    {
        // can delete a template if they have delete permission
        // Or if is their personal template
        boolean hasPermission    = user.hasPermission(ScarabSecurity.ITEM__DELETE, getIssue().getModule());
        boolean isCreatedBySelf  = user.getUserId().equals(getIssue().getCreatedBy().getUserId());
        boolean hasScopePersonal = getScopeId().equals(Scope.PERSONAL__PK);
        return (hasPermission || (isCreatedBySelf && hasScopePersonal));
    }

    public boolean canEdit(ScarabUser user)
        throws TorqueException
    {
        return canDelete(user);
    }

    public void saveAndSendEmail(final ScarabUser user, 
            final Module module, 
            final Context context)
        throws TorqueException, ScarabException
    {
        // If it's a module scoped template, user must have Item | Approve 
        //   permission, Or its Approved field gets set to false
        boolean success = true;
        final Issue issue = IssueManager.getInstance(getIssueId());

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
                final String template = Turbine.getConfiguration().
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

                final EmailContext ectx = new EmailContext();
                ectx.setUser(user);
                ectx.setModule(module);
                ectx.setDefaultTextKey("NewTemplateRequiresApproval");

                final String fromUser = "scarab.email.default";
                try
                {
                    Email.sendEmail(ectx, 
                        module, 
                        fromUser,
                        module.getSystemEmail(),
                        Arrays.asList(toUsers),
                        null, template);
                }
                catch(Exception e)
                {
                    save();  // Not shure about this, but i think it's ok,
                             // because we already did an issue.save(), see above
                    throw new ScarabException(L10NKeySet.ExceptionGeneral,e); 
                }
            }
        }
        save();
    }

    /*
     * Checks permission and approves or rejects template. If template
     * is approved,template type set to "module", else set to "personal".
     */
    public void approve(final ScarabUser user, final boolean approved)
         throws TorqueException, ScarabException
    {                
        final Module module = getIssue().getModule();

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
            throw new ScarabException(L10NKeySet.YouDoNotHavePermissionToAction);
        }            
    }

}
