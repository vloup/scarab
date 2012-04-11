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

import java.util.HashMap;
import java.util.Hashtable;


import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LinkedMap;

import org.apache.log4j.Category;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.util.ScarabException;

/**
 * Handler which provides all necessary methods to create an issues via xml-rpc interface.
 * 
 * @author pti
 *
 */
public class NewTicketHandler {
	
	Category logger = Category.getInstance(NewTicketHandler.class);

	/**
	 * Creates a new issue from given parameters.
	 * 
	 * @param moduleCode		The module to add the ticket to (shortcut of moudle)
	 * @param issueTypeName 	The issuetype as which the ticket should be entered (name of issue type)
	 * @param userName      	The user as which the ticket should be entered (login name)
	 * @param attribs   		A map with the attributes to be entered. (name/value mappings)
	 * @return : Unique id of issue: {module shortcut}{count}
	 * @throws TorqueException 
	 * @throws ScarabException 
	 */
	public String createNewTicket( String moduleCode, 
								   String issueTypeName, 
								   String userName, 
								   Hashtable attribs) throws TorqueException, ScarabException {
		
		//init environment for the new issue
		Module module = ModuleManager.getInstance(null, null, moduleCode);
		IssueType issueType = IssueType.getInstance(issueTypeName);
		Issue issue = module.getNewIssue(issueType);
		ScarabUser user = ScarabUserManager.getInstance(userName);
        
        Attachment reason = new Attachment();
        reason.setData("Created by xmlrpc");
        reason.setName("reason");
        
        //init issue, set and store attributes
		ActivitySet activitySet = null;
        activitySet = issue.setInitialAttributeValues(activitySet, reason, new HashMap(), user);

        // Save any unsaved attachments as part of this ActivitySet as well.
        setAttributes(issue,activitySet,reason,user,attribs);
        activitySet = issue.doSaveFileAttachments(activitySet, user);
        activitySet.save();
        
        return issue.getUniqueId();
	}

	/**
	 * Gets new attribute values from given map, matches them against the attributes from the new issue.
	 * Afterwards the resulting new attribute values will be set for the new issue.
	 * 
	 * @param issue				Issue where attriutes should be set.
	 * @param activitySet		Activity set.
	 * @param attachment		Attachment for issue: e.g. reason for change.
	 * @param user				User for issue creation.
	 * @param attribs			Map with name/value mappings for new attributes values.
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

}
