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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.services.pull.ApplicationTool;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeTypePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.WorkflowLifecycle;
import org.tigris.scarab.om.WorkflowLifecycleManager;
import org.tigris.scarab.om.WorkflowLifecyclePeer;
import org.tigris.scarab.om.WorkflowStateValidation;
import org.tigris.scarab.om.WorkflowStateValidationManager;
import org.tigris.scarab.om.WorkflowStateValidationPeer;
import org.tigris.scarab.om.WorkflowTransition;
import org.tigris.scarab.om.WorkflowTransitionManager;
import org.tigris.scarab.om.WorkflowTransitionPeer;
import org.tigris.scarab.om.WorkflowTransitionRole;
import org.tigris.scarab.om.WorkflowValidationClass;
import org.tigris.scarab.om.WorkflowValidationClassManager;
import org.tigris.scarab.om.WorkflowValidationClassPeer;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.workflow.validations.WorkflowValidation;


/**
 * This scope is an object that is made available as a global
 * object within the system.
 *
 * These methods, unlike in the Workflow interface, are used to
 * administer the workflow set-up.
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class WorkflowTool implements ApplicationTool
{

    public void init(Object data)
    {
    }

    public void refresh()
    {
    }

    /**
     * Constructor does initialization stuff
     */
    public WorkflowTool()
    {
    }

    /**
     * Get a specific lifecycle by key value. Returns null if
     * the Lifecycle could not be found
     *
     * @param key a <code>String</code> value
     * @return a <code>WorkflowLifecycle</code> value
     */
    public WorkflowLifecycle getWorkflowLifecycle(String key)
        throws Exception
    {
        WorkflowLifecycle wl = null;
        if ( key != null && key.length() > 0 )
        {
            wl = WorkflowLifecyclePeer.retrieveByPK(new NumberKey(key));
        }
        return wl;
    }

    /**
     * Get a specific lifecycle by key value. Returns null if
     * the Lifecycle could not be found
     *
     * @param key a <code>String</code> value
     * @return a <code>WorkflowLifecycle</code> value
     */
    public WorkflowLifecycle getWorkflowLifecycle()
        throws Exception
    {
        WorkflowLifecycle wl = null;
        wl = WorkflowLifecycleManager.getInstance();
        return wl;
    }

    /*
        Get the workflowLifecycles for the module that are active
    */
    public List getModuleWorkflowLifecycles(Module module, boolean activeOnly)
        throws ScarabException
    {
        return WorkflowLifecyclePeer.getModuleWorkflowLifecycles(module, activeOnly);
    }

    /*
        Get the attributes for the module, issuetype that are active
    */
    public List getAttributes(Module module,String issueTypeId)
        throws ScarabException
    {
        List rmas = null;

        try
        {
            Criteria crit = new Criteria();
            crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, issueTypeId)
                .add(RModuleAttributePeer.MODULE_ID, module.getModuleId())
                .add(RModuleAttributePeer.ACTIVE, true)
                .addJoin(AttributePeer.ATTRIBUTE_ID, RModuleAttributePeer.ATTRIBUTE_ID)
                .add(AttributePeer.ATTRIBUTE_TYPE_ID, AttributeTypePeer.DROPDOWN_LIST_TYPE_KEY);

        rmas = RModuleAttributePeer.doSelect(crit);

        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getAttributes Unable to retrieve dropdown lists", e);
        }

        return rmas;
    }


    public RModuleOption getRModuleOption(String moduleId, String issueTypeId, String optionId)
        throws ScarabException
    {
        List rmos = null;

        RModuleOption rmo = null;

        try
        {
            Criteria crit = new Criteria();
            crit.add(RModuleOptionPeer.ISSUE_TYPE_ID, issueTypeId)
                .add(RModuleOptionPeer.MODULE_ID, moduleId)
                .add(RModuleOptionPeer.OPTION_ID, optionId);

            rmos = RModuleOptionPeer.doSelect(crit);

            if((rmos != null) && (rmos.size() > 0))
            {
                rmo = (RModuleOption) rmos.get(0);
            }

        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getAttributes Unable to retrieve dropdown lists", e);
        }

        return rmo;

    }

    /**
     * Get a specific state by key value. Returns null if
     * the state could not be found
     *
     * @param key a <code>String</code> value
     * @return a <code>WorkflowState</code> value
     */
    public AttributeOption getAttributeOption(String key)
        throws Exception
    {
        AttributeOption state = null;

        if ( key != null && key.length() > 0 )
        {
            try
            {
                state = AttributeOptionManager.getInstance(new NumberKey(key));
            }
            catch (Exception e)
            {
                Log.get().info("WorkflowTool.getState Unable to retrieve State: " +
                         key, e);
            }
        }

        return state;
    }

    /**
     * Get a specific transition by key value. Returns null if
     * the transition could not be found
     *
     * @param key a <code>String</code> value
     * @return a <code>WorkflowTransition</code> value
     */
    public WorkflowTransition getWorkflowTransition(String key)
        throws ScarabException
    {
        WorkflowTransition wt = null;
        if ( key != null && key.length() > 0 )
        {
            try
            {
                wt = WorkflowTransitionManager.getInstance(new NumberKey(key));
            }
            catch (Exception e)
            {
                throw new ScarabException("WorkflowTool.getWorkflowTransition Unable to retrieve Transition: " + key, e);
            }
        }
        return wt;
    }

    /*
        Get a new workflow transition
    */
    public WorkflowTransition getWorkflowTransition()
        throws ScarabException
    {
        WorkflowTransition wt = null;
        try
        {
            wt = WorkflowTransitionManager.getInstance();
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowTransition Unable to retrieve new Transition", e);
        }
        return wt;
    }


    /*
        Get the workflow transitions for the lifecycle
    */
    public List getWorkflowTransitions(WorkflowLifecycle wl)
        throws ScarabException
    {
        List wts = null;
        Criteria crit = new Criteria(10);
        try
        {
            crit.addAscendingOrderByColumn(WorkflowTransitionPeer.FROM_OPTION_ID);
            crit.addAscendingOrderByColumn(WorkflowTransitionPeer.TO_OPTION_ID);
            crit.addAscendingOrderByColumn(WorkflowTransitionPeer.TRANSITION_ID);
            wts = wl.getWorkflowTransitions(crit);
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowTransitions(wl) Unable to retrieve Transitions", e);
        }
        return wts;
    }


    /*
        Get the workflow transitions for the lifecycle
    */
    public List getWorkflowTransitionRoleNames(WorkflowTransition transition)
        throws ScarabException
    {
        List roles = null;
        List roleNames = new ArrayList(10);

        try
        {
            roles = transition.getWorkflowTransitionRoles();
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowTransitionRoleNames Unable to retrieve roles", e);
        }

        Iterator iter = roles.iterator();
        while (iter.hasNext())
        {
            WorkflowTransitionRole wtr = (WorkflowTransitionRole)iter.next();
            roleNames.add(wtr.getRoleName());
        }

        return roleNames;
    }

    /*
        Get a new workflow transition
    */
    public boolean hasInitialTransition(WorkflowLifecycle wl)
        throws ScarabException
    {
        boolean result = false;

        try
        {
            result = WorkflowLifecyclePeer.hasInitialTransition(wl);
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.hasInitialTransition threw exception", e);
        }
        return result;
    }


    public List getWorkflowStateValidations(String lifecycleId, String toOptionId)
        throws ScarabException
    {
        List lsvs = null;

        try
        {
            Criteria crit = new Criteria();
            crit.add(WorkflowStateValidationPeer.LIFECYCLE_ID, lifecycleId)
                .add(WorkflowStateValidationPeer.OPTION_ID, toOptionId);

            lsvs = WorkflowStateValidationPeer.doSelect(crit);

        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowStateValidations Unable to retrieve validations", e);
        }

        return lsvs;
    }

    public WorkflowValidationClass getValidationClass(String validationId)
        throws ScarabException
    {
        List vcs = null;
        WorkflowValidationClass vc = null;

        try
        {
            Criteria crit = new Criteria();
            crit.add(WorkflowValidationClassPeer.VALIDATION_ID, validationId);

            vcs = WorkflowValidationClassPeer.doSelect(crit);

            if((vcs != null) && (vcs.size() > 0))
            {
                vc = (WorkflowValidationClass) vcs.get(0);
            }

        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowStateValidations Unable to retrieve validations", e);
        }

        return vc;
    }

    public WorkflowStateValidation getWorkflowStateValidation(String stateValidationId)
        throws ScarabException
    {
        WorkflowStateValidation wsv= null;

        try
        {
            wsv = WorkflowStateValidationManager.getInstance(new NumberKey(stateValidationId));
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getLifecycleStateValidations Unable to retrieve validations", e);
        }

        return wsv;
    }

    public WorkflowValidation getWorkflowValidation(String validationId)
        throws ScarabException
    {

        WorkflowValidation wv;

        try
        {
            WorkflowValidationClass wvc = WorkflowValidationClassManager.getInstance(new NumberKey(validationId));
            Class wvClass = Class.forName(wvc.getJavaClassName());
            wv = (WorkflowValidation) wvClass.newInstance();
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getWorkflowValidation could not get validation class: " + e);
        }

        return wv;

    }
    public List getWorkflowValidationClasses()
        throws ScarabException
    {
        List lsvs = null;

        try
        {
            Criteria crit = new Criteria();
            crit.add(WorkflowValidationClassPeer.VALIDATION_ID, 0, Criteria.GREATER_THAN);

            lsvs = WorkflowValidationClassPeer.doSelect(crit);
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTool.getLifecycleStateValidations Unable to retrieve validations", e);
        }

        return lsvs;
    }
}
