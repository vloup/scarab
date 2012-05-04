package org.tigris.scarab.om;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.notification.Notification;

/**
 * Notification rule table to determine when a notification has to be sent out to whom. Used by NotificationManager.
 *
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class NotificationRulePeer
    extends org.tigris.scarab.om.BaseNotificationRulePeer
{
        
        /**
         * Return the list of configured managers for this user, this module
         * and this activityType.
         * NOTE: Currently only the Scarab default Notification manager is
         * supported, so the List will mostly contain 1 element. It is planned
         * to add more managers in the future, so be prepared to find multiple
         * entries in the List.
         * If no manager is configured, return an empty List
         * @param moduleId
         * @param userId
         * @param activityType
         * @return
         * @throws TorqueException 
         */
        static public List<NotificationRule> getCustomization(Object moduleId, Object userId, Object activityType) throws TorqueException
        {
            List<NotificationRule> entries = null;
            Criteria crit = new Criteria();
            crit.add(MODULE_ID,       moduleId,     Criteria.EQUAL);
            crit.add(USER_ID,         userId,       Criteria.EQUAL);
            crit.add(ACTIVITY_TYPE,   activityType, Criteria.EQUAL);
            try {
                entries = (List<NotificationRule>)doSelect(crit);
            } catch (TorqueException e) {
                log.error("getPendingNotifications(): " + e);
            }
            /*
            if(entries.size()==0)
            {
                NotificationRule rule = 
                    NotificationRule.createDefaultRule(
                            (Integer)moduleId,
                            (Integer)userId,
                            NotificationManagerFactory.getInstance().getManagerId(),
                            ActivityType.getActivityType((String)activityType));
            }
            */
            return entries;
        }
        
        static public Map<String,List<NotificationRule>> getCustomization(Object moduleId, Object userId) throws TorqueException
        {
            Map<String,List<NotificationRule>> entries = new Hashtable<String,List<NotificationRule>>();
            Set<String> codes = (Set<String>)ActivityType.getActivityTypeCodes();
            Iterator<String> iter = codes.iterator();
            while(iter.hasNext())
            {
                String code = (String)iter.next();
                List<NotificationRule> items = getCustomization(moduleId, userId, code);
                entries.put(code,items);
            }
            return entries;
        }
        
        static public List<NotificationRule> getNotificationRules(ScarabUser user, Module module) throws TorqueException
        {
            List<NotificationRule> entries = new ArrayList<NotificationRule>();
            Set<String> codes = (Set<String>)ActivityType.getActivityTypeCodes();
            Iterator<String> iter = codes.iterator();
            Integer moduleId = module.getModuleId();
            Integer userId   = user.getUserId();
            while(iter.hasNext())
            {
                String code = (String)iter.next();
                List<NotificationRule> items = getCustomization(moduleId, userId, code);
                if(items != null && items.size()>0)
                entries.add(items.get(0));
            }
            return entries;
        }
        
        /**
         * UpLoad the User Notification options' IDs from the template combo.
         * @param aOptionId
         * @throws TorqueException
         */
        public static void saveConditions(ScarabUser user, Module module, Integer[] aOptionId, Integer operator) throws TorqueException
        {
            deleteConditions(user, module);
            ConditionManager.clear();
            if (aOptionId != null)
                for (int i=0; i<aOptionId.length; i++)
                {
                    if (aOptionId[i].intValue() != 0)
                    {
                        Condition cond = new Condition();
                        cond.setAttributeId(null);
                        cond.setOptionId(aOptionId[i]);
                        cond.setModuleId(module.getModuleId());
                        cond.setIssueTypeId(null);
                        cond.setTransitionId(null);
                        cond.setUserId(user.getUserId());
                        cond.setOperator(operator);
                        cond.save();
                    }
                }
        }

        /**
         * Returns the list of conditions associated for the given user/module
         * combination. Used with NotificationManager.
         * @param user
         * @param module
         * @return
         * @throws TorqueException
         */
        public static List<Condition> getConditions(ScarabUser user, Module module) throws TorqueException
        {
            List<Condition> result = null;
            Criteria crit = new Criteria();
            crit.add(ConditionPeer.USER_ID, user.getUserId());
            crit.add(ConditionPeer.MODULE_ID, module.getModuleId());
            result = (List<Condition>)ConditionPeer.doSelect(crit);
            return result;
        }
        
        public static Notification getEmptyNotificationFor(ScarabUser user, Module module) throws TorqueException
        {
            Notification notification = null;
            return notification;
            
        }

        public static void deleteConditions(ScarabUser user, Module module) throws TorqueException
        {
            Criteria crit = new Criteria();
            crit.add(ConditionPeer.ATTRIBUTE_ID, null);
            crit.add(ConditionPeer.MODULE_ID, module.getModuleId());
            crit.add(ConditionPeer.ISSUE_TYPE_ID, null);
            crit.add(ConditionPeer.TRANSITION_ID, null);
            crit.add(ConditionPeer.USER_ID, user.getUserId());
            ConditionPeer.doDelete(crit);
        }
        
        public static List<AttributeOption> getSelectedAttributeOptionsForNotification(ScarabUser user, Module module)
        {
            return null;
        }
}
