package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
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
 * individuals on behalf of Collab.Net.
 */ 

import java.util.Date;
import java.util.List;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import java.sql.Connection;

import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;

/**
 * This class represents Activity records.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class Activity 
    extends BaseActivity
    implements Persistent
{
    private AttributeOption oldAttributeOption;                 
    private AttributeOption newAttributeOption;                 
    private static final Integer COPIED = new Integer(1);
    private static final Integer MOVED = new Integer(2);

    protected static final String GET_ATTACHMENT = 
        "getAttachment";

    /**
     * This method properly handles the case where there may not be
     * any rows selected and returns null in that case.
     */
    public Attachment getAttachment()
    {
        Attachment attachment = null;
        Object obj = ScarabCache.get(this, GET_ATTACHMENT); 
        if (obj == null)
        {
            try
            {
                Criteria crit = new Criteria();
                crit.add(ActivityPeer.ACTIVITY_ID, this.getActivityId());
                crit.addJoin(AttachmentPeer.ATTACHMENT_ID, ActivityPeer.ATTACHMENT_ID);
                List results = AttachmentPeer.doSelect(crit);
                if (!results.isEmpty())
                {
                    attachment = (Attachment) results.get(0);
                    ScarabCache.put(attachment, this, GET_ATTACHMENT);
                }
            }
            catch (Exception e)
            {
                getLog().error("Activity.getAttachment(): ", e);
            }
        }
        else
        {
            attachment = (Attachment)obj;
        }
        return attachment;
    }

    /**
     * Gets the AttributeOption object associated with the Old Value field
     * (i.e., the old value for the attribute before the change.)
     */
    public AttributeOption getOldAttributeOption() throws Exception
    {
        if (oldAttributeOption==null && (getOldValue() != null))
        {
            oldAttributeOption = AttributeOptionManager
                .getInstance(new Integer(getOldValue()));
        }
        return oldAttributeOption;
    }

    /**
     * Gets the AttributeOption object associated with the New Value field
     * (i.e., the new value for the attribute after the change.)
     */
    public AttributeOption getNewAttributeOption() throws Exception
    {
        if (newAttributeOption==null && (getNewValue() != null))
        {
            newAttributeOption = AttributeOptionManager
                .getInstance(new Integer(getNewValue()));
        }
        return newAttributeOption;
    }

    public void save(Connection dbCon)
        throws TorqueException
    {
        if (isNew()) 
        {
            Criteria crit = new Criteria();
            // If there are previous activities on this attribute and value
            // Set End Date
            if (this.getOldValue() != null)
            {
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivityPeer.ATTRIBUTE_ID, getAttributeId());
                crit.add(ActivityPeer.END_DATE, null);
                crit.add(ActivityPeer.NEW_VALUE, this.getOldValue());
                List result = ActivityPeer.doSelect(crit);
                int resultSize = result.size();
                if (resultSize > 0)
                {
                    for (int i=0; i<resultSize;i++)
                    {
                        Activity a = (Activity)result.get(i);
                        a.setEndDate(getActivitySet().getCreatedDate());
                        a.save(dbCon);
                    }
                }
            }
        }
        // If they have just deleted a user assignment, set end date
        if (getAttribute().isUserAttribute() && this.getNewUserId() == null && this.getOldUserId() != null)
        {
            this.setEndDate(getActivitySet().getCreatedDate());
        }
        super.save(dbCon);
    }

    /**
     * @deprecated Use getDescription(ScarabLocalizationTool l10nTool) instead
     */
    public String getDescription()
    {
        return super.getDescription();
    }
    
    public String getDescription(ScarabLocalizationTool l10nTool)
    {
        String desc = null;
        ActivityType type = ActivityType.getActivityType(this.getActivityType());
        // If the activity was stored before the field Type existed,
        // we fallback to the good old unlocalized description stored in the activity.
        if (type == null)
            return super.getDescription();
        
        if (ActivityType.URL_CHANGED.equals(type))
        {
            desc = this.getUrlChangedDescription(this.getOldValue(), this.getNewValue(), l10nTool);
        }
        else if (ActivityType.URL_ADDED.equals(type))
        {
            desc = this.getUrlAddedDescription(this.getAttachment().getData(), this.getAttachment().getName(), l10nTool);
        }
        else if (ActivityType.URL_DESC_CHANGED.equals(type))
        {
            desc = this.getUrlDescChangedDescription(this.getOldValue(), this.getNewValue(), l10nTool);
        }
        else if (ActivityType.URL_DELETED.equals(type))
        {
            desc = this.getUrlDeletedDescription(this.getAttachment().getData(), this.getAttachment().getName(), l10nTool);
        }
        else if (ActivityType.COMMENT_ADDED.equals(type))
        {
            desc = this.getCommentAddedDescription(this.getAttachment().getData(), l10nTool);
        }
        else if (ActivityType.COMMENT_CHANGED.equals(type))
        {
            desc = this.getCommentChangedDescription(l10nTool);
        }
        else if (ActivityType.ATTACHMENT_CREATED.equals(type))
        {
            desc = this.getFileSavedDescription(this.getAttachment().getFileName(), l10nTool);
        }
        else if (ActivityType.ISSUE_CREATED.equals(type))
        {
            desc = this.getIssueCreatedDescription(l10nTool);
        }
        else if (ActivityType.ISSUE_MOVED.equals(type))
        {
            desc = this.getIssueCopiedOrMovedDescription(COPIED, this.getOldValue(), this.getNewValue(), l10nTool);
        }
        else if (ActivityType.ISSUE_COPIED.equals(type))
        {
            desc = this.getIssueCopiedOrMovedDescription(MOVED, this.getOldValue(), this.getNewValue(), l10nTool);
        }
        else if (ActivityType.ATTACHMENT_REMOVED.equals(type))
        {
            desc = new L10NMessage(L10NKeySet.AttachmentDeletedDesc, this.getAttachment().getFileName()).getMessage(l10nTool);
        }
        else if (ActivityType.DEPENDENCY_CREATED.equals(type))
        {
            desc = getDependencyAddedDescription(l10nTool);
        }
        else if(ActivityType.DEPENDENCY_CHANGED.equals(type))
        {
            desc = getDependencyChangedDescription(l10nTool);
        }
        else if(ActivityType.DEPENDENCY_DELETED.equals(type))
        {
            desc = getDependencyDeletedDescription(l10nTool);
        }
        else if (ActivityType.ATTRIBUTE_CHANGED.equals(type))
        {
            desc = getAttributeChangedDescription(l10nTool);
        }
        else if (ActivityType.USER_ATTRIBUTE_CHANGED.equals(type))
        {
            desc = getUserAttributeChangedDescription(l10nTool);
        }
        else if (ActivityType.ISSUE_DELETED.equals(type))
        {
            desc = getIssueDeletedDescription(l10nTool);
        }
        else
        {
            desc = "----";
        }
        return desc;
    }

    private String getUrlChangedDescription(String oldUrl, String newUrl,
            ScarabLocalizationTool l10nTool)
    {
        Object[] args =
            { oldUrl, newUrl };
        L10NMessage msg = new L10NMessage(L10NKeySet.UrlChangedDesc, args);
        return msg.getMessage(l10nTool);
    }
    
    private String getUrlAddedDescription(String url, String desc, ScarabLocalizationTool l10nTool)
    {
        if (desc != null && desc.length() > 0)
            url += " (" + desc + ")";
        L10NMessage msg = new L10NMessage(L10NKeySet.UrlAddedDesc, url);
        return msg.getMessage(l10nTool);
    }
    
    private String getUrlDeletedDescription(String url, String desc, ScarabLocalizationTool l10nTool)
    {
        if (desc != null && desc.length() > 0)
            url += " (" + desc + ")";        
        L10NMessage msg = new L10NMessage(L10NKeySet.UrlDeletedDesc, url);
        return msg.getMessage(l10nTool);
    }
    
    private String getCommentAddedDescription(String comment, ScarabLocalizationTool l10nTool)
    {
        return L10NKeySet.AddedCommentToIssue.getMessage(l10nTool) + ": '" + comment + "'";
    }
    
    private String getCommentChangedDescription(ScarabLocalizationTool l10nTool)
    {
        // Generate description of modification
        Object[] args = {
            this.getOldValue(),
            this.getNewValue()
        };
        L10NMessage msg = new L10NMessage(L10NKeySet.ChangedComment, args);
        return msg.getMessage(l10nTool);
    }

    private String getFileSavedDescription(String name, ScarabLocalizationTool l10nTool)
    {
        L10NMessage msg = new L10NMessage(L10NKeySet.FileAddedDesc, name);
        return msg.getMessage(l10nTool);
    }
    
    private String getIssueCreatedDescription(ScarabLocalizationTool l10nTool)
    {
        return l10nTool.get(L10NKeySet.IssueCreated);
    }
    
    private String getIssueDeletedDescription(ScarabLocalizationTool l10nTool)
    {
        String issueId = null;
        try
        {
            issueId = this.getIssue().getUniqueId();
        }
        catch (TorqueException te)
        {
            getLog().error("getIssueDeletedDescription(): " + te);
        }
        L10NMessage msg = new L10NMessage(L10NKeySet.IssueDeleted, issueId);
        return msg.getMessage(l10nTool);
    }
    
    /**
     * 
     * @param actionChoice Value of COPIED or MOVED
     * @param oldIssue IssueID (with prefix) of the issue in the old location
     * @param newIssue IssueID (with prefix) of the issue in the new location
     * @param locale Locale to show the description in.
     * @return
     */
    private String getIssueCopiedOrMovedDescription(Integer actionChoice, String oldIssue, String newIssue, ScarabLocalizationTool l10nTool)
    {
        L10NMessage msg = null;
        try
        {
            Issue issue = this.getIssue();

            Object[] args =
                { actionChoice, oldIssue,
                        issue.getModule().getName(),
                        issue.getIssueType().getName() };
            LocalizationKey key = null;
            if (issue.getUniqueId().equals(oldIssue))
            {
                key = L10NKeySet.MovedToIssueDescription;
            }
            else
            {
                key = L10NKeySet.MovedFromIssueDescription;
            }
            msg = new L10NMessage(key, args);
        }
        catch (TorqueException te)
        {
            getLog().error("getIssueCopiedOrMovedDescription(): " + te);
        }
        return msg.getMessage(l10nTool);
    }
    
    private String getUrlDescChangedDescription(String oldDescription, String newDescription, ScarabLocalizationTool l10nTool)
    {
        Object[] args =
            { oldDescription, newDescription, };
        L10NMessage msg = new L10NMessage(L10NKeySet.UrlDescChangedDesc, args);
        return msg.getMessage(l10nTool);
    }
    
    private String getDependencyAddedDescription(ScarabLocalizationTool l10nTool)
    {
        String desc = null;
        try
        {
            Object[] args =
                {
                        this.getDepend().getIssueRelatedByObserverId().getUniqueId(),
                        this.getDepend().getAction(l10nTool.getLocale()),
                        this.getDepend().getIssueRelatedByObservedId()
                                .getUniqueId() };
            L10NMessage msg = new L10NMessage(L10NKeySet.AddDependency, args);
            desc = msg.getMessage(l10nTool);
        }
        catch (TorqueException te)
        {
            getLog().error("getDependencyAddedDescription(): " + te);
        }
        return desc;
    }
    
    private String getDependencyChangedDescription(ScarabLocalizationTool l10nTool)
    {
        String oldName = this.getOldValue();
        String newName = this.getNewValue();

        String desc = null;

        try
        {
            Object[] args =
                {
                        this.getDepend().getIssueRelatedByObserverId().getUniqueId(),
                        this.getDepend().getIssueRelatedByObservedId()
                                .getUniqueId(), oldName, newName };
            if (!newName.equals(oldName))
            {
                desc = (new L10NMessage(L10NKeySet.DependencyTypeChangedDesc, args)).getMessage(l10nTool);
            }
            else
            {
                desc = (new L10NMessage(L10NKeySet.DependencyRolesSwitchedDesc, args)).getMessage(l10nTool);
            }
        }
        catch (TorqueException te)
        {
            getLog().error("getDependencyChangedDescription(): " + te);
        }

        return desc;
    }
    
    private String getDependencyDeletedDescription(ScarabLocalizationTool l10nTool)
    {
        String desc = null;
        try
        {
            Object[] args =
                {
                        this.getDepend().getDependType().getName(),
                        this.getIssue().getUniqueId(),
                        this.getDepend().getIssueRelatedByObservedId()
                                .getUniqueId() };
            desc = (new L10NMessage(L10NKeySet.DependencyDeletedDesc, args)).getMessage(l10nTool);
        }
        catch (TorqueException te)
        {
            getLog().error("getDependencyDeletedDescription(): " + te);
        }
        return desc;
    }
    /**
     * Gives the name of the attribute in the module,or falls back to the global
     * name of the attribute if needed.
     * @return
     */
    public String getDisplayName()
    {
        String attrName = null;
        try
        {
            RModuleAttribute attr = this.getIssue().getModule().getRModuleAttribute(this.getAttribute(), this.getIssue().getIssueType());
            if (attr != null)
                attrName = attr.getDisplayValue();
            else
                attrName = this.getAttribute().getName();
        }
        catch (Exception e)
        {
        	getLog().error("getDisplayName(): " + e);
        }
        return attrName;
    }
    private String getAttributeChangedDescription(ScarabLocalizationTool l10nTool)
    {
        String desc = null;
    	String attrName = this.getDisplayName();
        String newValue = this.getNewValue();
        String oldValue = this.getOldValue();
        try
        {
            if (this.getAttribute().isDateAttribute())
            {
                if (null != newValue)
                    newValue = DateAttribute.dateFormat(newValue, L10NKeySet.ShortDateDisplay.getMessage(l10nTool));
                if (null != oldValue)
                    oldValue = DateAttribute.dateFormat(oldValue, L10NKeySet.ShortDateDisplay.getMessage(l10nTool));
            }
        }
        catch (Exception e)
        {
            getLog().error("getAttributeChangedDescription(): " + e);
        }
        if (oldValue == null)
        {
            Object []args =
                { attrName, newValue };
            desc = (new L10NMessage(L10NKeySet.AttributeSetToNewValue, args)).getMessage(l10nTool);
        }
        else
        {
            Object []args =
                { attrName, oldValue, newValue };
            desc = (new L10NMessage(L10NKeySet.AttributeChangedFromToNewValue, args)).getMessage(l10nTool);
        }
        return desc;
    }
    
    private String getUserAttributeChangedDescription(ScarabLocalizationTool l10nTool)
    {
        String desc = null;
        try
        {
            LocalizationKey key = L10NKeySet.UserAttributeSetToNewValue;
            String value = this.getNewValue();
            if (value == null)
            {
                value = this.getOldValue();
                key = L10NKeySet.UserAttributeRemovedFrom;
            }
            String attrName = this.getIssue().getModule().getRModuleAttribute(
                    this.getAttribute(),
                    this.getIssue().getIssueType()).getDisplayValue();
            Object[] args =
                { attrName, value };
            desc = (new L10NMessage(key, args)).getMessage(l10nTool);
            return desc;            
        }
        catch (Exception e)
        {
            getLog().error("getUserAttributeChangedDescription(): " + e);
        }
        return desc;
    }
    
    public Activity copy(Issue issue, ActivitySet activitySet)
        throws Exception
    {
        Activity newA = new Activity();
        newA.setIssueId(issue.getIssueId());
        newA.setDescription(getDescription());
        newA.setAttributeId(getAttributeId());
        newA.setTransactionId(activitySet.getActivitySetId());
        newA.setOldNumericValue(getOldNumericValue());
        newA.setNewNumericValue(getNewNumericValue());
        newA.setOldUserId(getOldUserId());
        newA.setNewUserId(getNewUserId());
        newA.setOldValue(getOldValue());
        newA.setNewValue(getNewValue());
        newA.setDependId(getDependId());
        newA.setEndDate(getEndDate());
        newA.setAttachmentId(getAttachmentId());
        newA.setActivityType(getActivityType());
        newA.save();
        return newA;
    }
}
