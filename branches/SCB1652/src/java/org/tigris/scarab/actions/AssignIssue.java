package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.torque.TorqueException;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.notification.NotificationManagerFactory;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;

/**
 * This class is responsible for assigning users to attributes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id$
 */
public class AssignIssue extends BaseModifyIssue
{
    private static final String ADD_USER = "add_user";
    private static final String SELECTED_USER = "select_user";
    
    private static int USERS_ADDED = 1;
    private static int USERS_REMOVED=2;
    private static int ERR_NO_USERS_SELECTED=3;
        
    /**
     * Adds users to temporary working list.
     */
    public void doAdd(RunData data, TemplateContext context) 
        throws Exception
    {
        int returnCode = 0;
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        ParameterParser params = data.getParameters();
        StringBuffer msg = new StringBuffer();
        String[] userIds = params.getStrings(ADD_USER);
        Map userAttributes = new HashMap();
        if (userIds != null)
        {
            for (int i=0; i<userIds.length; i++)
            {
                userAttributes.put(userIds[i], params.get("user_attr_" + userIds[i]));
            }
            returnCode = addUsersToList(user, module, userAttributes, msg);
            if (returnCode == USERS_ADDED)
            {
                scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereAdded);
            }
            if (returnCode == USERS_REMOVED)
            {
                L10NMessage l10nMsg = new L10NMessage(L10NKeySet.UserAttributeRemoved,
                        msg.toString());
                scarabR.setAlertMessage(l10nMsg);
            }
            if (returnCode == ERR_NO_USERS_SELECTED)
            {
                scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
            }
        }
    }

    /**
     * Adds the current user to the temporary working list.
     * @param data
     * @param context
     * @throws Exception
     */
    public void doAddmyself(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        String attributeId = data.getParameters().get("myself_attribute");
        String userId = user.getUserId().toString();
        Map map = new HashMap();
        map.put(userId, attributeId);
        StringBuffer msg = new StringBuffer();
        int returnCode = addUsersToList(user, scarabR.getCurrentModule(), map, msg);
        if (returnCode == USERS_ADDED)
        {
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereAdded);
        }
        if (returnCode == USERS_REMOVED)
        {
            L10NMessage l10nMsg = new L10NMessage(L10NKeySet.UserAttributeRemoved,
                    msg.toString());
            scarabR.setAlertMessage(l10nMsg);
        }
        if (returnCode == ERR_NO_USERS_SELECTED)
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }       
    }
    
    /**
     * Adds the current user to the temporary working list.
     * @param data
     * @param context
     * @throws Exception
     */
    public void doRemovemyself(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = this.getScarabRequestTool(context);
        ScarabUser user = (ScarabUser) data.getUser();
        Integer myUid = user.getUserId();
        Issue issue = scarabR.getIssue();
        Set userSet = issue.getAssociatedUsers();
        Iterator iter = userSet.iterator();
        int removeCounter = 0;
        int index = 0;
        while(iter.hasNext())
        {
            ArrayList entry = (ArrayList)iter.next();
            ScarabUser su = (ScarabUser)entry.get(1);
            if (su.getUserId().equals(myUid))
            {
                userSet.remove(entry);     // now the iterator is potentially invalid
                iter = userSet.iterator(); // rebuild the iterator (suboptimal)
                removeCounter++;
            }
        }
        if(removeCounter>0)
        {
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereRemoved);
        }
        else
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }
    
    /**
     * Decoupled method that adds users to the temporary working list of the AssignIssue screen.
     * @param user Currently connected user
     * @param module Current module (to check availaible userattributes)
     * @param userAttributes Map containing pairs userId-userAttrId
     * @param msg Output parameter that will containt the removed users if applicable.
     * @return
     * @throws Exception
     * @throws TorqueException
     */
    private int addUsersToList(ScarabUser user, Module module, Map userAttributes, StringBuffer msg) throws Exception, TorqueException
    {
        int returnCode;
        Map userMap = user.getAssociatedUsersMap();
        if (userAttributes != null && userAttributes.size() > 0) 
        {
            boolean isUserAttrRemoved = false;
            List removedUserAttrs = null;
            for (Iterator it = userAttributes.keySet().iterator(); it.hasNext(); )
            {
                String userId = (String)it.next();
                List item = new ArrayList();
                String attrId = (String)userAttributes.get(userId);
                Attribute attribute = AttributeManager
                    .getInstance(new Integer(attrId));
                ScarabUser su = ScarabUserManager
                    .getInstance(new Integer(userId));
                item.add(attribute);
                item.add(su);
                List issues = (List)user.getAssignIssuesList();
                for (int j=0; j<issues.size(); j++)
                {
                    Issue issue = (Issue)issues.get(j);
                    Long issueId = issue.getIssueId();
                    Set userList = (Set) userMap.get(issueId);
                    if (userList == null)
                    {
                        userList = new HashSet();
                    }
                    List attributeList = module
                        .getUserAttributes(issue.getIssueType(), true);
                    if (!attributeList.contains(attribute))
                    {
                        if (removedUserAttrs == null)
                        {
                            removedUserAttrs = new ArrayList();
                            removedUserAttrs.add(attribute);
                            msg.append("'").append(attribute.getName())
                                .append("'");
                        }
                        else if (!removedUserAttrs.contains(attribute))
                        {
                            removedUserAttrs.add(attribute);
                            msg.append(", '").append(attribute.getName())
                                .append("'");
                        }
                        isUserAttrRemoved = true;
                    }
                    else
                    {
                        userList.add(item);
                        // userMap.put(issueId, userList);
                        // user.setAssociatedUsersMap(userMap);
                    }
                } 
            }
            if (!isUserAttrRemoved)
            {
                returnCode = USERS_ADDED;
            }
            else 
            {
                returnCode = USERS_REMOVED;
            }
        }
        else
        {
            returnCode = ERR_NO_USERS_SELECTED;
        }
        return returnCode;
    }
        
    /**
     * Removes users from temporary working list.
     */
    private void remove(RunData data, TemplateContext context, Long issueId) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Set userList = (Set) user.getAssociatedUsersMap().get(issueId);
        ParameterParser params = data.getParameters();
        String[] selectedUsers =  params.getStrings(SELECTED_USER);
        if (selectedUsers != null && selectedUsers.length > 0) 
        {
            for (int i =0; i<selectedUsers.length; i++)
            {
                List item = new ArrayList(2);
                String selectedUser = selectedUsers[i];
                String userId = selectedUser.substring(1, selectedUser.indexOf('_')-1);
                String attrId = selectedUser.substring(selectedUser.indexOf('_')+1, selectedUser.length());
                Attribute attribute = AttributeManager
                    .getInstance(new Integer(attrId));
                ScarabUser su = ScarabUserManager
                    .getInstance(new Integer(userId));
                item.add(attribute);
                item.add(su);
                userList.remove(item);
            }
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereRemoved);
        }
        else 
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    private void update(RunData data, TemplateContext context, Long issueId) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Set userList = (Set) user.getAssociatedUsersMap().get(issueId);
        ParameterParser params = data.getParameters();
        String[] selectedUsers =  params.getStrings(SELECTED_USER);
        if (selectedUsers != null && selectedUsers.length > 0) 
        {
            for (int i =0; i < selectedUsers.length; i++)
            {
                String selectedUser = selectedUsers[i];
                String userId = selectedUser.substring(1, selectedUser.indexOf('_')-1);
                String attrId = selectedUser.substring(selectedUser.indexOf('_')+1, selectedUser.length());
                Attribute attribute = AttributeManager
                    .getInstance(new Integer(attrId));
                ScarabUser su = ScarabUserManager
                    .getInstance(new Integer(userId));
                List item = new ArrayList(2);
                List newItem = new ArrayList(2);
                item.add(attribute);
                item.add(su);
                userList.remove(item);

                String newKey = "asso_user_{" + userId + "}_attr_{" + attrId + "}_issue_{" + issueId + '}';
                String newAttrId = params.get(newKey);
                Attribute newAttribute = AttributeManager
                     .getInstance(new Integer(newAttrId));
                newItem.add(newAttribute);
                newItem.add(su);
                userList.add(newItem);
            }
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereModified);
        }
        else 
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }

    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List issues = null;
        String singleIssueId = data.getParameters().getString("id");
        if (singleIssueId != null)
        {
            Issue issue = scarabR.getIssue(singleIssueId);
            if (issue != null)
            {
                issues = new ArrayList();
                issues.add(issue);
            }
        }
        else
        {
            issues = (List)((ScarabUser)data.getUser()).getAssignIssuesList();
        }

        Map userMap = user.getAssociatedUsersMap();
        ScarabUser assigner = (ScarabUser)data.getUser();
        String reason = data.getParameters().getString("reason", "");
        Attachment attachment = null;
        ActivitySet activitySet = null;
        boolean isUserAttrRemoved = false;
        StringBuffer msg = new StringBuffer();
        List removedUserAttrs = null;

        for (int i=0; i < issues.size(); i++)
        {
            Issue issue = (Issue)issues.get(i);
            Set userList = (Set) userMap.get(issue.getIssueId());
            List oldAssignees = issue.getUserAttributeValues();
            List attributeList = scarabR.getCurrentModule()
                                .getUserAttributes(issue.getIssueType(), true);
            // save attachment with user-provided reason
            if (reason != null && reason.length() > 0)
            {
                attachment = new Attachment();
                attachment.setData(reason);
                attachment.setName("comment");
                attachment.setTextFields(assigner, issue,
                                         Attachment.MODIFICATION__PK);
                attachment.save();
            }

            // loops through users in temporary working list
            for (Iterator iter = userList.iterator(); iter.hasNext();)
            {
                List item = (List)iter.next();
                Attribute newAttr = (Attribute)item.get(0);
                ScarabUser assignee = (ScarabUser)item.get(1);
                Integer assigneeId = assignee.getUserId();
                boolean alreadyAssigned = false;
                if (!attributeList.contains(newAttr))
                {
                    if (removedUserAttrs == null)
                    {
                        removedUserAttrs = new ArrayList();
                        removedUserAttrs.add(newAttr);
                        msg.append("'").append(newAttr.getName()).append("'");
                    }
                    else if (!removedUserAttrs.contains(newAttr))
                    {
                        removedUserAttrs.add(newAttr);
                        msg.append(", '").append(newAttr.getName()).append("'");
                    }
                    isUserAttrRemoved = true;
                }
                else
                {
                    for (int k=0; k < oldAssignees.size(); k++)
                    {
                            AttributeValue oldAttVal = (AttributeValue)
                                                        oldAssignees.get(k);
                        Attribute oldAttr = oldAttVal.getAttribute();
                        // ignore already assigned users
                        if (assigneeId.equals(oldAttVal.getUserId()))
                        {
                            // unless user has different attribute id, then
                            // switch their user attribute
                                if (!newAttr.getAttributeId().equals(
                                                oldAttr.getAttributeId()))
                            {
                                List tmpItem = new ArrayList();
                                tmpItem.add(newAttr);
                                tmpItem.add(assignee);
                                if (!userList.contains(tmpItem))
                                {
                                    alreadyAssigned = true;
                                    activitySet = issue.changeUserAttributeValue(
                                                          activitySet, assignee,
                                                          assigner, oldAttVal,
                                                          newAttr, attachment);
                                }
                            }
                            else
                            {
                                alreadyAssigned = true;
                            }
                        }
                    }
                    // if user was not already assigned, assign them
                    if (!alreadyAssigned)
                    {
                            activitySet = issue.assignUser(activitySet, assignee,
                                        assigner, newAttr, attachment);
                    }
                }
            }

            // loops thru previously assigned users to find ones that
            // have been removed
            for (int m=0; m < oldAssignees.size(); m++)
            {
                boolean userStillAssigned = false;
                AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(m);
                for (Iterator iter = userList.iterator(); iter.hasNext();)
                {
                    List item = (List)iter.next();
                    Attribute attr = (Attribute)item.get(0);
                    ScarabUser su = (ScarabUser)item.get(1);
                    if (su.getUserId().equals(oldAttVal.getUserId())
                        && attr.getAttributeId().equals(oldAttVal.getAttributeId()))
                    {
                         userStillAssigned = true;
                    }
                }
                if (!userStillAssigned)
                {
                    ScarabUser assignee = scarabR.getUser(oldAttVal.getUserId());
                    // delete the user
                    activitySet = issue.deleteUser(activitySet, assignee, 
                                                   assigner, oldAttVal, attachment);
                }
            }
            if (activitySet != null) {
                try
                {
                    NotificationManagerFactory.getInstance()
                            .addActivityNotification(
                                    ActivityType.USER_ATTRIBUTE_CHANGED,
                                    activitySet, issue);
                }
                catch(Exception e)
                {
                    L10NMessage l10nMessage = new L10NMessage(EMAIL_ERROR,e);
                    scarabR.setAlertMessage(l10nMessage);
                }
            }
        }
        if (isUserAttrRemoved)
        {
            L10NMessage l10nMsg = new L10NMessage(L10NKeySet.UserAttributeRemoved,
                                                  msg.toString());
            scarabR.setAlertMessage(l10nMsg);
        }
        
        Object alertMessage = scarabR.getAlertMessage();
        if (alertMessage == null || 
            alertMessage.toString().length() == 0)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);
        }
    }

    public void doPerform(RunData data, TemplateContext context) 
        throws Exception
    {
        
        Object[] keys =  data.getParameters().getKeys();
        try
        {
            for (int i =0; i<keys.length; i++)
            {
                String key = keys[i].toString();
                if (key.startsWith("eventsubmit_doremove"))
                {
                    String issueId = key.substring(21);
                    remove(data, context, new Long(issueId));
                }
                else if (key.startsWith("eventsubmit_doupdate"))
                {
                    String issueId = key.substring(21);
                    update(data, context, new Long(issueId));
                }
            }
        }
        catch (NumberFormatException nfe) // new Integer(issueId) above could fail
        {
            getScarabRequestTool(context).setAlertMessage(L10NKeySet.BadIntegerConversion);
        }
    }

}
