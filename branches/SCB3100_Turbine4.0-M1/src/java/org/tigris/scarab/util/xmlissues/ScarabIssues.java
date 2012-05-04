package org.tigris.scarab.util.xmlissues;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.fulcrum.localization.Localization;
import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivityManager;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetType;
import org.tigris.scarab.om.ActivitySetTypeManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ComponentLocator;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

/**
 * <p>This class manages the validation and importing of issues.</p>
 *
 * <p>This classes has a format dictated by the <a
 * href="http://jakarta.apache.org/commons/betwixt/">Betwixt</a>
 * parser. As the parser extracts elements out of the XML file, it
 * looks for the public getters and setters in this class's signature
 * and thereby makes determinations on what JavaBean methods to call
 * in this class. Reading this source file in isolation: Doing so
 * would make it look like you could do things like remove the data
 * member 'issue' and its accessor methods because it looks as though
 * they are unused, whereas they are in fact signals to the Betwixt
 * parser: It reads them and interprets their presence and instruction
 * to create instances of issues from the XML being parsed.</p>
 *
 * <p>Also, of note, the design of this class is that, it has two
 * modes based off the setting of the inValidationMode class.  When
 * parsing w/ the {@link #inValidationMode} flag set, the db is not
 * touched. The code just validates the XML's data content checking
 * the users exist in the db, that the attributes and modules
 * referenced already exit.  A parse with the {@link
 * #inValidationMode} set to false will do actual insert of the XML
 * issues.</p>
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id$
 */
public class ScarabIssues implements java.io.Serializable
{
    private static final Logger LOG = Logger.getLogger(ScarabIssues.class);

    private XmlModule module = null;

    /**
     * Betwixt parser adds here instance of issues found in parsed xml.
     */
    private List issues = null;

    private String importType = null;

    private int importTypeCode = -1;
    
    private final List allDependencies = new ArrayList();

    /**
     * Maps issue IDs from the XML file to IDs assigned by the DB.
     */
    private final Map issueXMLMap = new HashMap();

    /**
     * Maps activity set IDs from the XML file to IDs assigned by the
     * DB.
     */
    private final Map activitySetIdMap = new HashMap();

    /**
     * Maps attachment IDs from the XML file to IDs assigned by the
     * DB.
     */
    private final Map attachmentIdMap = new HashMap();

    /**
     * Maps dependency IDs from the XML file to IDs assigned by the
     * DB.
     */
    private final Set/*<Dependency>*/ dependActivitySetId = new HashSet/*<Dependency>*/();

    private static final int CREATE_SAME_DB = 1;
    private static final int CREATE_DIFFERENT_DB = 2;
    private static final int UPDATE_SAME_DB = 3;

    private static Attribute nullAttribute = null;

    /**
     * We default to be in validation mode.  Insert only occurs
     * post-validation.
     */
    private boolean inValidationMode = true;

    /**
     * A record of any errors encountered during the import.  Set by
     * ImportIssues after an instance is created during XML parsing.
     */
    ImportErrors importErrors;

    /**
     * The users referenced by the XML file.
     */
    private final Set importUsers = new HashSet();
    
    /** property for adding users during the import **/
    private final boolean addUsers;

    /**
     * The current file attachment handling code has a security bug
     * that can allow a user to see any file on the host that is
     * readable by scarab.  It is not easy to exploit this hole, and
     * there are cases where we want to use the functionality and can
     * be sure the hole is not being exploited.  So adding a flag to
     * disallow file attachments when importing through the UI.
     *
     * This flag is set by ImportIssues after our instance is created
     * during XML parsing.
     */
    private boolean allowFileAttachments = false;

    private boolean allowGlobalImport = false;
    
    public ScarabIssues()
    {
        issues = new ArrayList();
        if (nullAttribute == null)
        {
            try
            {
                nullAttribute = Attribute.getInstance(0);
            }
            catch (Exception e)
            {
                LOG.warn("Could not assign nullAttribute", e);
            }
        }
        // fetch property here so it can be changed at runtime
        addUsers = Turbine.getConfiguration()                 
            .getBoolean(ScarabConstants.IMPORT_ADD_USERS, false);
    }

    /**
     * Instances of this class will have their {@link
     * #allowFileAttachments} flag set to this value upon
     * instantiation.  Current file attachment handling code has a
     * security bug that can allow a user to see any file on the host
     * that is readable by scarab.  It is not easy to exploit this
     * hole, and there are cases where we want to use the
     * functionality and can be sure the hole is not being exploited.
     * So adding a flag to allow file attachments under certain
     * circumstances.
     */
    public void allowFileAttachments(final boolean flag)
    {
        this.allowFileAttachments = flag;
    }
    
    public void allowGlobalImports(final boolean flag){
        allowGlobalImport = flag;
    }

    public void inValidationMode(final boolean flag)
    {
        inValidationMode = flag;
    }

    public void setImportType(final String value)
    {
        this.importType = value;
        if (importType.equals("create-same-db"))
        {
            importTypeCode = CREATE_SAME_DB;
        }
        else if (importType.equals("create-different-db"))
        {
            importTypeCode = CREATE_DIFFERENT_DB;
        }
        else if (importType.equals("update-same-db"))
        {
            importTypeCode = UPDATE_SAME_DB;
        }
    }

    public String getImportType()
    {
        return this.importType;
    }

    public int getImportTypeCode()
    {
        return importTypeCode;
    }

    /**
     * @return Map of original id -> new scarab id.
     */
    public Map getIDs()
    {
        return this.issueXMLMap;
    }

    public XmlModule getModule()
    {
            // it's ok to not define a module in the xml
            //  just use the an empty module
        return ( module != null )? module : new XmlModule();
    }

    public void setModule(final XmlModule module)
    {
        LOG.debug("Module.setModule(): " + module.getName());
        this.module = module;
    }

