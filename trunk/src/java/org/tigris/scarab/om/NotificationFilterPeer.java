package org.tigris.scarab.om;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.notification.ActivityType;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class NotificationFilterPeer
    extends org.tigris.scarab.om.BaseNotificationFilterPeer
{
    
    public List getCustomization(Object moduleId, Object userId, Object activityType)
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
     	return entries;
    }
    
    public Map getCustomization(Object moduleId, Object userId)
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
