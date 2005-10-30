package org.tigris.scarab.notification;

import java.util.HashMap;
import java.util.Map;

import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;

public class ActivityType
{
    String code = null;
    
    public static final ActivityType ISSUE_CREATED = new ActivityType("issue_created");
    public static final ActivityType ISSUE_MOVED = new ActivityType("issue_moved");
    public static final ActivityType ISSUE_COPIED = new ActivityType("issue_copied");
    public static final ActivityType ISSUE_DELETED = new ActivityType("issue_deleted");
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
    
    private static Map types = new HashMap();

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
   
    private ActivityType(String desc)
    {
        this.code = desc;
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
}
