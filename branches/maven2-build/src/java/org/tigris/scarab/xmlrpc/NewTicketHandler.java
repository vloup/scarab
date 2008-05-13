package org.tigris.scarab.xmlrpc;
/* ====================================================================
*
* Copyright (c) 2006 CollabNet.
*
* Licensed under the
*
*     CollabNet/Tigris.org Apache-style license (the "License");
*
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://scarab.tigris.org/LICENSE
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
* implied. See the License for the specific language governing
* permissions and limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of CollabNet.
*
* [Additional notices, if required by prior licensing conditions]
*
*/

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.log4j.Category;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.notification.NotificationManagerFactory;
import org.tigris.scarab.om.ActivityManager;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.AttachmentTypeManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeValueManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabException;

/**
 * @author pti
 *
 */
public class NewTicketHandler {
	Category logger = Category.getInstance(NewTicketHandler.class);

	/**
	 * @param module	The module to add the ticket to
	 * @param issueType The issuetype as which the ticket should be entered
	 * @param user      The user as which the ticket should be entered
	 * @param attribs   A map with the attributes to be entered
	 * @return
	 * @throws TorqueException 
	 * @throws ScarabException 
	 */
	public String createNewTicket( String moduleName, 
								   String issueTypeName, 
								   String userName, 
								   Hashtable attribs) throws TorqueException, ScarabException {
		Module module = getModuleByCode(moduleName);
		IssueType issueType = IssueType.getInstance(issueTypeName);
		Issue issue = module.getNewIssue(issueType);
		ScarabUser user = ScarabUserManager.getInstance(userName);
		HashMap values = getAttributes(issue);
        ActivitySet activitySet = null;
        Attachment reason = new Attachment();
        reason.setData("Created by xmlrpc");
        reason.setName("reason");
        activitySet = issue
            .setInitialAttributeValues(activitySet, reason, values, user);
        
//        issue.setAttributeValues(activitySet, values, reason, user);
        // Save any unsaved attachments as part of this ActivitySet as well
        setAttributes(issue,activitySet,reason,user,attribs);
        activitySet = issue.doSaveFileAttachments(activitySet, user);
        activitySet.save();
        return issue.getUniqueId();
	}

	/**
	 * @param attribs
	 * @return
	 * @throws TorqueException 
	 * @throws ScarabException 
	 */
	private void setAttributes(Issue issue, ActivitySet activitySet, Attachment attachment, ScarabUser user, Hashtable attribs) throws TorqueException, ScarabException {

        LinkedMap avMap = issue.getModuleAttributeValuesMap(false); 
        HashMap newValues = new HashMap();
        
        for (MapIterator i = avMap.mapIterator();i.hasNext();) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
        	String key = aval.getAttribute().getName();
        	String newValue = (String)attribs.get(key);
        	
        	if (newValue != null) {
        		AttributeValue newVal = aval.copy();
        		newVal.setValue(newValue);
				newValues.put(aval.getAttributeId(),newVal);
        	}
        }
        issue.setAttributeValues(activitySet, newValues, attachment, user);
	}

	/**
	 * @param attribs
	 * @return
	 * @throws TorqueException 
	 */
	private HashMap getAttributes(Issue issue) throws TorqueException {

			
		return new HashMap(issue.getAttributeValuesMap());
	}

	/**
	 * @param module
	 * @throws TorqueException 
	 */
	private Module getModuleByCode(String module) throws TorqueException {
        final Criteria crit = new Criteria();
        if( module != null )
        {
            crit.add(ScarabModulePeer.MODULE_CODE, module);
        }
        final List result = ScarabModulePeer.doSelect(crit);
        if (result.size() != 1)
        {
            throw new TorqueException ("Selected: " + result.size() + 
                " rows. Expected 1."); //EXCEPTION
        }
        return (Module) result.get(0);		// TODO Auto-generated method stub
		
	}

}
