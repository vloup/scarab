//  ================================================================
//  Copyright (c) 2000-2005 CollabNet.  All rights reserved.
//  
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//  
//  1. Redistributions of source code must retain the above copyright
//  notice, this list of conditions and the following disclaimer.
// 
//  2. Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  
//  3. The end-user documentation included with the redistribution, if
//  any, must include the following acknowlegement: "This product includes
//  software developed by Collab.Net <http://www.Collab.Net/>."
//  Alternately, this acknowlegement may appear in the software itself, if
//  and wherever such third-party acknowlegements normally appear.
//  
//  4. The hosted project names must not be used to endorse or promote
//  products derived from this software without prior written
//  permission. For written permission, please contact info@collab.net.
//  
//  5. Products derived from this software may not use the "Tigris" or 
//  "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
//  prior written permission of Collab.Net.
//  
//  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
//  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
//  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
//  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
//  IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 
//  ====================================================================
//  
//  This software consists of voluntary contributions made by many
//  individuals on behalf of Collab.Net.
package org.tigris.scarab.om;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.notification.NotificationManagerFactory;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class NotificationFilterPeer
    extends BaseNotificationFilterPeer
{
    
    /**
     * Return the list of configured managers for this user, this module
     * and this activityType.
     * NOTE: Currently only the Scarab default Notification manager is
     * supported, so the List will mostly contain 1 element. It isplanned
     * to add more managers in the future, so be prepared to find multiple
     * entries in the List.
     * If no manager is configured, return an empty List
     * @param moduleId
     * @param userId
     * @param activityType
     * @return
     * @throws TorqueException 
     */
    public List getCustomization(Object moduleId, Object userId, Object activityType) throws TorqueException
    {
    	List entries = null;
    	Criteria crit = new Criteria();
    	crit.add(MODULE_ID,       moduleId,     Criteria.EQUAL);
    	crit.add(USER_ID,         userId,       Criteria.EQUAL);
    	crit.add(ACTIVITY_TYPE,   activityType, Criteria.EQUAL);
    	try {
    		entries = doSelect(crit);
    	} catch (TorqueException e) {
    		log.error("getPendingNotifications(): " + e);
    	}
        if(entries.size()==0)
        {
            NotificationFilter filter = 
                NotificationFilter.createDefaultFilter(
                        (Integer)moduleId,
                        (Integer)userId,
                        NotificationManagerFactory.getInstance().getManagerId(),
                        ActivityType.getActivityType((String)activityType));
        }
     	return entries;
    }
    
    public Map getCustomization(Object moduleId, Object userId) throws TorqueException
    {
    	Map entries = new Hashtable();
    	Criteria crit = new Criteria();
    	Set codes = ActivityType.getActivityTypeCodes();
    	Iterator iter = codes.iterator();
    	while(iter.hasNext())
    	{
    	    String code = (String)iter.next();
    	    List items = getCustomization(moduleId, userId, code);
    	    entries.put(code,items);
    	}
     	return entries;
    }
    
}
