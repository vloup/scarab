

package org.tigris.scarab.om;

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/** 
 * This class manages GlobalParameter objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class GlobalParameterManager
    extends BaseGlobalParameterManager
{
    /**
     * Creates a new <code>GlobalParameterManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public GlobalParameterManager()
        throws TorqueException
    {
        super();
    }

    public static GlobalParameter getInstance(String name)
        throws TorqueException
    {
        GlobalParameter result = null;
        Criteria crit = new Criteria();
        crit.add(GlobalParameterPeer.NAME, name);
        List parameters = GlobalParameterPeer.doSelect(crit);
        if (!parameters.isEmpty()) 
        {
            result = (GlobalParameter)parameters.get(0);
        }
        return result;
    }
}




