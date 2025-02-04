package org.tigris.scarab.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.tigris.scarab.actions.base.ScarabTemplateAction;
import org.tigris.scarab.notification.NotificationManagerFactory;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationRule;
import org.tigris.scarab.om.NotificationRuleManager;
import org.tigris.scarab.om.NotificationRulePeer;
import org.tigris.scarab.om.NotificationStatus;
import org.tigris.scarab.om.NotificationStatusPeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabGlobalTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabConstants;

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
    /**
     * Delete notifications from the current user's notification list.
     * This method is mainly called from the Notification Screen to
     * remove marked notifications. We assume the request parameters
     * follow the name convention: 
     * 
     * name="notificationId_${ActivityId}_${CreatorId}_${ReceiverId}"
     * 
     * with all ${..} variables as integer numbers.
     * @param data
     * @param context
     * @throws Exception
     */
    public void doDeletenotifications( RunData data, TemplateContext context)
    throws Exception
    {                
        deleteMarkedEntries(data, context);
    }
    

    /**
     * This is the work horse for the Notification deletion process.
     * @param data
     * @param context
     * @throws Exception
     */
    private void deleteMarkedEntries(RunData data, TemplateContext context)
            throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        ScarabUser user = (ScarabUser) data.getUser();

        // loop over all notification ids contained in the
        // current parameter set. These notifications will
        // be removed from the Notificaiton List now without
        // rollback!
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
     * Get the primary keys for the object to be deleted.
     * We assume the following syntax in the given String:
     * 
     * key="notificationId_${ActivityId}_${CreatorId}_${ReceiverId}"
     * 
     * The returned array contains three keys in the order:
     * 
     * ActivityId
     * CreatorId
     * ReceiverId
     * 
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
    
    // ===============================================================
    // The following methods are primary for customization issues
    // ===============================================================

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
        String key;
        ScarabUser   user   = (ScarabUser) data.getUser();
        Integer userId = user.getUserId();

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        Integer moduleId = module.getModuleId();
        
        
        // The ruleMap contains all rules for this user and this module
        NotificationRulePeer nfp = new NotificationRulePeer();
        Map<String, List<NotificationRule>> ruleMap = NotificationRulePeer.getCustomization(moduleId, userId);

        // The list of activityTypes
        ScarabGlobalTool scarabG = getScarabGlobalTool(context);
        List activityTypeList = scarabG.getAllNotificationTypeCodes();
        
        Iterator iter = activityTypeList.iterator();
        while(iter.hasNext())
        {
            String code = (String)iter.next();
            boolean theStatus      = data.getParameters().getBoolean(code+":status");
            boolean theSendSelf    = data.getParameters().getBoolean(code+":self");
            boolean theSendFailure = data.getParameters().getBoolean(code+":fail");
            Integer managerId      = NotificationManagerFactory.getInstance().getManagerId();
            
            NotificationRule rule = null;
            List<NotificationRule> rules = ruleMap.get(code);
            if(rules.size() == 0)
            {    
                rule = NotificationRule.createDefaultRule
                (
                    moduleId,
                    userId,
                    managerId,
                    code
                );
            }
            else
            {
                rule = rules.get(0);
            }
            markUpdateOrNew(rule);
            
            // adjust the new attribute values
            rule.setSendSelf(theSendSelf);
            rule.setSendFailures(theSendFailure);
            rule.setFilterState(theStatus);
            
            // finally modify in database.
            modifyInDatabase(rule);
        }
        scarabR.setConfirmMessage(L10NKeySet.ChangesSaved);
    }


    /**
     * Check whether the rule needs to be created, updated or removed
     * and process the particular database call.
     * from the database.
     * @param rule
     * @throws TorqueException
     * @throws Exception
     */
    private void modifyInDatabase(NotificationRule rule) throws TorqueException, Exception
    {
        if (equalsDefaultCustomization(rule))
        {
            if (rule.isNew())
            {
                // don't need to create this rule
            }
            else
            {
                // can safely remove this rule
                ObjectKey pk = rule.getPrimaryKey();
                NotificationRulePeer.doDelete(pk);
            }
         }
        else
        {
            // need to store this rule
            rule.save();
        }
    }


    /**
     * Check wether the given rule is allready contained
     * in the repository and mark it either as new or
     * already existing.
     * @param rule
     * @return
     */
    private void markUpdateOrNew(NotificationRule rule)
    {
        // Check if the entry already exists in database:
        ObjectKey pk = rule.getPrimaryKey();
        try
        {
            if (NotificationRuleManager.getInstance(pk) != null)
            {
                rule.setNew(false);
            }
        }
        catch (Exception e)
        {
            rule.setNew(true);
        }
    }


    /**
     * Check equality to default customization.
     * This rule is equal to the default rule, when it
     * is equal in all attributes.
     * Currently the default rule is hard coded, see below
     * @param rule
     * @return
     */
    private boolean equalsDefaultCustomization(NotificationRule rule)
    {
        // currently we assume, that the rule is
        // equivalent to the default setting when:
        
        if (!rule.getFilterState()) return false;  // rule enabled
        if (rule.getSendSelf())     return false;  // dont send to me
        if (rule.getSendFailures()) return false;  // dont send failures

        // this behaviour will be changed as soon as default settings
        // are available.
        
        return true;
    }
    
    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    public ScarabGlobalTool getScarabGlobalTool(TemplateContext context)
    {
        return (ScarabGlobalTool)context
            .get(ScarabConstants.SCARAB_GLOBAL_TOOL);
    }
        
}