    void doValidateUsers()
    {
        if (importUsers != null && !importUsers.isEmpty())
        {
            for (Iterator itr = importUsers.iterator(); itr.hasNext();)
            {
                final String userStr = (String)itr.next();
                try
                {
                    ScarabUser user = findUser(userStr);
                    if (user == null && addUsers)
                    {
                        user  = ScarabUserManager.getAnonymousUser();
                        user.setUserName(userStr);
                        user.setFirstName(userStr);
                        user.setLastName("");
                        user.setEmail(userStr.indexOf('@') >0 ? userStr : userStr+"@localhost");
                        user.setPassword(userStr);

                        user.createNewUser();

                        // if we got here, then all must be good...

                        ScarabUserImpl.confirmUser(userStr);
                        // force the user to change their password the first time they login
                        user.setPasswordExpire(Calendar.getInstance());
                    }
                }
                catch (Exception e)
                {
                    final String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotLocateUsername", userStr);
                    importErrors.add(error);
                }
            }
        }
    }

    void doValidateDependencies()
    {
        if (allDependencies != null && !allDependencies.isEmpty())
        {
            for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
            {
                final XmlActivity activity = (XmlActivity)itr.next();
                final Dependency dependency = activity.getDependency();
                
//          // FIXME the following checks don't work because issueXMLMap hasn't been filled                
//                final String child = (String)issueXMLMap.get(dependency.getChild());
//                final String parent = (String)issueXMLMap.get(dependency.getParent());
//                if (parent == null || child == null)
//                {
//                    LOG.debug("Could not find issues for parent '" + parent + "'(" + dependency.getChild() 
//                            + ") and child '" + child + "\' (" + dependency.getParent() + ')');
//                }
//                else
//                {
//                    try
//                    {
//                        final Issue parentIssueOM = IssueManager.getIssueById(parent);
//                        if (parentIssueOM == null)
//                        {
//                            throw new IllegalArgumentException("Missing parent issue"); //EXCEPTION
//                        }
//                    }
//                    catch (Exception e)
//                    {
//                        final String error = Localization.format(
//                            ScarabConstants.DEFAULT_BUNDLE_NAME,
//                            getLocale(),
//                            "CouldNotLocateParentDepend", parent);
//                        importErrors.add(error);
//                    }
//                    try
//                    {
//                        final Issue childIssueOM = IssueManager.getIssueById(child);
//                        if (childIssueOM == null)
//                        {
//                            throw new IllegalArgumentException("Missing child issue"); //EXCEPTION
//                        }
//                    }
//                    catch (Exception e)
//                    {
//                        final String error = Localization.format(
//                            ScarabConstants.DEFAULT_BUNDLE_NAME,
//                            getLocale(),
//                            "CouldNotLocateChildDepend", child);
//                        importErrors.add(error);
//                    }
//                }
            }
        }
        allDependencies.clear();
    }

