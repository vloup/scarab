

package org.tigris.scarab.om;


import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.om.ScarabUser;

/** 
 * This class manages MITList objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class MITListManager
    extends BaseMITListManager
{
    /**
     * Creates a new <code>MITListManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public MITListManager()
        throws TorqueException
    {
        super();
    }

    public static MITList getCurrentModuleAllIssueTypesList(ScarabUser user)
        throws TorqueException
    {
        MITList list = getInstance(MITListPeer.CURRENT_MODULE_ALL_ISSUETYPES)
            .copy();
        list.setScarabUser(user);
        return list;
    }
}





