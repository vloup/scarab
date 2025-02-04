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

import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.turbine.modules.screens.TemplateScreen;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.velocity.context.Context;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.ConditionManager;
import org.tigris.scarab.om.ConditionPeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributeManager;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.om.RModuleIssueTypePeer;
import org.tigris.scarab.om.Transition;
import org.tigris.scarab.om.TransitionManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;

public class ConditionEdit extends RequireLoginFirstAction
{
    /**
     * This action only handles events, so this method does nothing.
     */
    public void doPerform(RunData data, Context context) throws Exception
    {
    }

    public void doDelete(RunData data, Context context) throws TorqueException, Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group attrGroup = intake.get("ConditionEdit", IntakeTool.DEFAULT_KEY);
        data.getParameters().remove(attrGroup.get("ConditionsArray").getKey());
        updateObject(data, context, null);
    }

    private void delete(RunData data) throws TorqueException, Exception
    {
        int nObjectType = data.getParameters().getInt("obj_type");
        Criteria crit = new Criteria();
        switch (nObjectType)
        {
            case ScarabConstants.TRANSITION_OBJECT:
            	crit.add(ConditionPeer.TRANSITION_ID, data.getParameters().getInt("transition_id"));
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
        }
        ConditionPeer.doDelete(crit);
    	ConditionManager.clear();
    	TransitionManager.clear();
    }
    
    private void updateObject(RunData data, Context context, Integer aConditions[]) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ParameterParser parser = data.getParameters();
        switch (data.getParameters().getInt("obj_type"))
        {
            case ScarabConstants.TRANSITION_OBJECT:
                Transition transition = scarabR.getTransition(getIntParameter(parser, "transition_id"));
            	transition.setConditionsArray(aConditions);
            	transition.save();
                break;
            case ScarabConstants.GLOBAL_ATTRIBUTE_OBJECT:
                Attribute attribute = scarabR.getAttribute(getIntParameter(parser, "attId"));
            	attribute.setConditionsArray(aConditions);
            	attribute.save();
                break;
            case ScarabConstants.MODULE_ATTRIBUTE_OBJECT:
            	RModuleAttribute rma = RModuleAttributePeer.retrieveByPK(getIntParameter(parser, "moduleId"), getIntParameter(parser, "attId"), getIntParameter(parser, "issueTypeId"));
                rma.setConditionsArray(aConditions);
                RModuleAttributeManager.clear();
                ConditionManager.clear();
                rma.save(); /** TODO: Esto sobra! **/
        		break;
        	case ScarabConstants.BLOCKED_MODULE_ISSUE_TYPE_OBJECT:
        	    RModuleIssueType rmit = RModuleIssueTypePeer.retrieveByPK(scarabR.getCurrentModule().getModuleId(), getIntParameter(parser, "issuetypeid"));
        		rmit.setConditionsArray(aConditions);
        	    rmit.save();
        	    RModuleIssueTypeManager.clear();
        	    ConditionManager.clear();
        	    break;
        }
    	AttributeManager.clear();        
    }
    
    public void doSave(RunData data, Context context) throws Exception
    {
        this.delete(data);
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
            TemplateScreen.setTemplate(data, lastTemplate);
        }
        else
        {
            super.doCancel(data, context);
        }
    }
    
    /**
     * Ideally we would use Integer.valueOf(0), but this only became
     * available with version 1.5 of the JDK.
     */
    private static final Integer ZERO = new Integer(0);
    
    /**
     * Returns the value of an integer parameter from the given
     * parameter parser. If no parameter with the given name can
     * be found, this method returns Integer(0). Expect an
     * exception if the parameter exists but is not of integer
     * type.
     * @param parser The parser to extract the parameter from.
     * @param name The name of the parameter to extract.
     * @return The parameter's value as an integer.
     */
    private Integer getIntParameter(ParameterParser parser, String name)
    {
        Integer param = parser.getIntObject(name);
        if (param == null)
            param = ZERO;
        return param;
    }
}

