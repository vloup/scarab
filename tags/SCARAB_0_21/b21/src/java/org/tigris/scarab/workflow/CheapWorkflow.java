package org.tigris.scarab.workflow;

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

import org.tigris.scarab.om.*;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;

import org.apache.torque.TorqueException;
import org.apache.fulcrum.security.entity.Role;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple implementation of Workflow, relies on the Transition tables, where every record defines a
 * transition available for a given Role, from an option to another.
 * <ul>
 * <li>If there are no transitions defined, it will always return true</li>
 * <li>If the "from" option of a transition is null, it will mean "Any option", and the "To" will
 * be available from any option.</li>
 * <li>If the "to" option of a transition is null, any option will be available from the "From" option.</li>
 * <li>If both the 'to' and 'from' options are null, the role will be able to change freely from one value
 * to another.</li>
 * If any transition happens to have associated Conditions, will only be available when this condition
 * evals to true. 
 * </ul>
 */
public class CheapWorkflow extends DefaultWorkflow{
 
    /**
     * Returns true if the transition from the option fromOption to toOption is
     * allowed for the current user.
     *  
     */
    public boolean canMakeTransition(ScarabUser user,
            AttributeOption fromOption, AttributeOption toOption, Issue issue)
    {
        boolean result = false;
        List allTransitions = null;
        Module module = null;
        try
        {
            if (fromOption.equals(toOption))
            {
                result = true;
            }
            else
            {
                if (TransitionPeer.hasDefinedTransitions(toOption
                        .getAttribute()))
                {
                    allTransitions = TransitionPeer.getTransitions(fromOption,
                            toOption);
                    allTransitions = filterConditionalTransitions(allTransitions, issue);
                    module = issue.getModule();
                    Iterator iter = allTransitions.iterator();
                    while (!result && iter.hasNext())
                    {
                        Object obj = iter.next();
                        Transition tran = (Transition) obj;
                        Role requiredRole = tran.getRole();
                        if (requiredRole != null)
                        { 	// A role is required for this transition to be
                        	// allowed
                        	result = user.hasRoleInModule(requiredRole, module);
                        }
                        else
                        {
                            result = true;
                        }
                    }
                }
                else
                {
                    result = true;
                }
            }
        }
        catch (TorqueException te)
        {
            Log.get(this.getClass().getName())
                    .error("canMakeTransition: " + te);
        }
        return result;
    }

    /**
     * Returns true if at least one transition from the fromOption 
     * to any other option is allowed on the given attribute in the scope
     * of the given IssueType and for the current user.
     * @throws TorqueException 
     */
    public boolean canMakeTransitionsFrom(ScarabUser user,
    		IssueType issueType,
            Attribute attribute,
            AttributeOption fromOption) throws ScarabException
    {
    	Module module = user.getCurrentModule();
        boolean result = false;
        List availableOptions = getAvailableOptions(issueType, attribute, module);		
        List allTransitions = TransitionPeer.getTransitionsFrom(availableOptions, attribute, fromOption);
        Iterator iter = allTransitions.iterator();
        if(!iter.hasNext())
        {
        	return true; // no transition rules available -> any transition possible
        }
        
        while (!result && iter.hasNext())
        {
            Object obj = iter.next();
            Transition tran = (Transition) obj;

			if(transitionIsSupportedByOptions(tran, availableOptions))
			{
                Role requiredRole = tran.getRole();
                if (requiredRole != null)
                { 	// A role is required for this transition to be
                	// allowed
                	result = user.hasRoleInModule(requiredRole, module);
                }
                else
                {
                    result = true;
                }
			}
        }
        return result;
    }

