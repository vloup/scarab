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
import java.util.Iterator;

// Turbine Stuff
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.turbine.tool.IntakeTool;
//import org.apache.fulcrum.intake.model.Group;
//import org.apache.fulcrum.security.util.PasswordMismatchException;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.PendingGroupUserRole;
import org.tigris.scarab.tools.SecurityAdminTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for moderated self-serve role assignments
 * within a particular module.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 */
public class HandleRoleRequests extends RequireLoginFirstAction
{
    public void doRequestroles(RunData data, TemplateContext context)
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabUser user = (ScarabUser)data.getUser();
        SecurityAdminTool scarabA = getSecurityAdminTool(context);

        // List roles = scarabA.getNonRootRoles();
        List groups = scarabA.getNonMemberGroups(user);

        Iterator gi = groups.iterator();
        while (gi.hasNext()) 
        {
            Module module = ((Module)gi.next());
            String[] autoRoles = 
                ((ScarabModule)module).getAutoApprovedRoles();
            String role = data.getParameters().getString(module.getName());
            if (role != null && role.length() > 0) 
            {
                boolean autoApprove = false;
                if (autoRoles != null) 
                {
                    for (int i=0; i<autoRoles.length; i++) 
                    {
                        if (role.equals(autoRoles[i])) 
                        {
                            autoApprove = true;
                            break;
                        }
                    }
                }
                
                if (autoApprove) 
                {
                    TurbineSecurity.grant( user, 
                        (org.apache.fulcrum.security.entity.Group)module, 
                        TurbineSecurity.getRole(role) );
                    getScarabRequestTool(context).setConfirmMessage(
                        "Your role request was granted.");    
                }
                else 
                {
                    PendingGroupUserRole pend = new PendingGroupUserRole();
                    pend.setGroupId(module.getModuleId());
                    pend.setUserId(user.getUserId());
                    pend.setRoleName(role);
                    pend.save();
                    getScarabRequestTool(context).setInfoMessage(
                        "Your role request is awaiting approval.");    
                }                
            }
        }
        setTarget(data, nextTemplate);
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    private SecurityAdminTool getSecurityAdminTool(TemplateContext context)
    {
        return (SecurityAdminTool)context
            .get(ScarabConstants.SECURITY_ADMIN_TOOL);
    }
}

