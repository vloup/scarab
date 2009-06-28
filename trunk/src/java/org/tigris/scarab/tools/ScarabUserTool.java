package org.tigris.scarab.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.turbine.RunData;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationStatusManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.word.IssueSearch;

public class ScarabUserTool 
{

    /**
     * Return a specific User by username.
     */
    public static ScarabUser getUserByUserName(String username)
    {
        ScarabUser su = null;
        try
        {
            su = ScarabUserManager.getInstance(username);
        }
        catch (Exception e)
        {
            // Logged at debug level, as a null user is interpreted
            // as an invalid user name
            Log.get().debug("User, "+username+" could not be found,", e);
        }
        return su;
    }
    
    /**
     * Return a specific User by ID from within the system.
     * You can pass in either a Integer or something that
     * will resolve to a String object as id.toString() is
     * called on everything that isn't a Integer.
     */
    public static ScarabUser getUser(Object userId)
        throws TorqueException
    {
        if (userId == null)
        {
            return null;
        }

        if(IssueSearch.SEARCHING_USER_KEY.equalsIgnoreCase(userId.toString()))
        {
            return IssueSearch.getSearchingUserPlaceholder();
        }

        Integer pk = null;
        try
        {
            pk = new Integer(userId.toString());
        }
        catch( NumberFormatException e)
        {
            return null;
        }

        ScarabUser su = null;
        try
        {
            su = ScarabUserManager.getInstance(pk);
        }
        catch (TorqueException e)
        {
            return null;
        }
        return su;
    }
    
    /**
     * Sort users on name or email.
     */
    public static List sortUsers(List userList,  RunData data)  throws Exception
    {
        final String sortColumn = data.getParameters().getString("sortColumn");
        final String sortPolarity = data.getParameters().getString("sortPolarity");
        final int polarity = ("desc".equals(sortPolarity)) ? -1 : 1;
        Comparator c = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                int i = 0;
                if ("username".equals(sortColumn))
                {
                    i =  polarity * ((ScarabUser)o1).getUserName()
                         .compareTo(((ScarabUser)o2).getUserName());
                }
                else
                {
                    i =  polarity * ((ScarabUser)o1).getName()
                         .compareTo(((ScarabUser)o2).getName());
                }
                return i;
             }
        };
        Collections.sort(userList, c);
        return userList;
    }

    
    /**
     * Returns if the system is configurated to allow anonymous login.
     *
     */
    public static boolean isAnonymousLoginAllowed()
        throws TorqueException
    {
        return ScarabUserManager.anonymousAccessAllowed();
    }
    
    public static int getNotificationCount(Module module, ScarabUser user) throws TorqueException
    {
        return NotificationStatusManager.getNotificationCount(module, user);
    }


}
