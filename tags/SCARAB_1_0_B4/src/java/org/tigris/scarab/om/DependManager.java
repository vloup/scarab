

package org.tigris.scarab.om;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

/** 
 * This class manages Depend objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class DependManager
    extends BaseDependManager
{
    /**
     * Creates a new <code>DependManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public DependManager()
        throws TorqueException
    {
        super();
        validFields = new HashMap();
        validFields.put(DependPeer.OBSERVER_ID, null);
        validFields.put(DependPeer.OBSERVED_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        // super method checks for correct class, so just cast it
        Depend d = (Depend)om;

        Map subsetMap = (Map)listenersMap.get(DependPeer.OBSERVER_ID);
        if (subsetMap != null) 
        {
            ObjectKey issue_id = d.getObserverId();
            List listeners = (List)subsetMap.get(issue_id);
            notifyListeners(listeners, oldOm, om);
        }
        subsetMap = (Map)listenersMap.get(DependPeer.OBSERVED_ID);
        if (subsetMap != null) 
        {
            ObjectKey issue_id = d.getObservedId();
            List listeners = (List)subsetMap.get(issue_id);
            notifyListeners(listeners, oldOm, om);
        }
        return oldOm;
    }
}





