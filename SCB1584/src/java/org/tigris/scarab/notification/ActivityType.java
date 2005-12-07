package org.tigris.scarab.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityType
{
    String code = null;
    String resourceId = null;
    
    public static final ActivityType ISSUE_CREATED = new ActivityType("issue_created","ActivityTypeIssueCreated");
    public static final ActivityType ISSUE_MOVED = new ActivityType("issue_moved","ActivityTypeIssueMoved");
    public static final ActivityType ISSUE_COPIED = new ActivityType("issue_copied","ActivityTypeIssueCopied");
    public static final ActivityType ATTRIBUTE_CHANGED = new ActivityType("attribute_changed","ActivityTypeAttributeChanged");
    public static final ActivityType USER_ATTRIBUTE_CHANGED = new ActivityType("user_attribute_changed","ActivityTypeUserAttributeChanged");
    public static final ActivityType COMMENT_ADDED = new ActivityType("comment_added","ActivityTypeCommentAdded");
    public static final ActivityType COMMENT_CHANGED = new ActivityType("comment_changed","ActivityTypeCommentChanged");
    public static final ActivityType URL_ADDED = new ActivityType("url_added","ActivityTypeURLAdded");
    public static final ActivityType URL_CHANGED = new ActivityType("url_changed","ActivityTypeURLChanged");
    public static final ActivityType URL_DESC_CHANGED = new ActivityType("url_desc_changed","ActivityTypeURLDescChanged");
    public static final ActivityType URL_DELETED = new ActivityType("url_deleted","ActivityTypeURLDeleted");
    public static final ActivityType ATTACHMENT_CREATED = new ActivityType("attachment_created","ActivityTypeAttachmentCreated");
    public static final ActivityType ATTACHMENT_REMOVED = new ActivityType("attachment_removed","ActivityTypeAttachmentRemoved");
    public static final ActivityType DEPENDENCY_CREATED = new ActivityType("dependency_created","ActivityTypeDependencyCreated");
    public static final ActivityType DEPENDENCY_CHANGED = new ActivityType("dependency_changed","ActivityTypeDependencyChanged");
    public static final ActivityType DEPENDENCY_DELETED = new ActivityType("dependency_deleted","ActivityTypeDependencyDeleted");
    
    private static Map types       = new HashMap();

    static
    {
        types.put(ISSUE_CREATED.getCode(), ISSUE_CREATED);
        types.put(ISSUE_MOVED.getCode(), ISSUE_MOVED);
        types.put(ISSUE_COPIED.getCode(), ISSUE_COPIED);
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
   
    private ActivityType(String desc, String resourceId)
    {
        this.code = desc;
        this.resourceId = resourceId;
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
