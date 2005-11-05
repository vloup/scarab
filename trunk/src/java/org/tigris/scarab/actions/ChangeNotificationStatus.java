package org.tigris.scarab.actions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.actions.base.ScarabTemplateAction;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationFilter;
import org.tigris.scarab.om.NotificationFilterManager;
import org.tigris.scarab.om.NotificationFilterPeer;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusManager;
import org.tigris.scarab.om.NotificationStatusPeer;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabGlobalTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;

/* ================================================================
 * Copyright (c) 2005 CollabNet.  All rights reserved.
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


/**
 * @author hdab
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChangeNotificationStatus extends ScarabTemplateAction
{
    public void doDeletenotifications( RunData data, TemplateContext context)
    throws Exception
    {                
        deleteMarkedEntries(data, context);
    }
    
    
    public void doCustomize( RunData data, TemplateContext context)
    throws Exception
    {                
        customize(data, context);
    }

    
    /**
     * @param data
     * @param context
     * @throws TorqueException
     */
    private void customize(RunData data, TemplateContext context) 
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        ScarabUser   user   = (ScarabUser) data.getUser();
        Integer userId = user.getUserId();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        Integer moduleId = module.getModuleId();
        
        NotificationFilterPeer nfp = new NotificationFilterPeer();
        Map filterMap = nfp.getCustomization(moduleId, userId);
        
        for (int i = 0; i < keys.length; i++)
        {
            key = keys[i].toString();
            CustomizationItem item = null;
            try
            {
                item = new CustomizationItem(key);
            }
            catch(RuntimeException rte)
            {
                item = null; // the key does neither contain an ActivityType nor a value.
                             // This is not an error.
            }
            if (item != null)
            {
                // Setup new entry.
                NotificationFilter filter = NotificationFilterManager.getInstance();
                filter.setModuleId(moduleId);
                filter.setUserId(userId);
                filter.setActivityType(item.getCode());
                filter.setFilterType(item.getValue());
                if (NotificationFilterManager.exists(filter))
                {
                    // Nothing to do. filter already exists.
                }
                else
                {
                    filter.save(); // create new Filter in DB
                }
                
                List filters = (List)filterMap.get(item.getCode());
                Iterator iter = filters.iterator();
                while(iter.hasNext())
                {
                    NotificationFilter nf = (NotificationFilter)iter.next();
                    if(nf.equals(filter))
                    {
                        filters.remove(nf);
                        break;
                    }
                }
            }
        }
        
        // Now remove all remaining filters in the filtermap from the DB
        
        Iterator iter = filterMap.keySet().iterator();
        while(iter.hasNext())
        {
            key = (String)iter.next();
            List filters = (List)filterMap.get(key);
            for (int index=0; index < filters.size(); index++)
            {
                NotificationFilter filter = (NotificationFilter) filters.get(index);
                NotificationFilterPeer.doDelete(filter);
            }
        }
        
    }


    public void deleteMarkedEntries(RunData data, TemplateContext context)
            throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        ScarabUser user = (ScarabUser) data.getUser();

        for (int i = 0; i < keys.length; i++)
        {
            key = keys[i].toString();
            if (key.toLowerCase().startsWith("notificationid_"))
            {
                Object[] pkeys = extractPrimarykeys(key);
                Long tid    = (Long)pkeys[0];
                Integer cid = (Integer)pkeys[1];
                Integer rid = (Integer)pkeys[2];
                NotificationStatus ns = NotificationStatusPeer.retrieveByPK(tid,cid,rid);
                ns.markDeleted();
            }
        }
    }

    
    /**
     * @param key
     * @return
     */
    private Object[] extractPrimarykeys(String keys)
    {
        Object[] keyset = new Object[3]; 
        String subkey;
        
        StringTokenizer stok = new StringTokenizer(keys,"_");
        stok.nextToken(); // remove leading "id_"
        subkey = stok.nextToken(); // this is the TRANSACTION_ID
        keyset[0] = Long.decode(subkey);
        
        subkey = stok.nextToken(); // this is the CREATOR_ID
        keyset[1] = Integer.decode(subkey);

        subkey = stok.nextToken(); // this is the RECEIVER_ID
        keyset[2] = Integer.decode(subkey);

        return keyset;
    } 

    
    
    private class CustomizationItem
    {
        private ActivityType type;
        private Integer      value;

        CustomizationItem(String key)
        {
            int sepi = key.indexOf(':');
            if(sepi<0)
            {
                throw new RuntimeException("Invalid keyCode");
            }

            String typeCode = key.substring(0,sepi);
            String val      = key.substring(sepi+1);
            type            = ActivityType.getActivityType(typeCode);
            value           = Integer.decode(val);
        }
        
        String getCode()
        {
            return type.getCode();
        }
        
        Integer getValue()
        {
            return value;
        }
    }
        
}
