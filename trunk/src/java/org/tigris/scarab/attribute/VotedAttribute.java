package org.tigris.scarab.attribute;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.baseom.*;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.om.security.TurbineUser;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.*;

import com.workingdogs.village.*;

import java.util.*;
/**
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision$ $Date$
 */
public abstract class VotedAttribute extends OptionAttribute
{
    private Hashtable votes;
    private String result;
    
    protected Hashtable getVotes()
    {
        return votes;
    }
    
    /** loads Internal Value from the database
     *
     */
    public void init() throws Exception
    {
        int i;
        votes = new Hashtable();
        ScarabIssueAttributeVote vote;
        Criteria crit = new Criteria();
        crit.add(ScarabIssueAttributeVotePeer.ATTRIBUTE_ID, getId())
            .add(ScarabIssueAttributeVotePeer.ISSUE_ID, getIssue().getPrimaryKeyAsLong());
        Vector res = ScarabIssueAttributeVotePeer.doSelect(crit);
        for (i=0; i<res.size(); i++)
        {
            vote = (ScarabIssueAttributeVote)res.get(i);
            votes.put(new Integer(vote.getUserId()), getOptionById(vote.getOptionId()));
        }
        Criteria crit1 = new Criteria()
            .add(ScarabIssueAttributeValuePeer.ATTRIBUTE_ID, getId())
            .add(ScarabIssueAttributeValuePeer.ISSUE_ID, getIssue().getPrimaryKeyAsLong());
        if (ScarabIssueAttributeValuePeer.doSelect(crit1).size()==1)
            loaded = true;
        result = computeResult();
    }

    /**
     *  This method calculates result of the vote
     *
     */
    protected abstract String computeResult();
    
    /** Updates both InternalValue and Value of the Attribute object and saves them
     * to database
     * @param newValue String representation of new value.
     * @param data app data. May be needed to get user info for votes and/or for security checks.
     * @throws Exception Generic exception
     *
     */
    public void setValue(String newValue,RunData data) throws Exception
    {
        Integer userId = new Integer(((ScarabUser)data.getUser()).getPrimaryKeyAsInt());
        ScarabAttributeOption vote = getOptionById(Integer.parseInt(newValue));
        Criteria crit = new Criteria();
        crit.add(ScarabIssueAttributeVotePeer.ISSUE_ID, getIssue().getPrimaryKeyAsLong())
            .add(ScarabIssueAttributeVotePeer.ATTRIBUTE_ID, getId())
            .add(ScarabIssueAttributeVotePeer.USER_ID, ((TurbineUser)data.getUser()).getPrimaryKeyAsLong());
        if (votes.containsKey(userId))
        {
            if (newValue == null)
            {
                // withdraw the vote
                ScarabIssueAttributeVotePeer.doDelete(crit);
                votes.remove(userId);
            }
            else
            {
                //change the vote
                crit.add(ScarabIssueAttributeVotePeer.OPTION_ID, vote.getId()); //FOIXME: is this correct?
                ScarabIssueAttributeVotePeer.doUpdate(crit);
                votes.put(userId, vote);
            }
        }
        else
        {
            if (newValue == null)
            {
                //there was no vote and user tries to withdraw it. Do nothing or maybe throw?
                return;
            }
            else
            {
                //new vote
                crit.add(ScarabIssueAttributeVotePeer.OPTION_ID, vote.getId());
                ScarabIssueAttributeVotePeer.doInsert(crit);
                votes.put(userId, vote);
            }
        }
        
        result = computeResult();
        Criteria crit1 = new Criteria();
        crit1.add(ScarabIssueAttributeValuePeer.ATTRIBUTE_ID, getId())
            .add(ScarabIssueAttributeValuePeer.ISSUE_ID, getIssue().getId())
            .add(ScarabIssueAttributeValuePeer.VALUE, result);
        if (loaded)
        {
            ScarabIssueAttributeValuePeer.doUpdate(crit1);
        }
        else
        {
            ScarabIssueAttributeValuePeer.doInsert(crit1);
            loaded = true;
        }
    }
    /** Gets the Value attribute of the Attribute object
     *
     * @return    The Value value
     */
    public String getValue()
    {
        return result;
    }
}