package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.mimetype.TurbineMimeTypes;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependManager;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.ScarabException;

import org.tigris.scarab.attribute.OptionAttribute;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;

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

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);       
        // Comment field is required to modify attributes
        Group commentGroup = intake.get("Attachment", "attCommentKey", false);
        Field commentField = null;
        commentField = commentGroup.get("Data");
        commentField.setRequired(true);
        if (commentGroup == null || !commentField.isValid())
        {
            commentField.setMessage(
                "ExplanatoryCommentRequiredToModifyAttributes");
        }

        // Set any other required flags
        IssueType issueType = issue.getIssueType();
        List requiredAttributes = issue.getModule()
                                              .getRequiredAttributes(issueType);
        AttributeValue aval = null;
        Group group = null;
        SequencedHashMap modMap = issue.getModuleAttributeValuesMap();
        Iterator iter = modMap.iterator();
        while (iter.hasNext()) 
        {
            aval = (AttributeValue)modMap.get(iter.next());
            group = intake.get("AttributeValue", aval.getQueryKey(), false);

            if (group != null) 
            {            
                Field field = null;
                if (aval instanceof OptionAttribute) 
                {
                    field = group.get("OptionId");
                }
                else
                {
                    field = group.get("Value");
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

        if (intake.isAllValid()) 
        {
            AttributeValue aval2 = null;
            HashMap newAttVals = new HashMap();

            // Set the attribute values entered 
            SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
            Iterator iter2 = avMap.iterator();
            boolean modifiedAttribute = false;
            while (iter2.hasNext())
            {
                aval = (AttributeValue)avMap.get(iter2.next());
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
                    if (newValue.length() != 0 && 
                        (oldValue == null  || !oldValue.equals(newValue)))
                    {
                        group.setProperties(aval2);
                        newAttVals.put(aval.getAttributeId(), aval2);
                        modifiedAttribute = true;
                    }
                }
            } 
            if (!modifiedAttribute)
            {
                scarabR.setAlertMessage(l10n.get("MustModifyAttribute"));
                return;
            }
            Attachment attachment = AttachmentManager.getInstance();
            commentGroup.setProperties(attachment);
            try
            {
                ActivitySet activitySet = issue.setAttributeValues(null, newAttVals, attachment, user);
                intake.removeAll();
                sendEmail(activitySet, issue, DEFAULT_MSG, context);
                scarabR.setConfirmMessage(l10n.get("ChangesSaved"));
            }
            catch (Exception se)
            {
                scarabR.setAlertMessage(se.getMessage());
            }
        } 
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }

        IntakeTool intake = getIntakeTool(context);


        List urls = issue.getAttachments();
        for (int i = 0; i<urls.size(); i++)
        {
            Attachment attachment = (Attachment)urls.get(i);
            if (attachment.getTypeId().equals(Attachment.URL__PK)
                && !attachment.getDeleted())
            {
                Group group = intake.get("Attachment", attachment.getQueryKey(), false);

                Field nameField = group.get("Name"); 
                Field dataField = group.get("Data"); 
                if (nameField.isValid())
                {
                    nameField.setRequired(true);
                }
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

                    if (!oldDescription.equals(newDescription) ||
                        !oldURL.equals(newURL))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        attachment.registerSaveURLActivity(
                            (ScarabUser)data.getUser(), issue, 
                            oldDescription, newDescription, 
                            oldURL, newURL);
                    }
                
                }
            }
        }

        // if there is a new URL, add it
        Group newGroup = intake.get("Attachment", "urlKey", false);
        if (newGroup != null) 
        {
            Field newNameField = newGroup.get("Name"); 
            if (newNameField != null && 
                !newNameField.toString().equals(""))
            {
               handleAttachment(data, context, Attachment.URL__PK, 
                                newGroup, issue);
            }
        }
    }

    /**
     *  Adds an attachment of type "comment".
     */
    public void doSubmitcomment (RunData data, TemplateContext context) 
         throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "commentKey", false);
        if (group != null) 
        {
            handleAttachment(data, context, Attachment.COMMENT__PK, group, issue);
        }
    } 

    /**
     * Add an attachment of type "file"
     */
    public void doSubmitfile (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "fileKey", false);
        if (group != null) 
        {
            handleAttachment(data, context, Attachment.FILE__PK, group, issue);
        }
    }

    private void handleAttachment (RunData data, TemplateContext context, 
                                 NumberKey typeId, Group group, Issue issue)
        throws Exception
    {
        // grab the data from the group
        Field nameField = group.get("Name"); 
        Field dataField = group.get("Data");
        // set some required fields
        if (nameField.isValid())
        {
            nameField.setRequired(true);
        }
        if (dataField.isValid() && (typeId == Attachment.COMMENT__PK 
                                    || typeId == Attachment.URL__PK))
        {
            dataField.setRequired(true);
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        // validate intake
        if (intake.isAllValid())
        {
            // create the new attachment
            Attachment attachment = AttachmentManager.getInstance();
            String message = null;
            boolean addSuccess = false;
            if (typeId == Attachment.FILE__PK)
            {
                // adding a file is a special process
                addSuccess = addFileAttachment(issue, group, attachment, 
                              scarabR, data, intake);
                issue.save();
                message = "FileSaved";
            }
            else if (typeId == Attachment.URL__PK || typeId == Attachment.COMMENT__PK)
            {
                // set the form data to the attachment object
                group.setProperties(attachment);
                if (typeId == Attachment.URL__PK)
                {
                    message = "UrlSaved";
                }
                else
                {
                    message = "CommentSaved";
                }
                addSuccess = true;
            }

            if (addSuccess)
            {
                ScarabUser user = (ScarabUser)data.getUser();
                String nameFieldString = nameField.toString();
                String dataFieldString = dataField.toString();
                // register the add activity
                ActivitySet activitySet = attachment.registerAddActivity(user,
                    issue, typeId, nameFieldString, dataFieldString);
                // remove the group
                intake.remove(group);
                sendEmail(activitySet, issue, l10n.get(message), context);
                scarabR.setConfirmMessage(l10n.get(message));
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    static boolean addFileAttachment(Issue issue, Group group, Attachment attachment, 
        ScarabRequestTool scarabR, RunData data, IntakeTool intake)
        throws Exception
    {
        ScarabLocalizationTool l10n = (ScarabLocalizationTool)
            getTemplateContext(data).get(ScarabConstants.LOCALIZATION_TOOL);

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
                        ((FileItem)fileField.getValue()).getFileName();
                    String contentType = 
                        TurbineMimeTypes.getContentType(filename, null);
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
                group.setProperties(attachment);
                attachment.setMimeType(mimeType);
                ScarabUser user = (ScarabUser)data.getUser();
                attachment.setCreatedBy(user.getUserId());
                issue.addFile(attachment);
                // remove the group so that the form data doesn't show up again
                intake.remove(group);
                return true;
            }
            else
            {
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get("CouldNotLocateAttachmentGroup"));
        }
        return false;
    }

    /**
     * Eventually, this should be moved somewhere else once we can figure
     * out how to separate email out of the request context scope.
     */
    private void sendEmail(ActivitySet activitySet, Issue issue, String msg,
                           TemplateContext context)
        throws Exception
    {
        if (!activitySet.sendEmail(new ContextAdapter(context), issue))
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            String emailError = l10n.get(EMAIL_ERROR);
            StringBuffer sb = 
                new StringBuffer(msg.length() + emailError.length());
            sb.append(msg).append(emailError);
            getScarabRequestTool(context).setConfirmMessage(sb.toString());
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
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }

        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
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
                activitySet = issue.doEditComment(activitySet, newComment, attachment, user);
            }
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
            sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoCommentsChanged"));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
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
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            sendEmail(activitySet, issue, l10n.get("UrlDeleted"), 
                      context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoUrlsChanged"));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i =0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("file_delete_"))
            {
                String attachmentId = key.substring(12);
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                activitySet = issue.doDeleteFile(activitySet, attachment, user);
            } 
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            sendEmail(activitySet, issue, l10n.get("FileDeleted"), 
                      context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoFilesChanged"));
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    public void doUpdatedependencies (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            ScarabUser user = (ScarabUser)data.getUser();
            Issue issue = null;
            try
            {
                issue = getIssueFromRequest(data.getParameters());
            }
            catch (ScarabException se)
            {
                scarabR.setAlertMessage(se.getMessage());
                return;
            }

            if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                                   issue.getModule()))
            {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
                return;
            }

            List dependencies = issue.getAllDependencies();
            ActivitySet activitySet = null;
            for (int i=0; i< dependencies.size(); i++)
            {
                Depend depend = (Depend)dependencies.get(i);
                Group group = intake.get("Depend", depend.getQueryKey(), false);

                DependType oldDependType = depend.getDependType();
                // set properties on the object
                group.setProperties(depend);
                DependType newDependType = depend.getDependType();

                // make the changes
                if (depend.getDeleted() == true)
                {
                    activitySet = 
                        issue.doDeleteDependency(activitySet, depend, user);
                    intake.remove(group);
                }
                else
                {
                    // make the changes
                    activitySet = 
                        issue.doChangeDependencyType(activitySet, depend, 
                                                     oldDependType, newDependType, user);
                    intake.remove(group);
                }
            }
            // something changed...
            if (activitySet != null)
            {
                scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
                sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), context);
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    /**
     *  Adds a dependency between this issue and another issue.
     *  This issue will be the child issue. 
     */
    public void doAdddependency (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Depend", IntakeTool.DEFAULT_KEY);
        Issue childIssue = null;
        boolean isValid = true;

        // Check that dependency type entered is valid
        Field type = group.get("TypeId");
        type.setRequired(true);
        if (!type.isValid())
        {
            type.setMessage("EnterValidDependencyType");
            isValid = false;
        }

        // Check that child ID entered is valid
        Field childId = group.get("ObserverUniqueId");
        childId.setRequired(true);
        if (!childId.isValid())
        {
            childId.setMessage("EnterValidIssueId");
            isValid = false;
        }
        else
        {
            // Check that child ID entered corresponds to a valid issue
            childIssue = scarabR.getIssue(childId.toString());
            if (childIssue == null)
            {
                childId.setMessage("EnterValidIssueId");
                isValid = false;
            }
            // Make sure issue is not being marked as dependant on itself.
            else if (childIssue != null && childIssue.equals(issue))
            {
                childId.setMessage("CannotAddSelfDependency");
                isValid = false;
            }
        }
        if (intake.isAllValid() && isValid)
        {
            Depend depend = DependManager.getInstance();
            depend.setDefaultModule(scarabR.getCurrentModule());
            group.setProperties(depend);
            ActivitySet activitySet = null;
            try
            {
                activitySet = issue
                    .doAddDependency(activitySet, depend, childIssue, user);
                intake.remove(group);
            }
            catch (Exception e)
            {
                scarabR.setAlertMessage(e.getMessage());
                return;
            }

            if (activitySet != null)
            {
                // FIXME: I think that we are sending too many emails here
                sendEmail(activitySet, childIssue, l10n.get(DEFAULT_MSG), 
                          context);
                sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), 
                          context);
            }
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = null;
        try
        {
            issue = getIssueFromRequest(data.getParameters());
        }
        catch (ScarabException se)
        {
            scarabR.setAlertMessage(se.getMessage());
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               issue.getModule()))
        {
            data.getParameters().add("issue_ids", issue.getUniqueId());
            scarabR.resetAssociatedUsers();
            setTarget(data, "AssignIssue.vm");
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
        }
    }

    /**
     * Redirects to MoveIssue page with move action selected.
     */
    public void doMove(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().add("mv_0rb", "move");
        setTarget(data, "MoveIssue.vm");            
    }

    /**
     * Redirects to MoveIssue page with copy action selected.
     */
    public void doCopy(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().add("mv_0rb", "copy");
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
}
