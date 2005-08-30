package org.tigris.scarab.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
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
    public void addActivityNotification(NotificationEvent event,
            ActivitySet activitySet, Issue issue)
    {
        this.addActivityNotification(
                event,
                null,
                activitySet,
                issue,
                null,
                null);
    }
    
    /**
     * Long version of the addActivityNotification method, allowing to pass
     * an EmailContext so additionsl information can be made availaible to
     * the templates and the sets of user involved as To or CC.
     */
    public void addActivityNotification(NotificationEvent event,
            EmailContext ectx, ActivitySet activitySet, Issue issue,
            Set toUsers, Set ccUsers)
    {
        if (log.isDebugEnabled())
            log.debug("addActivityNotification: " + issue.getIdPrefix()
                    + issue.getIssueId() + "-" + event);
        
        if (null == ectx)
            ectx = new EmailContext();

        String template = configureEmailContext(event, ectx);

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
                String desc = activity.getDescription();
                changes.add(desc);
            }
        }
        catch (Exception e)
        {
            log.error("addActivityNotification: Error accessing activityset: "
                    + e);
        }

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
    private String configureEmailContext(NotificationEvent event,
            EmailContext ectx)
    {
        String template = null;

        // Select appropiate template depending on the event
        if (event == NotificationManager.EVENT_ASSIGN_ISSUE)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.assignissue.template",
                    "ModifyIssue.vm");

            ectx.setSubjectTemplate("AssignIssueModifyIssueSubject.vm");
        }
        else if (event == NotificationManager.EVENT_NEW_ISSUE)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.reportissue.template",
                    "NewIssueNotification.vm");
        }
        else if (event == NotificationManager.EVENT_MOVED_OR_COPIED_ISSUE)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.moveissue.template",
                    "MoveIssue.vm");
            String action = (String) ectx.get("action");
            if (action != null && action.equals("copy"))
            {
                ectx.setDefaultTextKey("CopiedIssueEmailSubject");
            }
            else
            {
                ectx.setDefaultTextKey("MovedIssueEmailSubject");
            }
        }
        else
        {
            /** Rest of cases will use, by default, ModifyIssue.vm * */
            template = Turbine.getConfiguration().getString(
                    "scarab.email.modifyissue.template",
                    "ModifyIssue.vm");
            ectx.setSubjectTemplate("AssignIssueModifyIssueSubject.vm");
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
        if (context == null)
        {
            context = new EmailContext();
        }

        // add data to context
        context.setIssue(issue);
        context.put("attachment", attachment);
        context.put("uniqueActivityDescriptions", activityDesc);

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
