package org.tigris.scarab.actions.workflow;

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
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.actions.base.RequireLoginFirstAction;

import org.tigris.scarab.om.WorkflowLifecycle;
import org.tigris.scarab.om.WorkflowLifecycleManager;
import org.tigris.scarab.om.WorkflowLifecyclePeer;
import org.tigris.scarab.om.WorkflowTransition;
import org.tigris.scarab.om.WorkflowTransitionPeer;
import org.tigris.scarab.om.WorkflowTransitionManager;
import org.tigris.scarab.om.WorkflowTransitionRole;
import org.tigris.scarab.om.WorkflowTransitionRolePeer;
import org.tigris.scarab.om.WorkflowTransitionRoleManager;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class deals with modifying Workflow Transitions and associated roles
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class WorkflowTransitions extends RequireLoginFirstAction
{

    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages.";


    public void doSavetransition(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        WorkflowTransition wt = null;
        Group wtGroup = null;

        String transitionid = intake.get("WorkflowTransition", IntakeTool.DEFAULT_KEY).get("TransitionId").toString();
        if ( transitionid == null || transitionid.length() == 0 )
        {
            transitionid = data.getParameters().getString("transitionid");
        }

        wt = WorkflowTransitionManager.getInstance(new NumberKey(transitionid));
        wtGroup = intake.get("WorkflowTransition", wt.getQueryKey());

        if ( intake.isAllValid() )
        {
            wtGroup.setProperties(wt);
            wt.save();
            scarabR.setWorkflowTransition(wt);
            data.setMessage(DEFAULT_MSG);
            intake.remove(wtGroup);
        }
        else
        {
          scarabR.setAlertMessage(ERROR_MESSAGE);
        }

    }

    public void doSaveroles(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key, transitionid;

        transitionid = data.getParameters().getString("transitionid");

        //delete the current roles for this transition
        Criteria c = new Criteria()
            .add(WorkflowTransitionRolePeer.TRANSITION_ID, transitionid);
        WorkflowTransitionRolePeer.doDelete(c);

        //insert back the checked off roles
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("role_"))
            {
                String newRole = params.getString(key);
                WorkflowTransitionRole wtr = WorkflowTransitionRoleManager.getInstance();
                wtr.setRoleName(newRole);
                wtr.setTransitionId(transitionid);
                wtr.save();
            }
        }
    }
}
