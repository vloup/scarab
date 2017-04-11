package org.tigris.scarab.notification;

import java.util.Set;

import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Issue;

/**
 * The Notification Manager is meant to be the central service to provide notifications
 * related with the usual flow of activities in Scarab.
 * It has essentially two functions:
 * <ul>
 * <li>To allow the system to <b>create notifications</b> regarding activity in the issues.</li>
 * <li>To process the notifications so that they arrive to the involved users.</li>
 * </ul>
 * <p>
 * The simpler notification manager implementation might directly send emails to the involved users in
 * the moment the notifications are created.<br/>
 * </p>
 * <p>
 * A slightly improved implementation will be able to:
 * <ul>
 * <li><b>Filter the events</b> to decide if each involved user should be notified or not (basing the decision
 * on user-preferences, globa-preferences, whatever...</li>
 * <li><b>Aggregate pending notifications</b> (probably consolidating on issue and user to be notified) so the
 * user will only get one notification for every bunch of changes done in an issue (at least in a given
 * interval of time)</li>
 * </ul>
 * </p>
 * The real implementation of this interface to be
 * instantiated is defined by scarab.notificationmanager.classname property
 * (defaults to ScarabNotificationManager)<br>
 * This interface also holds the
 * constants defining the different types of events in the system.
 * 
 * @see org.tigris.scarab.notification.NotificationManagerFactory
 * @author jorgeuriarte
 */
public interface NotificationManager
{

    /**
     * This method should add a notification to be processed. It must decide if
     * the activities are relevant to the recipients and filter accordingly.
     * 
     * @see addActivityNotification(NotificationEvent, EmailContext,
     *      ActivitySet, Issue, Set, Set)
     * @param event
     *            The event that originated the notification
     * @param activitySet
     *            The activity set describing the event
     * @param issue
     *            The issue affected by the event
     * @param fromUser TODO
     * @see #addActivityNotification(NotificationEvent, EmailContext, ActivitySet, Issue, Set, Set)
     */
    public void addActivityNotification(ActivityType event,
            ActivitySet activitySet, Issue issue, ScarabUser fromUser);

    /**
     * Long version of the addActivityNotification method, allowing to pass the sets of
     * users involved as 'To' or 'CC'.
     * 
     * @param event
     *            The event that originated the notification
     * @param activitySet
     *            The activity set describing the event
     * @param issue
     *            The issue affected by the event
     * @param toUsers
     *            List of users intended to be notified as 'To:'
     * @param ccUsers
     *            List of users intended to be notified in 'CC:'
     * @param fromUser TODO
     * @see #addActivityNotification(NotificationEvent, ActivitySet, Issue, ScarabUser)
     */
    public void addActivityNotification(ActivityType event,
            ActivitySet activitySet, Issue issue,
            Set toUsers, Set ccUsers, ScarabUser fromUser);
    
    /**
     * Implementations of this method should provide the means to send the
     * pending notifications. Only makes sense when the implementation is
     * 'offline', probably calling regularly to this method.
     */
    public void sendPendingNotifications();
    
    /**
     * Each manager has its own id (Which happens to be
     * a small integer value.
     * @return
     */
    public Integer getManagerId();

    /**
     * Implementations of this method should provide the means to wakeup issues,
     * which are currently in the "onHold state" and have timed out. For such issues
     * the NM should sends out "wakeup notifications" to all observers and assignees.
     */
    public void wakeupOnHoldTimeouts();
    
    /**
     * Implementations of this method should the means to change the Issue state of issues
     * according to a use case specific algorithm. The NM Should perform a state switch
     * according to the use case specific rules.
     */
    public void autocloseNotifications();

    /**
     * Create a new onHoldNotification. This notification will only be sent out AFTER
     * the onHoldDate has expired! So it shall be treated differently from the "normal"
     * notifications.
     * @param activitySet 
     * @param issue
     * @param user
     */
    public void addOnHoldNotification(ActivitySet activitySet, Issue issue, ScarabUser user);

    /**
     * Cancel a previously created onHoldNotification. If no such Notification exists,
     * this method will terminate gracefully (nothing will happen). Otherwise
     * any pending onHoldNotification for that issue will be removed.
     * @param issue
     */
    public void cancelOnHoldNotification(Issue issue);
}
