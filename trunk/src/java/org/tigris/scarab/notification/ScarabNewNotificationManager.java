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
import org.tigris.scarab.tools.ScarabLocalizationTool;
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
        log.debug("sendPendingNotifications(): Started process...");
        List pending = NotificationStatusPeer.getPendingNotifications();
        Map issueYoungerNotification = new HashMap();
        Map issueUserActivities = new HashMap();
        Set creators = new HashSet();
        NotificationStatus firstNotification = null;
        NotificationStatus lastNotification  = null;
        for (Iterator it = pending.iterator(); it.hasNext();)
        {
            NotificationStatus notification = (NotificationStatus) it.next();
            if (null == firstNotification)
                firstNotification = notification;
            lastNotification = notification;
            /**
             * Only add the notification when it's related to THIS issue (needed
             * for notification related to dependencies or moving, so we don't
             * get duplicated descriptions)
             */
            try
            {
                ScarabUser user = notification.getReceiver();
                creators.add(notification.getCreator());
                Issue issue = IssueManager.getInstance(notification
                        .getIssueId());
                if (notification.getActivity().getIssue().equals(issue))
                {
                    LocalizationKey type = this.getActivityGroup(ActivityType
                            .getActivityType(notification.getActivityType()));
                    Map issueActivities = (Map) issueUserActivities.get(issue);
                    if (null == issueActivities)
                    {
                        issueActivities = new HashMap();
                        issueUserActivities.put(issue, issueActivities);
                    }
                    Map userActivities = (Map) issueActivities.get(user);
                    if (null == userActivities)
                    {
                        userActivities = new HashMap();
                        issueActivities.put(user, userActivities);
                    }
                    List typeNotifications = (List) userActivities.get(type);
                    if (null == typeNotifications)
                    {
                        typeNotifications = new ArrayList();
                        userActivities.put(type, typeNotifications);
                    }
                    typeNotifications.add(notification);

                    /**
                     * Keep the time of the younger notification for every issue
                     */
                    Long issueTime = (Long) issueYoungerNotification
                            .get(notification.getIssueId());
                    long newTime = notification.getCreationDate().getTime();
                    if (issueTime == null)
                    {
                        issueTime = new Long(newTime);
                    }
                    else
                    {
                        if (issueTime.longValue() < newTime)
                            issueTime = new Long(newTime);
                    }
                    issueYoungerNotification.put(notification.getIssueId(),
                            issueTime);
                }
            }
            catch (TorqueException te)
            {
                log.error("sendPendingNotifications(): " + te);
            }
        }
        for (Iterator it = issueUserActivities.keySet().iterator(); it
                .hasNext();)
        {
            Issue issue = (Issue) it.next();
            Long timestamp = (Long) issueYoungerNotification.get(issue
                    .getIssueId());
            if (isOldEnough(timestamp))
            {
                Map issueActivities = (Map) issueUserActivities.get(issue);
                for (Iterator itUsers = issueActivities.keySet().iterator(); itUsers
                        .hasNext();)
                {
                    ScarabUser user = (ScarabUser) itUsers.next();
                    EmailContext ectx = new EmailContext();
                    ectx.put("issue", issue);
                    ectx.put("link", new ScarabLink());
                    ectx.put("creators", creators);
                    ectx.put("firstNotification", firstNotification);
                    ectx.put("lastNotification", lastNotification);
                    ScarabLocalizationTool l10n = new ScarabLocalizationTool();
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
                    boolean bOk;

                    Exception exception = null;
                    try
                    {
                        this.sendEmail(ectx, issue, user);
                        bOk = true;
                    }
                    catch (Exception e)
                    {
                        bOk = false;
                        exception = e;
                    }
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
                            if (bOk)
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
                }
            }
        }
        log.debug("sendPendingNotifications(): ...finished!");
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
    private LocalizationKey getActivityGroup(ActivityType type)
    {
        L10NKey key = (L10NKey)typeDescriptions.get(type.getCode());
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
