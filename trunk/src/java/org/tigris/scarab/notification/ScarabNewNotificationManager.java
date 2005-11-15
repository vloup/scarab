package org.tigris.scarab.notification;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabLink;

/**
 * This class provides the new implementation for the Notification Manager.
 * It will queue the notifications, and then process them consolidating by user and issue, so
 * a user will only get ONE email for issue containing every activity relating this
 * issue since the last notification.
 * <br/>
 * 
 * @author jorgeuriarte
 */
public class ScarabNewNotificationManager extends HttpServlet implements NotificationManager
{

    public static Logger log = Log.get(ScarabNewNotificationManager.class
            .getName());

    private static final Integer NOTIFICATION_MANAGER_ID = new Integer(1);
    public Integer getManagerId()
    {
        return NOTIFICATION_MANAGER_ID;
    }
    
    
    /**
     * Receives an activitySet from which to generate notification. Current
     * implementation does only online email sending, with no aggregation or
     * filtering.
     */
    public void addActivityNotification(ActivityType event,
            ActivitySet activitySet, Issue issue)
    {
        this.addActivityNotification(
                event,
                activitySet,
                issue,
                null,
                null);
    }
    
    /**
     * Long version of the addActivityNotification method, allowing to pass the sets of
     * users involved as 'To' or 'CC'.
     */
    public void addActivityNotification(ActivityType event, ActivitySet activitySet, Issue issue,
            Set toUsers, Set ccUsers)
    {
        if (log.isDebugEnabled())
            log.debug("addActivityNotification: " + issue.getIdPrefix()
                    + issue.getIssueId() + "-" + event.getCode());
        this.queueNotifications(activitySet, issue);
    }
    
