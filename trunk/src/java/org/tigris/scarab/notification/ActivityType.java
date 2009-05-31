package org.tigris.scarab.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityType
{
    String code           = null;
    String resourceId     = null;
    String notificationId = null;
    Integer notificationPriority      = null;
    
    public static final ActivityType ISSUE_CREATED = new ActivityType("issue_created","ActivityTypeIssueCreated", "NotificationIssueCreated", 17);
    public static final ActivityType ISSUE_MOVED = new ActivityType("issue_moved","ActivityTypeIssueMoved", "NotificationIssueMoved", 16);
    public static final ActivityType ISSUE_COPIED = new ActivityType("issue_copied","ActivityTypeIssueCopied", "NotificationIssueCopied", 15);
    public static final ActivityType ISSUE_DELETED = new ActivityType("issue_deleted","ActivityTypeIssueDeleted", "NotificationIssueDeleted", 14);
    public static final ActivityType COMMENT_ADDED = new ActivityType("comment_added","ActivityTypeCommentAdded", "NotificationCommentAdded", 13);
    public static final ActivityType COMMENT_CHANGED = new ActivityType("comment_changed","ActivityTypeCommentChanged", "NotificationCommentChanged", 12);
    public static final ActivityType ATTRIBUTE_CHANGED = new ActivityType("attribute_changed","ActivityTypeAttributeChanged", "NotificationAttributeChanged", 11);
    public static final ActivityType USER_ATTRIBUTE_CHANGED = new ActivityType("user_attribute_changed","ActivityTypeUserAttributeChanged", "NotificationUserAttributeChanged", 10);
    public static final ActivityType URL_ADDED = new ActivityType("url_added","ActivityTypeURLAdded", "NotificationURLAdded", 9);
    public static final ActivityType URL_CHANGED = new ActivityType("url_changed","ActivityTypeURLChanged", "NotificationURLCHanged", 8);
    public static final ActivityType URL_DESC_CHANGED = new ActivityType("url_desc_changed","ActivityTypeURLDescChanged", "NotificationURLDESCCHanged", 7);
    public static final ActivityType URL_DELETED = new ActivityType("url_deleted","ActivityTypeURLDeleted", "NotificationURLDeleted", 6);
    public static final ActivityType ATTACHMENT_CREATED = new ActivityType("attachment_created","ActivityTypeAttachmentCreated", "NotificationAttachmentCreated", 5);
    public static final ActivityType ATTACHMENT_REMOVED = new ActivityType("attachment_removed","ActivityTypeAttachmentRemoved", "NotificationAttachmentRemoved", 4);
    public static final ActivityType DEPENDENCY_CREATED = new ActivityType("dependency_created","ActivityTypeDependencyCreated", "NotificationDependencyCreated", 3);
    public static final ActivityType DEPENDENCY_CHANGED = new ActivityType("dependency_changed","ActivityTypeDependencyChanged", "NotificationDependencyChanged", 2);
    public static final ActivityType DEPENDENCY_DELETED = new ActivityType("dependency_deleted","ActivityTypeDependencyDeleted", "NotificationDependencyDeleted", 1);
    public static final ActivityType OTHER = new ActivityType("other","ActivityTypeOther", "NotificationIssueOther", 0);
    
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

    }

    /**
     * Return the L10N Id for the Notification hint
     * @return
     */
    public String getHint() {
        return notificationId;
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
   
    private ActivityType(String desc, String resourceId, String notificationId, Integer notificationPriority)
    {
        this.code = desc;
        this.resourceId = resourceId;
        this.notificationId = notificationId;
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
