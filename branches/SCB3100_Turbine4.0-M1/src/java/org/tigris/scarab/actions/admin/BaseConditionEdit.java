package org.tigris.scarab.actions.admin;

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

import org.apache.fulcrum.intake.model.Group;
import org.apache.turbine.om.security.User;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.turbine.services.intake.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.Condition;
import org.tigris.scarab.om.ConditionManager;
import org.tigris.scarab.om.ConditionPeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationRulePeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributeManager;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.om.RModuleIssueTypePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Transition;
import org.tigris.scarab.om.TransitionManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;

public class BaseConditionEdit extends RequireLoginFirstAction
{

    public void doDelete(RunData data, Context context) throws TorqueException, Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group attrGroup = intake.get("ConditionEdit", IntakeTool.DEFAULT_KEY);
        data.getParameters().remove(attrGroup.get("ConditionsArray").getKey());
        updateObject(data, context, null);
    }

    private void delete(RunData data, Context context) throws TorqueException, Exception
    {
        int nObjectType = data.getParameters().getInt("obj_type");
        Criteria crit = new Criteria();
        switch (nObjectType)
        {
            case ScarabConstants.TRANSITION_OBJECT:
            	Integer tranId = data.getParameters().getInteger("transition_id");
            	crit.add(ConditionPeer.TRANSITION_ID, tranId);
            	TransitionManager.getMethodResult().remove(TransitionManager.getInstance(tranId), TransitionManager.GET_CONDITIONS);
                break;
            case ScarabConstants.GLOBAL_ATTRIBUTE_OBJECT:
                crit.add(ConditionPeer.ATTRIBUTE_ID, data.getParameters().getInt("attId"));
            	crit.add(ConditionPeer.MODULE_ID, 0);
            	crit.add(ConditionPeer.ISSUE_TYPE_ID, 0);
                break;
            case ScarabConstants.MODULE_ATTRIBUTE_OBJECT:
        		crit.add(ConditionPeer.ATTRIBUTE_ID, data.getParameters().getInt("attId"));
            	crit.add(ConditionPeer.MODULE_ID, data.getParameters().getInt("module_id"));
            	crit.add(ConditionPeer.ISSUE_TYPE_ID, data.getParameters().getInt("issueTypeId"));
        		break;
            case ScarabConstants.NOTIFICATION_ATTRIBUTE_OBJECT:
                ScarabRequestTool scarabR = getScarabRequestTool(context);
                ScarabUser user = (ScarabUser)data.getUser();
                if(user == null)
                {
                    throw new TorqueException("No user found in RunData during Notification customization (constraints on attributes)");
                }
                Module module = scarabR.getCurrentModule();
                if(module == null)
                {
                    throw new TorqueException("No module found in RunData during Notification customization (constraints on attributes)");
                }
                NotificationRulePeer.deleteConditions(user, module);
                return;
        }
        ConditionPeer.doDelete(crit);
    	ConditionManager.clear();
    	TransitionManager.clear();
    }
    
    private void updateObject(RunData data, Context context, Integer aConditions[]) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Integer operator = data.getParameters().getInteger("combineWith");
        switch (data.getParameters().getInt("obj_type"))
        {
            case ScarabConstants.TRANSITION_OBJECT:
                Transition transition = scarabR.getTransition(data.getParameters().getInteger("transition_id"));
            	transition.setConditionsArray(aConditions, operator);
            	transition.save();
            	TransitionManager.getMethodResult().remove(transition, TransitionManager.GET_CONDITIONS);
                AttributeManager.clear();        
                break;
            case ScarabConstants.GLOBAL_ATTRIBUTE_OBJECT:
                Attribute attribute = scarabR.getAttribute(data.getParameters().getInteger("attId"));
            	attribute.setConditionsArray(aConditions, operator);
            	attribute.save();
                AttributeManager.clear();        
                break;
            case ScarabConstants.MODULE_ATTRIBUTE_OBJECT:
            	RModuleAttribute rma = RModuleAttributePeer.retrieveByPK(data.getParameters().getInteger("moduleId"), data.getParameters().getInteger("attId"), data.getParameters().getInteger("issueTypeId"));
                rma.setConditionsArray(aConditions, operator);
                RModuleAttributeManager.clear();
                ConditionManager.clear();
                rma.save(); /** TODO: do we need it? **/
                AttributeManager.clear();        
        		break;
        	case ScarabConstants.BLOCKED_MODULE_ISSUE_TYPE_OBJECT:
        	    RModuleIssueType rmit = RModuleIssueTypePeer.retrieveByPK(scarabR.getCurrentModule().getModuleId(), data.getParameters().getInteger("issuetypeid"));
        		rmit.setConditionsArray(aConditions, operator);
        	    rmit.save();
        	    RModuleIssueTypeManager.clear();
        	    ConditionManager.clear();
                AttributeManager.clear();        
        	    break;
        	case ScarabConstants.NOTIFICATION_ATTRIBUTE_OBJECT:
                ScarabUser user = (ScarabUser)data.getUser();
                if(user == null)
                {
                    throw new TorqueException("No user found in RunData during Notification customization (constraints on attributes)");
                }
                Module module = scarabR.getCurrentModule();
                if(module == null)
                {
                    throw new TorqueException("No module found in RunData during Notification customization (constraints on attributes)");
                }
                NotificationRulePeer.saveConditions(user, module, aConditions, operator);
        	    break;
        }
    }
    
    public void doSave(RunData data, Context context) throws Exception
    {
        this.delete(data, context);
        IntakeTool intake = getIntakeTool(context);
        Group attrGroup = intake.get("ConditionEdit", IntakeTool.DEFAULT_KEY);
        Integer aConditions[] = ((Integer[])attrGroup.get("ConditionsArray").getValue());
        updateObject(data, context, aConditions);
            
    }
    
    public void doCancel(RunData data, Context context) throws Exception
    {
        String lastTemplate = getCancelTemplate(data);
        if (lastTemplate != null)
        {
            setTarget(data, lastTemplate);
        }
        else
        {
            super.doCancel(data, context);
        }
    }
}

