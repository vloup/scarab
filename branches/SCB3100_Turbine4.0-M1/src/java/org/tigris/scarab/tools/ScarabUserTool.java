package org.tigris.scarab.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.turbine.RunData;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
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

    /**
     * Check, if the user wants to edit this issue.
     * Scarab first looks if the user has pushed the edit button.
     * Then Scarab checks, if it should behave "smart". If smart
     * behaviour is enabled (experimental feature), then Scarab
     * will open the issue in edit-mode per default.
     * Note:[HD] I am not sure, if this is a good idea. But it is 
     * a wanted feature. I will try it out for a while. If it turns
     * out to be unusefull, it will be removed agin. Any opinions ?
     * @param user
     * @param issue
     * @param data
     * @return
     * @throws TorqueException
     */
    public static boolean wantEdit(ScarabUser user, Issue issue, RunData data) throws TorqueException
    {
        Object testExists = data.getParameters().get("edit_attributes");
        if(testExists != null)
        {
            boolean wantToOpenEditor = data.getParameters().getBoolean("edit_attributes");
            return wantToOpenEditor;
        }

        // Now check for condition
        String behaviour = GlobalParameterManager.getStringFromHierarchy(
                                                  "scarab.edit.behaviour",
                                                  issue.getModule(),
                                                  "default");
        if(behaviour.equals("smart"))
        {
            if(issue.isSealed())
            {
                return false; // don't open this issue in edit mode, because it is sealed!
            }
            // Check if user is assigned to issue.
            // If that is the case, open the issue
            // in edit mode per default.
            List<AttributeValue> userAttributeValues = issue.getUserAttributeValues(user);
            Iterator<AttributeValue> iter = userAttributeValues.iterator();
            while(iter.hasNext())
            {
                AttributeValue attributeValue = iter.next();
                Attribute attribute = attributeValue.getAttribute();
                String permission = attribute.getPermission();
                if("Issue | Edit".equals(permission))
                {
                    return true; // if can edit and want edit, lets edit ...
                }
            }
        }
        else
        {
            // Scarab default behaviour: Open issue in view mode.
        }
        return false;
    }

}