    /**
     * Queue the notifications for the passed activity set.
     * 
     * @param activitySet
     */
    private void queueNotifications(ActivitySet activitySet, Issue issue)
    {
        try
        {
            for (Iterator it = activitySet.getActivityList().iterator(); it.hasNext(); )
            {
                Activity act = (Activity)it.next();
                if (act.getIssue().equals(issue))
                {
                    Set users = act.getIssue().getAllUsersToEmail(AttributePeer.EMAIL_TO);
                    users.addAll(act.getIssue().getAllUsersToEmail(AttributePeer.CC_TO));
                    // FIXME: Should we still make difference between CC & TO? If so...
                    // ...do we need this info in the notification_status table??
                    
                    // FIXME: Should we call the ActivityFilter here to discover every user
                    // interested in this issue, beyond those directly assigned to it?
                    
                    // FIXME: SCB1439. does the user really have permissions
                    // to view this attribute?
                    
                    for (Iterator itusers = users.iterator(); itusers.hasNext(); )
                    {
                        ScarabUser user = (ScarabUser)itusers.next();
                        // FIXME: This should add to the real table ;-)
                        NotificationStatus notification = new NotificationStatus(user, act);
                        NotificationStatusPeer.doInsert(notification);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("queueNotifications(): e");
        }

    }

    /**
     * This method process the pending notifications and send them
     * to the users.
     * If the NotificationManager is not activated, this method
     * will not do anything (the mail should have been sent already).
     */
    public void sendPendingNotifications()
    {
        log.debug("sendPendingNotifications(): Collect pending notifications ...");
        List pending = NotificationStatusPeer.getPendingNotifications();

        log.debug("rearrange pending notifications per issue ...");
        Map pendingIssueMap = getPendingIssueMap(pending);

        Map issueActivities                  = new HashMap(); 
        Set creators                         = new HashSet();
        NotificationStatus firstNotification;
        NotificationStatus lastNotification;

        //Process each Issue ...
        Iterator pendingIssuesIterator = pendingIssueMap.keySet().iterator();
        while( pendingIssuesIterator.hasNext())
        {
            Issue issue = (Issue)pendingIssuesIterator.next();
            String issueId = "???";
            // clear volatile data structures ...
            issueActivities.clear();
            creators.clear();
            firstNotification        = null;
            lastNotification         = null;
            Long issueTime           = null;
            
            Set notificationSet = (Set)pendingIssueMap.get(issue);

            //Process each Notification for current Issue ...
            for (Iterator it = notificationSet.iterator(); it.hasNext();)
            {
                NotificationStatus notification = (NotificationStatus) it.next();
                if (null == firstNotification)
                {
                    firstNotification = notification;
                }
                if(!it.hasNext())
                {
                    lastNotification = notification;
                }

                try
                {
                    issueId = issue.getUniqueId();
                    ScarabUser receiver = notification.getReceiver();
                    creators.add(notification.getCreator());
                   
                    Map userNotifications = getNotificationsForUser(issueActivities, receiver);
                    addNotification(notification, userNotifications);

                    issueTime = adjustTimeToNewer(issueTime, notification);
                }
                catch (TorqueException te)
                {
                    log.error("sendPendingNotifications(): " + te);
                }
            }
            
            /*
             * Now we got all notifications for current issue sorted by receivers
             * and collected in issueActivities. We now can iterate throug the 
             * issueActivities and send one E-Mail per receiver for this issue: 
             */
            
            if (isOldEnough(issueTime))
            {
                log.debug("processing notifications for issue : ["+issueId+"]");
                Iterator userIterator = issueActivities.keySet().iterator();
                while( userIterator.hasNext())
                {
                    ScarabUser user = (ScarabUser) userIterator.next();

                    // Prepare E-Mail context ...
                    EmailContext ectx = new EmailContext();
                    ectx.put("issue", issue);
                    ectx.put("link", new ScarabLink());
                    ectx.put("creators", creators);
                    ectx.put("firstNotification", firstNotification);
                    ectx.put("lastNotification", lastNotification);
                    
                    Map groupedActivities = (Map) issueActivities.get(user);
                    ectx.put("ActivityIssue", groupedActivities
                            .get(L10NKeySet.ActivityIssue));
                    ectx.put("ActivityAttributeChanges", groupedActivities
                            .get(L10NKeySet.ActivityAttributeChanges));
                    ectx.put("ActivityPersonnelChanges", groupedActivities
                            .get(L10NKeySet.ActivityPersonnelChanges));
                    ectx.put("ActivityComments", groupedActivities
                            .get(L10NKeySet.ActivityComments));
                    ectx.put("ActivityAssociatedInfo", groupedActivities
                            .get(L10NKeySet.ActivityAssociatedInfo));
                    ectx.put("ActivityDependencies", groupedActivities
                            .get(L10NKeySet.ActivityDependencies));

                    Exception exception = null;
                    try
                    {
                        this.sendEmail(ectx, issue, user);
                    }
                    catch (Exception e)
                    {
                        exception = e;
                    }

                    updateNotificationRepository(groupedActivities, exception);
                }
            }
            else
            {
                try
                {
                    log.debug("Issue " + issue.getUniqueId()
                            + ": Is not old enough.");
                }
                catch (TorqueException e)
                {
                    // in case an error ocurs, we can't do anything here,
                    // because the logging submodule itself is broken.
                    // Hence give up without any further warning.
                }
            }
        }
        log.debug("sendPendingNotifications(): ...finished!");
    }


    /**
     * Update the Notification status in the database. If exception is
     * supplied, this method assumes, an error has occured and sets
     * he status to DEFERRED. Otherwise the E-Mail is considered to be
     * delivered with success and the Notification status is set to SENT.
     * This is done for ALL activities beeing reported to this user in this
     * issue.
     * @param groupedActivities
     * @param exception
     */
    private void updateNotificationRepository(Map groupedActivities, Exception exception)
    {
        /**
         * Update the notifications' status with the result of the
         * email sending
         */
        for (Iterator confirm = groupedActivities.values()
                .iterator(); confirm.hasNext();)
        {
            List notifications = (List) confirm.next();
            for (Iterator n = notifications.iterator(); n.hasNext();)
            {
                NotificationStatus notif = (NotificationStatus) n
                        .next();
                if (exception == null)
                    notif.setStatus(NotificationStatus.SENT);
                else
                {
                    notif.setStatus(NotificationStatus.DEFERRED);
                    notif.setComment(exception.getMessage());
                }
                try
                {
                    notif.save();
                }
                catch (Exception e)
                {
                    log.error("sendPendingNotifications(): Updating: " + e);
                }
            }
        }
    }


    /**
     * @param notificationTime
     * @param notification
     */
    private Long adjustTimeToNewer(Long notificationTime, NotificationStatus notification)
    {
        /**
         * Keep the time of the younger notification for every issue
         */
        long newTime = notification.getCreationDate().getTime();
        if (notificationTime == null)
        {
            notificationTime = new Long(newTime);
        }
        else
        {
            if (notificationTime.longValue() < newTime)
            {
                notificationTime = new Long(newTime);
            }
            else
            {
                // keep current notificationTime;
            }
        }
        return notificationTime;
    }


    /**
     * @param issueActivities
     * @param user
     * @return
     */
    private Map getNotificationsForUser(Map issueActivities, ScarabUser user)
    {
        Map userActivities = (Map) issueActivities.get(user);
        if (null == userActivities)
        {
            userActivities = new HashMap();
            issueActivities.put(user, userActivities);
        }
        return userActivities;
    }


    /**
     * @param notification
     * @param userActivities
     */
    private void addNotification(NotificationStatus notification, Map userActivities)
    {
        LocalizationKey activityGroup = getActivityGroup(notification.getActivityType());
        List typeNotifications = (List) userActivities.get(activityGroup);
        if (null == typeNotifications)
        {
            typeNotifications = new ArrayList();
            userActivities.put(activityGroup, typeNotifications);
        }
        typeNotifications.add(notification);
    }


    /**
     * @param pending
     */
    private Map getPendingIssueMap(List pending)
    {
        Map issueMap = new HashMap();
        for (Iterator it = pending.iterator(); it.hasNext();)
        {
            NotificationStatus notification = (NotificationStatus) it.next();
            Issue issue = null;
            try
            {
                issue = IssueManager.getInstance(notification.getIssueId());

                /**
                 * Only add the notification when it's related to THIS issue (needed
                 * for notification related to dependencies or moving, so we don't
                 * get duplicated descriptions)
                 */

                if (notification.getActivity().getIssue().equals(issue))
                {

                    Set notificationSet = (Set)issueMap.get(issue);
                    if(notificationSet == null)
                    {
                        notificationSet = new HashSet();
                        issueMap.put(issue,notificationSet);
                    }
                    notificationSet.add(notification);
                }
            }
            catch (TorqueException te)
            {
                log.error("sendPendingNotifications(): " + te);
                continue;
            }
        }
        return issueMap;
    }    
    

    /**
     * Will return 'true' if the time since the passed timestamp is at least
     * the minimal quiet time configured for issues.
     * 
     * @param timestamp
     * @return
     */
    private boolean isOldEnough(Long timestamp)
    {
        boolean bRdo = true;
        long lTimestamp = timestamp.longValue();
        long minimalAge = Turbine.getConfiguration().getLong("scarab.notificationmanager.issuequiettime", 0);
        if ((new Date().getTime() - lTimestamp) < minimalAge)
            bRdo = false;
        return bRdo;
    }
    
    /**
     * Sends email to the users associated with the issue. That is associated
     * with this activitySet. If no subject and template specified, assume
     * modify issue action. throws Exception
     * 
     * @param activityDesc
     *            Set containing the different descriptions of this activityset
     * @param attachment
     *            Attachment of the activityset (if present)
     * @param context
     *            Any contextual information for the message.
     * @param issue
     *            The issue
     * @param creator
     *            The user originating the notification event
     * @param toUsers
     * @param ccUsers
     * @param template
     *            The name of the velocity template containing the mail text
     */
    private void sendEmail(EmailContext context, Issue issue, ScarabUser user)
            throws Exception
    {
        context.setSubjectTemplate("notification/IssueActivitySubject.vm");
        Set toUsers = new HashSet();
        toUsers.add(user);
        String[] replyToUser = issue.getModule().getSystemEmail();
        Email.sendEmail(
                context,
                issue.getModule(),
                replyToUser,
                replyToUser,
                toUsers,
                null,
                "notification/IssueActivity.vm");
    }

    private static Map typeDescriptions = new HashMap();
    
    /*
     * ActivityTypes are grouped for description purposes
     */
    static
    {
        typeDescriptions.put(ActivityType.ISSUE_CREATED.getCode(), L10NKeySet.ActivityIssue);
        typeDescriptions.put(ActivityType.ISSUE_MOVED.getCode(), L10NKeySet.ActivityIssue);
        typeDescriptions.put(ActivityType.ISSUE_COPIED.getCode(), L10NKeySet.ActivityIssue);
        typeDescriptions.put(ActivityType.ATTRIBUTE_CHANGED.getCode(), L10NKeySet.ActivityAttributeChanges);
        typeDescriptions.put(ActivityType.USER_ATTRIBUTE_CHANGED.getCode(), L10NKeySet.ActivityPersonnelChanges);
        typeDescriptions.put(ActivityType.COMMENT_ADDED.getCode(), L10NKeySet.ActivityComments);
        typeDescriptions.put(ActivityType.COMMENT_CHANGED.getCode(), L10NKeySet.ActivityComments);
        typeDescriptions.put(ActivityType.URL_ADDED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.URL_CHANGED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.URL_DESC_CHANGED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.URL_DELETED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.ATTACHMENT_CREATED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.ATTACHMENT_REMOVED.getCode(), L10NKeySet.ActivityAssociatedInfo);
        typeDescriptions.put(ActivityType.DEPENDENCY_CREATED.getCode(), L10NKeySet.ActivityDependencies);
        typeDescriptions.put(ActivityType.DEPENDENCY_CHANGED.getCode(), L10NKeySet.ActivityDependencies);
        typeDescriptions.put(ActivityType.DEPENDENCY_DELETED.getCode(), L10NKeySet.ActivityDependencies);
    }    
    /**
     * Returns the group to which the activity type belongs (for organizational purposes)
     * 
     * @param type The type for which we want to get the corresponding group's name
     * @return
     */
    private LocalizationKey getActivityGroup(String activityCode)
    {
        ActivityType activityType = ActivityType.getActivityType(activityCode);
        L10NKey key = (L10NKey)typeDescriptions.get(activityType.getCode());
        return key;        
    }
        
    /**
     * TODO: Remove this method as soon as Quartz is setup!!
     */
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
    {
        this.sendPendingNotifications();
    }
}
