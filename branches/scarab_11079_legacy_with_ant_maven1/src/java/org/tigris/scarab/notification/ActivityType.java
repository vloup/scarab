package org.tigris.scarab.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.LocalizationKey;

public class ActivityType
{
    String code                     = null;
    String resourceId               = null;
    LocalizationKey notificationKey = null;
    Integer notificationPriority    = null;
    
    public static final ActivityType ISSUE_REMINDER     = new ActivityType("issue_reminder","ActivityTypeIssueReminder",         L10NKeySet.NotificationIssueReminder, 19);
    public static final ActivityType ISSUE_ONHOLD       = new ActivityType("issue_onhold","ActivityTypeIssueOnHold",             L10NKeySet.NotificationIssueOnHold, 18);
    public static final ActivityType ISSUE_CREATED      = new ActivityType("issue_created","ActivityTypeIssueCreated",           L10NKeySet.NotificationIssueCreated, 17);
    public static final ActivityType ISSUE_MOVED        = new ActivityType("issue_moved","ActivityTypeIssueMoved",               L10NKeySet.NotificationIssueMoved, 16);
    public static final ActivityType ISSUE_COPIED       = new ActivityType("issue_copied","ActivityTypeIssueCopied",             L10NKeySet.NotificationIssueCopied, 15);
    public static final ActivityType ISSUE_DELETED      = new ActivityType("issue_deleted","ActivityTypeIssueDeleted",           L10NKeySet.NotificationIssueDeleted, 14);
    public static final ActivityType ATTRIBUTE_CHANGED  = new ActivityType("attribute_changed","ActivityTypeAttributeChanged",   L10NKeySet.NotificationAttributeChanged, 13);
    public static final ActivityType USER_ATTRIBUTE_CHANGED = new ActivityType("user_attribute_changed","ActivityTypeUserAttributeChanged", L10NKeySet.NotificationUserAttributeChanged, 12);
    public static final ActivityType URL_ADDED          = new ActivityType("url_added","ActivityTypeURLAdded",                   L10NKeySet.NotificationURLAdded, 11);
    public static final ActivityType URL_CHANGED        = new ActivityType("url_changed","ActivityTypeURLChanged",               L10NKeySet.NotificationURLCHanged, 10);
    public static final ActivityType URL_DESC_CHANGED   = new ActivityType("url_desc_changed","ActivityTypeURLDescChanged",      L10NKeySet.NotificationURLDESCCHanged, 9);
    public static final ActivityType URL_DELETED        = new ActivityType("url_deleted","ActivityTypeURLDeleted",               L10NKeySet.NotificationURLDeleted, 8);
    public static final ActivityType ATTACHMENT_CREATED = new ActivityType("attachment_created","ActivityTypeAttachmentCreated", L10NKeySet.NotificationAttachmentCreated, 7);
    public static final ActivityType ATTACHMENT_REMOVED = new ActivityType("attachment_removed","ActivityTypeAttachmentRemoved", L10NKeySet.NotificationAttachmentRemoved, 6);
    public static final ActivityType DEPENDENCY_CREATED = new ActivityType("dependency_created","ActivityTypeDependencyCreated", L10NKeySet.NotificationDependencyCreated, 5);
    public static final ActivityType DEPENDENCY_CHANGED = new ActivityType("dependency_changed","ActivityTypeDependencyChanged", L10NKeySet.NotificationDependencyChanged, 4);
    public static final ActivityType DEPENDENCY_DELETED = new ActivityType("dependency_deleted","ActivityTypeDependencyDeleted", L10NKeySet.NotificationDependencyDeleted, 3);
    public static final ActivityType COMMENT_ADDED      = new ActivityType("comment_added","ActivityTypeCommentAdded",           L10NKeySet.NotificationCommentAdded, 2);
    public static final ActivityType COMMENT_CHANGED    = new ActivityType("comment_changed","ActivityTypeCommentChanged",       L10NKeySet.NotificationCommentChanged, 1);
    public static final ActivityType OTHER              = new ActivityType("other","ActivityTypeOther",                          L10NKeySet.NotificationIssueOther, 0);
    
