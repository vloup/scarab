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

import java.util.List;
import java.util.Date;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

// import org.apache.turbine.om.peer.BasePeer;
// import org.apache.turbine.Log;
// import org.apache.turbine.util.db.pool.DBConnection;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.util.ScarabException;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class Activity 
    extends BaseActivity
    implements Persistent
{
    private Attribute aAttribute;                 
    private AttributeOption oldAttributeOption;                 
    private AttributeOption newAttributeOption;                 


    /**
     * Gets the Attribute that was changed for this Activity record.
     */
    public Attribute getAttribute() throws TorqueException
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = AttributeManager.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    /**
     * Sets the Attribute that was changed for this Activity record.
     */
    public void setAttribute(Attribute v) throws TorqueException
    {
        aAttribute = v;
        super.setAttribute(v);
    }

    /**
     * Populates a new Activity object.
     */
    public void create(Issue issue, Attribute attribute, 
                       String desc, Transaction transaction,
                       int oldNumericValue, int newNumericValue,
                       NumberKey oldUserId, NumberKey newUserId,
                       NumberKey oldOptionId, NumberKey newOptionId,
                       String oldValue, String newValue)
         throws TorqueException, Exception
    {
        create(issue, attribute, desc, transaction,
               oldNumericValue, newNumericValue,
               oldUserId, newUserId,
               oldOptionId, newOptionId,
               oldValue, newValue, null);
        issue.getActivity(true).add(this);
    }

    /**
     * Populates a new Activity object.
     */
    public void create(Issue issue, Attribute attribute, 
                       String desc, Transaction transaction,
                       int oldNumericValue, int newNumericValue,
                       NumberKey oldUserId, NumberKey newUserId,
                       NumberKey oldOptionId, NumberKey newOptionId,
                       String oldValue, String newValue, DBConnection dbCon)
         throws TorqueException
    {
            setIssue(issue);
            if (attribute == null)
            {
                attribute = Attribute.getInstance(0);
            }
            setAttribute(attribute);
            setDescription(desc);
            setTransaction(transaction);
            setOldNumericValue(oldNumericValue);
            setNewNumericValue(newNumericValue);
            setOldUserId(oldUserId);
            setNewUserId(newUserId);
            setOldOptionId(oldOptionId);
            setNewOptionId(newOptionId);
            setOldValue(oldValue);
            setNewValue(newValue);
            if (dbCon == null) 
            {
                try
                {
                    save();
                }
                catch (Exception e)
                {
                    if (e instanceof TorqueException) 
                    {
                        throw (TorqueException)e;
                    }
                    else 
                    {
                        throw new TorqueException(e);
                    }
                }
            }
            else 
            {
                save(dbCon);
            }
            // Make sure new activity is added to activity cache
            List activityList = null;
            try
            {
                activityList = issue.getActivity(true);
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
            if (!activityList.contains(this))
            {
                activityList.add(this);
            }
    }

    /**
     * Populates a new Activity object.
     */
    public void create(Issue issue, Attribute attribute, 
                       String desc, Transaction transaction,
                       String oldValue, String newValue)
         throws TorqueException
    {
        create(issue, attribute, desc, transaction,
               0, 0, null, null, null, null,
               oldValue, newValue, null);
    }


    /**
     * Gets the AttributeOption object associated with the Old Value field
     * (i.e., the old value for the attribute before the change.)
     */
    public AttributeOption getOldAttributeOption() throws Exception
    {
        if ( oldAttributeOption==null && (getOldValue() != null) )
        {
            oldAttributeOption = AttributeOptionManager
                .getInstance(new NumberKey(getOldValue()));
        }
        return oldAttributeOption;
    }

    /**
     * Sets the Old Attribute Option associated with this Activity record
    public void setOldAttributeOption(AttributeOption v) throws Exception
    {
        oldAttributeOption  = v;
        super.setOldValue(v);
    }
     */

    /**
     * Gets the AttributeOption object associated with the New Value field
     * (i.e., the new value for the attribute after the change.)
     */
    public AttributeOption getNewAttributeOption() throws Exception
    {
        if ( newAttributeOption==null && (getNewValue() != null) )
        {
            newAttributeOption = AttributeOptionManager
                .getInstance(new NumberKey(getNewValue()));
        }
        return newAttributeOption;
    }

    /**
     * Sets the New Attribute Option associated with this Activity record
    public void setNewAttributeOption(AttributeOption v) throws Exception
    {
        newAttributeOption  = v;
        super.setNewValue(v);
    }
     */

    public void save(DBConnection dbCon)
        throws TorqueException
    {
        // make sure to mark last related activity as done
        if ( isNew() ) 
        {
            Criteria crit = new Criteria();
            crit.add(ActivityPeer.ISSUE_ID, getIssueId());
            crit.add(ActivityPeer.ATTRIBUTE_ID, getAttributeId());
            crit.add(ActivityPeer.END_DATE, null);
            List result = ActivityPeer.doSelect(crit);
            if ( result.size() == 1 ) 
            {
                Activity a = (Activity)result.get(0);
                a.setEndDate(getTransaction().getCreatedDate());
                a.save(dbCon);
            }
            else if ( result.size() > 1 ) 
            {
                // something is wrong with database
                throw new TorqueException(
                    new ScarabException("Multiple activities on the same"
                                        +" attribute are active.") );
            }
            else if ( result.size() == 0 ) 
            {
                // this is okay if the issue is new or has had no previous
                // activity on this attribute.  Go ahead and check that
                // database is not corrupt.
                crit = new Criteria();
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivityPeer.ATTRIBUTE_ID, getAttributeId());
                result = ActivityPeer.doSelect(crit);
                if ( result.size() != 0 ) 
                {
                    throw new TorqueException(
                        new ScarabException("Previous activity has occured" 
                        + " on the same attribute but none are active.") );
                }
            }
        }
        super.save(dbCon);
    }
}



