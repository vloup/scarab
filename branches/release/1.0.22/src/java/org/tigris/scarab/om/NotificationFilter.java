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


import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.tigris.scarab.notification.ActivityType;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class NotificationFilter
    extends BaseNotificationFilter
    implements Persistent
{

    /**
     * Create a default filter.
     * 
     * @param moduleId
     * @param userId
     * @param managerId
     * @param activityType
     * @return
     * @throws TorqueException
     */
    public static NotificationFilter createDefaultFilter(Integer moduleId,
                                            Integer userId,
                                            Integer managerId,
                                            ActivityType activityType) throws TorqueException
    {
        NotificationFilter filter = new NotificationFilter();
        filter.setModuleId(moduleId);
        filter.setUserId(userId);
        filter.setActivityType(activityType.getCode());
        filter.setManagerId(managerId);

        // default settings.
        // currently hard coded. will later be
        // replaced by a default customization

        filter.setFilterState(true);   // enabled by default
        filter.setSendSelf(false);     // dont send to myself by default
        filter.setSendFailures(false); // dont notify me aout failures by default
        
        return filter;
    }
}