    private static Map types       = new HashMap();
    private static HashMap activityPriority;
    static
    {

        types.put(ISSUE_CREATED.getCode(), ISSUE_CREATED);
        types.put(ISSUE_MOVED.getCode(), ISSUE_MOVED);
        types.put(ISSUE_COPIED.getCode(), ISSUE_COPIED);
        types.put(ISSUE_DELETED.getCode(), ISSUE_DELETED);
        types.put(ATTRIBUTE_CHANGED.getCode(), ATTRIBUTE_CHANGED);
        types.put(USER_ATTRIBUTE_CHANGED.getCode(), USER_ATTRIBUTE_CHANGED);
        types.put(COMMENT_ADDED.getCode(), COMMENT_ADDED);
        types.put(COMMENT_CHANGED.getCode(), COMMENT_CHANGED);
        types.put(URL_ADDED.getCode(), URL_ADDED);
        types.put(URL_CHANGED.getCode(), URL_CHANGED);
        types.put(URL_DESC_CHANGED.getCode(), URL_DESC_CHANGED);
        types.put(URL_DELETED.getCode(), URL_DELETED);
        types.put(ATTACHMENT_CREATED.getCode(), ATTACHMENT_CREATED);
        types.put(ATTACHMENT_REMOVED.getCode(), ATTACHMENT_REMOVED);
        types.put(DEPENDENCY_CREATED.getCode(), DEPENDENCY_CREATED);
        types.put(DEPENDENCY_CHANGED.getCode(), DEPENDENCY_CHANGED);
        types.put(DEPENDENCY_DELETED.getCode(), DEPENDENCY_DELETED);
        types.put(ISSUE_ONHOLD.getCode(), ISSUE_ONHOLD);
        types.put(ISSUE_REMINDER.getCode(), ISSUE_REMINDER);

    }

    /**
     * Return the L10N Id for the Notification hint
     * @return
     */
    public LocalizationKey getNotificationKey() {
        return notificationKey;
    }
    
    /**
     * Return the reporting priority.
     * higher values == higher priority
     * Currently only used for Notifications.
     * @param at
     * @return
     */
    public int getPriority()
    {
        return notificationPriority;
    }
    
    /**
     * Return an iterator over all available ActivityType codes.
     * @return
     */
    public static Set getActivityTypeCodes()
    {
        return types.keySet();
    }
    
    /**
     * Returns the activitytype constant given its database value.
     * 
     * @param code The internal code of the type (the value that gets stored in database)
     * @return A constant AcivityType matching the given code
     */
    public static ActivityType getActivityType(String code)
    {
        return (ActivityType)types.get(code);        
    }
    
    public String getCode()
    {
        return this.code;
    }
   
    private ActivityType(String desc, String resourceId, LocalizationKey notificationKey, Integer notificationPriority)
    {
        this.code = desc;
        this.resourceId = resourceId;
        this.notificationKey = notificationKey;
        this.notificationPriority = notificationPriority;
    }
    
    /**
     * Compares two ActivityType objects with their <code>code</code> attribute.
     */
    public boolean equals(Object obj)
    {
        boolean bRdo = false;
        if (obj != null)
            bRdo = this.code.equals(((ActivityType)obj).getCode());
        return bRdo;
    }

    /**
     * Return the resource id for the given activity code.
     * Note[HD]: Unfortunately the name convention for resources is
     * different from the name convention used in ActivityType for
     * activity codes. We should switch to the convention for resources
     * here. Thus we can avoid this remapping from code to resource id.
     * @param code
     * @return
     */
    public static String getResourceId(String code)
    {
        return getActivityType(code).resourceId;
    }

}