    void doHandleDependencies()
        throws ScarabException, TorqueException
    {
        LOG.debug("Number of dependencies found: " + allDependencies.size());
        for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
        {
            final Object[] data = (Object[])itr.next();
            final ActivitySet activitySetOM = (ActivitySet) data[0];
            final XmlActivity activity = (XmlActivity) data[1];
            

            final Dependency dependency = activity.getDependency();
            final String child = (String)issueXMLMap.get(dependency.getChild());
            final String parent = (String)issueXMLMap.get(dependency.getParent());
            if (parent == null || child == null)
            {
                if(null != parent || null != child)
                {
                    // add a comment into the issue that informs of the dependency
                    final Issue issueOM = IssueManager.getIssueById(null == parent ? child : parent);
                    final Attachment attachmentOM = new Attachment();
                    attachmentOM.setName("comment");
                    attachmentOM.setTypeId(Attachment.COMMENT__PK);
                    attachmentOM.setMimeType("text/plain");
                    // TODO i18n this
                    final String text = "Dependency \"" 
                            + parent + " (originally " + dependency.getParent() + ") " + dependency.getType() + ' ' 
                            + child + " (originally " + dependency.getParent()
                        + ") \" was not imported due to " 
                        + (null == parent ? dependency.getParent() : dependency.getChild()) + " not being resolved";
                    attachmentOM.setData(text);                    
                    issueOM.addComment(attachmentOM, ScarabUserManager.getInstance("Administrator"));
                }
                
                LOG.debug("Could not find issues: parent: " + parent + " child: " + child);
                LOG.debug("----------------------------------------------------");
                continue;
            }
            LOG.debug("doHandleDependencies: " + dependency);
            if (getImportTypeCode() == UPDATE_SAME_DB)
            {
                LOG.error("[TODO] update-same-db import type not yet implemented");
                // trick here is that dependencies don't have ids or unique keys to find the 
                //  correct existing instance against.
            }
            else
            {
                try
                {
                    final String type = dependency.getType();
                    final Depend newDependOM = DependManager.getInstance();
                    final Issue parentIssueOM = IssueManager.getIssueById(parent);
                    final Issue childIssueOM = IssueManager.getIssueById(child);
                    newDependOM.setDefaultModule(parentIssueOM.getModule());
                    newDependOM.setObservedId(parentIssueOM.getIssueId());
                    newDependOM.setObserverId(childIssueOM.getIssueId());
                    newDependOM.setDependType(type);
                    LOG.debug("Dep: " + type + " Parent: " + parent + " Child: " + child);
                    LOG.debug("XML Activity id: " + activity.getId());
                    if (activity.isAddDependency())
                    {
                        parentIssueOM
                          .doAddDependency(activitySetOM, newDependOM, childIssueOM, null);
                        LOG.debug("Added Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("----------------------------------------------------");
                    }
                    else if (activity.isDeleteDependency())
                    {
                        parentIssueOM
                          .doDeleteDependency(activitySetOM, newDependOM, null);
                        LOG.debug("Deleted Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("----------------------------------------------------");
                    }
                    else if (activity.isUpdateDependency())
                    {
                        final Depend oldDependOM = parentIssueOM.getDependency(childIssueOM);
                        if (oldDependOM == null)
                        {
                            throw new IllegalArgumentException ("Whoops! Could not find the original dependency!"); //EXCEPTION
                        }
                        // we definitely know we are doing an update here.
                        newDependOM.setDeleted(false);
                        parentIssueOM
                          .doChangeDependencyType(activitySetOM, oldDependOM, newDependOM, null);
                        LOG.debug("Updated Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("Old Type: " + oldDependOM.getDependType().getName() + " New type: " + newDependOM.getDependType().getName());
                        LOG.debug("----------------------------------------------------");
                    }
                }
                catch (Exception e)
                {
                    LOG.error("Failed to handle dependencies", e);
                    throw new ScarabException(new L10NKey("Failed to handle dependencies <localize me>"),e); //EXCEPTION
                }                
            }
        }
    }

    public List getIssues()
    {
        return issues;
    }

    public void addIssue(final XmlIssue issue)
        throws ScarabException
    {
        LOG.debug("Module.addIssue(): " + issue.getId());
        try
        {
            if (inValidationMode)
            {
                importErrors.setParseContext((issue.hasModuleCode()
                    ?"":module.getCode()) + issue.getId());
                doIssueValidateEvent(getModule(), issue);
            }
            else
            {
                doIssueEvent(getModule(), issue);
            }
        }
        catch (TorqueException e)
        {
            e.printStackTrace();
            throw  new ScarabException(new L10NKey("Exception adding issue"+issue.getId()),e); // FIXME localise
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw  new ScarabException(new L10NKey("Exception adding issue"+issue.getId()),e); // FIXME localise
        }
        finally
        {
            importErrors.setParseContext(null);
        }
    }

    /**
     * Validates the data from a XML representation of an issue.
     * Examines as much of the data as possible, even if errors are
     * encountered in parent data.
     *
     * @param module The module containing <code>issue</code>.
     * @param issue The issue to validate.
     */
    private void doIssueValidateEvent(final XmlModule module, 
            final XmlIssue issue)
        throws TorqueException
    {
        // Check for the existance of the module.
        Module moduleOM = null;
        try
        {
            moduleOM = getModuleForIssue(module,issue);
            if (moduleOM == null)
            {
                throw new IllegalArgumentException(); //EXCEPTION
            }

            // TODO: Handle user import.  Until then, ignore the
            // module's owner.
            //importUsers.add(module.getOwner());
        }
        catch (Exception e)
        {
            final Object[] args = (issue.hasModuleCode()
                ? new Object[]{null, issue.getModuleCode(), module.getDomain()}
                : new Object[]{module.getName(), module.getCode(), module.getDomain()});
            final String error = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "CouldNotFindModule", args);
            importErrors.add(error);
        }

        // Check for the existance of the issue type.
        IssueType issueTypeOM = null;
        try
        {
            issueTypeOM = IssueType.getInstance(issue.getArtifactType());
            if (issueTypeOM == null)
            {
                throw new IllegalArgumentException(); //EXCEPTION
            }
            if (!moduleOM.getRModuleIssueType(issueTypeOM).getActive())
            {
                final String error = Localization.format(
                    ScarabConstants.DEFAULT_BUNDLE_NAME,
                    getLocale(),
                    "IssueTypeInactive", issue.getArtifactType());
                importErrors.add(error);
            }
            List moduleAttributeList = null;
            if (moduleOM != null)
            {
                moduleAttributeList = moduleOM.getAttributes(issueTypeOM);
            }

            final List activitySets = issue.getActivitySets();
            for (Iterator itr = activitySets.iterator(); itr.hasNext();)
            {
                final XmlActivitySet activitySet = (XmlActivitySet) itr.next();
                if (activitySet.getCreatedBy() != null)
                {
                    importUsers.add(activitySet.getCreatedBy());
                }
                if (activitySet.getAttachment() != null)
                {
                    final String attachCreatedBy = activitySet.getAttachment().getCreatedBy();
                    if (attachCreatedBy != null)
                    {
                        importUsers.add(attachCreatedBy);
                    }
                }

                // Validate the activity set's type.
                try
                {
                    final ActivitySetType ttOM =
                        ActivitySetTypeManager.getInstance(activitySet.getType());
                    if (ttOM == null)
                    {
                        throw new IllegalArgumentException(); //EXCEPTION
                    }
                }
                catch (Exception e)
                {
                    final String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotFindActivitySetType", activitySet.getType());
                    importErrors.add(error);
                }

                // Validate the activity set's date.
                validateDate(activitySet.getCreatedDate(), true);

                final List activities = activitySet.getActivities();
                for (Iterator itrb = activities.iterator(); itrb.hasNext();)
                {
                    validateActivity(moduleOM, issueTypeOM, moduleAttributeList,
                                     activitySet, (XmlActivity) itrb.next());
                }
            }
        }
        catch (Exception e)
        {
            final String error = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "CouldNotFindIssueType", issue.getArtifactType());
            importErrors.add(error);
        }
    }

    /**
     * Validates an individual activity.  A helper method for {@link
     * #doIssueValidateEvent(XmlModule, XmlIssue)}.
     *
     * @param moduleOM
     * @param issueTypeOM
     * @param moduleAttributeList The attributes for
     * <code>moduleOM</code> and <code>issueTypeOM</code>.
     * @param activitySet The transaction which <code>activity</code>
     * was a part of.
     * @param activity The activity to validate.
     * @see #doIssueValidateEvent(XmlModule, XmlIssue)
     */
    private void validateActivity(final Module moduleOM, 
            final IssueType issueTypeOM,
            final List moduleAttributeList,
            final XmlActivitySet activitySet,
            final XmlActivity activity)
    {
        validateDate(activity.getEndDate(), false);
        if (activity.getOldUser() != null)
        {
            importUsers.add(activity.getOldUser());
        }
        if (activity.getNewUser() != null)
        {
            importUsers.add(activity.getNewUser());
        }
        final XmlAttachment activityAttachment = activity.getAttachment();
        if (activityAttachment != null)
        {
            if (allowFileAttachments &&
                activityAttachment.getReconcilePath() &&
                !new File(activityAttachment.getFilename()).exists())
            {
                final String error = Localization.format
                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                     "CouldNotFindFileAttachment",
                     activityAttachment.getFilename());
                importErrors.add(error);
            }

            validateDate(activityAttachment.getCreatedDate(), true);
            validateDate(activityAttachment.getModifiedDate(), false);

            final String attachCreatedBy = activityAttachment.getCreatedBy();
            if (attachCreatedBy != null)
            {
                importUsers.add(attachCreatedBy);
            }
        }

        // Get the Attribute associated with the Activity
        Attribute attributeOM = null;
        final String activityAttribute = activity.getAttribute();
        try
        {
            attributeOM = Attribute.getInstance(activityAttribute);
            if (attributeOM == null)
            {
                throw new Exception(); //EXCEPTION
            }
        }
        catch (Exception e)
        {
            final String error = Localization.format
                (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                 "CouldNotFindGlobalAttribute", activityAttribute);
            importErrors.add(error);
        }

        if (attributeOM != null)
        {
            if (attributeOM.equals(nullAttribute))
            {
                // Add any dependency activities to a list for later
                // processing.
                if (isDependencyActivity(activity))
                {
                    if (!isDuplicateDependency(activity))
                    {
                        allDependencies.add(activity);
                        LOG.debug("+------------Stored Dependency # " +
                                  allDependencies.size() + '[' + activity.getDependency() + ']');
                    }

                    // Dependency activities don't require further
                    // validation.
                    return;
                }
            }
            else 
                try 
                {
                    if( !attributeOM.isUserAttribute() )
                    {
                        // The null attribute will never be in this list.
                        if (moduleAttributeList != null &&
                            moduleAttributeList.indexOf(attributeOM) < 0)
                        {
                            final String error = Localization.format
                                (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                                 "CouldNotFindRModuleAttribute", activityAttribute);
                            importErrors.add(error);
                        }
                        else if (activity.getNewOption() != null)
                        {
                            // check for global options
                            AttributeOption attributeOptionOM = null;
                            try
                            {
                                attributeOptionOM = AttributeOptionManager.getInstance(
                                                attributeOM, activity.getNewOption(),
                                                moduleOM, issueTypeOM);
                                if (attributeOptionOM == null)
                                {
                                    throw new Exception(); //EXCEPTION
                                }
                            }
                            catch (Exception e)
                            {
                                final Object[] args = {
                                                  activity.getNewOption(),
                                                  attributeOM.getName(),
                                                  issueTypeOM.getName()};
                                final String error = Localization.format
                                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                                     "CouldNotFindAttributeOption", args);
                                importErrors.add(error);
                                
                                AttributeOptionManager.getInstance(
                                                attributeOM, activity.getNewOption(),
                                                moduleOM, issueTypeOM);
                            }
                            // check for module options
                            try
                            {
                                final RModuleOption rmo = RModuleOptionManager
                                    .getInstance(moduleOM, issueTypeOM,
                                                 attributeOptionOM);
                                if (rmo == null)
                                {
                                    throw new Exception(); //EXCEPTION
                                }
                            }
                            catch (Exception e)
                            {
                                final Object[] args = {
                                        activity.getNewOption(),
                                        attributeOM.getName(),
                                        issueTypeOM.getName()};
                                final String error = Localization.format
                                    (ScarabConstants.DEFAULT_BUNDLE_NAME,
                                     getLocale(),
                                     "CouldNotFindModuleAttributeOption",
                                     args);
                                importErrors.add(error);
                            }
                        }
                        else if (activity.getOldOption() != null)
                        {
                            AttributeOption attributeOptionOM = null;
                            try
                            {
                                attributeOptionOM = AttributeOptionManager
                                    .getInstance(attributeOM, activity.getOldOption());
                                if (attributeOptionOM == null)
                                {
                                    throw new Exception(); //EXCEPTION
                                }
                            }
                            catch (Exception e)
                            {
                                final String error = Localization.format
                                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                                     "CouldNotFindAttributeOption",
                                     activity.getOldOption());
                                importErrors.add(error);
                            }
                            // check for module options
                            try
                            {
                                final RModuleOption rmo = RModuleOptionManager
                                    .getInstance(moduleOM, issueTypeOM,
                                                 attributeOptionOM);
                                if (rmo == null)
                                {
                                    throw new Exception(); //EXCEPTION
                                }
                            }
                            catch (Exception e)
                            {
                                final Object[] args = { activity.getOldOption(),
                                                  attributeOM.getName() };
                                final String error = Localization.format
                                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                                     "CouldNotFindModuleAttributeOption", args);
                                importErrors.add(error);
                            }
                        }
                    }
                } 
                catch (TorqueException ex) 
                {
                    ex.printStackTrace();
                    importErrors.add(ex);
                }
        }
    }

    /**
     * Records any validation errors encountered.
     *
     * @param xmlDate The XML bean for the date.
     * @param required Whether a valid date is required (parse errors
     * are reported regardless of this setting).
     */
    private void validateDate(final BaseDate xmlDate, final boolean required)
    {
        try
        {
            // Report parse error failures even for optional dates.
            if ((xmlDate != null && xmlDate.getDate() == null) && required)
            {
                // Trigger error handling.
                throw new ParseException(null, -1); //EXCEPTION
            }
        }
        catch (ParseException e)
        {
            final String errorMsg =
                (e.getErrorOffset() != -1 ? ": " + e.getMessage() : "");
            final String[] args = { xmlDate.getTimestamp(), xmlDate.getFormat(),
                              errorMsg };
            final String error = Localization.format
                (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                 "InvalidDate", args);
            importErrors.add(error);
        }
    }

    private Issue createNewIssue(final XmlModule module, final XmlIssue issue, final String id)
        throws TorqueException,ScarabException
    {
        // get the instance of the module
        final Module moduleOM = getModuleForIssue(module,issue);
        // get the instance of the issue type
        final IssueType issueTypeOM = IssueType.getInstance(issue.getArtifactType());
        issueTypeOM.setName(issue.getArtifactType());
        // get me a new issue since we couldn't find one before
        final Issue issueOM = Issue.getNewInstance(moduleOM, issueTypeOM);

        // The import data may nominate its ID
        if (id != null) {
            // This will cause Issue.save() to use this ID
            issueOM.setFederatedId(id);
        }
        // create the issue in the database

        // Add the mapping between the issue id and the id that was created.
        // This mapping is used dependency checking and printing out in 
        // results list of original id and new id. The original issue id can be
        // null. In this case, have the original id show as 'null (index)'
        // where index is count into the issueXMLMap. We add the index to keep
        // the key unique. This substitute original id also shouldn't interfere
        // w/ issueXMLMap's use dependency checking.
        String issueID = "Null (" + Integer.toString(issueXMLMap.size()) + ")";
        if(issue.getId() != null)
        {
            issueID = (issue.hasModuleCode()?"":module.getCode())
                + issue.getId();
        }
        issueXMLMap.put(issueID, issueOM.getUniqueId());

        LOG.debug("Created new Issue: " + issueOM.getUniqueId());
        return issueOM;
    }    

    private void doIssueEvent(final XmlModule module, final XmlIssue issue)
        throws TorqueException,ScarabException,ParseException
    {
/////////////////////////////////////////////////////////////////////////////////  
        // Get me an issue
        Issue issueOM = null;
        final String issueID = (issue.hasModuleCode() ? "" : module.getCode()) + issue.getId();
        if (getImportTypeCode() == CREATE_SAME_DB || getImportTypeCode() == CREATE_DIFFERENT_DB)
        {
            // Check if the new issue nominates an ID and if the database does
            // not already contain an issue with that ID
            if (issue.getId() != null && IssueManager.getIssueById(issueID) == null)
            {
                // Create the new issue with the nominated ID
                issueOM = createNewIssue(module, issue, issue.getId());
            }
            else
            {
                // Crate the new issue with an automatically allocated ID
                issueOM = createNewIssue(module, issue, null);
            }
        }
        else if (getImportTypeCode() == UPDATE_SAME_DB) // nice to specify just for searching/refactoring
        {
            issueOM = IssueManager.getIssueById(issueID);
            
            if (issueOM == null)
            {
                issueOM = createNewIssue(module, issue, null);
            }
            else
            {
                LOG.debug("Found Issue in db: " + issueOM.getUniqueId());
            }
        }

/////////////////////////////////////////////////////////////////////////////////  

        // Loop over the XML activitySets
        final List activitySets = issue.getActivitySets();
        LOG.debug("-----------------------------------");
        LOG.debug("Number of ActivitySets in Issue: " + activitySets.size());
        for (Iterator itr = activitySets.iterator(); itr.hasNext();)
        {
            final XmlActivitySet activitySet = (XmlActivitySet) itr.next();
            LOG.debug("Processing ActivitySet: " + activitySet.getId());

/////////////////////////////////////////////////////////////////////////////////  
            // Deal with the attachment for the activitySet
            final XmlAttachment activitySetAttachment = activitySet.getAttachment();
            Attachment activitySetAttachmentOM = null;
            if (activitySetAttachment != null)
            {
                if (getImportTypeCode() == UPDATE_SAME_DB)
                {
                    try
                    {
                        activitySetAttachmentOM = AttachmentManager
                            .getInstance(activitySetAttachment.getId());
                        LOG.debug("Found existing ActivitySet Attachment");
                    }
                    catch (TorqueException e)
                    {
                        activitySetAttachmentOM = createAttachment(issueOM, activitySetAttachment);
                    }
                }
                else
                {
                    activitySetAttachmentOM = createAttachment(issueOM, activitySetAttachment);
                    LOG.debug("Created ActivitySet Attachment object");
                }
            }
            else
            {
                LOG.debug("OK- No Attachment in this ActivitySet");
            }

/////////////////////////////////////////////////////////////////////////////////  
            // Attempt to get the activitySet OM
            boolean alreadyCreated = false;
            ActivitySet activitySetOM = null;
            if (getImportTypeCode() == UPDATE_SAME_DB)
            {
                try
                {
                    activitySetOM = ActivitySetManager.getInstance(activitySet.getId());
                    LOG.debug("Found ActivitySet: " + activitySet.getId() + 
                              " in db: " + activitySetOM.getActivitySetId());
                }
                catch (Exception e)
                {
                    activitySetOM = ActivitySetManager.getInstance();
                }
            }
            else
            {
                // first try to get the ActivitySet from the internal map
                if (activitySetIdMap.containsKey(activitySet.getId()))
                {
                    activitySetOM = ActivitySetManager.getInstance(
                        (String)activitySetIdMap.get(activitySet.getId()));
                    alreadyCreated = true;
                    LOG.debug("Found ActivitySet: " + activitySet.getId() + 
                              " in map: " + activitySetOM.getActivitySetId());
                }
                else // it hasn't been encountered previously
                {
                    activitySetOM = ActivitySetManager.getInstance();
                    LOG.debug("Created new ActivitySet");
                }
            }

            final ScarabUser activitySetCreatedByOM = findUser(activitySet.getCreatedBy());
            
            if (LOG.isDebugEnabled())
            {
                LOG.debug("ActivitySet: " + activitySet.getId() + "; of type: " + activitySet.getType() + "; by: " + activitySet.getCreatedBy());
                LOG.debug("   alreadyCreated: " + alreadyCreated);
            }
            
            if (!alreadyCreated)
            {
                // Populate the ActivitySet
                // Get the ActivitySet type/createdby values (we know these are valid)
                final ActivitySetType ttOM = ActivitySetTypeManager.getInstance(activitySet.getType());
                activitySetOM.setActivitySetType(ttOM);
                if( activitySetCreatedByOM != null ){
                    activitySetOM.setCreatedBy(activitySetCreatedByOM.getUserId());
                }
                else
                {
                	// Anonymous user. better than nothing.
                	try {
						activitySetOM.setCreatedBy(ScarabUserManager.getAnonymousUser().getUserId());
					} catch (Exception e) {
						LOG.error("doIssueEvent: Cannot get Anonymous user: e");
					}
                }
                activitySetOM.setCreatedDate(activitySet.getCreatedDate().getDate());
                if (activitySetAttachmentOM != null)
                {
                    activitySetAttachmentOM.save();
                    activitySetOM.setAttachment(activitySetAttachmentOM);
                }
                activitySetOM.save();
                if( activitySet.getId() != null){
                    // if id is valid, save for later re-use.
                    activitySetIdMap.put(activitySet.getId(), 
                                         activitySetOM.getPrimaryKey().toString());
                }
            }

            // Determine if this ActivitySet should be marked as the 
            // creation event
            final ActivitySet creationSet = issueOM.getActivitySetRelatedByCreatedTransId();
            if (ActivitySetTypePeer.CREATE_ISSUE__PK
                .equals(activitySetOM.getTypeId()) 
               ||
               (ActivitySetTypePeer.MOVE_ISSUE__PK
                .equals(activitySetOM.getTypeId()) && 
                        (creationSet == null || activitySetOM.getCreatedDate()
                         .before(creationSet.getCreatedDate()))) ) 
            {
                issueOM.setActivitySetRelatedByCreatedTransId(activitySetOM);
                issueOM.save();
            }

/////////////////////////////////////////////////////////////////////////////////  
// Deal with changing user attributes. this code needs to be in this *strange*
// location because we look at the entire activityset in order to determine
// that this is a change user activity set. of course in the future, it would
// be really nice to create an activityset/activiy type that more accurately 
// reflects what type of change this is. so that it is easier to case for. for
// now, we just look at some fingerprints to determine this information. -JSS

            if (activitySet.isChangeUserAttribute())
            {
                final List activities = activitySet.getActivities();
                final XmlActivity activityA = (XmlActivity)activities.get(0);
                final XmlActivity activityB = (XmlActivity)activities.get(1);
                
                final ScarabUser assigneeOM = findUser(activityA.getOldUser());
                final ScarabUser assignerOM = findUser(activityB.getNewUser());

                final Attribute oldAttributeOM = Attribute.getInstance(activityA.getAttribute());

                final AttributeValue oldAttValOM = issueOM.getUserAttributeValue(assigneeOM, oldAttributeOM);
                if (oldAttValOM == null)
                {
                    LOG.error("User '" + assigneeOM.getName() + "' was not previously '" + oldAttributeOM.getName() + "' to the issue!");
                }

                // Get the Attribute associated with the new Activity
                final Attribute newAttributeOM = Attribute.getInstance(activityB.getAttribute());

                issueOM.changeUserAttributeValue(activitySetOM,
                            assigneeOM, 
                            assignerOM, 
                            oldAttValOM,
                            newAttributeOM, null);
                LOG.debug("-------------Updated User AttributeValue------------");
                continue;
            }

/////////////////////////////////////////////////////////////////////////////////  

            // Deal with the activities in the activitySet
            final List activities = activitySet.getActivities();
            LOG.debug("Number of Activities in ActivitySet: " + activities.size());

            final LinkedMap avMap = issueOM.getModuleAttributeValuesMap();
            LOG.debug("Total Module Attribute Values: " + avMap.size());
            for (Iterator itrb = activities.iterator(); itrb.hasNext();)
            {
                final XmlActivity activity = (XmlActivity) itrb.next();
                LOG.debug("Looking at activity id: " + activity.getId());

                // Get the Attribute associated with the Activity
                final Attribute attributeOM = Attribute.getInstance(activity.getAttribute());

                // deal with the activity attachment (if there is one)
                final XmlAttachment activityAttachment = activity.getAttachment();
                Attachment activityAttachmentOM = null;
                if (activityAttachment != null)
                {
                    // look for an existing attachment in the activity
                    // the case is when we have a URL and we create it
                    // and then delete it, the attachment id is still the
                    // same so there is no reason to re-create the attachment
                    // again.
                    final String previousXmlId = activityAttachment.getId();
                    final String previousId = (String)attachmentIdMap.get(previousXmlId);
                    if (previousId == null) 
                    {
                        activityAttachmentOM = createAttachment(issueOM, activityAttachment);
                        activityAttachmentOM.save();
                        attachmentIdMap.put(previousXmlId, activityAttachmentOM.getPrimaryKey().toString());
                        
                        // Special case. After the Attachment object has been 
                        // saved, if the ReconcilePath == true, then assume 
                        // that the fileName is an absolute path to a file and
                        // copy it to the right directory
                        // structure under Scarab's path.
                        if (allowFileAttachments && activityAttachment.getReconcilePath())
                        {
                            try
                            {
                                activityAttachmentOM
                                    .copyFileFromTo(activityAttachment.getFilename(), 
                                                    activityAttachmentOM.getFullPath());
                            } 
                            catch (FileNotFoundException ex)
                            {
                                // FIXME correct error message "ExceptionCouldNotFindFile"
                                throw new ScarabException(L10NKeySet.ExceptionGeneral,ex);
                            } 
                            catch (IOException ex)
                            {
                                // FIXME correct error message "ExceptionCouldNotReadFile"
                                throw new ScarabException(L10NKeySet.ExceptionGeneral,ex);
                            }
                        }
                        LOG.debug("Created Activity Attachment object");
                    }
                    else 
                    {
                        activityAttachmentOM = AttachmentManager
                            .getInstance(previousId);
                        LOG.debug("Found existing Activity Attachment");
                    }
                }
                else
                {
                    LOG.debug("OK- No Attachment in this Activity");
                }

                // deal with null attributes (need to do this before we create the 
                // activity right below because this will create its own activity).
                if (attributeOM.equals(nullAttribute))
                {
                    // add any dependency activities to a list for later processing
                    if (isDependencyActivity(activity))
                    {
                        if (!isDuplicateDependency(activity))
                        {
                            final Object[] obj = {activitySetOM, activity, activityAttachmentOM};
                            allDependencies.add(obj);
                            dependActivitySetId.add(activity.getDependency());
                            LOG.debug("-------------Stored Dependency # " 
                                    + allDependencies.size() + '[' + activity.getDependency() + ']');
                            continue;
                        }
                    }
                    else
                    {
                        // create the activity record.
                        ActivityManager.createTextActivity(issueOM, nullAttribute, activitySetOM, 
                                    ActivityType.getActivityType(activity.getActivityType()), activity.getDescription(), activityAttachmentOM, 
                                    activity.getOldValue(), activity.getNewValue());
        
                        LOG.debug("-------------Saved Null Attribute-------------");
                        continue;
                    }
                }

                // create the activityOM
                createActivity(activity, module, 
                                            issueOM, attributeOM, activitySetOM);

                // check to see if this is a new activity or an update activity
                AttributeValue avalOM = null;
                for (Iterator moduleAttributeValueItr = avMap.mapIterator(); 
                     moduleAttributeValueItr.hasNext() && avalOM == null;)
                {
                    final AttributeValue testAvalOM = (AttributeValue)
                        avMap.get(moduleAttributeValueItr.next());
                    final Attribute avalAttributeOM = testAvalOM.getAttribute();

                    LOG.debug("Checking Attribute match: " + avalAttributeOM.getName() + 
                              " against: " + attributeOM.getName());
                    if (avalAttributeOM.equals(attributeOM))
                    {
                        avalOM = testAvalOM;
                    }
                }

                if (avalOM != null) 
                {
                    final Attribute avalAttributeOM = avalOM.getAttribute();
                    LOG.debug("Attributes match!");
                    AttributeValue avalOM2 = null;
                    if (!activity.isNewActivity())
                    {
                        LOG.debug("Activity is not new.");
                        avalOM2 = AttributeValue.getNewInstance(
                            avalAttributeOM.getAttributeId(), 
                            avalOM.getIssue());
                        avalOM2.setProperties(avalOM);
                    }

                    if (avalAttributeOM.isOptionAttribute())
                    {
                        LOG.debug("We have an Option Attribute: " + 
                                  avalAttributeOM.getName());
                        final AttributeOption newAttributeOptionOM = 
                                AttributeOptionManager.getInstance(
                                         attributeOM, activity.getNewOption(),
                                         issueOM.getModule(),
                                         issueOM.getIssueType());
                        if (activity.isNewActivity())
                        {
                            if (newAttributeOptionOM != null)
                            {
                                avalOM.setOptionId(newAttributeOptionOM.getOptionId());
                                avalOM.startActivitySet(activitySetOM);
                                avalOM.setAttribute(attributeOM);
                                avalOM.save();
                                LOG.debug("-------------Saved Attribute Value-------------");
                            }
                            else
                            {
                                LOG.warn("NewAttributeOptionOM is null for " +
                                         activity.getNewOption());
                            }
                        }
                        else if(newAttributeOptionOM != null)
                        {
                            avalOM2.setOptionId(newAttributeOptionOM.getOptionId());
                            final HashMap map = new HashMap();
                            map.put(avalOM.getAttributeId(), avalOM2);
                            issueOM.setAttributeValues(activitySetOM, map, null, activitySetCreatedByOM);
                            LOG.debug("-------------Saved Option Attribute Change-------------");
                        }
                    }
                    else if (avalAttributeOM.isUserAttribute())
                    {
                        LOG.debug("We have a User Attribute: " 
                                  + avalAttributeOM.getName());
                        if (activity.isNewActivity())
                        {
                            // Don't need to pass in the attachment because
                            // it is already in the activitySetOM.
                            // If we can't get an assignee new-user, then 
                            // use the activity set creator as assignee.
                            ScarabUser assigneeOM = findUser(activity.getNewUser());
                            assigneeOM = (assigneeOM != null)
                                ? assigneeOM: activitySetCreatedByOM;
                            if( assigneeOM != null ){
                                issueOM.assignUser(activitySetOM, 
                                    assigneeOM, null, avalAttributeOM, null);
                            }
                            LOG.debug("-------------Saved User Assign-------------");
                        }
                        else if (activity.isRemoveUserActivity())
                        {
                            // remove a user activity
                            final ScarabUser oldUserOM = findUser(activity.getOldUser());
                            // need to reset the aval because the current one
                            // is marked as new for some reason which causes an
                            // insert and that isn't the right behavior here 
                            // (we want an update)
                            avalOM = null;
                            for (Iterator i = issueOM.getAttributeValues(
                                 avalAttributeOM).iterator(); 
                                 i.hasNext() && avalOM == null;) 
                            {
                                final AttributeValue av = (AttributeValue)i.next();
                                if (oldUserOM.getUserId().equals(av.getUserId())) 
                                {
                                    avalOM = av;
                                }
                            }

                            if (avalOM == null) 
                            {
                                if (LOG.isDebugEnabled()) 
                                {
                                    LOG.debug("Could not find previous AttributeValue assigning " +
                                        (oldUserOM == null ? "NULL" : 
                                        oldUserOM.getUserName()) + 
                                        " to attribute " + 
                                              avalAttributeOM.getName());
                                }                                
                            }
                            else 
                            {
                                // don't need to pass in the attachment because
                                // it is already in the activitySetOM
                                issueOM.deleteUser(activitySetOM, oldUserOM, 
                                    activitySetCreatedByOM, avalOM, null);
                                LOG.debug("-------------Saved User Remove-------------");
                            }
                        }
                    }
                    else if (avalAttributeOM.isTextAttribute() || avalAttributeOM.isIntegerAttribute())
                    {
                        LOG.debug("We have a Text Attribute: " + avalAttributeOM.getName());

                        avalOM.startActivitySet(activitySetOM);
                        avalOM.setAttribute(attributeOM);

                        if (activity.isNewActivity())
                        {
                            avalOM.setValue(activity.getNewValue());
                        }
                        else if (!activity.getNewValue()
                                .equals(avalOM.getValue()))
                        {
                            avalOM2.setValue(activity.getNewValue());
                            avalOM.setProperties(avalOM2);
                        }
                        
                        avalOM.save();
                        LOG.debug("-------------Saved Attribute Value-------------");
                    }
                }
                issueOM.save();
                LOG.debug("-------------Saved Issue-------------");
            }
        }
    }

    /**
     * Checks to see if there is a Dependency value for the Activity
     */
    private boolean isDependencyActivity(final XmlActivity activity)
    {
        return (activity.getDependency() != null);
    }

    private boolean isDuplicateDependency(final XmlActivity activity)
    {
        return dependActivitySetId.contains(activity.getDependency());
    }

    private Activity createActivity(final XmlActivity activity,  
                                         final XmlModule module,
                                         final Issue issueOM, 
                                         final Attribute attributeOM,
                                         final ActivitySet activitySetOM)
        throws TorqueException, ParseException, ScarabException
    {
        Activity activityOM = null;
        if (getImportTypeCode() == UPDATE_SAME_DB)
        {
            try
            {
                activityOM = ActivityManager.getInstance(activity.getId());
            }
            catch (Exception e)
            {
                activityOM = ActivityManager.getInstance();
            }
        }
        else
        {
            activityOM = ActivityManager.getInstance();
        }

        activityOM.setIssue(issueOM);
        activityOM.setAttribute(attributeOM);
        activityOM.setActivityType(ActivityType.OTHER.getCode());
        activityOM.setActivitySet(activitySetOM);
        if (activity.getEndDate() != null)
        {
            activityOM.setEndDate(activity.getEndDate().getDate());
        }

        // Set the attachment for the activity
        Attachment newAttachmentOM = null;
        if (activity.getAttachment() != null)
        {
            newAttachmentOM = createAttachment(issueOM, activity.getAttachment());
            newAttachmentOM.save();
            activityOM.setAttachment(newAttachmentOM);
        }

        LOG.debug("Created New Activity");
        return activityOM;
    }

    private Attachment createAttachment(final Issue issueOM, 
            final XmlAttachment attachment)
        throws TorqueException, ScarabException, ParseException
    {
        final Attachment attachmentOM = AttachmentManager.getInstance();
        attachmentOM.setIssue(issueOM);
        final AttachmentType type = AttachmentType.getInstance(attachment.getType());
        if (allowFileAttachments || !Attachment.FILE__PK.equals(type.getAttachmentTypeId())) 
        {
            attachmentOM.setName(attachment.getName());
            attachmentOM.setAttachmentType(type);
            attachmentOM.setMimeType(null != attachment.getMimetype()
                    ? attachment.getMimetype()
                    : ComponentLocator.getMimeTypeService().getContentType(attachment.getFilename(), null));
            attachmentOM.setFileName(attachment.getFilename());        
            attachmentOM.setData(attachment.getData());
        }
        else 
        {
            // add a comment that the file will not be imported.  An alternative would be
            // to skip the activity altogether, but we will then need to check that there 
            // are other activities or we need to completely ignore the ActivitySet
            attachmentOM.setName("comment");
            attachmentOM.setTypeId(Attachment.COMMENT__PK);
            attachmentOM.setMimeType("text/plain");
            String text = "File, " + attachment.getFilename() + 
                ", was not imported. The old description follows:\n\n" + attachment.getName();
            final String data = attachment.getData();  // this should be null, but just in case
            if (data != null) 
            {
                text += "\n\n" + data;
            }
            attachmentOM.setData(text);
        }
        
        attachmentOM.setCreatedDate(attachment.getCreatedDate().getDate());
        final ModifiedDate modifiedDate = attachment.getModifiedDate();
        if (modifiedDate != null)
        {
            attachmentOM.setModifiedDate(modifiedDate.getDate());
        }
        final ScarabUser creUser = findUser(attachment.getCreatedBy());
        if (creUser != null)
        {
            attachmentOM.setCreatedBy(creUser.getUserId());
        }

        final String modifiedBy = attachment.getModifiedBy();
        if (modifiedBy != null)
        {
            final ScarabUser modUserOM = findUser(attachment.getModifiedBy());
            if (modUserOM != null)
            {
                attachmentOM.setModifiedBy(modUserOM.getUserId());
            }
        }

        attachmentOM.setDeleted(attachment.getDeleted());
        return attachmentOM;
    }

    private Locale getLocale()
    {
        return ScarabConstants.DEFAULT_LOCALE;
    }
    
    private ScarabUser findUser(final String userStr) 
        throws TorqueException,ScarabException
    {
        
        ScarabUser user = ScarabUserManager.getInstance(userStr);
        if (user == null && userStr != null && userStr.indexOf("@") <0 )
        {
            LOG.debug("user specified possibly by email address: "+userStr);
            // maybe it's an email not a username
            user = ScarabUserManager.getInstanceByEmail(userStr);
            LOG.debug("found "+user);
        }
        return user;        
    }
    
    private Module getModuleForIssue(final XmlModule module, final XmlIssue issue)
        throws TorqueException, ScarabException
    {
        
        if(issue.hasModuleCode() && !issue.getModuleCode().equals(module.getCode()) && !allowGlobalImport){
            throw new ScarabException(
                    new L10NKey("Lacking permission to cross-module import. Contact your administor. <localize me>"));
        }
        
        return issue.hasModuleCode()
                ? ModuleManager.getInstance(module.getDomain(),
                                            null,issue.getModuleCode())
                : ModuleManager.getInstance(module.getDomain(),
                                            module.getName(), module.getCode());
     }
}
