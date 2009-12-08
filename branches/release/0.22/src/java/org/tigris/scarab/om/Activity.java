package org.tigris.scarab.om;

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

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import java.sql.Connection;

import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;

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
    public AttributeOption getOldAttributeOption() throws TorqueException
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
    public AttributeOption getNewAttributeOption() throws TorqueException
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
    
    public String getDescription(ScarabLocalizationTool l10n)
        throws Exception
    {
        return getDisplayName(l10n) + ": " + getNewValue(l10n) + " (" + getOldValue(l10n) + ")";
    }

    public ActivityType getType()
    {
        return ActivityType.getActivityType(this.getActivityType());
    }

    public static String firstChars( String s, int n )
    {
       int end = s.length() > n ? n : s.length(); 
       return s.substring(0, end ) + "..."; 
    }

    /**
     * Returns old value, before activity took place.
     * @param l10n : Instance of localization.
     * @return : Old value.
     * @throws Exception
     */
    public String getOldValue(ScarabLocalizationTool l10n) 
        throws Exception
    {
        String value = null;
        if(   getType().equals(ActivityType.DEPENDENCY_DELETED)
           || getType().equals(ActivityType.DEPENDENCY_CHANGED))
         {
             value = getDepend().getIssueRelatedByObserverId().getUniqueId()
            + " " + l10n.get(DependTypeManager.getManager().getL10nKey(getOldValue()))
             + " " + getDepend().getIssueRelatedByObservedId().getUniqueId();             
         } else if( getType().equals(ActivityType.ATTACHMENT_REMOVED))
         {
             value = getAttachment().getFileName();
         } else if( getType().equals(ActivityType.URL_DELETED))
         {
             value = getAttachment().getData();
         } else if( getType().equals(ActivityType.COMMENT_CHANGED))
         {
             value = getOldValue()!=null ? firstChars( getOldValue(), 50 ) : "";
         } else {
        	//assumption that an attribute was changed
        	 if(getAttribute() != null && getAttribute().isDateAttribute()){
         		value = DateAttribute.dateFormat(getOldValue()!=null ? getOldValue() : "",  L10NKeySet.ShortDateDisplay.getMessage(l10n));
         	}
         	else{
         		value = getOldValue()!=null ? getOldValue() : "";
         	}
         }

        return value;
    }
    
    /**
     * Returns new value, after activity took place.
     * @param l10n : Instance of localization.
     * @return : New value.
     * @throws Exception
     */
    public String getNewValue(ScarabLocalizationTool l10n)
        throws Exception
    {
        String value = null;
        if(   getType().equals(ActivityType.DEPENDENCY_CREATED)
           || getType().equals(ActivityType.DEPENDENCY_CHANGED))
        {
            value = getDepend().getIssueRelatedByObserverId().getUniqueId()
            + " " + l10n.get(DependTypeManager.getManager().getL10nKey(getNewValue()))
            + " " + getDepend().getIssueRelatedByObservedId().getUniqueId();             
        } else if( getType().equals(ActivityType.ATTACHMENT_CREATED))
        {
            value = getAttachment().getFileName();
        } else if(    getType().equals(ActivityType.URL_ADDED)
                || getType().equals(ActivityType.COMMENT_ADDED))
        {
            value = getAttachment().getData();
        } else if( getType().equals(ActivityType.COMMENT_CHANGED))
        {
            value = getNewValue()!=null ? firstChars( getNewValue(), 50 ) : "";
        } else if( getType().equals(ActivityType.OTHER))
        {
            value = super.getDescription()!=null ? super.getDescription() : "";
        } else {
        	//assumption that an attribute was changed
        	if(getAttribute() != null && getAttribute().isDateAttribute()){
        		value = DateAttribute.dateFormat(getNewValue()!=null ? getNewValue() : "",  L10NKeySet.ShortDateDisplay.getMessage(l10n));
        	}
        	else{
        		value = getNewValue()!=null ? getNewValue() : "";
        	}
            
        }
        return value;
    }

    public String getDisplayName(ScarabLocalizationTool l10n)
        throws Exception
    {
        String name=null;
        if(this.getAttribute().getAttributeId().intValue()!=0)
        {
            name = getDisplayName();
        } else if(   getType().equals(ActivityType.OTHER))
        {
            name = l10n.get(L10NKeySet.OtherActivity);
        } else if(    getType().equals(ActivityType.ATTACHMENT_CREATED)
                   || getType().equals(ActivityType.ATTACHMENT_REMOVED))
        {
            name = l10n.get(L10NKeySet.Attachment);
        } else if(    getType().equals(ActivityType.DEPENDENCY_CREATED)
                   || getType().equals(ActivityType.DEPENDENCY_CHANGED)
                   || getType().equals(ActivityType.DEPENDENCY_DELETED))
        {
            name = l10n.get(L10NKeySet.Link);
        } else if(    getType().equals(ActivityType.URL_ADDED)
                   || getType().equals(ActivityType.URL_CHANGED)
                   || getType().equals(ActivityType.URL_DESC_CHANGED)
                   || getType().equals(ActivityType.URL_DELETED))
        {
            name = l10n.get(L10NKeySet.URL);
        } else if(    getType().equals(ActivityType.COMMENT_ADDED)
                   || getType().equals(ActivityType.COMMENT_CHANGED))
        {
            name = l10n.get(L10NKeySet.Comment);
        }
        else if(    getType().equals(ActivityType.ISSUE_COPIED)
                || getType().equals(ActivityType.ISSUE_MOVED)
                || getType().equals(ActivityType.ISSUE_CREATED)
                || getType().equals(ActivityType.ISSUE_DELETED))
	     {
	         name = l10n.get(L10NKeySet.IssueId);
	     }
        return name;
    }
    
    /**
     * Gives the name of the attribute in the module,or falls back to the global
     * name of the attribute if needed.
     * @return
     */
    public String getDisplayName()
        throws Exception
    {
        RModuleAttribute attr = getIssue().getModule().getRModuleAttribute(this.getAttribute(), this.getIssue().getIssueType());
        if (attr!=null)
            return attr.getDisplayValue();
        else
            return getAttribute().getName();
    }

    public Activity copy(Issue issue, ActivitySet activitySet)
        throws TorqueException
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
