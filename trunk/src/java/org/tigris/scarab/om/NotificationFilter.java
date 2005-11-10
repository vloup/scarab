
package org.tigris.scarab.om;


import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.tigris.scarab.notification.ActivityType;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class NotificationFilter
    extends org.tigris.scarab.om.BaseNotificationFilter
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
    public static NotificationFilter createDefaultFilter(Integer moduleId,
                                            Integer userId,
                                            Integer managerId,
                                            ActivityType activityType) throws TorqueException
    {
        NotificationFilter filter = new NotificationFilter();
        filter.setModuleId(moduleId);
        filter.setUserId(userId);
        filter.setActivityType(activityType.getCode());
        filter.setManagerId(managerId);

        // default settings.
        // currently hard coded. will later be
        // replaced by a default customization

        filter.setFilterState(true);   // enabled by default
        filter.setSendSelf(false);     // dont send to myself by default
        filter.setSendFailures(false); // dont notify me aout failures by default
        
        return filter;
    }
}
