package org.tigris.scarab.om;

import java.util.List;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.WorkflowTransition;
import org.tigris.scarab.om.WorkflowLifecycle;
import org.tigris.scarab.om.WorkflowTransitionPeer;
import org.tigris.scarab.om.WorkflowTransitionRolePeer;

import org.tigris.scarab.util.ScarabException;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class WorkflowTransitionPeer
    extends org.tigris.scarab.om.BaseWorkflowTransitionPeer
{

    /**
     * Get the transition that matches the submitted lifecycle, options, and user roles
     * returns a null WorkflowTransition if none found to match the criteria.
     *
     * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
     * @version $Id$
     */
    public static WorkflowTransition getWorkflowTransition(WorkflowLifecycle lifecycle,
                                                           AttributeOption fromOption,
                                                           AttributeOption toOption,
                                                           ScarabUser user)
        throws ScarabException
    {
        List collWorkflowTransitions = null;
        WorkflowTransition wt = null;

        try
        {
            //now get user roles
            List roleNames = user.getRoleNames(lifecycle.getModule());

            Criteria criteria = new Criteria(10);
            criteria.add(WorkflowTransitionPeer.LIFECYCLE_ID, lifecycle.getLifecycleId())
                    .add(WorkflowTransitionPeer.FROM_OPTION_ID, fromOption.getOptionId())
                    .add(WorkflowTransitionPeer.TO_OPTION_ID, toOption.getOptionId())
                    .addJoin(WorkflowTransitionPeer.TRANSITION_ID, WorkflowTransitionRolePeer.TRANSITION_ID)
                    .addIn(WorkflowTransitionRolePeer.ROLE_NAME, roleNames);

            collWorkflowTransitions = WorkflowTransitionPeer.doSelect(criteria);

            if (collWorkflowTransitions != null && collWorkflowTransitions.size() > 0)
            {
                wt = (WorkflowTransition) collWorkflowTransitions.get(0);
            }
        }
        catch (Exception e)
        {
            throw new ScarabException("[WorkflowTransitionPeer] getWorkflowTransition: failed to get transition",e);
        }

        return wt;
    }

    /**
     * Find if transition matches the submitted lifecycle, and options
     * returns a null WorkflowTransition if none found to match the criteria.
     * Used to check for dupes
     *
     * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
     * @version $Id$
     */
    public static boolean isUnique(String lifecycleId,
                                   String fromOptionId,
                                   String toOptionId)
        throws ScarabException
    {
        List collWorkflowTransitions = null;
        WorkflowTransition wt = null;
        boolean result = true;

        try
        {
            Criteria criteria = new Criteria(10);
            criteria.add(WorkflowTransitionPeer.LIFECYCLE_ID, lifecycleId)
                    .add(WorkflowTransitionPeer.FROM_OPTION_ID, fromOptionId)
                    .add(WorkflowTransitionPeer.TO_OPTION_ID, toOptionId);

            collWorkflowTransitions = WorkflowTransitionPeer.doSelect(criteria);

            if (collWorkflowTransitions != null && collWorkflowTransitions.size() > 0)
            {
                result = false;
            }
        }
        catch (Exception e)
        {
            throw new ScarabException("[WorkflowTransitionPeer] getWorkflowTransition: failed to get transition",e);
        }

        return result;
    }

    public static void delete(ObjectKey transitionId)
        throws ScarabException
    {
        //delete the actions related to the transition
        // NOT IMPLEMENTED - each action should also delete its parameters

        //delete the transition itself
        Criteria crit = new Criteria(10);
        crit.add(WorkflowTransitionPeer.TRANSITION_ID, transitionId);
        try
        {
            doDelete(crit);
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowTransitionPeer.delete failed",e);
        }
    }

}
