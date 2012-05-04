package org.tigris.scarab.notification;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Condition;
import org.tigris.scarab.om.Conditioned;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationRule;
import org.tigris.scarab.om.NotificationRulePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Log;

public class Notification implements Conditioned
{
    private List<Condition> conditions;
    private Set<Integer> optionIds;
    private List<NotificationRule> rules;
    private Issue issue;
    private ScarabUser user;
    private Module module;
    
    public static Logger log = Log.get(Notification.class.getName());
    

    /**
     * This is a NotificationPlaceholder. It is NOT associated to an issue
     * and it serves only as support for the NotificationCondition editor.
     * It could be used as generic instance, which can be dynamically associated 
     * using setIssue() (see below)
     **/
    public Notification(ScarabUser user, Module module) throws TorqueException
    {
        this.user       = user;
        this.module     = module;

        this.conditions = null; // creation on demand
        this.optionIds  = null; // creation on demand
        this.rules      = null; // creation on demand
        this.issue      = null; // no issue --> for ConditionEditor only
    }

    /**
     * Create a new Notification instance for a given user/issue pair
     * This instance can be checked for send conditions. It is currently
     * used solely in the Notificationmanager
     **/        
    public Notification(ScarabUser user, Issue issue ) throws TorqueException
    {
        this.user       = user;
        this.module     = issue.getModule();
        this.issue      = issue;
        this.rules      = null;
        this.conditions = null;
    }
    
    /**
     * set the issue for this notification (currently not used and added
     * for future features)
     */
    public void setIssue(Issue issue) throws TorqueException
    {
        this.module     = issue.getModule();
        this.issue      = issue;
        this.conditions = null;
        this.rules      = null;
        this.optionIds  = null;
    }

    /**
     * Get the list of Condition instances for this context
     * the List contains an array of Integers
     */
    public Integer[] getConditionsArray() 
    {
        Integer[] entries = new Integer[conditions.size()];
        Iterator<Condition> iter;
        try {
            iter = getConditions().iterator();
        } catch (TorqueException e) {
            throw new RuntimeException(e);
        }
        int index = 0;
        while(iter.hasNext())
        {
            Condition condition = iter.next();
            entries[index] = condition.getConditionId().intValue();
        }
        return entries;
    }

