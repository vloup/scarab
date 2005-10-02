package org.tigris.scarab.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;

/**
 * This class provides the default implementation for the provided notification
 * manager. It just send the notifications by email in the moment they are added.
 * 
 * @author jorgeuriarte
 */
public class ScarabNotificationManager implements NotificationManager
{
    public static Logger log = Log.get(ScarabNotificationManager.class
            .getName());

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
                    + issue.getIssueId() + "-" + event);
        
        Attachment activityAttch = null;
        Set changes = null;
        try
        {
            activityAttch = activitySet.getAttachment();
            List activityList = activitySet.getActivityList();
            changes = new HashSet(activityList.size());
            for (Iterator itr = activityList.iterator(); itr.hasNext();)
            {
                Activity activity = (Activity) itr.next();
                if (activity.getIssue().equals(issue))
                {
                    changes.add(activity);
                }
            }
        }
        catch (Exception e)
        {
            log.error("addActivityNotification: Error accessing activityset: "
                    + e);
        }
        EmailContext ectx = new EmailContext();
        ectx.setIssue(issue);
        ectx.put("attachment", activityAttch);
        ectx.put("uniqueActivityDescriptions", changes);

        String template = configureEmailContext(ectx, event, activitySet, issue);

        try
        {
            // FIXME: This should really be 'queued', delaying the sending
            // untill sendPendingNotifications is called
            this.sendEmail(changes, activityAttch, ectx, issue, activitySet
                    .getCreator(), toUsers, ccUsers, template);
        }
        catch (Exception e)
        {
            log.error("addNotification: " + e);
        }
    }
    
    /**
     * Does nothing, because this implementation currently send the
     * notifications online in the moment they are generated calling
     * addActivityNotification methods.
     */
    public void sendPendingNotifications()
    {
        log.debug("sendPendingNotifications(): No implementation required.");
    }

    /**
     * Decides what templates to use for email, and set needed values in the
     * context, depending on the event that originated the notification.
     * 
     * @param event
     *            The NotificationEvent originated
     * @param ectx
     *            Returned initialized/updated by reference
     * @return The name of the email template to be used
     */
    private String configureEmailContext(EmailContext ectx, ActivityType event,
            ActivitySet acttivitySet, Issue issue)
    {
        String template = null;

        // Select appropiate template depending on the event
        if (event == ActivityType.USER_ATTRIBUTE_CHANGED)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.assignissue.template",
                    "ModifyIssue.vm");

            ectx.setSubjectTemplate("AssignIssueModifyIssueSubject.vm");
        }
        else if (event == ActivityType.ISSUE_CREATED)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.reportissue.template",
                    "NewIssueNotification.vm");
        }
        else if (event == ActivityType.ISSUE_MOVED || event == ActivityType.ISSUE_COPIED)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.moveissue.template",
                    "MoveIssue.vm");
            ectx.setDefaultTextKey("MovedIssueEmailSubject");

            // placed in the context for the email to be able to access them
            // from within the email template
            try
            {
                Issue newIssue = ((Activity)acttivitySet.getActivitys().get(0)).getIssue();
                ectx.put("issue", newIssue);
                ectx.put("module", newIssue.getModule());
                ectx.setModule(newIssue.getModule());
                Attachment reason = acttivitySet.getAttachment();
                ectx.put("reason", (reason == null)?"[no reason provided]":reason.getData());
                ectx.put("oldModule", issue.getModule());
                ectx.put("oldIssueType", issue.getIssueType());
                ectx.put("oldIssue", issue);
                if (event == ActivityType.ISSUE_COPIED)
                    ectx.put("action", "copy");
                else
                    ectx.put("action", "move");
            }
            catch (TorqueException te)
            {
                Log.get(this.getClass().getName()).error(
                        "configureEmailContext: Can't get the issues from activitySet="
                                + acttivitySet.getActivitySetId());
            }
            
            if (event == ActivityType.ISSUE_COPIED) 
                ectx.setDefaultTextKey("CopiedIssueEmailSubject");
            else
                ectx.setDefaultTextKey("MovedIssueEmailSubject");
        }
        else if (event == ActivityType.ATTACHMENT_CREATED)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.modifyissue.template",
                    "ModifyIssue.vm");
        }
        else
        {
            /** Rest of cases will use, by default, ModifyIssue.vm * */
            template = Turbine.getConfiguration().getString(
                    "scarab.email.modifyissue.template",
                    "ModifyIssue.vm");
            ectx.setDefaultTextKey("DefaultModifyIssueEmailSubject");
        }
        return template;
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
    private void sendEmail(Set activityDesc, Attachment attachment,
            EmailContext context, Issue issue, ScarabUser creator,
            Collection toUsers, Collection ccUsers, String template)
            throws Exception
    {
        // add data to context

        if (toUsers == null)
        {
            // Then add users who are assigned to "email-to" attributes
            toUsers = issue.getAllUsersToEmail(AttributePeer.EMAIL_TO);
        }

        if (ccUsers == null)
        {
            // add users to cc field of email
            ccUsers = issue.getAllUsersToEmail(AttributePeer.CC_TO);
        }

        String[] replyToUser = issue.getModule().getSystemEmail();

        if (Turbine.getConfiguration().getString("scarab.email.replyto.sender")
                .equals("true"))
        {
            Email.sendEmail(
                    context,
                    issue.getModule(),
                    creator,
                    creator,
                    toUsers,
                    ccUsers,
                    template);
        }
        else
        {
            Email.sendEmail(
                    context,
                    issue.getModule(),
                    creator,
                    replyToUser,
                    toUsers,
                    ccUsers,
                    template);
        }
    }

}
