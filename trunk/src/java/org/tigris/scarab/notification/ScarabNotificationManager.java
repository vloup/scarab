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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.tigris.scarab.om.ActivityManager;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetType;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationRuleManager;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabLink;

/**
 * This class provides the default implementation for the Notification Manager.
 * It will queue the notifications, and then process them consolidating by user and issue, so
 * a user will only get ONE email for issue containing every activity relating this
 * issue since the last notification. The time after which a notification is sent out
 * to the user(s) is calculated from the time when the last modification has been applied
 * plus the scarab.notificationmanager.issuequiettime (customizable via build/runtime properties)
 * <br/>
 * 
 * @authors jorgeuriarte, hdab
 */
public class ScarabNotificationManager extends HttpServlet implements NotificationManager
{

    public static Logger log = Log.get(ScarabNotificationManager.class
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
            for (Iterator<Activity> it = activitySet.getActivityList().iterator(); it.hasNext(); )
            {
                Activity act = (Activity)it.next();
                if (act.getIssue().equals(issue))
                {
                    try
                    {
                        notification = new NotificationStatus(Email.getArchiveUser(), act);
                        NotificationStatusPeer.doInsert(notification);
    
                        Module module = issue.getModule();
                        Set<ScarabUser> users = issue.getAllUsersToEmail(AttributePeer.EMAIL_TO);
                        users.addAll(issue.getAllUsersToEmail(AttributePeer.CC_TO));
                        users.addAll(activitySet.getRemovedUsers(issue));
    
                        // Add all ScarabUsers defined in the module's ArchiveEmail string
                        // Note 1: Only those entries will be taken into account, which can
                        //         be identified as valid and existing Scarab users in the local
                        //         repository.
                        // Note 2: the users notification settings apply here!
                        // Note 3: All foreign EmailAddresses stored in the module's ArchiveEmail
                        //         will be ignored here and later added without any constraints
                        //         during actual sending of the EMail!
                        users.addAll(module.getArchivingScarabUsers());
    
                        // FIXME: Should we still make difference between CC & TO? If so...
                        // ...do we need this info in the notification_status table??
    
                        // FIXME: SCB1439. does the user really have permissions
                        // to view this attribute?
    
                        String activityType = act.getActivityType();
    
                        for (Iterator<ScarabUser> itusers = users.iterator(); itusers.hasNext(); )
                        {
                            ScarabUser user     = (ScarabUser)itusers.next();
                            Integer userId      = user.getUserId();
    
                            boolean isSelf = userId.equals(fromUser.getUserId());
                            boolean wantsNotification = NotificationRuleManager.isNotificationEnabledFor(user, issue, isSelf, activityType);
                            
                            if(wantsNotification)
                            {
                                notification = new NotificationStatus(user, act);
                                NotificationStatusPeer.doInsert(notification);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("queueNotifications(): while processing activity queue: " + e.getMessage(),e);
                        logNotificationData(notification);
                        log.error("queueNotifications(): Abort this run");
                        break;
                    }
                    
                }
            }
        }
        catch (Exception e)
        {
            log.error("queueNotifications(): while setting up the activity queue: " + e.getMessage(),e);
        }
    }


    private void logNotificationData(NotificationStatus notification) throws TorqueException
    {
        try
        {
            log.error("queueNotifications():comment:"      + notification.getComment());
            log.error("queueNotifications():changeDate:"   + notification.getChangeDate());
            log.error("queueNotifications():creationDate:" + notification.getCreationDate());
            log.error("queueNotifications():creator:"      + notification.getCreator().toString());
            log.error("queueNotifications():issueId:"      + notification.getIssueId());
            log.error("queueNotifications():primaryKey:"   + notification.getPrimaryKey().toString());
            log.error("queueNotifications():queryKey:"     + notification.getQueryKey());
            log.error("queueNotifications():receiver:"     + notification.getReceiver().toString());
            log.error("queueNotifications():status:"       + notification.getStatusLabel());
        }
        catch(Exception e)
        {
            log.error("queueNotifications(): while dumping notificationData: " + e.getMessage(),e);
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
        ScarabCache.clear();
        log.debug("sendPendingNotifications(): Collect pending notifications ...");
        // It is now guaranteed, that the notifications arrive in order of CreationDate!
        List<Notification> pending = NotificationStatusPeer.getPendingNotifications();

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

        //Process each Issue ...
        Iterator pendingIssuesIterator = pendingIssueMap.keySet().iterator();
        int pendingIssueCount = pendingIssueMap.size();
        int processedIssueCount = 0;
        while( pendingIssuesIterator.hasNext())
        {
            Issue issue = (Issue)pendingIssuesIterator.next();
            
            String issueId;
            try
            {
                issueId = issue.getUniqueId();
            }
            catch (TorqueException te)
            {
                log.error("sendPendingNotifications(): No access to Issue [" + te + "]");
                // Can not proceed with this issue !
                continue;
            }
                

            // clear volatile data structures ...
            issueActivities.clear();
            archiverActivities.clear();
            creators.clear();
            
            NotificationStatus firstNotification        = null;
            NotificationStatus lastNotification         = null;
            NotificationStatus mostRelevantNotification = null;
            
            List notificationList = (List)pendingIssueMap.get(issue);

            //Process each Notification for current Issue ...
            for (Iterator it = notificationList.iterator(); it.hasNext();)
            {
                NotificationStatus currentNotification = (NotificationStatus) it.next();
                if(firstNotification == null)
                {
                    firstNotification = currentNotification;
                }
                if(!it.hasNext())
                {
                    lastNotification = currentNotification;
                }

                mostRelevantNotification = getMostRelevantNotification(currentNotification, mostRelevantNotification, issue);
                try
                {
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
                }
                catch (TorqueException te)
                {
                    log.error("sendPendingNotifications(): No access to current Scarab User" + te);
                    // We can continue processing here. We just don't know how to process Emails
                    // for the current user.
                }
            }
            
            /*
             * Now we got all notifications for current issue sorted by receivers
             * and collected in issueActivities. We now can iterate throug the 
             * issueActivities and send one E-Mail per receiver for this issue: 
             */
            
            Long issueTime = lastNotification.getCreationDate().getTime();
            if (isOldEnough(issueTime))
            {
                processedIssueCount += 1;
                
                // ===========================================================
                // Determine the changeKey (l10n) for the most relevant notification 
                // The resolved l10n key will appear in the subject line of the email!
                // Note: If the key can not be resolved, the key itself will be 
                // used as replacement without further notification! (to be changed in the future)
                // ===========================================================
                LocalizationKey changeKey;
                if (isStatusNotification(mostRelevantNotification, issue))
                {
                    changeKey = getStatusKey(mostRelevantNotification, issue);
                }
                else
                {
                    changeKey = getNotificationKey(mostRelevantNotification, issue);
                }
                
                log.debug("processing notifications for issue : ["+issueId+"]");
                Iterator userIterator = getUsersToNotifyIterator(issueActivities);
                while( userIterator.hasNext())
                {
                    ScarabUser user = (ScarabUser) userIterator.next();

                    EmailContext ectx = new EmailContext();
                    ectx.setIssue(issue);
                    ectx.setLinkTool(new ScarabLink());
                    ectx.put("creators", creators);
                    ectx.put("firstNotification", firstNotification);
                    ectx.put("lastNotification", lastNotification);                    
                    ectx.put("changeKey",changeKey);

                    ectx.put("cr", "\n"); // for email template to get a reliable Carriage return

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
                        log.error("Failed to send email :" + e);
                    }

                    updateNotificationRepository(groupedActivities, exception);
                }
                                
            }
            else
            {
                log.debug("Issue " + issueId + ": Is not old enough.");
            }
        }
        if(pendingIssueCount > 0)
        {
            log.info("sendPendingNotifications(): processed " + processedIssueCount + " of " + pendingIssueCount + " pending issues.");
        }
        else
        {
            log.debug("sendPendingNotifications(): nothing todo.");
        }
    }

    /**
     * Implementations of this method should provide the means to wakeup issues,
     * which are currently in the "onHold state" and have timed out. Such issues
     * shall be moved to the configured "processing state"
     */
    public void wakeupOnHoldTimeouts() 
    {
        ScarabCache.clear();
        log.debug("wakeupOnHoldTimeouts() : Collect onHold notifications ...");
        // It is now guaranteed, that the notifications arrive in order of CreationDate!
        List pending = NotificationStatusPeer.getOnholdNotifications();

        if(pending == null)
        {
            log.warn("sendPendingNotifications(): ...Could not retrieve pending notifications from Database. Try again later.");
            return;
        }
        
        Iterator<NotificationStatus> iter = pending.iterator();
        Calendar now = new GregorianCalendar();
        try
        {
            while(iter.hasNext())
            {
                NotificationStatus ns = iter.next();
                Activity activity = ns.getActivity();
                Calendar endDate = new GregorianCalendar();
                endDate.setTime(activity.getEndDate()); // that is the onHoldExpiration date
                if (endDate.before(now)) // onHold expired
                {
                    Issue issue = activity.getIssue();
                    // onHoldTimeout reached.
                    boolean notificationNeeded = false;
                    
                    Calendar changeDate = new GregorianCalendar();
                    Date lastChangeAt = ns.getChangeDate();
                    if(lastChangeAt == null)
                    {
                        // first reminder, set the change date and mark notification needed.
                        ns.setChangeDate(now.getTime());
                        ns.save();
                        notificationNeeded = true;
                    }
                    else
                    {
                        int reminderPeriod = issue.getReminderPeriod();
                        if(reminderPeriod > 0)
                        {
                            // Only send reminders if the reminderPeriod is set to a 
                            // positive value
                            // note: The reminderPeriod is declared in system property
                            // 
                            // "scarab.common.status.onhold.reminder.period"
                            // 
                            Calendar gc = new GregorianCalendar();
                            gc.setTime(lastChangeAt);
                            gc.add(Calendar.MINUTE, reminderPeriod); // look if we are reminderPeriod minutes after last change date.
                            if(gc.before(now))
                            {
                                // wait period expired. mark for resending notification and update changeDate.
                                ns.setChangeDate(now.getTime());
                                ns.save();
                                notificationNeeded = true;
                            }
                        }
                    }
                    
                    if(notificationNeeded)
                    {
                        
                        // Create a set of notifications to all observers.
                        // Note: This hap[pens as long as the onHoldNotification remains in the database.
                        // The only way to stop reminder notifications is to change the issue state to 
                        // something different from the "onHoldState"!
                        
                        ActivitySet activitySet = activity.getActivitySet();
                        Integer userId          = activitySet.getCreatedBy();
                        ScarabUser user         = ScarabUserManager.getInstance(userId);
                        createWakeupNotification(issue, user, "WakeupFromOnHoldstate");
                    }
                }
            }
        }
        catch(TorqueException te)
        {
            Log.get().warn("Can not access Database while processing wakeupOnHoldTimeouts");
        }
        
    }
    
    private void createWakeupNotification(Issue issue, ScarabUser user, String wakeupMessage) throws TorqueException
    {
        Date date;
        try {
            date = issue.getOnHoldUntil();
        } catch (Exception e) 
        {
            throw new RuntimeException("Can not retrieve the onHoldUntil date from the current issue");
        }
            Activity activity = ActivityManager.getInstance();
            Attribute attribute = issue.getMyStatusAttribute();

            activity.setAttribute(attribute);
            activity.setActivityType(ActivityType.ISSUE_ONHOLD.getCode());
            activity.setDescription("WakeupFromOnHoldstate");
            activity.setIssue(issue);
            activity.setEndDate(date);
            activity.setNewValue("");
            activity.setOldValue("");
            activity.setOldOptionId(0);
            activity.setNewOptionId(0);
            
            Attachment attachment = AttachmentManager.getInstance();
            attachment.setTextFields(user, issue,Attachment.COMMENT__PK);
            attachment.setData(wakeupMessage);
            attachment.setName("comment");
            attachment.save();
            
            activity.setAttachment(attachment);
            
            Integer tt = ActivitySetTypePeer.EDIT_ISSUE__PK;
            ActivitySet activitySet;
            try {
                activitySet = ActivitySetManager.getInstance(tt, user);
                activitySet.addActivity(activity);
                activitySet.save();
                
                addActivityNotification(ActivityType.ISSUE_REMINDER, activitySet, issue, user);                
            } catch (ScarabException e) {
                throw new RuntimeException(e);
            }
    }
    
    
    
    /**
     * Create an activity and a Notification. Add the structures
     * to the persistent storage. Note: The Notification will never be
     * sent out. But after the expiration date has been reached, a set
     * of "expire" notifications will be created instead once per day
     * until the issue state is changed. See wakeupOnHoldTimeouts() and
     * createWakeupActivity() above for further information. 
     * 
     * Important:The expiration end date is stored in the activity.endDate!
     */
    public void addOnHoldNotification(ActivitySet activitySet, Issue issue, ScarabUser user)
    {
        Date date;
        try {
            date = issue.getOnHoldUntil();
            Activity activity = ActivityManager.getInstance();
            Attribute attribute = issue.getMyStatusAttribute();
            activity.setAttribute(attribute);
            activity.setActivityType(ActivityType.ISSUE_ONHOLD.getCode());
            activity.setIssue(issue);
            activity.setEndDate(date);
            activity.setNewValue("GeneratedOnHoldState");
            activity.setOldValue("");
            activity.setOldOptionId(0);
            activity.setNewOptionId(0);
            activity.setDescription("GeneratedOnHoldState");
            activitySet.addActivity(activity);
            activitySet.save();
            NotificationStatus notification = new NotificationStatus(user, activity);
            notification.setStatus(NotificationStatus.ON_HOLD);
            NotificationStatusPeer.doInsert(notification);
        } catch (Exception e) 
        {
            throw new RuntimeException("Can not retrieve the onHoldUntil date from the current issue");
        }
        
    }
    
    /**
     * Cancel a previously created onHoldNotification. If no such Notification exists,
     * this method will terminate gracefully (nothing will happen). Otherwise
     * any pending onHoldNotification for that issue will be removed.
     * @param issue
     */
    public void cancelOnHoldNotification(Issue issue)
    {
        ScarabCache.clear();
        log.debug("cancelOnHoldTimeouts() : Collect onHold notifications ...");
        // It is now guaranteed, that the notifications arrive in order of CreationDate!
        List<NotificationStatus> pending = NotificationStatusPeer.getOnholdNotifications();

        if(pending == null)
        {
            log.error("cancelOnHoldNotification(): Could not retrieve pending notifications for Issue ["+issue.getIdCount()+"]. Maybe something wrong with the issue state ?");
            return;
        }
        
        Iterator<NotificationStatus> iter = pending.iterator();
        Date now = new Date();
        try
        {
            while(iter.hasNext())
            {
                NotificationStatus ns = iter.next();
                Activity activity = ns.getActivity();
                Issue activityIssue = activity.getIssue();
                if(issue.equals(activityIssue))
                {
                    ns.delete();
                }
            }
        }
        catch(TorqueException te)
        {
            Log.get().warn("Can not access Database while processing wakeupOnHoldTimeouts");
        }
        
    }
    
    /**
     * Return the L10NKey associated to the Attribute change.
     * @param notification
     * @param issue
     * @return
     */
    private LocalizationKey getNotificationKey(NotificationStatus notification, Issue issue) 
    {
        LocalizationKey result = null;
        ActivityType activityType = notification.getActivityType();
        result = activityType.getNotificationKey();
        return result;
    }
    
    /**
     * Return the L10NKey representation of the status value.
     * Note: This method strongly assumes, that the given notification
     * contains an Activity of type ATTRIBUTE_CHANGED and the contained
     * attribute is expected to be a status-attribute. This method throws
     * an exception, if these constraints are not fulfilled!
     * In case of success, the method returns an L10N key. This key is currently
     * not expected to be backed by an l10n resource. Please consider this
     * as a preparation for a future enhancement, where it will become possible
     * to define localized attribute values. For now the key is a verbatim copy of the
     * attribute value.
     * @param notification
     * @param issue
     * @return
     */
    private LocalizationKey getStatusKey(NotificationStatus notification, Issue issue) 
    {
        LocalizationKey result = null;
        try
        {
            Attribute attribute = notification.getActivity().getAttribute();
            if (attribute == null || !getIsStatusAttribute(attribute, issue))
            {
                throw new IllegalArgumentException("Expected a notification containing a status attribute.");
            }

            String name = attribute.getName();
            AttributeValue av = issue.getAttributeValue(name);
            if(av != null)
            {
                result = new L10NKey(av.getValue()); // interpret the value as a L10NKey (for future use!)
            }
            else
            {
                throw new IllegalArgumentException("Received a notification containing a status attribute without a given value.");
            }
        }
        catch (TorqueException e)
        {
            Log.get().warn("Database acess error while retrieving status attribute value.(ignored)");
            Log.get().warn("db layer reported: ["+e.getMessage()+"]");
        }
        
        return result;
    }
    
    

     

    private NotificationStatus getMostRelevantNotification(
            NotificationStatus currentNotification,
            NotificationStatus mostRelevantNotification,
            Issue issue)
    {
        ActivityType currentActivityType      = currentNotification.getActivityType();

        // =====================================================================
        // Check if the Issue status has changed. This is of highest relevance.
        // =====================================================================
        if(currentActivityType.equals(ActivityType.ATTRIBUTE_CHANGED))
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
                        return currentNotification; // that is the most relevant notification!
                    }
                }
            }
            catch (TorqueException e)
            {
                Log.get().warn("Database acess error while retrieving status attribute value.(ignored)");
                Log.get().warn("db layer reported: ["+e.getMessage()+"]");
            }
        }
        
        if(mostRelevantNotification == null)
        {
            mostRelevantNotification = currentNotification;
        }
        else
        {
            if( !isStatusNotification(mostRelevantNotification, issue))
            {
                ActivityType mostRelevantActivityType = mostRelevantNotification.getActivityType();
                if  (  currentActivityType.getPriority() >= mostRelevantActivityType.getPriority() )
                {
                    mostRelevantNotification = currentNotification;
                }
            }
        }
        return mostRelevantNotification;
    }
        
    /**
     * Tell if the notification contains a status-attribute.
     * @param notification
     * @param issue
     * @return
     */
    private boolean isStatusNotification(NotificationStatus notification, Issue issue) 
    {
        boolean result = false;
        {
            ActivityType activityType = notification.getActivityType();
            if(activityType.equals(ActivityType.ATTRIBUTE_CHANGED))
            {
                try
                {
                    Attribute attribute = notification.getActivity().getAttribute();
                    if (getIsStatusAttribute(attribute, issue))
                    {
                        String name = attribute.getName();
                        AttributeValue av = issue.getAttributeValue(name);
                        if(av != null)
                        {
                            result = true;
                        }
                    }
                }
                catch (TorqueException e)
                {
                    Log.get().warn("Database acess error while retrieving status attribute value.(ignored)");
                    Log.get().warn("db layer reported: ["+e.getMessage()+"]");
                }
            }
        }
        return result;
    }




    /**
     * This method returns true, if the attribute is identified as 
     * the "status_attribute" for the given issue.
     * 
     * @param attribute
     * @param issue
     * @return
     * @throws TorqueException
     */
    private boolean getIsStatusAttribute(Attribute attribute, Issue issue)
    throws TorqueException
    {
        Attribute statusAttribute = issue.getMyStatusAttribute();
        if(statusAttribute == null)
        {
            return false;
        }
        boolean result = statusAttribute.equals(attribute);
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
                    log.error("sendPendingNotifications(): Updating: " + e, e);
                }
            }
        }
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

                    List notificationList = (List)issueMap.get(issue);
                    if(notificationList == null)
                    {
                        notificationList = new ArrayList();
                        issueMap.put(issue,notificationList);
                    }
                    notificationList.add(notification);
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
        context.setDefaultTextKey(issue.getDefaultText());
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
        typeDescriptions.put(ActivityType.ISSUE_ONHOLD.getCode(),       L10NKeySet.ActivityComments);
        typeDescriptions.put(ActivityType.ISSUE_REMINDER.getCode(),     L10NKeySet.ActivityComments);
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
