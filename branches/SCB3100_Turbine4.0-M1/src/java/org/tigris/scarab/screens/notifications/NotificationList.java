package org.tigris.scarab.screens.notifications;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.screens.Default;

/**
 * Support class for the NotificationList screen.
 * It puts in the context:
 * <ul>
 * <li>
 * <b>notifications</b>: List of the notifications for the user</b>
 * </li>
 * </ul>
 * 
 * @author jorgeuriarte
 * 
 */
public class NotificationList extends Default
{

    protected void doBuildTemplate(RunData data, TemplateContext context) throws Exception
    {
        List notifs = NotificationStatusManager
                .getNotificationsFor((ScarabUser) data.getUser());
        OrderedMap issueSets = new ListOrderedMap();
        for (Iterator it = notifs.iterator(); it.hasNext(); )
        {
            NotificationStatus notif = (NotificationStatus)it.next();
            Issue issue = notif.getActivity().getIssue();
            OrderedMap setActivities = (OrderedMap)issueSets.get(issue);
            if (null == setActivities)
            {
                setActivities = new ListOrderedMap();
                issueSets.put(issue, setActivities);
            }
            Activity act = notif.getActivity();
            ActivitySet set = act.getActivitySet();
            List notifications = (List)setActivities.get(set);
            if (null == notifications)
            {
                notifications = new ArrayList();
                setActivities.put(set, notifications);
            }
            notifications.add(notif);
        }
        context.put("notificationStructure", issueSets);
        super.doBuildTemplate(data, context);
    }
    
}