    /**
     * Returns the list of transitions allowed for the current user
     * in the current module/issueType/attribute combination
     * @throws TorqueException 
     */
    public List getTransitions(ScarabUser user,
            IssueType issueType,
            Attribute attribute) throws ScarabException
    {
        Module module = user.getCurrentModule();
        List result = null;
        List availableOptions = getAvailableOptions(issueType, attribute, module);

        Iterator optionsIter = availableOptions.iterator();
        while(optionsIter.hasNext())
        {
            RModuleOption rmoduleOption = (RModuleOption)optionsIter.next();
            AttributeOption fromOption;
            try
            {
                fromOption = rmoduleOption.getAttributeOption();
            }
            catch (TorqueException te)
            {
                L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionTorqueGeneric,te);
                throw new ScarabException(msg);
            }
            List list = getTransitionsFrom(user,issueType,attribute,fromOption);
            if(list != null)
            {
                if(result == null)
                {
                    result = list;
                }
                else
                {
                    result.addAll(list);
                }
            }
        }
        return result;
    }

    /**
     * Returns the tree of transitions
     * in the current module/issueType/attribute combination.
     * @throws TorqueException 
     */
    public TransitionNode getTransitionTree(ScarabUser user,
            IssueType issueType,
            Attribute attribute) throws ScarabException
    {
        Module module = user.getCurrentModule();
        TransitionNode result = null;
        List availableOptions = getAvailableOptions(issueType, attribute, module,false);
        List visitedTransitions = null;
        Iterator optionsIter = availableOptions.iterator();
        while(optionsIter.hasNext())
        {
            RModuleOption rmoduleOption = (RModuleOption)optionsIter.next();
            AttributeOption fromOption;
            try
            {
                fromOption = rmoduleOption.getAttributeOption();
            }
            catch (TorqueException te)
            {
                L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionTorqueGeneric,te);
                throw new ScarabException(msg);
            }
            List list = getTransitionsFrom(user,issueType,attribute,fromOption,false);
            if(list != null)
            {
                if(result == null)
                {
                    result = new TransitionNode(fromOption);
                }
                for(int index=0; index < list.size(); index++)
                {
                    Transition t = (Transition)list.get(index);
                    if(visitedTransitions == null)
                    {
                        visitedTransitions = new ArrayList();
                    }
                    if(visitedTransitions.contains(t))
                    {
                        continue;
                    }
                    visitedTransitions.add(t);
                    TransitionNode child = result.addNode(t);
                    getTransitionTree(user, issueType, attribute, t.getTo(), child, visitedTransitions);
                }
            }
        }
        return result;
    }

    /**
     * Returns the list of transitions allowed for the current user
     * in the current module/issueType/attribute combination
     * @throws TorqueException 
     */
    public void getTransitionTree(ScarabUser user,
            IssueType issueType,
            Attribute attribute,
            AttributeOption fromOption,
            TransitionNode node,
            List visitedTransitions) throws ScarabException
    {
        List list = getTransitionsFrom(user,issueType,attribute,fromOption,false);
        if(list != null)
        {
            for(int index=0; index < list.size(); index++)
            {
                Transition t = (Transition)list.get(index);
                if(visitedTransitions.contains(t))
                {
                    continue;
                }
                visitedTransitions.add(t);
                TransitionNode child = node.addNode(t);
                AttributeOption toOption = t.getTo();
                TransitionNode parent = node.getParent();
                while(parent != null && !parent.getOption().equals(toOption))
                {
                    parent = parent.getParent();
                }
                if(parent == null)
                {
                    getTransitionTree(user,issueType,attribute,t.getTo(),child,visitedTransitions);
                }
            }
        }
    }
    
    /**
     * @param issueType
     * @param attribute
     * @param module
     * @return
     * @throws ScarabException
     */
    private List getAvailableOptions(IssueType issueType, Attribute attribute, Module module) throws ScarabException
    {
        return getAvailableOptions(issueType, attribute, module, true);
    }
    /**
     * @param issueType
     * @param attribute
     * @param module
     * @return
     * @throws ScarabException
     */
    private List getAvailableOptions(IssueType issueType, Attribute attribute, Module module, boolean activeOnly) throws ScarabException
    {
        List availableOptions;
        try 
        {
            availableOptions = module.getOptionTree(attribute,issueType,activeOnly);
        } catch (TorqueException e) 
        {
            LocalizationKey key = L10NKeySet.ExceptionTorqueGeneric;
            L10NMessage msg = new L10NMessage(key,e);
            throw new ScarabException(msg,e);
        }
        return availableOptions;
    }

    
    /**
     * Returns the list of transitions allowed for the current user
     * in the current module/issueType/attribute combination
     * starting from fromOption.
     * @throws TorqueException 
     */
    public List getTransitionsFrom(ScarabUser user,
            IssueType issueType,
            Attribute attribute,
            AttributeOption fromOption) throws ScarabException
    {
        return getTransitionsFrom(user, issueType, attribute, fromOption, false);
    }

    private List getTransitionsFrom(ScarabUser user,
                                   IssueType issueType,
                                   Attribute attribute,
                                   AttributeOption fromOption,
                                   boolean activeOnly) throws ScarabException
                           {
        Module module = user.getCurrentModule();
        List result;
        List availableOptions = getAvailableOptions(issueType, attribute, module, activeOnly);
        List allTransitions = TransitionPeer.getTransitionsFrom(availableOptions, attribute, fromOption);
        Iterator iter = allTransitions.iterator();
        if(iter.hasNext())
        {
            result = new ArrayList();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                Transition transition = (Transition) obj;

                boolean isSupportedByOptions = transitionIsSupportedByOptions(transition, availableOptions);
                if (isSupportedByOptions || !activeOnly)
                {
                    boolean addTransition;
                    
                    if(activeOnly)
                    {
                        Role requiredRole = transition.getRole();
                        if (requiredRole != null)
                        {
                            // A role is required for this transition to be allowed
                            addTransition = user.hasRoleInModule(
                                    requiredRole, module);
                        }
                        else
                        {
                            addTransition = true;
                        }
                    }
                    else
                    {
                        addTransition = true;
                    }

                    if (addTransition)
                    {
                        result.add(transition);
                    }
                }
            }
        }
        else
        {
            result = null; // no transitions defined -> all transitions allowed
        }
        return result;
    }
    
    
    /**
     * It is possible that a defined transition can not be processed, because
     * either the source option or the target option is not available in the
     * current scope. This may happen when an option has been defined in the
     * global attributes section, but later removed in the module scope.
     * @param t
     * @param availableOptions
     * @return
     */
    private static boolean transitionIsSupportedByOptions(Transition t,
            List availableOptions)
    {
        Integer fromId = t.getFromOptionId();
        Integer toId   = t.getToOptionId();
        Iterator iter  = availableOptions.iterator();
        int count = 0;
        
        if(toId == null)
        {
            // allow any target option -> return true if at least one available option exists
            return availableOptions.size() > 0;
        }
        
        
        if (fromId == null || !fromId.equals(toId))
        {
            if (fromId==null || fromId.intValue() == 0)
            {
                // fromId is either any option (null), or emtpy option (0)
                count++;
            }
            while (iter.hasNext() && count < 2)
            {
                RModuleOption attributeOption = (RModuleOption) iter.next();
                Integer id = attributeOption.getOptionId();
                if ( (fromId!=null && id.equals(fromId)) || id.equals(toId))
                {
                    count++;
                }
            }
        }
        return (count < 2) ? false : true;
    }

    /**
     * Filter the allowed transitions so only those not-conditioned, 
     * those whose condition fulfill, and those not restricted by 
     * the blocking condition, will remain.  
     * @param transitions
     * @param issue
     * @return
     * @throws TorqueException
     */
    public List filterConditionalTransitions(List transitions, Issue issue) throws TorqueException
    {
        try
        {
            boolean blockedIssue = issue.isBlocked();
	        if (transitions != null)
	        {
		        for (int i=transitions.size()-1; i>=0; i--)
		        {
		            Transition tran = (Transition)transitions.get(i);
		            if (blockedIssue && tran.getDisabledIfBlocked())
		            {
		                transitions.remove(i);
		                continue;
		                
		            }
		            List conditions = tran.getConditions();
		            if (null != conditions && conditions.size() > 0)
		            {
		                boolean bRemove = true;
		                for (Iterator itReq = conditions.iterator(); bRemove && itReq.hasNext(); )
		                {
		                    Condition cond = (Condition)itReq.next();
		                    Attribute requiredAttribute = cond.getAttributeOption().getAttribute();
		                    Integer optionId = cond.getOptionId();
                            AttributeValue av = issue.getAttributeValue(requiredAttribute);
                            if (av != null)
                            {
                                Integer issueOptionId = av.getOptionId(); 
                                if (issueOptionId != null && issueOptionId.equals(optionId))
                                {
                                    bRemove = false;
                                }
                            }
		                }
		                if (bRemove)
		                {
		                    transitions.remove(i);
		                }
		            }
		        }
	        }
	    }
        catch (Exception e)
    	{
    	    Log.get().error("filterConditionalTransitions: " + e);
    	}

        return transitions;
    }
}
