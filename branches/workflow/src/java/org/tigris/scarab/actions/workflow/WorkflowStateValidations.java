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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.WorkflowStateValidation;
import org.tigris.scarab.om.WorkflowStateValidationManager;
import org.tigris.scarab.om.WorkflowValidationClass;
import org.tigris.scarab.om.WorkflowValidationParameter;
import org.tigris.scarab.om.WorkflowValidationParameterManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.workflow.validations.WorkflowValidation;

/**
 * This class deals with modifying Workflow State Validations
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class WorkflowStateValidations extends RequireLoginFirstAction
{

    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages.";


    public void doSavevalidation(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        WorkflowStateValidation wsv = null;
        Group wsvGroup = null;

        String stateValidationId = intake.get("WorkflowStateValidation", IntakeTool.DEFAULT_KEY).get("StateValidationId").toString();
        if ( stateValidationId == null || stateValidationId.length() == 0 )
        {
            stateValidationId = data.getParameters().getString("stateValidationId");
        }

        if ( stateValidationId == null || stateValidationId.length() == 0 )
        {
            wsv = WorkflowStateValidationManager.getInstance();
            wsvGroup = intake.get("WorkflowStateValidation", IntakeTool.DEFAULT_KEY);
        }
        else
        {
            wsv = WorkflowStateValidationManager.getInstance(new NumberKey(stateValidationId));
            wsvGroup = intake.get("WorkflowStateValidation", wsv.getQueryKey());
        }

        if (intake.isAllValid())
        {
            wsvGroup.setProperties(wsv);
            wsv.save();
            System.out.println("wsv.wsvid = " + wsv.getStateValidationId());
            scarabR.setWorkflowStateValidation(wsv);
            data.setMessage(DEFAULT_MSG);
            intake.remove(wsvGroup);

            //get the validation class, then from that load up the WorkflowValidation
            WorkflowValidation wv  = wsv.getWorkflowValidationClass().getWorkflowValidation();
            
            List wParameters = wv.getParameterList();
            Map parameterHash = new HashMap(10);
            
            ParameterParser params = data.getParameters();
    
            Iterator paramIterator = wParameters.iterator();
            while(paramIterator.hasNext())
            {
                WorkflowValidation.ParameterDescription paramDescription = (WorkflowValidation.ParameterDescription) paramIterator.next();
                String value = params.getString("vp_" + paramDescription.getName());
                parameterHash.put(paramDescription.getName(), value);
            }
            
            String checkParams = wv.checkArguments(parameterHash);
            
            if(checkParams == null || checkParams.length() <= 0)
            {
              scarabR.setAlertMessage(ERROR_MESSAGE);
            }
            else
            {
                Set keys = parameterHash.keySet();
                Iterator keyIterator = keys.iterator();
                while(keyIterator.hasNext())
                {
                    String key = (String) keyIterator.next();
                    WorkflowValidationParameter wvp = WorkflowValidationParameterManager.getInstance();
                    wvp.setName(key);
                    wvp.setValue((String)parameterHash.get(key));
                    wvp.setStateValidationId(wsv.toString());
                    wvp.save();
                }
            }
        }
        else
        {
          scarabR.setAlertMessage(ERROR_MESSAGE);
        }

    }
}
