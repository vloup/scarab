
package org.tigris.scarab.om;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;


import org.tigris.scarab.util.ScarabException;

import org.apache.torque.TorqueException;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class WorkflowStateValidation
    extends org.tigris.scarab.om.BaseWorkflowStateValidation
    implements Persistent
{

    public Map getWorkflowValidationParametersMap()
        throws ScarabException
    {
        List params = null;

        try
        {
            params = super.getWorkflowValidationParameters();
        }
        catch(TorqueException te)
        {
            throw new ScarabException(te);
        }

        Map result = new HashMap(10);

        Iterator paramsIterator = params.iterator();

        while(paramsIterator.hasNext())
        {
            WorkflowValidationParameter wvp = (WorkflowValidationParameter) paramsIterator.next();
            result.put(wvp.getName().toString(), wvp.getValue().toString());
        }

        return result;

    }

}