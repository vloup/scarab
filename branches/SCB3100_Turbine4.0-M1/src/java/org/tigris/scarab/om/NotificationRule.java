package org.tigris.scarab.om;


import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

/**
 * Notification rule table to determine when a notification has to be sent out to whom. Used by NotificationManager.
 *
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class NotificationRule
    extends org.tigris.scarab.om.BaseNotificationRule
    implements Persistent
{

    /**
     * Create a default filter.
     * 
     * @param moduleId
     * @param userId
     * @param managerId
     * @param activityType
     * @return
     * @throws TorqueException
     */
    public static NotificationRule createDefaultRule(Integer moduleId,
                                            Integer userId,
                                            Integer managerId,
                                            String activityType) throws TorqueException
    {
        NotificationRule rule = new NotificationRule();
        rule.setModuleId(moduleId);
        rule.setUserId(userId);
        rule.setActivityType(activityType);
        rule.setManagerId(managerId);

        // default settings.
        // currently hard coded. will later be
        // replaced by a default customization

        rule.setFilterState(true);   // enabled by default
        rule.setSendSelf(false);     // don't send to myself by default
        rule.setSendFailures(false); // don't notify me about failures by default
        rule.save();
        return rule;
    }
    
}
