
package org.tigris.scarab.om;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.om.WorkflowValidationParameter;
import org.tigris.scarab.om.WorkflowValidationParameterPeer;

import org.tigris.scarab.util.ScarabException;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class WorkflowStateValidation
    extends org.tigris.scarab.om.BaseWorkflowStateValidation
    implements Persistent
{

    /**
     * Get the validation parameters for the current workflow state.
     * Used for checking the transition to this state.
     *
     * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
     * @version $Id$
     */
    public Map getWorkflowValidationParameters()
        throws ScarabException
    {
        Map result = new HashMap(10);
        List params = null;

        Criteria criteria = new Criteria(10);
        criteria.add(WorkflowValidationParameterPeer.LIFECYCLE_ID, getLifecycleId() );
        criteria.add(WorkflowValidationParameterPeer.OPTION_ID, getOptionId() );
        criteria.add(WorkflowValidationParameterPeer.VALIDATION_ID, getValidationId() );

        try
        {
            params = WorkflowValidationParameterPeer.doSelect(criteria);
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowStateValidation.getParameters raised exception: ", e);
        }

        Iterator iter = params.iterator();
        while (iter.hasNext())
        {
            WorkflowValidationParameter wvp = (WorkflowValidationParameter)iter.next();
            result.put(wvp.getName(), wvp.getValue());
        }

        return result;


    }
}