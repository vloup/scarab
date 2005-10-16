package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabException;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class NotificationStatus
    extends org.tigris.scarab.om.BaseNotificationStatus
    implements Persistent
{

    static public final Integer WAIT           = new Integer(1);
    static public final Integer SCHEDULED      = new Integer(2);
    static public final Integer DEFERRED       = new Integer(3);
    static public final Integer FAIL           = new Integer(4);
    static public final Integer SENT           = new Integer(5);
    static public final Integer MARK_DELETED   = new Integer(6);
       
    public NotificationStatus() throws TorqueException
    {
      this.setCreationDate(new Date());
      this.setStatus(WAIT);
    }
    
    /**
     * Create a new NotificationStatus entry.
     * @param creator
     * @param receiver
     * @param activitySet
     * @throws TorqueException
     */
    public NotificationStatus(ScarabUser creator, ScarabUser receiver, ActivitySet activitySet) throws TorqueException
    {
      this.setActivitySet(activitySet);
      this.setCreatorId(creator.getUserId());
      this.setReceiverId(receiver.getUserId());
      this.setCreationDate(new Date());
      this.setStatus(WAIT);
    }
    
    /**
     * Transform the database representation of the status (INTEGER)
     * into a readable label (String)
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
     * Get the list of issues associated with the current NotificationStatus.
     * .e. the list contains multiple issue when the dependencies between two
     * issues have changed.
     * @return
     * @throws Exception
     */
    public Set getIssues() throws Exception
    {
        HashSet result = new HashSet();
        ActivitySet activitySet = this.getActivitySet();
        List activities = activitySet.getActivityList();
        Iterator iter = activities.iterator();
        while(iter.hasNext())
        {
            Activity activity = (Activity)iter.next();
            result.add(activity.getIssue());
        }
        return result;
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
                .add(NotificationStatusPeer.TRANSACTION_ID, this
                        .getTransactionId());
        crit.add(NotificationStatusPeer.CREATOR_ID, this.getCreatorId());
        crit.add(NotificationStatusPeer.RECEIVER_ID, this.getReceiverId());
        NotificationStatusPeer.doDelete(crit);
    }

    public void markDeleted() throws Exception
    {
        this.setStatus(MARK_DELETED);
        this.setChangeDate(new Date());
        this.save();
    }

}
