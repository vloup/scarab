package org.tigris.scarab.notification;

public class ActivityType
{
    String desc = null;
    
    public static final ActivityType ISSUE_CREATED = new ActivityType("issue_created");
    public static final ActivityType ISSUE_MOVED = new ActivityType("issue_moved");
    public static final ActivityType ISSUE_COPIED = new ActivityType("issue_copied");
    public static final ActivityType ATTRIBUTE_CHANGED = new ActivityType("attribute_changed");
    public static final ActivityType USER_ATTRIBUTE_CHANGED = new ActivityType("user_attribute_changed");
    public static final ActivityType COMMENT_ADDED = new ActivityType("comment_added");
    public static final ActivityType COMMENT_CHANGED = new ActivityType("comment_changed");
    public static final ActivityType URL_ADDED = new ActivityType("url_added");
    public static final ActivityType URL_CHANGED = new ActivityType("url_changed");
    public static final ActivityType URL_DESC_CHANGED = new ActivityType("url_desc_changed");
    public static final ActivityType URL_DELETED = new ActivityType("url_deleted");
    public static final ActivityType ATTACHMENT_CREATED = new ActivityType("attachment_created");
    public static final ActivityType ATTACHMENT_REMOVED = new ActivityType("attachment_removed");
    public static final ActivityType DEPENDENCY_CREATED = new ActivityType("dependency_created");
    public static final ActivityType DEPENDENCY_CHANGED = new ActivityType("dependency_changed");
    public static final ActivityType DEPENDENCY_DELETED = new ActivityType("dependency_deleted");
    
    public static ActivityType getActivityType(String desc)
    {
        return new ActivityType(desc);
    }
    
    public String getCode()
    {
        return this.desc;
    }
   
    private ActivityType(String desc)
    {
        this.desc = desc;
    }
    
    public boolean equals(Object obj)
    {
        boolean bRdo = false;
        if (obj != null)
            bRdo = this.desc.equals(((ActivityType)obj).getCode());
        return bRdo;
    }
}
