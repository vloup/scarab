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

import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.WorkflowLifecycle;
import org.tigris.scarab.om.WorkflowLifecycleManager;
import org.tigris.scarab.om.WorkflowLifecyclePeer;
import org.tigris.scarab.om.WorkflowTransition;
import org.tigris.scarab.om.WorkflowTransitionManager;
import org.tigris.scarab.om.WorkflowTransitionPeer;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * This class deals with modifying Workflow Lifecycles
 * and inserting transitions
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class WorkflowLifecycles extends RequireLoginFirstAction
{

    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages.";


    public void doSavelifecycle(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        WorkflowLifecycle wl = null;
        Group wlGroup = null;

        String lifecycleid = intake.get("WorkflowLifecycle", IntakeTool.DEFAULT_KEY).get("LifecycleId").toString();
        if ( lifecycleid == null || lifecycleid.length() == 0 )
        {
            lifecycleid = data.getParameters().getString("lifecycleid");
        }

        if ( lifecycleid == null || lifecycleid.length() == 0 )
        {
            wl= WorkflowLifecycleManager.getInstance();
            wlGroup = intake.get("WorkflowLifecycle", IntakeTool.DEFAULT_KEY);

            // Check for duplicate WorkflowLifecycle names.
            if (!WorkflowLifecyclePeer.isUnique(wlGroup.get("DisplayValue").toString(),new NumberKey(wlGroup.get("ModuleId").toString())))
            {
                scarabR.setAlertMessage("Cannot create a duplicate Workflow Lifecycle with the same name!");
                return;
            }
        }
        else
        {
            wl = WorkflowLifecycleManager.getInstance(new NumberKey(lifecycleid));
            wlGroup = intake.get("WorkflowLifecycle", wl.getQueryKey());
        }


        if ( intake.isAllValid() )
        {
            wlGroup.setProperties(wl);
            wl.save();
            scarabR.setWorkflowLifecycle(wl);
            data.setMessage(DEFAULT_MSG);
            intake.remove(wlGroup);
        }
        else
        {
          scarabR.setAlertMessage(ERROR_MESSAGE);
        }

    }

    public void doDeletelifecycles( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String lifecycleId;

        for (int i =0; i < keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_lc_"))
            {
                lifecycleId = params.getString(key);
                WorkflowLifecyclePeer.delete(new NumberKey(lifecycleId));
                setTarget(data, getLastTemplate(data));
            }
        }

        data.setMessage(DEFAULT_MSG);
    }


    public void doDeletetransitions( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String transitionId;

        String lifecycleId = data.getParameters().getString("lifecycleid");

        WorkflowLifecycle wl = WorkflowLifecycleManager.getInstance(new NumberKey(lifecycleId));
        scarabR.setWorkflowLifecycle(wl);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_trans_"))
            {
                transitionId = params.getString(key);
                WorkflowTransitionPeer.delete(new NumberKey(transitionId));
            }
        }

        data.setMessage(DEFAULT_MSG);
    }

    public void doAddtransition(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        WorkflowTransition wt = null;
        Group wtGroup = null;

        wt= WorkflowTransitionManager.getInstance();
        wtGroup = intake.get("WorkflowTransition", wt.getQueryKey());

        WorkflowLifecycle wl = WorkflowLifecycleManager.getInstance(new NumberKey(wtGroup.get("LifecycleId").toString()));
        scarabR.setWorkflowLifecycle(wl);

        if ( intake.isAllValid() )
        {
            //check for duplicates (same lifecycle, from, and to)
            if(!WorkflowTransitionPeer.isUnique(wtGroup.get("LifecycleId").toString(),
                                                wtGroup.get("FromOptionId").toString(),
                                                wtGroup.get("ToOptionId").toString()))
            {
                scarabR.setAlertMessage("Cannot create a duplicate transition with the same states.");
                return;
            }

            //check for from and to matching
            if( wtGroup.get("FromOptionId").toString().equals(wtGroup.get("ToOptionId").toString()) )
            {
                scarabR.setAlertMessage("Cannot create a transition from a state to itself.");
                return;
            }

            wtGroup.setProperties(wt);
            wt.save();
            scarabR.setWorkflowLifecycle(wt.getWorkflowLifecycle());
            data.setMessage(DEFAULT_MSG);
            intake.remove(wtGroup);
        }
        else
        {
          scarabR.setAlertMessage(ERROR_MESSAGE);
        }

    }


    /*
     * Manages clicking of the AllDone button
     */
    public void doDone( RunData data, TemplateContext context )
        throws Exception
    {
        doSavelifecycle(data, context);
        doCancel(data, context);
    }
}