    /**
     * Return the boolean operator to be used to combine different attributes.
     * Although the operator is fully implemented, each Conditioned class
     * still must implement its usage. Currently the operator is only 
     * active for Notification conditions.
     * @return
     */
    public Integer getConditionOperator()
    {
        Integer operator = 0;
        List<Condition> conditions;
        try {
            conditions = getConditions();
        } catch (TorqueException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        if(conditions.size() > 0)
        {
            operator = conditions.get(0).getOperator();
        }
        return operator;
    }
    

    /**
     * Return s true, if the user has defined conditions for this 
     * module via "condition rules" in the admin toolbox (in scarab GUI)
     */
    public boolean isConditioned()
    {
        boolean conditioned;
        try {
            conditioned = getConditions().size() > 0;
        } catch (TorqueException e) 
        {
            throw new RuntimeException(e);
        }
        return conditioned;
    }

    /**
     * Check, if the given optionId is contained in the condition list
     **/
    public boolean isRequiredIf(Integer optionID) throws TorqueException 
    {
        boolean isRequired = getOptionIds().contains(optionID);
        return isRequired;
    }

    /**
     * For Condition editing: Create a conditionset for the associated user/module
     * combination containing the given list of OptionIds
     **/
    public void setConditionsArray(Integer[] optionId, Integer operator) throws TorqueException 
    {
        NotificationRulePeer.saveConditions(user, module, optionId, operator);
    }
    
    /**
     * Get the list of Condition ionstances which are defined for the given
     * user/module combination
     **/
    public List<Condition> getConditions() throws TorqueException
    {
        if(conditions == null)
        {
            conditions = NotificationRulePeer.getConditions(user, module);
        }
        return conditions;
    }

    /**
     * Get the send rules. Currently the returned list contains 18 entries, one for each activityType.
     * Explanation: Each Activity_type has an associated send rule which allows to enable/disable 
     * notification sending depending on what modifications have been made on an issue. 
     * E.g. you could create a rule, which effectively disables any notification to the user
     * when an issue-attachment is added, or when a comment is added, etc.
     * The interface on the Scarab GUI is in the admin toolbox ("notification rules")
     **/
    public List<NotificationRule> getRules() throws TorqueException
    {
        if(rules == null)
        {
            NotificationRulePeer.getNotificationRules(user, module);
        }
        return rules;
    }

    /**
     * Get the list of OptionIDs associated to the set of conditions
     * Take care to NOT intermix conditions and rules. A condition
     * specifies which optionId has to be set to a specific value in order
     * to let the condition match. While a rule applies to the current issueType
     * (and if set to disabled it disables notiofications disregarding any conditions)
     * In order to get conditions into effect, at least one of the rules must be set 
     * to "enabled". Otherwise notification sendinjg is completely suppressed.
     **/ 
    private Set<Integer> getOptionIds() throws TorqueException 
    {
        Iterator<Condition> iter = getConditions().iterator();
        Set<Integer>optionIds = new HashSet<Integer>();
        while(iter.hasNext())
        {
            Condition condition = iter.next();
            Integer optionId = condition.getOptionId();
            if(optionId != null)
            {
                optionIds.add(optionId);
            }
        }
        return optionIds;
    }

    /**
     * Check if the current combination of user/module/issue-content
     * meets the conditions under which a notification shall be sent.
     * Note: If the condition operator is set to "OR", any matching
     * notification option in any attribute will return true
     * If the operator is set to "AND" Each attribute for which options have
     * been set must match at least one of the selected options.
     * hence you could for instance let notifications only send when
     * attribute status="closed" AND severity="show stopper"
     * Note: Currently it is not possible to define multiple condition sets for
     * one user/module pair.
     **/
    public boolean sendConditionsMatch() throws TorqueException 
    {
        List<Condition> conditions = getConditions();
        if(conditions.size() == 0)
        {
            return true; // no conditions defined, hence no constraints, hence return true
        }
        
        Map<String,AttributeValue> attributeValues =  issue.getModuleOptionAttributeValuesMap();
        Integer operator = getConditionOperator();
        
        Iterator<Condition> iter = conditions.iterator();
        Set<Integer>attributeIds = new HashSet<Integer>();

        String forIssueId = "";
        if(this.issue!=null)
        {
            forIssueId = "for issue " + this.issue.getIdPrefix()+ this.issue.getIdCount();
        }
        log.info("test sendNotification conditions "+forIssueId+" -------------");
        
        int matchCounter = 0;
        while(iter.hasNext())
        {
            Condition condition = iter.next();
            AttributeOption ao  = condition.getAttributeOption();
            int optionId = ao.getOptionId();
            Attribute attribute = ao.getAttribute();
            int attributeId = attribute.getAttributeId();
            attributeIds.add(attributeId);
            
            AttributeOption option = AttributeOptionManager.getInstance(optionId);
            
            //log.info("test for " + attribute.getName() + "=\"" + option.getName()    + "\"  (att:" + + attributeId + ",opt:" + optionId+")");
            
            Iterator<Entry<String,AttributeValue>> attvalIterator = attributeValues.entrySet().iterator();
            while( attvalIterator.hasNext())
            {
                Entry<String,AttributeValue> e = attvalIterator.next();
                AttributeValue av = e.getValue();
                int attvalAttributeId = av.getAttributeId();
                Attribute att = av.getAttribute();
                AttributeOption attOpt = av.getAttributeOption();
                if(attributeId == attvalAttributeId)
                {
                    int aoi = av.getOptionId();
                    boolean match = aoi == optionId;
                    //log.info( ((match)? "match ":"      ") + att.getName()       + "=\"" + attOpt.getName()    + "\"  (att:" + attvalAttributeId + ",opt:" + attOpt.getOptionId());
                    if( match)
                    {
                        String sendConditionMet = "SendCondition met. ("+attribute.getName() + "=\"" + option.getName()+")";
                        matchCounter++;
                        if ( operator.equals(OR) )
                        {
                            log.info(sendConditionMet + " -> (send notification)");
                            return true;  // quick exit. first match gives success.
                        }
                        log.info(sendConditionMet);
                        break; // we can break the inner iteration (we found the match)
                    }                    
                }
            }
            
        }

        boolean match; // just in case no condition is defined, we return true
        if(operator.equals(AND) && attributeIds.size() == matchCounter)
        {
            log.info("SendCondition met. (all conditions matched)");
            match = true; // operator 
        }
        else
        {
            if (matchCounter > 0)
            {
                log.info("Only " + matchCounter + " send Conditions out of "+attributeIds.size()+" met. (Send is not triggered.)");
            }
            else
            {
                log.info("None of the " + attributeIds.size() + " available conditions was applicable to this issue. (Send is not triggered.)" );
            }
            match = false;
        }
        
        return match;
    }

    
}
