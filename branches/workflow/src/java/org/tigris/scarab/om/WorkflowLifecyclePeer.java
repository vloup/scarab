package org.tigris.scarab.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.map.*;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.ScarabException;


/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class WorkflowLifecyclePeer
    extends org.tigris.scarab.om.BaseWorkflowLifecyclePeer
{
    //When transitioning on entry to the initial state, this is the from option
    public static NumberKey FROM_ENTRY_OPTION_ID = new NumberKey(0);
    public static AttributeOption FROM_ENTRY_ATTRIBUTE_OPTION;


    private static final String WORKFLOW_LIFECYCLE_PEER =
        "WorkflowLifecyclePeer";

    private static final String GET_ALL_WORKFLOW_LIFECYCLES =
        "getAllWorkflowLifecycles";

    private static final String GET_WORKFLOW_LIFECYCLE =
        "getWorkflowLifecycle";

    private static final String GET_MODULE_WORKFLOW_LIFECYCLES =
        "getModuleWorkflowLifecycles";


    static
    {
        try
        {
            FROM_ENTRY_ATTRIBUTE_OPTION = AttributeOptionManager.getInstance(FROM_ENTRY_OPTION_ID);
        }
        catch (Exception e)
        {
            FROM_ENTRY_ATTRIBUTE_OPTION = null;
        }
    }

    /**
     * Get the lifecycle for the current module, issuetype and attribute
     * there should be only one workflow for a certain module/issue type that is active
     */
    public static WorkflowLifecycle getWorkflowLifecycle(Module module, IssueType issueType, Attribute attribute, boolean active)
        throws ScarabException
    {
        Criteria criteria = new Criteria(10);
        List collLifecycles = null;
        WorkflowLifecycle result = null;

        Object obj = ScarabCache.get(WORKFLOW_LIFECYCLE_PEER, GET_WORKFLOW_LIFECYCLE, module, issueType, attribute);
        if ( obj == null )
        {

            criteria.add(WorkflowLifecyclePeer.MODULE_ID, module.getModuleId())
                    .add(WorkflowLifecyclePeer.ISSUE_TYPE_ID, issueType.getIssueTypeId())
                    .add(WorkflowLifecyclePeer.STATE_ATTRIBUTE_ID, attribute.getAttributeId());

            if(active)
            {
                criteria.add(WorkflowLifecyclePeer.ACTIVE, true);
            }

            try
            {
                collLifecycles = doSelect(criteria);
            }
            catch(Exception e)
            {
                throw new ScarabException("WorkflowLifecyclePeer.getWorkflowLifecycle select failed", e);
            }

            //get the first element of the list
            if (collLifecycles.size() > 0)
            {
                result = (WorkflowLifecycle) collLifecycles.get(0);
                ScarabCache.put(result, WORKFLOW_LIFECYCLE_PEER, GET_WORKFLOW_LIFECYCLE, module, issueType, attribute);
            }
        }
        else
        {
            result = (WorkflowLifecycle)obj;
        }

        return result;
    }

    /**
     * Get all the active lifecycles for the current module
     */
    public static List getModuleWorkflowLifecycles(Module module, boolean activeOnly)
        throws ScarabException
    {
        List collLifecycles = null;

        Object obj = ScarabCache.get(WORKFLOW_LIFECYCLE_PEER, GET_MODULE_WORKFLOW_LIFECYCLES, module, new Boolean(activeOnly));
        if ( obj == null )
        {

            Criteria criteria = new Criteria(10);
            criteria.add(WorkflowLifecyclePeer.MODULE_ID, module.getModuleId());

            if (activeOnly)
            {
                criteria.add(WorkflowLifecyclePeer.ACTIVE, true);
            }

            try
            {
                collLifecycles = doSelect(criteria);
                ScarabCache.put(collLifecycles, WORKFLOW_LIFECYCLE_PEER, GET_MODULE_WORKFLOW_LIFECYCLES, module, new Boolean(activeOnly));
            }
            catch(Exception e)
            {
                throw new ScarabException("WorkflowLifecyclePeer.getWorkflowLifecycle select failed", e);
            }
        }
        else
        {
            collLifecycles = (List)obj;
        }

        return collLifecycles;
    }

    /**
     *  Gets a List of all of the lifecycles in the database,
     */
    public static List getAllWorkflowLifecycles()
        throws ScarabException
    {
        List result = null;
        Object obj = ScarabCache.get(WORKFLOW_LIFECYCLE_PEER, GET_ALL_WORKFLOW_LIFECYCLES);
        if ( obj == null )
        {
            Criteria crit = new Criteria();
            crit.addAscendingOrderByColumn(WorkflowLifecyclePeer.DISPLAY_VALUE);
            try
            {
                result = doSelect(crit);
                ScarabCache.put(result, WORKFLOW_LIFECYCLE_PEER, GET_ALL_WORKFLOW_LIFECYCLES);
            }
            catch(Exception e)
            {
                throw new ScarabException("WorkflowLifecyclePeer.getAllWorkflowLifecycles select failed", e);
            }
        }
        else
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * Checks to see if the name already exists within the module.
     *
     * @param name a <code>String</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public static boolean isUnique(String name, NumberKey moduleId)
        throws ScarabException
    {
        boolean unique = false;
        Criteria crit = new Criteria(10);
        crit.add(WorkflowLifecyclePeer.DISPLAY_VALUE, name)
            .add(WorkflowLifecyclePeer.MODULE_ID, moduleId)
            .setIgnoreCase(true);

        try
        {
            List types = WorkflowLifecyclePeer.doSelect(crit);
            if ( types.size() == 0 )
            {
                unique = true;
            }
        }
        catch(Exception e)
        {
            throw new ScarabException("WorkflowLifecyclePeer.isUnique select failed", e);
        }
        return unique;
    }

    public static void delete(ObjectKey lifecycleId)
        throws ScarabException
    {
        WorkflowLifecycle wl = null;
        List wts = null;

        try
        {
            //get the workflow to delete
            wl = WorkflowLifecycleManager.getInstance(lifecycleId);

            //delete the transitions one by one
            wts = wl.getWorkflowTransitions();

        }
        catch(Exception e)
        {
            throw new ScarabException("WorkflowLifecyclePeer.delete failed", e);
        }


        Iterator trans = wts.iterator();
        while(trans.hasNext())
        {
            WorkflowTransition wt = (WorkflowTransition) trans.next();
            WorkflowTransitionPeer.delete(wt.getTransitionId());
        }

        //delete from the Lifecycle table itself
        Criteria crit = new Criteria(10);
        crit.add(WorkflowLifecyclePeer.LIFECYCLE_ID, lifecycleId);
        try
        {
            doDelete(crit);
        }
        catch(Exception e)
        {
            throw new ScarabException("WorkflowLifecyclePeer.delete lifecycle failed", e);
        }
    }

    /**
     * Check to see if there is an initial transaction saved for this lifecycle
     *
     * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
     * @version $Id$
     */
    public static boolean hasInitialTransition(WorkflowLifecycle lifecycle)
        throws ScarabException
    {
        List collWorkflowTransitions = null;
        boolean result = false;

        try
        {
            Criteria criteria = new Criteria(10);
            criteria.add(WorkflowTransitionPeer.LIFECYCLE_ID, lifecycle.getLifecycleId())
                    .add(WorkflowTransitionPeer.FROM_OPTION_ID, FROM_ENTRY_OPTION_ID);

            collWorkflowTransitions = WorkflowTransitionPeer.doSelect(criteria);

            if (collWorkflowTransitions != null && collWorkflowTransitions.size() > 0)
            {
                result = true;
            }
        }
        catch (Exception e)
        {
            throw new ScarabException("[WorkflowTransitionPeer] getWorkflowTransition: failed to get transition",e);
        }

        return result;
    }

}
