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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.notification.NotificationManagerFactory;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Condition;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependManager;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.DependTypeManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabGlobalTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.ComponentLocator;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.MutableBoolean;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabUtil;

/**
 * This class is responsible for edit issue forms.
 * ScarabIssueAttributeValue
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class ModifyIssue extends BaseModifyIssue
{

    public void doSubmitattributes(RunData data, TemplateContext context)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        final ScarabRequestTool scarabR = getScarabRequestTool(context);
        final ScarabLocalizationTool l10n = getLocalizationTool(context);
        final Issue issue = scarabR.getIssue();
        final Module module = scarabR.getCurrentModule();

        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        final ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        final IntakeTool intake = getIntakeTool(context);
        
        final boolean isReasonRequired = module.isIssueReasonRequired();
        
        // Reason field is required to modify attributes
        final Group reasonGroup = intake.get("Attachment", "attCommentKey" + issue.getQueryKey(), false);
        final Field reasonField = reasonGroup.get("Data");
        
        if(isReasonRequired)
        {
            reasonField.setRequired(true);
        }
        
        // make sure to trim the whitespace
        String reasonFieldString = reasonField.toString();
        if (reasonFieldString != null)
        {
            reasonFieldString = reasonFieldString.trim();
        }
        String saveAsFieldString = data.getParameters().get("saveReasonAs");
        if (saveAsFieldString != null)
        {
            saveAsFieldString = saveAsFieldString.trim();
        }
        final boolean saveAsComment = "Comment".equalsIgnoreCase(saveAsFieldString);
        
        if (reasonGroup == null || !reasonField.isValid() ||
            reasonFieldString.length() == 0)
        {
            if (isReasonRequired)
            {
                reasonField.setMessage(
                    "ExplanatoryReasonRequiredToModifyAttributes");
            }
        }

        // Set any other required flags
        final Set selectedOptions = new HashSet();
        final Map conditionallyRequiredFields = new HashMap(); 
        final IssueType issueType = issue.getIssueType();
        final List requiredAttributes = issueType
            .getRequiredAttributes(issue.getModule());
        AttributeValue aval = null;
        Group group = null;
        final LinkedMap modMap = issue.getModuleAttributeValuesMap();
        for (Iterator iter = modMap.mapIterator(); iter.hasNext(); ) 
        {
            aval = (AttributeValue)modMap.get(iter.next());
            group = intake.get("AttributeValue", aval.getQueryKey(), false);

            if (group != null) 
            {            
                Field field = null;
                if (aval instanceof OptionAttribute) 
                {
                    field = group.get("OptionId");
                    // Will store the selected optionId, for later query.
                    final Object fieldValue = field.getValue();
                    if (null != fieldValue)
                    {
                        selectedOptions.add(fieldValue);
                    }                     
                }
                else
                {
                    field = group.get("Value");
                }
                /**
                 * If the field has any conditional constraint, will be added to the collection for later query.
                 */ 
                final List conditions = aval.getRModuleAttribute().getConditions(); 
                if (conditions.size() > 0)
                {
                    for (Iterator it = conditions.iterator(); it.hasNext(); )
                    {
                        final Condition cond = (Condition)it.next();
	                    final Integer id = cond.getOptionId();
	                    List fields = (List)conditionallyRequiredFields.get(id);
	                    if (fields == null)
	                    {
	                        fields = new ArrayList();
	                    }
	                    fields.add(field);
	                    conditionallyRequiredFields.put(id, fields);
                    }
                }
                
                for (int j=requiredAttributes.size()-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                         ((Attribute)requiredAttributes.get(j)).getPrimaryKey())) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            } 
        }
        /**
         * Now that we have all the info, we will force the 'required' status of any field
         * whose requiredOptionId has been set in the issue.
         */
        for (Iterator requiredIds = conditionallyRequiredFields.keySet().iterator(); requiredIds.hasNext(); )
        {
            final Integer attributeId= (Integer)requiredIds.next();
            if (selectedOptions.contains(attributeId))
            {
                final List fields = (List)conditionallyRequiredFields.get(attributeId);
                for (Iterator iter = fields.iterator(); iter.hasNext(); )
                {
                	final Field field = (Field)iter.next();
                        if (field.getValue().toString().length() == 0)
                        {
                            field.setRequired(true);
                            field.setMessage("ConditionallyRequiredAttribute");
                        }
                }
            }
        }         

        if (intake.isAllValid()) 
        {
            AttributeValue aval2 = null;
            final HashMap newAttVals = new HashMap();

            // Set the attribute values entered 
            final Iterator iter2 = modMap.mapIterator();
            boolean modifiedAttribute = false;
            while (iter2.hasNext())
            {
                aval = (AttributeValue)modMap.get(iter2.next());
                aval2 = AttributeValue.getNewInstance(aval.getAttributeId(), 
                                                      aval.getIssue());
                aval2.setProperties(aval);
 
                group = intake.get("AttributeValue", aval.getQueryKey(), false);
                String oldValue = "";
                String newValue = "";

                if (group != null) 
                {            
                    if (aval instanceof OptionAttribute) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }
                    // A value has been entered for the attribute.
                    // The old value is different from the new, or is unset:
                    // Set new value.
                    if (newValue.length() > 0
                         && ((oldValue == null) ||
                            (oldValue != null && !oldValue.trim().equals(newValue.trim()))))
                    {
                        group.setProperties(aval2);
                        newAttVals.put(aval.getAttributeId(), aval2);
                        modifiedAttribute = true;
                    }
                    // The attribute is being undefined. 
                    else if (oldValue != null && newValue.length() == 0 && 
                             oldValue.length() != 0)
                    {
                        aval2.setValue(null);
                        newAttVals.put(aval.getAttributeId(), aval2);
                        modifiedAttribute = true;
                    }
                }
            } 
            if (!modifiedAttribute && !saveAsComment)
            {
                scarabR.setAlertMessage(L10NKeySet.MustModifyAttribute);
                return;
            }
            final Attachment attachment = AttachmentManager.getInstance();
            reasonGroup.setProperties(attachment);

            try
            {
                DateAttribute.convertDateAttributes(newAttVals.values(), 
                        getLocalizationTool(context).get(L10NKeySet.ShortDatePattern));                
                final ActivitySet activitySet = issue.setAttributeValues(null, 
                        newAttVals, saveAsComment? null:attachment, user);
                // save reason as a comment
                if( saveAsComment ){
                    final ActivitySet commentActivitySet = issue.addComment(attachment, user);
                    // copy the activities over to the original activitySet
                    final List list = commentActivitySet.getActivityList();
                    for( Iterator it = list.iterator(); it.hasNext(); ){
                        activitySet.addActivity( (Activity)it.next() );
                    }
                }
                intake.removeAll();
                scarabR.setConfirmMessage(L10NKeySet.ChangesSaved);
                NotificationManagerFactory.getInstance()
                        .addActivityNotification(
                                ActivityType.ATTRIBUTE_CHANGED,
                                activitySet, issue);
            }
            catch (Exception se)
            {
                scarabR.setAlertMessage(l10n.getMessage(se));
            }
        } 
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }
    
    /**
     *  Modifies attachments of type "url".
     */
    public void doSaveurl (RunData data, TemplateContext context) 
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        ScarabUser user = (ScarabUser)data.getUser();

        List urls = issue.getUrls();
        ActivitySet activitySet = null;
        for (int i = 0; i<urls.size(); i++)
        {
            Attachment attachment = (Attachment)urls.get(i);
            if (attachment.getTypeId().equals(Attachment.URL__PK)
                && !attachment.getDeleted())
            {
                Group group = intake.get("Attachment", attachment.getQueryKey(), false);

                Field nameField = group.get("Name"); 
                Field dataField = group.get("Data"); 
                if (dataField.isValid())
                {
                    dataField.setRequired(true);
                }
                
                if (intake.isAllValid())
                {
                    // store the new and old data
                    String oldDescription = attachment.getName();
                    String oldURL = new String(attachment.getData());
                    String newDescription = nameField.toString();
                    String newURL = dataField.toString();

                    if (!oldDescription.equals(newDescription))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        activitySet = issue
                            .doChangeUrlDescription(activitySet, user, 
                                                    attachment, oldDescription);
                        scarabR.setConfirmMessage(L10NKeySet.UrlSaved);
                    }
                    if (!oldURL.equals(newURL))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        activitySet = issue
                            .doChangeUrlUrl(activitySet, user, 
                                            attachment, oldURL);
                        scarabR.setConfirmMessage(L10NKeySet.UrlSaved);
                    }
                }
                else
                {
                     scarabR.setAlertMessage(ERROR_MESSAGE);
                }
            }
        }

        // if there is a new URL, add it
        Group newGroup = intake.get("Attachment", "urlKey", false);
        if (newGroup != null)
        { 
            Field dataField = newGroup.get("Data");
            String dataFieldString = dataField.toString();
            if (dataFieldString != null && dataFieldString.trim().length() > 0)
            {
                // create the new attachment
                Attachment attachment = AttachmentManager.getInstance();
                // set the form data to the attachment object
                newGroup.setProperties(attachment);
                activitySet = issue.addUrl(attachment, user);

                // remove the group
                intake.remove(newGroup);
                scarabR.setConfirmMessage(L10NKeySet.UrlSaved);
                NotificationManagerFactory.getInstance()
                        .addActivityNotification(
                                ActivityType.URL_ADDED, activitySet,
                                issue);
                
            }
        }
    }

    /**
     * Enable edition mode for the comment page.
     */
    public void doEditcommentpage(RunData data, TemplateContext context)
         throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }
        data.getParameters().add("edit_comments", "true");
        data.getParameters().add("fullcomments", data.getParameters().get("fullcomments"));
        return;
    }

    /**
     *  Adds an attachment of type "comment".
     */
    public void doSubmitcomment (RunData data, TemplateContext context) 
         throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "commentKey", false);
        if (group != null) 
        {
            Attachment attachment = AttachmentManager.getInstance();
            try
            {
                group.setProperties(attachment);
            }
            catch (Exception e)
            {
                scarabR.setAlertMessage(ERROR_MESSAGE);
                return;
            }
            
            try
            {
                issue.addComment(attachment, (ScarabUser)data.getUser());
            }
            catch(Exception e)
            {
                String l10nMessage = l10n.getMessage(e);
                scarabR.setAlertMessage(l10nMessage);
                return;
            }
            scarabR.setConfirmMessage(L10NKeySet.CommentSaved);
            intake.remove(group);
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    } 

    /**
     * Add an attachment of type "file"
     */
    public void doSubmitfile (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "fileKey" + issue.getQueryKey(), false);
        Field nameField = group.get("Name"); 
        // set some required fields
        if (nameField.isValid())
        {
            nameField.setRequired(true);
        }
        // validate intake
        if (intake.isAllValid() && group != null)
        {
            // adding a file is a special process
            addFileAttachment(issue, group, (Attachment) null, scarabR, data,
                              intake);
            ActivitySet activitySet = issue.doSaveFileAttachments(user);
            if (activitySet != null)
            {
                scarabR.setConfirmMessage(L10NKeySet.FileSaved);
                NotificationManagerFactory.getInstance()
                        .addActivityNotification(
                                ActivityType.ATTACHMENT_CREATED,
                                activitySet, issue);
                
            }
            else
            {
                scarabR.setAlertMessage(ERROR_MESSAGE);
            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    /**
     * Adds a file attachment to an issue.
     *
     * @param issue The issue to add an attachment to.
     * @param group Intake group.
     * @param attachment The attachment to add, or <code>null</code>
     * to use a new blank one.
     * @param scarabR Request tool.
     * @param data Contextual data.
     * @param intake Intake tool.
     */
    static void addFileAttachment(Issue issue, Group group,
                                  Attachment attachment,
                                  ScarabRequestTool scarabR, RunData data,
                                  IntakeTool intake)
        throws Exception
    {
        if (group != null)
        {
            Field nameField = group.get("Name");
            Field fileField = group.get("File");
            nameField.setRequired(true);
            fileField.setRequired(true);
            Field mimeAField = group.get("MimeTypeA");
            Field mimeBField = group.get("MimeTypeB");
            String mimeA = mimeAField.toString();
            String mimeB = mimeBField.toString();
            String mimeType = null;
            if (mimeB != null && mimeB.trim().length() > 0)
            {
                mimeType = mimeB;
            }
            else if ("autodetect".equals(mimeA) && fileField.isValid())
            {
                try 
                {
                    String filename = 
                        ((FileItem)fileField.getValue()).getName();
                    String contentType = 
                        ComponentLocator.getMimeTypeService().getContentType(filename, null);
                    if (contentType == null) 
                    {
                        // could not match extension.
                        mimeAField
                            .setMessage("intake_CouldNotDetermineMimeType");
                    }
                    else 
                    {
                        mimeType = contentType;
                    }
                }
                catch (Exception e)
                {
                    // we do not want any exception thrown here to affect
                    // the user experience, it is just considered a 
                    // non-detectable file type.  But still the exception is
                    // not expected, so log it.
                    mimeAField.setMessage("intake_CouldNotDetermineMimeType");
                    Log.get().info(
                        "Could not determine mimetype of uploaded file.", e);
                }                
            }
            else
            {
                mimeAField.setRequired(true);
                mimeType = mimeA;
            }
            if (group.isAllValid()) 
            {
                if (attachment == null)
                {
                    attachment = AttachmentManager.getInstance();
                }
                group.setProperties(attachment);
                attachment.setMimeType(mimeType);
                issue.addFile(attachment, (ScarabUser)data.getUser());
                // remove the group so that the form data doesn't show up again
                intake.remove(group);
            }
            else
            {
                scarabR.setAlertMessage(ERROR_MESSAGE);
            }
        }
        else
        {
            scarabR.setAlertMessage(L10NKeySet.CouldNotLocateAttachmentGroup);
        }
    }

    /**
     *  Edits a comment.
     */
    public void doEditcomment (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i=0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("edit_comment_"))
            {
                String attachmentId = key.substring(13);
                String newComment = params.getString(key,"");
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                try
                {
                    activitySet = issue.doEditComment(activitySet, newComment, 
                                                      attachment, user);
                }
                catch (ScarabException se)
                {
                    scarabR.setAlertMessage(l10n.getMessage(se));
                }
            }
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);  
        }
        else
        {
            scarabR.setInfoMessage(L10NKeySet.NoCommentsChanged);
        }
    }

   /**
    *  Deletes a url.
    */
   public void doDeleteurl (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i=0; i < keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("url_delete_"))
            {
                String attachmentId = key.substring(11);
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                activitySet = issue.doDeleteUrl(activitySet, attachment, user);
            }
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);
            NotificationManagerFactory.getInstance().addActivityNotification(
                    ActivityType.URL_DELETED, activitySet, issue);
        }
        else
        {
            scarabR.setInfoMessage(L10NKeySet.NoUrlsChanged);
        }
    }
    
    /**
     *  Deletes a file.
     */
    public void doDeletefile (RunData data, TemplateContext context)
        throws Exception
    {      
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;

        boolean allFilesDeleted = true;

        boolean deletePhysically = Turbine.getConfiguration()
        .getBoolean("scarab.attachment.remove.permanent",false);

        for (int i = 0; i < keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("file_delete_"))
            {
                String attachmentId = key.substring(12);
                Attachment attachment = AttachmentManager.getInstance(
                new NumberKey(attachmentId), false);
                MutableBoolean physicallyDeleted = new MutableBoolean(false);
                activitySet = issue.doRemoveAttachment(activitySet, physicallyDeleted, attachment,  user);
                //set deleted to false if at least for one of the
                //deleted attachmnents the file was not removed from
                //disk. 
                if (deletePhysically && !physicallyDeleted.booleanValue())
                {
                    allFilesDeleted = false;
                }
            }
        }

        if (activitySet != null)
        {
            if (allFilesDeleted)
            {
                scarabR.setConfirmMessage(DEFAULT_MSG);
            }
            else
            {
                scarabR.setAlertMessage(L10NKeySet.FilesPartiallyDeleted);
            }
            NotificationManagerFactory.getInstance().addActivityNotification(
                    ActivityType.ATTACHMENT_REMOVED, activitySet,
                    issue);
        }
        else
        {
            scarabR.setInfoMessage(L10NKeySet.NoFilesChanged);
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    /*public void doDeletedependencies(RunData data, TemplateContext context)
        throws Exception
    {
        saveDependencyDeletions(data, context);
    }
    */
    

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    public void doSavedependencychanges(RunData data, TemplateContext context)
        throws Exception
    {
        saveDependencyChanges(data, context, false);
    }
    
    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    public void doSavenewdependencies(RunData data, TemplateContext context)
        throws Exception
    {
        saveNewDependencies(data, context, false);
    }    

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private void saveDependencyChanges(RunData data, TemplateContext context, 
                                       boolean doDelete)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        String modifyDepTo = data.getParameters().get("modifyDepTo");
        boolean changesMade = false;

        DependType newDependType = null;
        if(modifyDepTo == null || modifyDepTo.equals(""))
        {
            // no dependType set -> error
        }
        else if (modifyDepTo.equals("remove"))
        {
            changesMade = doRemoveSelectedDependencies(issue, intake, scarabR, 
                          context, l10n, user);
        }
        else
        {
            /* If modifyDepTo equals "-1" we want the current issue to become
             * a prerequisite for the other issue.
             */
            boolean setAsPrerequisite = false;
            if(modifyDepTo.equals("-1"))
            {
                modifyDepTo = modifyDepTo.substring(1);
                setAsPrerequisite = true;
            }

            newDependType = DependTypeManager.getInstanceById(modifyDepTo);
            changesMade = doUpdatedependencies(issue, intake, scarabR, 
                          context, user, newDependType, setAsPrerequisite);
        }        

        if (!changesMade)
        {
            scarabR.setInfoMessage(NO_CHANGES_MADE);
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private void saveNewDependencies(RunData data, TemplateContext context, 
                                       boolean doDelete)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Depend","newDep"+issue.getQueryKey(),false);

        boolean depAdded = doAdddependency(issue, intake, group, scarabR,
                                           context, l10n, user);
        if (!depAdded)
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
            scarabR.setInfoMessage(NO_CHANGES_MADE);
        }
        else
        {
            intake.remove(group);
        }
    }    

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    /*
    private void saveDependencyDeletions(RunData data, TemplateContext context)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        
        //"newDep"+issue.getQueryKey()         depnewDep2dti
        //                                     dep100del
        String intakeKey        = "newDep" + issue.getQueryKey();

        Group group = intake.get("Depend", intakeKey, false);
        if(group != null)
        {
        String reasonForChange = group.get("Description").toString();

        boolean done = doUpdatedependencies(issue, intake, scarabR, 
                                                   context, l10n, user, 
                                                   reasonForChange, true);
        if (!done)
        {
            scarabR.setInfoMessage(NO_CHANGES_MADE);
        }
        else
        {
            intake.remove(group);
        }
        }
    }
    */
    

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private boolean doAdddependency(Issue issue, IntakeTool intake,
                                 Group group, ScarabRequestTool scarabR,
                                 TemplateContext context,
                                 ScarabLocalizationTool l10n,
                                 ScarabUser user)
        throws Exception
    {
        // Check that dependency type entered is valid
        Field type = group.get("TypeId");
        Integer typeAsInteger = null;
        try
        {
            typeAsInteger = (Integer)type.getValue();
        }
        catch (Exception e)
        {
            type.setMessage(l10n.get(L10NKeySet.SelectRelation));
            return false;
        }
        boolean needRoleSwitch = false;
        if(typeAsInteger.intValue() < 1)
        {
            typeAsInteger = new Integer( -1 * typeAsInteger.intValue());
            needRoleSwitch = true;
        }

        type.setRequired(true);
        // Check that child ID entered is valid
        Field childId = group.get("ObserverUniqueId");
        childId.setRequired(true);
        if (!type.isValid() && childId.isValid())
        {
            type.setMessage(l10n.get(L10NKeySet.EnterValidDependencyType));
            return false;
        }
        else if (type.isValid() && !childId.isValid())
        {
            childId.setMessage(l10n.get(L10NKeySet.EnterValidIssueId));
            return false;
        }
        String childIdStr = childId.toString();
        // we need to struggle here because if there is no
        // issue id, we just want to return because the person
        // on the page could just be updating existing deps
        // and in this case, the issue id might be empty.
        if (childIdStr != null)
        {
            childIdStr.trim();
        }
        if (childIdStr == null || childIdStr.length() == 0)
        {
            return true;
        }
        // Check that child ID entered corresponds to a valid issue
        // The id might not have the prefix appended so use the current
        // module prefix as the thing to try.
        Issue childIssue = null;
        Module currentModule = scarabR.getCurrentModule();
        try
        {
            childIssue = IssueManager
                .getIssueById(childIdStr, currentModule.getCode());
        }
        catch(Exception e)
        {
            // Ignore this
        }
        if (childIssue == null || childIssue.getDeleted())
        {
            childId.setMessage(l10n.get(L10NKeySet.EnterValidIssueId));
            return false;
        }
        

        
        // Make sure issue is not being marked as dependant on itself.
        else if (childIssue.equals(issue))
        {
            childId.setMessage(l10n.get(L10NKeySet.CannotAddSelfDependency));
            return false;
        }
        if (intake.isAllValid())
        {
            Depend depend = DependManager.getInstance();
            depend.setDefaultModule(currentModule);
            group.setProperties(depend);
            depend.setTypeId(typeAsInteger);
            ActivitySet activitySet = null;
            try
            {
                Issue workingIssue = issue;
                if (needRoleSwitch)
                {
                    depend.exchangeRoles();
                    workingIssue = childIssue;
                }
                activitySet = issue
                    .doAddDependency(activitySet, depend, workingIssue, user);
            }
            catch (ScarabException se)
            {
                childId.setMessage(se.getL10nMessage().getMessage(l10n));
                return false;
            }
            catch (Exception e)
            {
                log().debug("Delete error: ", e);
                return false;
            }

            scarabR.setConfirmMessage(DEFAULT_MSG);
            if (activitySet != null)
            {
                // FIXME: I think that we are sending too many emails here
                NotificationManagerFactory.getInstance()
                        .addActivityNotification(
                                ActivityType.DEPENDENCY_CREATED,
                                activitySet, childIssue);
                NotificationManagerFactory.getInstance()
                        .addActivityNotification(
                                ActivityType.DEPENDENCY_CREATED,
                                activitySet, issue);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private boolean doUpdatedependencies(Issue issue, IntakeTool intake, 
                                       ScarabRequestTool scarabR,
                                       TemplateContext context,
                                       ScarabUser user,
                                       DependType newDependType,
                                       boolean setAsPrerequisite)
        throws Exception
    {

        ActivitySet activitySet = null;
        List dependencies = issue.getAllDependencies();

        for (int i=0; i < dependencies.size(); i++)
        {
            Issue workingIssue = issue;
            Depend oldDepend = (Depend)dependencies.get(i);
            DependType oldDependType = oldDepend.getDependType();
            String intakeKey = oldDepend.getObserverId().toString();
            boolean thisIsPrerequisite = true;
            Group group = intake.get("Depend", intakeKey, false);
            if (group == null)
            {
                thisIsPrerequisite = false;
                intakeKey = oldDepend.getObservedId().toString();
                group = intake.get("Depend", intakeKey, false);
                if (group == null)
                {
                    // there is nothing to do here.
                    continue;
                }
                workingIssue = IssueManager.getInstance(oldDepend.getObservedId(), false);
            }

            Depend newDepend = DependManager.getInstance();
            // copy oldDepend properties to newDepend
            newDepend.setProperties(oldDepend);


            // set properties on the object
            group.setProperties(newDepend);

            boolean isSelected = newDepend.getSelected();
            boolean dependTypeModified = !oldDependType.equals(newDependType);
            boolean needRoleSwitch = newDependType.getName().equals("blocking") && setAsPrerequisite!=thisIsPrerequisite;
            if (isSelected && (dependTypeModified || needRoleSwitch ))
            {
                // need to do this because newDepend could have the deleted
                // flag set to true if someone selected it as well as 
                // clicked the save changes button. this is why we have the 
                // doDeleted flag as well...issue.doChange will only do the
                // change if the deleted flag is false...so force it...
                newDepend.setDeleted(false);
                newDepend.setDependType(newDependType);

                if(needRoleSwitch)
                {
                    newDepend.exchangeRoles();
                }
                
                // make the changes
                activitySet = 
                    workingIssue.doChangeDependencyType(activitySet, oldDepend,
                                                 newDepend, user);
            }
            intake.remove(group);
        }

        // something changed...
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);
            NotificationManagerFactory.getInstance().addActivityNotification(
                    ActivityType.DEPENDENCY_CHANGED,
                    activitySet, issue);
            return true;
        }
        else // nothing changed
        {
            return false;
        }
    }
    
    
    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private boolean doRemoveSelectedDependencies(Issue issue, IntakeTool intake, 
                                       ScarabRequestTool scarabR,
                                       TemplateContext context,
                                       ScarabLocalizationTool l10n,
                                       ScarabUser user)
        throws Exception
    {

        ActivitySet activitySet = null;
        List dependencies = issue.getAllDependencies();

        for (int i=0; i < dependencies.size(); i++)
        {
            Depend oldDepend = (Depend)dependencies.get(i);
            
            // Assume, issue takes the role "observer"
            String intakeKey = oldDepend.getObserverId().toString();
            Group group = intake.get("Depend", intakeKey, false);
            Issue workingIssue = issue;

            if (group == null)
            {
                // Maybe issue takes the role "observed"
                intakeKey = oldDepend.getObservedId().toString();
                group = intake.get("Depend", intakeKey, false);
                if(group == null)
                {
                    // there is nothing to do here.
                    continue;
                }
                workingIssue = oldDepend.getIssueRelatedByObservedId();
            }

            group.setProperties(oldDepend);
            if (oldDepend.getSelected())
            {
                try
                {
                    activitySet = 
                        workingIssue.doDeleteDependency(activitySet, oldDepend, user);
                }
                catch (ScarabException se)
                {
                    // it will error out if they attempt to delete
                    // a dep via a child dep.
                    String l10nKey = se.getMessage();
                    scarabR.setAlertMessage(l10n.get(l10nKey));
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(ERROR_MESSAGE);
                    log().debug("Delete error: ", e);
                }
            }
            intake.remove(group);
        }

        // something changed...
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);
            NotificationManagerFactory.getInstance().addActivityNotification(
                    ActivityType.DEPENDENCY_CHANGED,
                    activitySet, issue);
            
            return true;
        }
        else // nothing changed
        {
            return false;
        }
    }    

    /**
     * Redirects to AssignIssue page.
     */
    public void doEditassignees(RunData data, TemplateContext context)
         throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getIssue();
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               issue.getModule()))
        {
            data.getParameters().add("id", issue.getUniqueId());
            data.getParameters().add("issue_ids", issue.getUniqueId());
            scarabR.resetAssociatedUsers();
            setTarget(data, "AssignIssue.vm");
        }
        else
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
        }
    }



    /**
     * Redirects to MoveIssue page with move action selected.
     */
    public void doMove(RunData data, TemplateContext context)
         throws Exception
    {
        boolean collisionOccurred = isCollision(data, context);
        context.put("collisionDetectedOnMoveAttempt", collisionOccurred ? Boolean.TRUE : Boolean.FALSE);
        if (collisionOccurred)
        {
            // Report the collision to the user.
            return;
        }
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        List moveToModules =((ScarabUser)data.getUser()).getCopyToModules(module, "move");
        if (moveToModules.size() > 0)
        {
            ParameterParser pp = data.getParameters();
            pp.setString("mv_0rb", "move");
            ((IntakeTool)context.get("intake")).get("MoveIssue")
                .getDefault().get("Action").init(pp);
            String[] issueIds = pp.getStrings("issue_ids");
            String currentIssueId = getScarabRequestTool(context).getIssue().getUniqueId();
            if (!ScarabUtil.contains(issueIds, currentIssueId))
            {
                pp.add("issue_ids", currentIssueId);
            }
            setTarget(data, "MoveIssue.vm");
        }
        else
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            setTarget(data, "ViewIssue.vm");
        }
    }

    /**
     * Redirects to MoveIssue page with copy action selected.
     */
    public void doCopy(RunData data, TemplateContext context)
         throws Exception
    {
        boolean collisionOccurred = isCollision(data, context);
        context.put("collisionDetectedOnMoveAttempt", collisionOccurred ? Boolean.TRUE : Boolean.FALSE);
        if (collisionOccurred)
        {
            // Report the collision to the user.
            return;
        }
        ParameterParser pp = data.getParameters();
        pp.setString("mv_0rb", "copy");
        ((IntakeTool)context.get("intake")).get("MoveIssue")
            .getDefault().get("Action").init(pp);
        String[] issueIds = pp.getStrings("issue_ids");
        String currentIssueId = getScarabRequestTool(context).getIssue().getUniqueId();
        if (!ScarabUtil.contains(issueIds, currentIssueId))
        {
            pp.add("issue_ids", currentIssueId);
        }
        setTarget(data, "MoveIssue.vm");            
    }

    /**
     * does not actually modify an issue, but sets the preferred view
     * of an issue for the current session
     */
    public void doSetissueview(RunData data, TemplateContext context)
         throws Exception
    {
        String tab = data.getParameters().getString("tab", 
                                          ScarabConstants.ISSUE_VIEW_ALL);
        data.getUser().setTemp(ScarabConstants.TAB_KEY, tab); 
    }
    
    /**
     * This action method delegates on the doAddmyself method from AssignIssue
     * action, assigning the current user to the issue in the specified userattribute.
     * 
     * @see AssignIssue#doAddmyself(RunData, TemplateContext)
     * @param data
     * @param context
     * @throws Exception
     */
    public void doAddmyself(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = this.getScarabRequestTool(context);
        
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssue();
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               issue.getModule()))
        {
            // We'll set the info required by AssignIssue.doAddmyself (the)
            // same that in doEditassignees in this same class.
            data.getParameters().add("id", issue.getUniqueId());
            data.getParameters().add("issue_ids", issue.getUniqueId());

            scarabR.resetAssociatedUsers();
            
            // Lets cross-call the AssignIssue Turbine action!
            AssignIssue assignAction = new AssignIssue();
            assignAction.doAddmyself(data, context);
            assignAction.doDone(data, context);
        }
        else
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
        }        
    }
    
    public void doDeleteissue(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = this.getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssue();
        if (user != null && issue != null)
        {
           issue.deleteIssue(user);
        }
        L10NMessage msg = new L10NMessage(L10NKeySet.IssueDeleted, issue.getUniqueId());
        scarabR.setInfoMessage(msg);
    }
     
}
