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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabException;

/**
 * This class holds the information for every notification generated
 * in the system (stored in NOTIFICATION_STATUS table)
 * 
 */
public  class NotificationStatus
    extends BaseNotificationStatus
    implements Persistent, Comparable
{

    static public final Integer WAIT           = new Integer(1);
    static public final Integer SCHEDULED      = new Integer(2);
    static public final Integer DEFERRED       = new Integer(3);
    static public final Integer FAIL           = new Integer(4);
    static public final Integer SENT           = new Integer(5);
    static public final Integer MARK_DELETED   = new Integer(6);
       
    private String activityType;
    private Long issueId;
    
    public NotificationStatus() throws TorqueException
    {
      this.setCreationDate(new Date());
      this.setStatus(SCHEDULED);
    }
    
    /**
     * Create a new NotificationStatus entry.
     * @param receiver
     * @param activity
     * @throws TorqueException
     */
    public NotificationStatus(ScarabUser receiver, Activity activity) throws ScarabException
    {
        try
        {
            this.setActivity(activity);
            this.setReceiverId(receiver.getUserId());
        }
        catch(TorqueException te)
        {
            throw new ScarabException(L10NKeySet.ExceptionTorqueGeneric, te);
        }
        this.setCreationDate(new Date());
        this.setStatus(SCHEDULED);
    }
    
    public Long getIssueId()
    {
    	return this.issueId;
    }
    
    public String getActivityType()
    {
    	return this.activityType;
    }
    
    /**
     * NOT ONLY sets the activityId; it also updates the depending values.
     */
    public void setActivityId(Long id)
    {
        try
        {
        	super.setActivityId(id);
            this.issueId = this.getActivity().getIssue().getIssueId();
            this.setCreationDate(this.getActivity().getActivitySet().getCreatedDate());
            this.setCreatorId(this.getActivity().getActivitySet().getCreator().getUserId());
            this.activityType = this.getActivity().getActivityType();
        }
        catch (TorqueException te)
        {
            getLog().error("setActivity(): Cannot find activity's issue!: " + te);
        }
    }
    
    /**
     * Transform the database representation of the status (INTEGER)
     * into a readable label (String)
     * 
     * TODO: Should this return a localized string (receiving parameter l10nTool?)
     * 
     * @return
     */
    public String getStatusLabel()
    {
        Integer status = getStatus();
        if (status.equals(WAIT)) return "wait";
        if (status.equals(SCHEDULED)) return "scheduled";
        if (status.equals(DEFERRED)) return "deferred";
        if (status.equals(FAIL)) return "fail";
        if (status.equals(SENT)) return "delivered";
        if (status.equals(MARK_DELETED)) return "deleted";
        throw new RuntimeException("Database inconsistency: status ["+status+"] is not known.");
    }

    /**
     * Formats the creationDate of the notification's activity
     * @param l10nTool
     * @return
     */
    public String getActivityCreationDate(ScarabLocalizationTool l10nTool)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat(L10NKeySet.ShortDateTimeDisplay.getMessage(l10nTool));
    	return sdf.format(this.getCreationDate());
    }
    
    /**
     * Get the creator of this entry
     * @return
     * @throws TorqueException
     */
    public ScarabUser getCreator() throws TorqueException
    {
        Integer id = this.getCreatorId();
        ScarabUser user = ScarabUserManager.getInstance(id);
        return user;
    }
    
    /**
     * get the Receiver for this entry
     * @return
     * @throws TorqueException
     */
    public ScarabUser getReceiver() throws TorqueException
    {
        Integer id = this.getReceiverId();
        ScarabUser user = ScarabUserManager.getInstance(id);
        return user;
    }

    public void delete() throws TorqueException
    {
        Criteria crit = new Criteria();
        crit
                .add(NotificationStatusPeer.ACTIVITY_ID, this
                        .getActivityId());
        crit.add(NotificationStatusPeer.CREATOR_ID, this.getCreatorId());
        crit.add(NotificationStatusPeer.RECEIVER_ID, this.getReceiverId());
        NotificationStatusPeer.doDelete(crit);
    }

    public void markDeleted() throws TorqueException
    {
        this.setStatus(MARK_DELETED);
        this.setChangeDate(new Date());
        this.save();
    }
    
    /**
     * Compare two Notification objects, following this criteria:
     * <ul>
     * <li>IssueId</li>
     * <li>User</li>
     * <li>Activity type</li>
     * <li>Timestamp</li>
     * </ul>
     * @author jorgeuriarte
     *
     */
    public int compareTo(Object o2)
    {
        int rdo = 0;
        NotificationStatus not1 = this;
        NotificationStatus not2 = (NotificationStatus)o2;
        if (null != not1 && null != not2)
        {
            Long id1 = not1.getIssueId();
            Long id2 = not2.getIssueId();
            if (null == id1 && null != id2)
                return -1;
            if (null != id1 && null == id2)
                return 1;

            Integer user1 = not1.getCreatorId();
            Integer user2 = not2.getCreatorId();
            rdo = user1.compareTo(user2);
            if (0 == rdo)
            {
                rdo = not1.getActivityType().compareTo(not2.getActivityType());
                if (0 == rdo)
                {
                    rdo = not1.getCreationDate().compareTo(not2.getCreationDate());
                }
            }
                
        }
        return rdo;
    }    

}
