package org.tigris.scarab.workflow;

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

import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.OptionWorkflow;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.WorkflowLifecycle;
import org.tigris.scarab.om.WorkflowLifecyclePeer;
import org.tigris.scarab.om.WorkflowRules;
import org.tigris.scarab.om.WorkflowStateValidation;
import org.tigris.scarab.om.WorkflowTransition;
import org.tigris.scarab.om.WorkflowTransitionPeer;
import org.tigris.scarab.om.WorkflowValidationClass;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.workflow.validations.WorkflowValidation;


/**
 * This scope is an object that is made available as a global
 * object within the system.
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $scarabWorkflow
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 * <p>
 * The methods herein could be moved to ScarabGlobalTool
 * but I am putting them in a seperate class to keep some division
 * of the workflow from the rest of the application.
 *
 * This class uses information stored in the same scarab database
 * to drive and specify workflow behaviour.
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class BasicWorkflow implements Workflow
{

/**
 *   Workflow Interface Methods
 */
    public boolean canMakeTransition(ScarabUser user,
                                     AttributeOption fromOption,
                                     AttributeOption toOption,
                                     Issue issue)
        throws ScarabException
    {
        boolean result = false;

        AttributeValue av = null;
        WorkflowLifecycle lifecycle = null;
        WorkflowTransition transition = null;
        Attribute attribute = null;
        Module module= null;
        IssueType issueType = null;

        if(fromOption.equals(toOption))
        {
            result = true;
        }
        else
        {
            try
            {
                module    = issue.getModule();
                issueType = issue.getIssueType();
                attribute = toOption.getAttribute();
            }
            catch (Exception e)
            {
                throw new ScarabException("BasicWorkflow.canMakeTransition get parameters failed", e);
            }

            //get the workflow for the module/issuetype/attribute
            lifecycle = WorkflowLifecyclePeer.getWorkflowLifecycle(module,issueType,attribute,true);

            //if there is no workflow, change is fine, otherwise check the transitions
            if (lifecycle != null)
            {
                //get the transition if there is one for the user's roles
                transition = WorkflowTransitionPeer.getWorkflowTransition(lifecycle,fromOption,toOption,user);

                if (transition != null)
                {
                    result = true;
                }
            }
            else
            {
                result = true;
            }
        }

        return result;
    }


    public String checkInitialTransition(AttributeOption toOption,
                                         Issue issue,
                                         HashMap newAttVals,
                                         ScarabUser user)
        throws ScarabException
    {
        return checkTransition(WorkflowLifecyclePeer.FROM_ENTRY_ATTRIBUTE_OPTION,
                               toOption,
                               issue,
                               newAttVals,
                               user);
    }


    public String checkTransition(AttributeOption fromOption,
                                  AttributeOption toOption,
                                  Issue issue, HashMap newAttVals,
                                  ScarabUser user)
        throws ScarabException
    {
        String message = null;

        AttributeValue av = null;
        WorkflowLifecycle lifecycle = null;
        WorkflowTransition transition = null;
        Attribute attribute = null;
        Module module= null;
        IssueType issueType = null;

        try
        {
            module    = issue.getModule();
            issueType = issue.getIssueType();
            attribute = toOption.getAttribute();
        }
        catch (Exception e)
        {
            throw new ScarabException("BasicWorkflow.checkTransition get parameters failed", e);
        }

        //get the workflow for the module/issuetype/attribute
        lifecycle = WorkflowLifecyclePeer.getWorkflowLifecycle(module,issueType,attribute,true);

        //if there is no workflow, transition is impossible, return false
        if (lifecycle != null)
        {
            //get the transition if there is one for the user's roles
            transition = WorkflowTransitionPeer.getWorkflowTransition(lifecycle,fromOption,toOption,user);

            if (transition != null)
            {
                //check validations
                message = checkValidations(transition,issue,newAttVals,user);
            }
            else
            {
                //there is workflow, but no transition available for this user/role
                message = "This transition is not allowed.";
            }
        }
        else
        {
            //no workflow defined, so no rule against transition
            message = null;
        }

        return message;
    }


    public void deleteWorkflowsForOption(AttributeOption option,
                                         Module module,
                                         IssueType issueType)
        throws ScarabException
    {
    }


    public void deleteWorkflowsForAttribute(Attribute attr,
                                            Module module,
                                            IssueType issueType)
        throws ScarabException
    {
    }


    //private aux methods
    private String checkValidations(WorkflowTransition transition,
                                    Issue issue,
                                    HashMap newAttVals,
                                    ScarabUser user)
        throws ScarabException
    {
        List validations = null;
        WorkflowStateValidation wsValidation = null;
        WorkflowValidationClass validation = null;
        WorkflowValidation aValidation = null;
        AttributeOption ao = null;
        Class wvClass = null;
        String message = null;

        try
        {
            //get the validations related to the state the issue is moving to
            validations = transition.getAttributeOptionRelatedByToOptionId().getWorkflowStateValidations();
        }
        catch(Exception e)
        {
            throw new ScarabException("BasicWorkflow.checkValidations cannot find required info to validate transition", e);
        }


        //foreach validation for the new state
        Iterator iterValidations = validations.iterator();

        Map validationContext = new HashMap(10);
        while (iterValidations.hasNext())
        {
            //if passed, return empty string
            //if not add error message returned by the state validation to message
            wsValidation = (WorkflowStateValidation)iterValidations.next();

            try
            {
                validation = wsValidation.getWorkflowValidationClass();
            }
            catch (Exception e)
            {
                throw new ScarabException("BasicWorkflow.checkValidations failed to get WorkflowValidationClass from WorkflowStateValidation", e);
            }

            try
            {
                aValidation = validation.getWorkflowValidation();
            }
            catch (Exception e)
            {
                throw new ScarabException("BasicWorkflow.checkValidations could not instantiate validation class ", e);
            }

            try
            {
                Map parameters = wsValidation.getWorkflowValidationParametersMap();
                Map objects = validationObjects(transition,issue,newAttVals,user);
                String validationResult = aValidation.doValidation(parameters, objects, validationContext);
                message = addToMessage(message, validationResult);
            }
            catch (Exception e)
            {
                throw new ScarabException("BasicWorkflow.checkValidations validation raised exception: ", e);
            }
        }

        if (message != null && message.length() == 0)
        {
            message = null;
        }

        return message;
    }



    private String addToMessage(String message, String addition)
    {
        String result;

        if(message != null && message.length() > 0)
        {
            result = message + ", " + addition;
        }
        else
        {
            result = addition;
        }

        return result;
    }


    private Map validationObjects(WorkflowTransition wt,
                                  Issue issue,
                                  HashMap newAttVals,
                                  ScarabUser user)
    {
        Map params = new HashMap(4);
        params.put("transition",wt);
        params.put("issue",issue);
        params.put("newAttVals",newAttVals);
        params.put("user",user);
        return params;
    }

    public OptionWorkflow getWorkflowForRole(AttributeOption fromOption,
                                             AttributeOption toOption,
                                             String roleName,
                                             Module module,
                                             IssueType issueType)
        throws ScarabException
    {
        return null;
    }

    public List getWorkflowsForRoleList(AttributeOption fromOption,
                                        AttributeOption toOption,
                                        List roleNames,
                                        Module module,
                                        IssueType issueType)
        throws ScarabException
    {
        return null;
    }

    public OptionWorkflow inherit(AttributeOption fromOption,
                                   AttributeOption toOption,
                                   String roleName, Module module,
                                   IssueType issueType)
        throws ScarabException
    {
        return null;
    }

    public void saveWorkflow(AttributeOption fromOption,
                             AttributeOption toOption,
                             String roleName, Module module,
                             IssueType issueType, WorkflowRules workflowRule)
        throws ScarabException
    {
        //nothing
    }

    public void resetWorkflow(AttributeOption fromOption,
                              AttributeOption toOption,
                              String roleName, Module module,
                              IssueType issueType)
        throws ScarabException
    {
       // nothing
    }

    public void resetWorkflows(String roleName, Module module, IssueType issueType,
                               boolean initial)
        throws ScarabException
    {
       // nothing
    }

    public List getWorkflowsForIssueType(IssueType issueType)
        throws ScarabException
    {
        return null;
    }

    public void addIssueTypeWorkflowToModule(Module module, 
                                            IssueType issueType)
        throws ScarabException
    {
       // nothing
    } 

    public void resetAllWorkflowsForIssueType(Module module, 
                                              IssueType issueType)
        throws ScarabException
    {
       // nothing
    }

}
