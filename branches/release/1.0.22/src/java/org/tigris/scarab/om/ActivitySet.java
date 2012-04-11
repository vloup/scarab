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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria; 

import org.apache.torque.om.Persistent;

import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * This object represents a ActivitySet. It is used as a container
 * for one or more Activity objects.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class ActivitySet 
    extends BaseActivitySet
    implements Persistent
{
    private static final String GET_ACTIVITY_LIST = 
        "getActivityList";
    
    /**
     * Sets the activity list for this activitySet.
     */
    public void setActivityList(List activityList)
        throws TorqueException
    {
        for (Iterator itr = activityList.iterator();itr.hasNext();)
        {
            Activity activity = (Activity) itr.next();
            activity.setActivitySet(this);
            activity.save();
        }
        ScarabCache.put(activityList, this, GET_ACTIVITY_LIST);
    }

    /**
     * Returns a list of Activity objects associated with this ActivitySet.
     */
    public List getActivityList() throws ScarabException
    {
        List result = null;
/* FIXME: caching is disabled here because new Activities can be
          added to this activityset and the addition does not trigger 
          a reset of this cache (JSS).
        Object obj = ScarabCache.get(this, GET_ACTIVITY_LIST); 
        if (obj == null) 
        {
*/
            Criteria crit = new Criteria()
                .add(ActivityPeer.TRANSACTION_ID, getActivitySetId());
            try
            {
                result = ActivityPeer.doSelect(crit);
            }
            catch (TorqueException e)
            {
                throw new ScarabException(L10NKeySet.ExceptionTorqueGeneric,e);
            }
//            ScarabCache.put(result, this, GET_ACTIVITY_LIST);
/*
        }
        else 
        {
            result = (List)obj;
        }
*/
        return result;
    }

    /**
     * Returns a list of Activity objects associated with this ActivitySet
     * And this issue.
     */
    public List getActivityList(Issue issue) throws TorqueException
    {
         List activityList = (List)ActivitySetManager.getMethodResult()
                .get(this, GET_ACTIVITY_LIST, issue );

        if(activityList==null)
        {
            Criteria crit = new Criteria()
                .add(ActivityPeer.TRANSACTION_ID, getActivitySetId())
                .add(ActivityPeer.ISSUE_ID, issue.getIssueId());
            activityList = ActivityPeer.doSelect(crit);

            ActivitySetManager.getMethodResult()
                .put(activityList, this, GET_ACTIVITY_LIST, issue );
        }
        return activityList;  
    }

    public ScarabUser getCreator()
        throws TorqueException
    {
        return getScarabUser();
    }

    public String getActivityReason() throws TorqueException
    {
        Attachment attachment = this.getAttachment();
        return attachment!=null ? attachment.getData() : "";
    }

    /**
     * Returns a set of ScarabUsers which are removed from changedIssue
     * in this ActivitySet
     */
    public Set getRemovedUsers(Issue changedIssue) throws TorqueException
    {
        Set removedUsers = new HashSet();
        for (Iterator it = getActivityList(changedIssue).iterator(); it.hasNext(); )
        {
            Activity act = (Activity)it.next();
            if(act.getOldUserId() != null && act.getNewUserId() == null)
            {
                ScarabUser removedUser = ScarabUserManager.getInstance(act.getOldUserId());
                removedUsers.add(removedUser);
            }
        }
        return removedUsers;
    }
    
    private List getActivityList(Issue issue, List activityTypes)
        throws Exception
    {
        List filteredActivities = new ArrayList();
        for(Iterator activities = getActivityList( issue ).iterator(); activities.hasNext();)
        {
            Activity activity = (Activity)activities.next();
            if(activityTypes.contains(ActivityType.getActivityType(activity.getActivityType())))
                filteredActivities.add(activity);
        }
        return filteredActivities;
    }
    
    private List getActivityList(Issue issue, ActivityType activityType)
        throws Exception
    {
        ActivityType[] types = {activityType};
        return getActivityList(issue, Arrays.asList(types));  
    }    
    
    public String getCommentForHistory(Issue issue) 
        throws Exception
    {
        String comment = null;
        List comments = getActivityList(issue, ActivityType.COMMENT_ADDED);
        if(comments.size()==1)
            comment=((Activity)comments.get(0)).getAttachment().getData();
        else
            comment=getActivityReason();
        
        return comment;
    }    

    private static final ActivityType[] historyTypes = new ActivityType[] {
        ActivityType.ISSUE_MOVED,
        ActivityType.ISSUE_COPIED,
        ActivityType.USER_ATTRIBUTE_CHANGED,
        ActivityType.COMMENT_CHANGED,
        ActivityType.URL_ADDED,
        ActivityType.URL_CHANGED,
        ActivityType.URL_DESC_CHANGED,
        ActivityType.URL_DELETED,
        ActivityType.ATTACHMENT_CREATED,
        ActivityType.ATTACHMENT_REMOVED,
        ActivityType.DEPENDENCY_CREATED,
        ActivityType.DEPENDENCY_CHANGED,
        ActivityType.DEPENDENCY_DELETED,
        ActivityType.ATTRIBUTE_CHANGED,
        ActivityType.OTHER
    };
    private static final List historyTypeList = Arrays.asList(historyTypes);
    
    public List getActivityListForHistory(Issue issue) 
        throws Exception
    {
        return getActivityList(issue, historyTypeList);  
    }

}
