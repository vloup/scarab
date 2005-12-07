package org.tigris.scarab.om;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.notification.NotificationManagerFactory;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class NotificationFilterPeer
    extends org.tigris.scarab.om.BaseNotificationFilterPeer
{
    
    /**
     * Return the list of configured managers for this user, this module
     * and this activityType.
     * NOTE: Currently only the Scarab default Notification manager is
     * supported, so the List will mostly contain 1 element. It isplanned
     * to add more managers in the future, so be prepared to find multiple
     * entries in the List.
     * If no manager is configured, return an empty List
     * @param moduleId
     * @param userId
     * @param activityType
     * @return
     * @throws TorqueException 
     */
    public List getCustomization(Object moduleId, Object userId, Object activityType) throws TorqueException
    {
    	List entries = null;
    	Criteria crit = new Criteria();
    	crit.add(MODULE_ID,       moduleId,     Criteria.EQUAL);
    	crit.add(USER_ID,         userId,       Criteria.EQUAL);
    	crit.add(ACTIVITY_TYPE,   activityType, Criteria.EQUAL);
    	try {
    		entries = doSelect(crit);
    	} catch (TorqueException e) {
    		log.error("getPendingNotifications(): " + e);
    	}
        if(entries.size()==0)
        {
            NotificationFilter filter = 
                NotificationFilter.createDefaultFilter(
                        (Integer)moduleId,
                        (Integer)userId,
                        NotificationManagerFactory.getInstance().getManagerId(),
                        ActivityType.getActivityType((String)activityType));
        }
     	return entries;
    }
    
    public Map getCustomization(Object moduleId, Object userId) throws TorqueException
    {
    	Map entries = new Hashtable();
    	Criteria crit = new Criteria();
    	Set codes = ActivityType.getActivityTypeCodes();
    	Iterator iter = codes.iterator();
    	while(iter.hasNext())
    	{
    	    String code = (String)iter.next();
    	    List items = getCustomization(moduleId, userId, code);
    	    entries.put(code,items);
    	}
     	return entries;
    }
    
}
