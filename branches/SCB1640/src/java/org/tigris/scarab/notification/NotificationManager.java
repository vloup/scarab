package org.tigris.scarab.notification;

import java.util.Set;

import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.EmailContext;

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
     * the activities are relevant to the recipients and filter acordingly.
     * 
     * @see addActivityNotification(NotificationEvent, EmailContext,
     *      ActivitySet, Issue, Set, Set)
     * @param event
     *            The event that originated the notification
     * @param activitySet
     *            The activity set describing the event
     * @param issue
     *            The issue affected by the event
     * @see #addActivityNotification(NotificationEvent, EmailContext, ActivitySet, Issue, Set, Set)
     */
    public void addActivityNotification(ActivityType event,
            ActivitySet activitySet, Issue issue);

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
     * @see #addActivityNotification(NotificationEvent, ActivitySet, Issue)
     */
    public void addActivityNotification(ActivityType event,
            ActivitySet activitySet, Issue issue,
            Set toUsers, Set ccUsers);
    
    /**
     * Implementations of this method should provide the means to send the
     * pending notifications. Only makes sense when the implementation is
     * 'offline', probably calling regularly to this method.
     */
    public void sendPendingNotifications();

}
