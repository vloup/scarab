package org.tigris.scarab.workflow;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Condition;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.tools.Environment;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.SimpleSkipFiltering;
import org.tigris.scarab.util.SkipFiltering;

/**
 * The state class contains a collection of methods, which introduce "state" to Scarab.
 * Following states are generically supported:
 * 
 * sealed: the issue is in a mode where it should not be modified any more
 * active: the issue is currently processed
 * onhold: the issue is currently not processed, but it is still active (*)
 * 
 * (*)The onhold state is always accompanied with a revocationDate.
 * 
 * Currently the state support is only available on a system wide scope. It is
 * mainly configured by using the Turbine runtime properties. It is planned to
 * leverage status support at least to module scope. I prefer to leverage it
 * to module/issueType scope.
 * 
 * @author hdab
 *
 */
public class IssueState 
{

    private Issue issue; // the issue associated to this State instance
    
    public IssueState(Issue issue)
    {
        this.issue = issue;
    }

    /**
     * Create a javascript snippet, which can be used to check if the given
     * attribute would become required if a specific attributeOption was set.
     * This is an example javascript showing how to use this snippet:
     *
     * 
     *      // Helper array contains all RequiredMarkup elements
     *      // which have already been visited form the current call of
     *      // the conditions_observer. It is needed to correctly
     *      // set the display mode (inline/none) of all visited markers.
     *      
     *      var visitedMarkers = {};
     *     
     *      // This function takes care about the "required attribute" markup.
     *      // It adjusts the required-markup dynamically while the user edits the issue.
     *      //
     *      function conditions_observer(data)
     *      {
     *        if(data[0] == "treeview")
     *        {   
     *            var attributeName = data[1];
     *            var displayValue  = data[4];
     *            
     *            //alert(data[1] + "=" + data[4]);
     *            
     *            #if ($currentIssue)
     *#set ($indent=12)
     *$currentIssue.createIssueChecker("setMarkerValueIf", $indent)
     *            #end
     *            
     *            visitedMarkers = {};
     *
     *        }
     *      }
     *      
     *      // This helper function actually makes the requiredMarkup
     *      //  visible or hides it depending on the condition.
     *      //  Note: Currently only OR conditions are supported here!!!
     *      function setMarkerValueIf(condAttName, isRequired)
     *      {
     *          var cid = "conditional:"+condAttName;
     *          var element = getElementByIdCompatible(cid);
     *          if (element != null)
     *          {
     *              if(isRequired)
     *              {
     *                visitedMarkers[condAttName] = "inline";
     *              }
     *              else
     *              {
     *                if (visitedMarkers[condAttName]==null)              
     *                {
     *                   visitedMarkers[condAttName] = "none";
     *                }
     *              }
     *              //alert ("Set " + cid + " to " + visitedMarkers[condAttName] );
     *              element.style.display = visitedMarkers[condAttName];              
     *            }
     *      }
     * 
     * @param attribute
     * @return
     * @throws TorqueException
     * @throws ScarabException
     */
    public  SkipFiltering createIssueChecker(String setMarkerFunction, int indent) throws TorqueException, ScarabException
    {
        String result = "";
        List<Attribute> attributes = null;
        Module module = issue.getModule();
        IssueType issueType = issue.getIssueType();
        attributes = issueType.getActiveAttributes(module);
        Iterator<Attribute> iter = attributes.iterator();
        while (iter.hasNext())
        {
            Attribute attribute = iter.next();
            RModuleAttribute rma  = module.getRModuleAttribute(attribute, issueType);
            result += createIssueChecker(rma, setMarkerFunction, indent);
        }
        
        String prepend;
        if(result.length() > 0)
        {
            prepend="\n";
        }
        else
        {
            prepend = "/* No conditional attributes found for this module/issueType */";
        }
        
        return new SimpleSkipFiltering(prepend+result);
    }

    /**
     * Helper function. Creates javascript code to examine the available required conditions
     * in order to dynamically set/hide the "required attribute markers on screen.
     * @param rma
     * @param setMarkerFunction
     * @param indent
     * @return
     * @throws TorqueException
     * @throws ScarabException
     */
    private String createIssueChecker(RModuleAttribute rma, String setMarkerFunction, int indent) throws TorqueException, ScarabException
    {
        String result = "";
        boolean isRequired = rma.getRequired();
        if(!isRequired)
        {
            List<Condition> conditions = rma.getConditions();
                
            Iterator<Condition> iter = conditions.iterator();
            while(iter.hasNext())
            {
                Condition condition = iter.next();
                result += condition.createConditionCheckerScript(rma, setMarkerFunction, indent);
            }
        }
        return result;
    }

    
    

