package org.tigris.scarab.actions.admin;

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

// Java Stuff
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.turbine.services.intake.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.om.RIssueTypeAttribute;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabLocalizedTorqueException;
import org.tigris.scarab.workflow.WorkflowFactory;

/**
 * action methods on RModuleAttribute or RIssueTypeAttribute tables
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class AttributeGroupEdit extends RequireLoginFirstAction
{
    /**
     * Updates attribute group info.
     */
    public boolean doSaveinfo (RunData data, Context context)
        throws Exception
    { 
        boolean success = true;
        // Set properties for group info
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return false;
        }
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        Group agGroup = intake.get("AttributeGroup", 
                                    ag.getQueryKey(), false);
        if (!ag.isGlobal() && scarabR.getIssueType().getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return false;
        }
        if (intake.isAllValid())
        {
            agGroup.setProperties(ag);
            ag.save();
            scarabR.setConfirmMessage(DEFAULT_MSG);
        }
        else
        {
            success = false;
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
        return success;
    }

    /**
     * Changes the properties of existing AttributeGroups and their attributes.
     */
    public boolean doSaveattributes (RunData data, Context context)
        throws Exception
    {
        boolean success = true;
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        IssueType issueType = scarabR.getIssueType();
        Module module = scarabR.getCurrentModule();

        // Check if issue type is system-defined, hence unmodifyable
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return false;
        }

        String groupId = data.getParameters().getString("groupId");
        AttributeGroup attributeGroup = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        
        LocalizationKey l10nKey = DEFAULT_MSG;

        // Check if issue type is locked
        if (!attributeGroup.isGlobal() && issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return false;
        }

        // Check for duplicate sequence numbers
        if (areThereDupeSequences(attributeGroup.getRAttributeAttributeGroups(), intake,
                "RAttributeAttributeGroup", "Order", 0))
        {
            scarabR.setAlertMessage(
                l10n.format("DuplicateSequenceNumbersFound",
                l10n.get(L10NKeySet.Attributes).toLowerCase()));
            return false;
        }

        List rmas = attributeGroup.getRModuleAttributes();
        ArrayList lockedAttrs = new ArrayList();

        if (intake.isAllValid())
        {
            // First iterate thru and check for required attributes
            // That have no active options
            Iterator i = rmas.iterator();
            while (i.hasNext()) 
            {
                RModuleAttribute rma = (RModuleAttribute)i.next();
                Group rmaGroup = intake.get("RModuleAttribute", 
                                 rma.getQueryKey(), false);
                Attribute attr = rma.getAttribute();
                if (attr.isOptionAttribute() && rmaGroup.get("Required").toString().equals("true"))
                {
                    List options = module.getRModuleOptions(rma.getAttribute(), issueType, true);
                    if (options == null || options.isEmpty())
                    {
                        scarabR.setAlertMessage(L10NKeySet.CannotRequireAttributeWithNoOptions);
                        success = false;
                    }
                }
            }
            if (success)
            {
                

                // Check whether a module specific statusAttribute was selected
                // and store it in the GLOBAL_PARAMETER table
                String key = "status_attribute_"+issueType.getIssueTypeId();
                String statusAttributeKey = data.getParameters()
                   .getString(key);
                if ( statusAttributeKey != null ) 
                {
                    String attributeId = GlobalParameterManager.getString(key,module);
                    if(attributeId == null || !attributeId.equals(statusAttributeKey))
                    {
                        GlobalParameterManager.setString(key, module, statusAttributeKey);
                    }
                }
                
                
                i = rmas.iterator();
                while (i.hasNext()) 
                {
                    boolean locked = false;
                    // Set properties for module-attribute mapping
                    RModuleAttribute rma = (RModuleAttribute)i.next();
                    Group rmaGroup = intake.get("RModuleAttribute", 
                                     rma.getQueryKey(), false);
                    Attribute attr = rma.getAttribute();

                    // Test to see if attribute is locked
                    RModuleAttribute rmaTest = rma.copy();
                    rmaTest.setModified(false);
                    rmaGroup.setProperties(rmaTest);
                    if (rmaTest.isModified())
                    {
                        RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attr);
                        if (ria != null &&  ria.getLocked())
                        {
                             lockedAttrs.add(attr);
                             locked = true;
                        }
                    }

                    if (!locked)
                    {
                        // if attribute gets set to inactive, delete dependencies
                        String newActive = rmaGroup.get("Active").toString();
                        String oldActive = String.valueOf(rma.getActive());
                        if (newActive.equals("false") && oldActive.equals("true"))
                        {
                            WorkflowFactory.getInstance()
                                .deleteWorkflowsForAttribute(attr, module, 
                                                             issueType);
                        }
                        rmaGroup.setProperties(rma);
                        String defaultTextKey = data.getParameters()
                          .getString("default_text");
                        if (defaultTextKey != null && 
                             defaultTextKey.equals(rma.getAttributeId().toString())) 
                        {
                            if (!rma.getRequired())
                            {
                                l10nKey = L10NKeySet.ChangesSavedButDefaultTextAttributeRequired;
                                intake.remove(rmaGroup);
                            }
                            rma.setIsDefaultText(true);
                            rma.setRequired(true);
                        }
                        
                        try
                        {
                            rma.save();
                            // Set properties for attribute-attribute group mapping
                            RAttributeAttributeGroup raag = 
                                attributeGroup.getRAttributeAttributeGroup(attr);
                            Group raagGroup = intake.get("RAttributeAttributeGroup", 
                                         raag.getQueryKey(), false);
                            raagGroup.setProperties(raag);
                            raag.save();
                            scarabR.setConfirmMessage(l10nKey);
                        }
                        catch (ScarabLocalizedTorqueException slte) 
                        {
                            String msg = slte.getMessage(l10n);
                            scarabR.setAlertMessage(msg);
                        }
                        catch (TorqueException te) 
                        {
                            String msg = te.getMessage();
                            scarabR.setAlertMessage(msg);
                        }
                    }

                    // If they attempted to modify locked attributes, give message.
                    if (lockedAttrs.size() > 0)
                    {
                        setLockedMessage(lockedAttrs, context);
                    }
                }
            }
        } 
        else
        {
            success = false;
            scarabR.setAlertMessage(L10NKeySet.MoreInformationWasRequired);
        }
        return success;
    }


    /**
     * Changes the properties of global AttributeGroups and their attributes.
     */
    public boolean doSaveglobal (RunData data, Context context)
        throws Exception
    {
        boolean success = true;
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        IssueType issueType = scarabR.getIssueType();

        // Check if issue type is system defined, hence unmodifyable
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return false;
        }
        if (issueType.getIssueTypeId() == null)
        {
            scarabR.setAlertMessage(L10NKeySet.IssueTypeNotFound);
            return false;
        }


        // Check for duplicate sequence numbers
        if (areThereDupeSequences(ag.getRAttributeAttributeGroups(), intake,
                                       "RAttributeAttributeGroup", "Order",0))
        {
            scarabR.setAlertMessage(l10n.format("DuplicateSequenceNumbersFound",
                l10n.get("Attributes").toLowerCase()));
            return false;
        }
        String l10nMsg = l10n.get(DEFAULT_MSG);

        if (intake.isAllValid())
        {
            List rias = ag.getRIssueTypeAttributes();

            // first check if there are required attributes
            // Without active options
            Iterator i = rias.iterator();
            while (i.hasNext()) 
            {
                RIssueTypeAttribute ria = (RIssueTypeAttribute)i.next();
                Group riaGroup = intake.get("RIssueTypeAttribute", 
                                 ria.getQueryKey(), false);
                Attribute attr = ria.getAttribute();
                if (attr.isOptionAttribute() && riaGroup.get("Required").toString().equals("true"))
                {
                    List options = issueType.getRIssueTypeOptions(ria.getAttribute(), true);
                    if (options == null || options.isEmpty())
                    if (issueType.getRIssueTypeOptions(attr, true).isEmpty())
                    {
                        scarabR.setAlertMessage(L10NKeySet.CannotRequireAttributeWithNoOptions);
                        success = false;
                    }
                }
            }
            i = rias.iterator();
            if (success)
            {
                while (i.hasNext()) 
                {
                    RIssueTypeAttribute ria = (RIssueTypeAttribute)i.next();
                    Group riaGroup = intake.get("RIssueTypeAttribute", 
                                     ria.getQueryKey(), false);
                    riaGroup.setProperties(ria);
                    String defaultTextKey = data.getParameters()
                        .getString("default_text");
                    if (defaultTextKey != null && 
                         defaultTextKey.equals(ria.getAttributeId().toString())) 
                    {
                        if (!ria.getRequired())
                        {
                            l10nMsg = l10n.get(L10NKeySet.ChangesSavedButDefaultTextAttributeRequired);
                        }
                        ria.setIsDefaultText(true);
                        ria.setRequired(true);
                        intake.remove(riaGroup);
                    }
                    ria.save();

                    // Set properties for attribute-attribute group mapping
                    RAttributeAttributeGroup raag = 
                        ag.getRAttributeAttributeGroup(ria.getAttribute());
                    Group raagGroup = intake.get("RAttributeAttributeGroup",
                                 raag.getQueryKey(), false);
                    raagGroup.setProperties(raag);
                    raag.save();
                }
                scarabR.setConfirmMessage(l10nMsg);
            }
        } 
        else
        {
            success = false;
            scarabR.setAlertMessage(l10nMsg);
        }
        return success;
    }

    /**
     * Unmaps attributes to modules.
     */
    public void doDeleteattributes(RunData data, Context context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
            .getInstance(new NumberKey(groupId), false);
        boolean hasAttributes = false;

        if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, module))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }
        if (!ag.isGlobal() && issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return;
        }
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        ArrayList lockedAttrs = new ArrayList();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
                hasAttributes = true;
                attributeId = key.substring(11);
                Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);
                RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
                if (!ag.isGlobal() && ria != null &&  ria.getLocked())
                {
                    lockedAttrs.add(attribute);
                }
                else
                {
                    try
                    {
                        ag.deleteAttribute(attribute, user, module);
                    }
                    catch (ScarabException e)
                    {
                        scarabR.setAlertMessage(l10n.getMessage(e));
                        Log.get().warn(
                            "This is an application error, if it is not permission related.", e);
                    }
                }
            }
        }
        if(!hasAttributes)
        {
            scarabR.setAlertMessage(L10NKeySet.NoAttributeSelected);
        }

        // If there are no attributes in any of the dedupe
        // Attribute groups, turn off deduping in the module
        boolean areThereDedupeAttrs = false;
        List attributeGroups = issueType.getAttributeGroups(module, true);
        if (attributeGroups.size() > 0)
        {
            for (int j=0; j<attributeGroups.size(); j++)
            {
                AttributeGroup agTemp = (AttributeGroup)attributeGroups.get(j);
                if (agTemp.getDedupe() && !agTemp.getAttributes().isEmpty())
                {
                   areThereDedupeAttrs = true;
                }
            }
            if (!areThereDedupeAttrs)
            {
                if (module == null)
                {
                    issueType.setDedupe(false);
                    issueType.save();
                }
                else
                {
                    RModuleIssueType rmit = module.getRModuleIssueType(issueType);
                    rmit.setDedupe(false);
                    rmit.save();
                }
            }
        }

        // If they attempted to modify locked attributes, give message.
        if (lockedAttrs.size() > 0)
        {
            setLockedMessage(lockedAttrs, context);
        }
        ScarabCache.clear();
        if(hasAttributes)
        {
            scarabR.setConfirmMessage(DEFAULT_MSG);
        }
    }

    /**
     * This manages clicking the create new button on AttributeSelect.vm
     */
    public void doCreatenewglobalattribute(RunData data,
                                            Context context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        Group attGroup = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
        intake.remove(attGroup);
        scarabR.setAttribute(null);
        data.setScreenTemplate(getOtherTemplate(data));
    }


    /**
     * Selects attribute to add to issue type and attribute group.
     */
    public void doSelectattribute(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        AttributeGroup ag = scarabR.getAttributeGroup();

        if (!ag.isGlobal() && scarabR.getIssueType().getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return;
        }
        String[] attributeIds = data.getParameters()
                                    .getStrings("attribute_ids");
 
        if (attributeIds == null || attributeIds.length <= 0)
        { 
            scarabR.setAlertMessage(L10NKeySet.SelectAttribute);
            return;
        }
        else
        {
            boolean alreadySubmited = false;
            for (int i=0; i < attributeIds.length; i++)
            {
                Attribute attribute =
                    scarabR.getAttribute(new Integer(attributeIds[i]));
                try
                {
                    ag.addAttribute(attribute);
                }
                catch (TorqueException e)
                {
                    alreadySubmited = true;
                    scarabR.setAlertMessage(L10NKeySet.ResubmitError);
                }
            }
            doCancel(data, context);
            if (!alreadySubmited)
            {
                scarabR.setConfirmMessage(DEFAULT_MSG);
            }
        }
    }

    /**
     * Saves all data when Done is clicked.
     */
    public void doDone (RunData data, Context context)
        throws Exception
    {
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        boolean infoSuccess = doSaveinfo(data, context);
        boolean attrSuccess = false;
        if (ag.isGlobal())
        {
            attrSuccess = doSaveglobal(data, context);
        }
        else
        {
            attrSuccess = doSaveattributes(data, context);
        }
        if (infoSuccess && attrSuccess)
        {
            doCancel(data, context);
        }
    }
        

    /**
     * If user attempts to modify locked attributes, gives message.
     */
    private void setLockedMessage (List lockedAttrs, Context context)
        throws Exception
    {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<lockedAttrs.size(); i++)
        {
            Attribute attr = (Attribute)lockedAttrs.get(i);
            buf.append(attr.getName());
            if (i == lockedAttrs.size()-1)
            {
                buf.append(".");
            }
            else
            {
                buf.append(",");
            }
        }
        getScarabRequestTool(context).setAlertMessage(getLocalizationTool(context).format("LockedAttributes", buf.toString()));
    }
}
