

package org.tigris.scarab.om;

import java.util.List;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.util.ScarabException;

/** 
 * This class manages ScarabUser objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class ScarabUserManager
    extends BaseScarabUserManager
{
    /**
     * Creates a new <code>ScarabUserManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ScarabUserManager()
        throws TorqueException
    {
        super();
    }

    /**
     * Return an instance of User based on username.  Domain is currently
     * unused.
     */
    public static ScarabUser getInstance(String username, String domainName) 
        throws Exception
    {
        ScarabUser user = null;
        if ( username != null ) 
        {
            Criteria crit = new Criteria();
            crit.add(ScarabUserImplPeer.USERNAME, username);
            List users = ScarabUserImplPeer.doSelect(crit);
            if ( users.size() == 1 ) 
            {
                user = (ScarabUser)users.get(0);
            }
            else if ( users.size() > 1 ) 
            {
                throw new ScarabException("duplicate usernames exist");
            }
        }
        return user;
    }

    /**
     * Gets a list of ScarabUsers based on usernames.  Domain is currently
     * unused.
     *
     * @param usernames a <code>String[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public static List getUsers(String[] usernames, String domainName) 
        throws Exception
    {
        List users = null;
        if ( usernames != null && usernames.length > 0 ) 
        {
            Criteria crit = new Criteria();
            crit.addIn(ScarabUserImplPeer.USERNAME, usernames);
            users = ScarabUserImplPeer.doSelect(crit);            
        }
        return users;
    }

}