    /**
     * Check if the properties scarab.common.status.id and scarab.common.status.sealed
     * exist and if the current value of the status attribute matches the sealed
     * value. Return true, if the issue is in the sealed state, otherwise return false.
     * This method is used to find out if an issue shoul dbe rendered read-only
     * because it is in closed (sealed) state and should never be touched again.
     * @return
     * @throws TorqueException
     */
    public boolean isSealed() throws TorqueException
    {        
        boolean result = false;
        String status = Environment.getConfigurationProperty("scarab.common.status.id", null);
        if (status != null)
        {
            String value = Environment.getConfigurationProperty("scarab.common.status.sealed", null);
            if(value != null)
            {
                AttributeValue attval = issue.getAttributeValue(status);
                if(attval != null && attval.getValue().equals(value))
                {
                    result = true;
                }
            }
        }
        return result;
    }
    
    /**
     * Check if this issue is on hold. Currently we use the attribute
     * which has been specified by the system property 
     * scarab.common.status.id as the relevant attribute to check. 
     * We tell this issue is on hold when the status-attribute contains
     * the value specified by the system property "scarab.common.status.onhold"
     * By default an issue is onhold if attribute "status" == "onhold")
     * @return
     * @throws TorqueException
     */
    public boolean isOnHold() throws TorqueException
    {        
        boolean result = false;
        String status = Environment.getConfigurationProperty("scarab.common.status.id", null);
        if (status != null)
        {
            String value = Environment.getConfigurationProperty("scarab.common.status.onhold", null);
            if(value != null)
            {
                AttributeValue attval = issue.getAttributeValue(status);
                if(attval != null && attval.getValue().equals(value))
                {
                    result = true;
                }
            }
        }
        return result;
    }
    
        
    /**
     * Check if this issue is active (not on hold and not sealed).
     * @param issue
     * @return
     * @throws TorqueException
     */
    public boolean isActive() throws TorqueException
    {
        return !(isOnHold() || isSealed());
    }

    /**
     * Returns the name of the attribute instance, which contains the issues status.
     * Note:  Currently Scarab expects that the Attribute is a drop down list
     * (i.e. its data type is AttributeOptionValue)
     * @return
     * @throws TorqueException
     */    
    public static String getStatusAttributeName() throws TorqueException
    {
        return Environment.getConfigurationProperty("scarab.common.status.id", null);
    }

    /**
     * Returns the attribute instance, which contains the issues status.
     * Note:  Currently Scarab expects that the Attribute is a drop down list
     * (i.e. its data type is AttributeOptionValue)
     * @return
     * @throws TorqueException
     */
    public Attribute getStatusAttribute() throws TorqueException
    {
        Attribute attribute = null;
        String attributeName = getStatusAttributeName();
        if(attributeName != null)
        {
            attribute = issue.getAttribute(attributeName);
        }
        return attribute;
    }

    /**
     * Returns the attribute instance, which contains the issues onHoldExpirationDate.
     * Note:  Currently Scarab expects that the Attribute is a DateAttribute
     * @return
     * @throws TorqueException
     */
    public Attribute getOnHoldExpirationDate() throws TorqueException
    {
        Attribute attribute = null;
        String attributeName = Environment.getConfigurationProperty("scarab.common.status.onhold.dateProperty", null);
        if(attributeName != null)
        {
            attribute = issue.getAttribute(attributeName);
        }
        return attribute;
    }

    
    /**
     * Get the date until which this issue is onhold. This method searches
     * for the attribute specified by the system property "scarab.common.status.onhold.dateProperty"
     * And we expect this attribute to contain a Date value.
     * @return
     * @throws TorqueException
     * @throws ParseException 
     */
    public Date getOnHoldUntil() throws TorqueException, ParseException
    {
        Date date = null;
        String attributeName = Environment.getConfigurationProperty("scarab.common.status.onhold.dateProperty", null);
        
        if (attributeName != null)
        {
            AttributeValue dateValue = issue.getAttributeValue(attributeName);
            if(dateValue!=null) 
            {
                String value = dateValue.getValue();
                if (value != null && value.length() > 0)
                {
                    date = DateAttribute.toDate(value);
                }
            }
        }
        return date;
    }

    /**
     * If an issue is onHold and the revocation time has expired, the NotificationManager
     * should periodically send a Notification to the Issue's Observer list. the reminderPeriod
     * tells how long to wait between 2 reminder notifications. The unit is "hours".
     * If this property is not set, return 0
     * @return
     */
    public int getReminderPeriod()
    {
        String rp = Environment.getConfigurationProperty("scarab.common.status.onhold.reminder.period", null);
        int result = 0;
        if (rp != null)
        {
            result = Integer.parseInt(rp);
        }
        return result;
    }
        
}
