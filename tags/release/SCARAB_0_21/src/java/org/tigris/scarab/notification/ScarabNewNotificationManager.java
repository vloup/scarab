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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationFilterManager;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.ScarabRuntimeException;

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
            ActivitySet activitySet, Issue issue, ScarabUser fromUser)
    {
        this.addActivityNotification(
                event,
                activitySet,
                issue,
                null,
                null, 
                fromUser);
    }
    
    /**
     * Long version of the addActivityNotification method, allowing to pass the sets of
     * users involved as 'To' or 'CC'.
     */
    public void addActivityNotification(ActivityType event, ActivitySet activitySet, Issue issue,
            Set toUsers, Set ccUsers, ScarabUser fromUser)
    {
        if (log.isDebugEnabled())
            log.debug("addActivityNotification: " + issue.getIdPrefix()
                    + issue.getIssueId() + "-" + event.getCode());
        this.queueNotifications(activitySet, issue, fromUser);
    }
    
    /**
     * Queue the notifications for the passed activity set.
     * 
     * @param activitySet
     */
    private void queueNotifications(ActivitySet activitySet, Issue issue, ScarabUser fromUser)
    {
        try
        {
            NotificationStatus notification = null;
            for (Iterator it = activitySet.getActivityList().iterator(); it.hasNext(); )
            {
                Activity act = (Activity)it.next();
                if (act.getIssue().equals(issue))
                {
                    notification = new NotificationStatus(Email.getArchiveUser(), act);
                    NotificationStatusPeer.doInsert(notification);

                    Set users = issue.getAllUsersToEmail(AttributePeer.EMAIL_TO);
                    users.addAll(issue.getAllUsersToEmail(AttributePeer.CC_TO));
                    users.addAll(activitySet.getRemovedUsers(issue));
                    
                    // FIXME: Should we still make difference between CC & TO? If so...
                    // ...do we need this info in the notification_status table??
                    
                    // FIXME: SCB1439. does the user really have permissions
                    // to view this attribute?
                    
                    Integer moduleId = issue.getModuleId();
                    String activityType = act.getActivityType();

                    for (Iterator itusers = users.iterator(); itusers.hasNext(); )
                    {
                        ScarabUser user     = (ScarabUser)itusers.next();
                        Integer userId      = user.getUserId();

                        boolean isSelf = userId.equals(fromUser.getUserId());
                        boolean wantsNotification = NotificationFilterManager.isNotificationEnabledFor(moduleId, userId, isSelf, activityType);
                        
                        if(wantsNotification)
                        {
                            notification = new NotificationStatus(user, act);
                            NotificationStatusPeer.doInsert(notification);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("queueNotifications(): ",e);
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

        if(pending == null)
        {
            log.warn("sendPendingNotifications(): ...Could not retrieve pending notifications from Database. Try again later.");
            return;
        }
        log.debug("rearrange pending notifications per issue ...");
        Map pendingIssueMap = getPendingIssueMap(pending);

        Map issueActivities                  = new HashMap(); 
        Map archiverActivities               = new HashMap();
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
            archiverActivities.clear();
            creators.clear();
            firstNotification        = null;
            lastNotification         = null;
            Long issueTime           = null;
            String changedStatusAttributeValue = "";
            
            Set notificationSet = (Set)pendingIssueMap.get(issue);

            //Process each Notification for current Issue ...
            for (Iterator it = notificationSet.iterator(); it.hasNext();)
            {
                NotificationStatus currentNotification = (NotificationStatus) it.next();

                ActivityType activityType = currentNotification.getActivityType();
                if(changedStatusAttributeValue.length() == 0 && activityType.equals(ActivityType.ATTRIBUTE_CHANGED))
                {
                    try
                    {
                        Attribute attribute = currentNotification.getActivity().getAttribute();
                        if (getIsStatusAttribute(attribute, issue))
                        {
                            String name = attribute.getName();
                            AttributeValue av = issue.getAttributeValue(name);
                            if(av != null)
                            {
                                changedStatusAttributeValue = av.getValue();
                            }
                        }
                    }
                    catch (TorqueException e)
                    {
                        Log.get().warn("Database acess error while retrieving status attribute value.(ignored)");
                        Log.get().warn("db layer reported: ["+e.getMessage()+"]");
                    }
                }
                
                firstNotification = getOldestNotification(currentNotification, firstNotification);
                lastNotification  = getYoungestNotification(currentNotification, lastNotification);
                try
                {
                    issueId = issue.getUniqueId();
                    Integer receiverId = currentNotification.getReceiverId();
                    ScarabUser receiver = null;
                    if(receiverId.equals(Email.getArchiveUser().getUserId()))
                    {
                        receiver = Email.getArchiveUser();
                    }
                    else
                    {
                        receiver = ScarabUserManager.getInstance(receiverId);                   
                    }
                    creators.add(currentNotification.getCreator());

                    Map userActivities = getActivitiesForUser(issueActivities, receiver);
                    addActivity(currentNotification, userActivities);
                    addActivity(currentNotification, archiverActivities);
                    issueTime = adjustTimeToNewer(issueTime, currentNotification);
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
                Iterator userIterator = getUsersToNotifyIterator(issueActivities);
                while( userIterator.hasNext())
                {
                    ScarabUser user = (ScarabUser) userIterator.next();

                    EmailContext ectx = new EmailContext();
                    ectx.put("issue", issue);
                    ectx.put("link", new ScarabLink());
                    ectx.put("creators", creators);
                    ectx.put("firstNotification", firstNotification);
                    ectx.put("lastNotification", lastNotification);
                    ectx.put("changedStatus",changedStatusAttributeValue);
                    Map groupedActivities = (Map) issueActivities.get(user);
                    if(groupedActivities == null)
                    {
                        groupedActivities = archiverActivities;
                    }
                    addActivitiesToEmailContext(ectx, groupedActivities);
                    
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
                log.debug("Issue " + issueId + ": Is not old enough.");
            }
        }    
        log.debug("sendPendingNotifications(): ...finished!");
    }

    
    /**
     * This method returns true, if the attribute is identified as 
     * the "status_attribute" for the current module/issue_type combination.
     * 
     * NOTE: The "status_attribute" id is searched in SCARAB_GLOBAL_ATTRIBUTE
     * first, although it currently should not find any entry there. In a future
     * release it is intended to allow a more sophisticated controll over what
     * a status attribute is and how it should be rendered e.g. into email subject.
     * 
     * @param attribute
     * @param issue
     * @return
     * @throws TorqueException
     */
    private boolean getIsStatusAttribute(Attribute attribute, Issue issue)
    throws TorqueException
    {
        boolean result=false;
        Module module = issue.getModule();
        String key = "status_attribute_"+attribute.getAttributeId();

        String statusId = GlobalParameterManager.getString(key,module);
        if(!statusId.equals(""))
        {
            result = true; // the attribute IS the status_attribute
        }
        else
        {
            String name = attribute.getName().toLowerCase();
            String globalStatusAttributeName = GlobalParameterManager.getString("scarab.common.status.id").toLowerCase();
            if(name.equals(globalStatusAttributeName))
            {
                result=true;
            }
        }
        return result;
    }
    
    

    /**
     * @param ectx
     * @param groupedActivities
     */
    private void addActivitiesToEmailContext(EmailContext ectx, Map groupedActivities)
    {
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
        ectx.put("ActivityReasons", consolidateActivityReasons(groupedActivities));
    }


    /**
     * @param issueActivities
     * @return
     */
    private Iterator getUsersToNotifyIterator(Map issueActivities)
    {
        return issueActivities.keySet().iterator();
    }

    /**
     * Return the Notification which is the youngest of n1,n2
     * Note: If one of the notificaitons is null, return the other.
     *       If both notificaitons are null, return null
     * @param n1
     * @param n2
     * @return
     */
    private NotificationStatus getYoungestNotification(NotificationStatus n1, NotificationStatus n2)
    {
        if(n1==null) return n2;
        if(n2==null) return n1;
        int compare = compareCreationDates(n1, n2);
        NotificationStatus result = (compare > 0) ? n1:n2;
        return result;
    }

    /**
     * Return a list of strings with the reasons for the activities to be
     * notified.
     * 
     * @return
     */
    private List consolidateActivityReasons(Map activities)
    {
        Set set = new HashSet();
        List list = new ArrayList();
        for (Iterator it = activities.values().iterator(); it.hasNext(); )
        {
            List l = (List)it.next();
            for (Iterator nots = l.iterator(); nots.hasNext(); )
            {
                NotificationStatus ns = (NotificationStatus)nots.next();
                String comment = ns.getComment();
                if (comment != null && !set.contains(comment))
                {
                    set.add(comment);
                    list.add(comment);
                }
            }
        }
        return list;
    }

    /**
     * Return the Notification which is the oldest of n1, n2
     * Note: If one of the notificaitons is null, return the other.
     *       If both notificaitons are null, return null
     * @param n1
     * @param n2
     * @return
     */
    private NotificationStatus getOldestNotification(NotificationStatus n1, NotificationStatus n2)
    {
        if(n1==null) return n2;
        if(n2==null) return n1;
        int compare = compareCreationDates(n1, n2);
        NotificationStatus result = (compare < 0) ? n1:n2;
        return result;
    }

    /*
     * Compares the creation dates of two notifications.
     * returns:
     * -1 : n1.date < n2.date
     *  0 : n1.date == n2.date
     * +1 : n1.date > n2.date
     * 
     * If both entries are null, they are reported as equal (0)
     * If one of the entries is null, its creation date is 
     * assumed to be "older than everything else".
     * Thrws a ScarabRuntimeException when one of the entries
     * has no CreationDate.
     */
    private int compareCreationDates(NotificationStatus n1,
                                     NotificationStatus n2)
    {
        // handle null entries:
        if(n1==n2)   return 0;
        if(n1==null) return -1;
        if(n2==null) return +1;

        int result;
        try
        {
            long n1d = n1.getCreationDate().getTime();
            long n2d = n2.getCreationDate().getTime();

            if (n1d == n2d)
            {
                result = 0;
            }
            else
            {
                result = (n1d > n2d) ? 1 : -1;
            }
        }
        catch (NullPointerException npe)
        {
            L10NMessage msg = new L10NMessage(
                    L10NKeySet.NotificationStatusNoCreationDate);
            log.warn(msg);
            throw new ScarabRuntimeException(msg, npe);
        }

        return result;
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
                {
                    //notif.setStatus(NotificationStatus.SENT);
                    try
                    {
                        NotificationStatusPeer.doDelete(notif);
                    }
                    catch(TorqueException te)
                    {
                        exception = te;
                    }
                }
                
                if(exception != null)
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
    private Map getActivitiesForUser(Map issueActivities, ScarabUser user)
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
    private void addActivity(NotificationStatus notification, Map userActivities)
    {
        LocalizationKey activityGroup = getActivityGroup(notification.getActivityType());
        List typeNotifications = (List) userActivities.get(activityGroup);
        if (null == typeNotifications)
        {
            typeNotifications = new ArrayList();
            userActivities.put(activityGroup, typeNotifications);
        }
        
        // We will only add this notification to the user's list if it's not
        // already present.
        boolean bAlreadyPresent = false;
        for (Iterator it = typeNotifications.iterator(); it.hasNext() && !bAlreadyPresent; )
        {
            NotificationStatus not = (NotificationStatus)it.next();
            bAlreadyPresent = (not.getActivityId().equals(notification.getActivityId()));
        }
        if (!bAlreadyPresent)
        {
            typeNotifications.add(notification);
        }
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
     * Sends email to the user regarding issue's activity.
     * 
     * @param context EmailContext preloaded with info about issue's activity
     * @param issue Issue to be notified about
     * @param user The user to be notified
     * 
     */ 
    private void sendEmail(EmailContext context, Issue issue, ScarabUser user)
            throws Exception
    {
        context.setSubjectTemplate("notification/IssueActivitySubject.vm");
        Set toUsers = new HashSet();
        toUsers.add(user);
        
        String[] fromUser    = getFromUser(issue, context);
        String[] replyToUser = issue.getModule().getSystemEmail();
        
        Email.sendEmail(
                context,
                issue.getModule(),
                fromUser,
                replyToUser,
                toUsers,
                null,
                "notification/IssueActivity.vm");
    }
    
    private String[] getFromUser(Issue issue, EmailContext context) throws TorqueException
    {
        String[] replyToUser = null;
    
        Set creators = (Set)context.get("creators");
        if (creators.size()==1)
        {
            // exactly one contributor to this E-Mail
            boolean exposeSender = Turbine.getConfiguration()
            .getBoolean("scarab.email.replyto.sender",false);

            if(exposeSender)
            {
                ScarabUser creator = (ScarabUser)creators.toArray()[0];
                replyToUser = new String[] { creator.getName(), creator.getEmail() };
            }
        }
        
        if(replyToUser == null)
        {
         replyToUser = issue.getModule().getSystemEmail();
        }
        
        return replyToUser;
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
    private LocalizationKey getActivityGroup(ActivityType activityType)
    {
        L10NKey key = (L10NKey)typeDescriptions.get(activityType.getCode());
        return key;        
    }
}
