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

import java.util.List;

import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RIssueTypeAttribute;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.workflow.WorkflowFactory;

/**
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class ModuleAttributeEdit extends RequireLoginFirstAction
{
    /**
     * Changes the properties of existing AttributeOptions.
     */
    public synchronized void doSave (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();

        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        Attribute attribute = scarabR.getAttribute();
        RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
        if (ria != null && ria.getLocked())
        {
            scarabR.setAlertMessage(l10n.format("LockedAttribute", attribute.getName()));
            return;
        }

        
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            List rmos = module.getRModuleOptions(attribute, issueType, false);
            // Check for duplicate sequence numbers
            if (areThereDupeSequences(rmos, intake, "RModuleOption", "Order", 0))

            {
                scarabR.setAlertMessage(l10n.format("DuplicateSequenceNumbersFound",
                         l10n.get("AttributeOptions").toLowerCase()));
                return;
            }
            if (rmos != null)
            {
                for (int i=rmos.size()-1; i>=0; i--) 
                {
                    RModuleOption rmo = (RModuleOption)rmos.get(i);
                    Group rmoGroup = intake.get("RModuleOption",
                                     rmo.getQueryKey(), false);
                    // if option gets set to inactive, delete dependencies
                    if (rmoGroup != null)
                    {
                        String newActive = rmoGroup.get("Active").toString();
                        String oldActive = String.valueOf(rmo.getActive());
                        if (newActive.equals("false") && oldActive.equals("true"))
                        {
                            WorkflowFactory.getInstance().deleteWorkflowsForOption(
                                                          rmo.getAttributeOption(),
                                                          module, issueType);
                        }
                        rmoGroup.setProperties(rmo);
                        rmo.save();
                    }
                    ScarabCache.clear();
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                }
            }
        }
        if (attribute.isOptionAttribute())
        {
            List options = module.getRModuleOptions(attribute, issueType, true);
            if (options == null || options.isEmpty())
            {
                RModuleAttribute rma = module.getRModuleAttribute(attribute, issueType);
                if (rma.getRequired())
                {
                    rma.setRequired(false);        
                    rma.save();
                    scarabR.setAlertMessage(l10n.get("DeletedOptionsFromRequiredAttribute"));
                }
            }
        }
    }

    /**
     * Unmaps attribute options to modules.
     */
    public void doDeleteattributeoptions(RunData data,
                                          TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        Attribute attribute = scarabR.getAttribute();
        RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
        if (ria != null && ria.getLocked())
        {
            scarabR.setAlertMessage(l10n.format("LockedAttribute", attribute.getName()));
            return;
        }

        Module module = scarabR.getCurrentModule();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String optionId;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
               optionId = key.substring(7);
               AttributeOption option = AttributeOptionManager
                  .getInstance(new Integer(optionId));

               RModuleOption rmo = module.getRModuleOption(option, issueType);
               rmo.delete();

               // Remove option - module mapping from template type
               RModuleOption rmo2 = module.getRModuleOption(option, 
                   scarabR.getIssueType(issueType.getTemplateId().toString()));
               rmo2.delete();
               scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
               ScarabCache.clear();
               getIntakeTool(context).removeAll();
               data.getParameters().add("att_0id", option.getAttribute().getAttributeId().toString());
            }
        }        
        if (attribute.isOptionAttribute())
        {
            List options = module.getRModuleOptions(attribute, issueType, true);
            if (options == null || options.isEmpty())
            {
                RModuleAttribute rma = module.getRModuleAttribute(attribute, issueType);
                if (rma.getRequired())
                {
                    rma.setRequired(false);        
                    rma.save();
                    scarabR.setAlertMessage(l10n.get("DeletedOptionsFromRequiredAttribute"));
                }
            }
        }
    }

    /**
     * Selects option to add to attribute.
     */
    public void doSelectattributeoption(RunData data, 
                                         TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        Attribute attribute = scarabR.getAttribute();
        RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
        if (ria != null && ria.getLocked())
        {
            scarabR.setAlertMessage(l10n.format("LockedAttribute", attribute.getName()));
            return;
        }
        Module module = scarabR.getCurrentModule();

        String[] optionIds = data.getParameters().getStrings("option_ids");
 
        if (optionIds == null || optionIds.length <= 0)
        { 
            scarabR.setAlertMessage(l10n.get("SelectOption"));
            return;
        }
        else
        {        
            for (int i=0; i < optionIds.length; i++)
            {
                AttributeOption option = null;
                try
                {
                    option = scarabR.getAttributeOption(new Integer(optionIds[i]));
                    module.addAttributeOption(issueType, option);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            ScarabCache.clear();
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
            doCancel(data, context);
        }
    }
    /**
     * Manages clicking of the Done button
     */
    public void doDone( RunData data, TemplateContext context )
        throws Exception
    {
        doSave(data, context);
        if (getScarabRequestTool(context).getAlertMessage() == null)
        {
            doCancel( data, context);
        }
    }
}
