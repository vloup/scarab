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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.turbine.om.security.Role;
import org.apache.turbine.services.security.torque.om.TurbineRolePeer;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * Every transition declare the ability of a role to change the value of an
 * attribute from one of its options to another.
 * 
 * @see org.tigris.scarab.workflow.CheapWorkflow
 *
 * @author Diego Martinez Velasco
 * @author Jorge Uriarte Aretxaga  
 */
public class Transition extends BaseTransition
        implements
            Persistent, Conditioned
{
    public Role getRole()
    {
        Role role = null;
        try
        {
            role = TurbineRolePeer.retrieveByPK(this.getRoleId());
        }
        catch (NoRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TooManyRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TorqueException e)
        {
            e.printStackTrace();
        }
        return role;
    }
    
    public AttributeOption getFrom()
    {
        AttributeOption from = null;
        if (null != this.getFromOptionId())
        {
            try
            {
                from = AttributeOptionPeer.retrieveByPK(this.getFromOptionId());
            }
            catch (NoRowsException e)
            {
                //Nothing to do, just ignore it
            }
            catch (TooManyRowsException e)
            {
                //Nothing to do, just ignore it
            }
            catch (TorqueException e)
            {
                e.printStackTrace();
            }
        }
        return from;
    }

    public AttributeOption getTo()
    {
        AttributeOption to = null;
        try
        {
            to = AttributeOptionPeer.retrieveByPK(this.getToOptionId());
        }
        catch (NoRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TooManyRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TorqueException e)
        {
            e.printStackTrace();
        }
        return to;
    }

    public boolean isRequiredIf(Integer optionID) throws TorqueException
    {
        Condition cond = new Condition();
        cond.setAttributeId(null);
        cond.setOptionId(optionID);
        cond.setModuleId(null);
        cond.setIssueTypeId(null);
        cond.setTransitionId(this.getTransitionId());
        return this.getConditions().contains(cond);
    }
    
    public String getFromName()
    {
        String fromName = null;
        AttributeOption from = this.getFrom();
        if (from == null)
        {
            ScarabLocalizationTool l10n = new ScarabLocalizationTool();
            fromName = l10n.get(L10NKeySet.TransitionsAnyOption);
        }
        else
        {
            fromName = from.getName();
        }
        return fromName;
    }

    public String getToName()
    {
        String toName = null;
        AttributeOption to = this.getTo();
        if (to == null)
        {
            ScarabLocalizationTool l10n = new ScarabLocalizationTool();
            toName = l10n.get(L10NKeySet.TransitionsAnyOption);
        }
        else
        {
            toName = to.getName();
        }
        return toName;
    }
    
    public String getRoleName()
    {
        String roleName = null;
        Role role = this.getRole();
        if (role == null)
        {
            ScarabLocalizationTool l10n = new ScarabLocalizationTool();
            roleName = l10n.get(L10NKeySet.TransitionsAnyRole);
        } 
        else
        {
            roleName = role.getName();
        }
        return roleName;
    }
    
    /**
     * Returns the conditions associated to this Transition
     */
    public List<Condition> getConditions() throws TorqueException
    {
    	List<Condition> conds = (List<Condition>)TransitionManager.getMethodResult().get(this, "getConditions");
    	if (conds == null)
    	{
        	conds = super.getConditions();
        	TransitionManager.getMethodResult().put(conds, this, "getConditions");
    	}
    	return conds;
    }

    /**
     * Returns the array of attributeOptionIds that will force the requiment of this
     * attribute if set. Used by templates to load the combo.
     * @return
     */
    public Integer[] getConditionsArray()
    {
        List<Condition> conditions = new ArrayList<Condition>();
        Integer[] aIDs = null;
        try
        {
            
            conditions = this.getConditions();
            aIDs = new Integer[conditions.size()];
            int i=0;
            for (Iterator<Condition> iter = conditions.iterator(); iter.hasNext(); i++)
            {
                Condition condition = iter.next();
                long id = condition.getConditionId();
                aIDs[i] = (int)id;
            }
        }
        catch (TorqueException e)
        {
            this.getLog().error("getConditionsArray: " + e);
        }
        return aIDs;
    }

    /**
     * Return the boolean operator to be used to combine different attributes.
     * @return
     */
    public Integer getConditionOperator()
    {
        Integer operator = 0;
        List<Condition> conditions;
        try {
            conditions = getConditions();
        } catch (TorqueException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        if(conditions.size() > 0)
        {
            operator = conditions.get(0).getOperator();
        }
        return operator;
    }
    
    
    /**
     * Load the attribute options' IDs from the template combo.
     * @param aOptionId
     * @throws TorqueException
     */
    public void setConditionsArray(Integer aOptionId[], Integer operator) throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(ConditionPeer.ATTRIBUTE_ID, null);
        crit.add(ConditionPeer.MODULE_ID, null);
        crit.add(ConditionPeer.ISSUE_TYPE_ID, null);
        crit.add(ConditionPeer.TRANSITION_ID, this.getTransitionId());
        ConditionPeer.doDelete(crit);
        this.getConditions().clear();
        ConditionManager.clear();
        if (aOptionId != null)
        {
	        for (int i=0; i<aOptionId.length; i++)
	        {
	            if (aOptionId[i].intValue() != 0)
	            {
		            Condition cond = new Condition();
		            cond.setTransitionId(this.getTransitionId());
		            cond.setOptionId(aOptionId[i]);
		            cond.setAttributeId(null);
		            cond.setModuleId(null);
		            cond.setIssueTypeId(null);
		            cond.setOperator(operator);
		            this.addCondition(cond);
		            cond.save();
	            }
	        }
        }
    }    

    public boolean isConditioned()
    {
        boolean bRdo = false;
        try {
        	bRdo = this.getConditions().size()>0;
        } catch (TorqueException te)
        {
            // Nothing to do
        }
        return bRdo;
    }    
    
    public String toString()
    {
        return this.getFromOptionId() + " -> " + this.getToOptionId()
                + " (role: " + this.getRoleId() + ")";
    }

}

