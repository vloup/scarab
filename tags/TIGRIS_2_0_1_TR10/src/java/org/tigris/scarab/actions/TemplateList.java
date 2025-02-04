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

import java.util.List;
import java.util.Iterator;

import org.apache.commons.collections.SequencedHashMap;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.torque.om.NumberKey; 

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTemplateInfo;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;


/**
    This class is responsible for report managing enter issue templates.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id$
*/
public class TemplateList extends RequireLoginFirstAction
{

    /**
        Creates new template.
    */
    public void doCreatenew( RunData data, TemplateContext context )
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();

        SequencedHashMap avMap = issue.getModuleAttributeValuesMap();
        AttributeValue aval = null;
        Group group = null;
        
        IssueTemplateInfo info = scarabR.getIssueTemplateInfo();
        Group infoGroup = intake.get("IssueTemplateInfo", info.getQueryKey() );
        Group issueGroup = intake.get("Issue", issue.getQueryKey() );
        issueGroup.setProperties(issue);

        Field name = infoGroup.get("Name");
        name.setRequired(true);

        if (intake.isAllValid() ) 
        {
            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(TransactionTypePeer.CREATE_ISSUE__PK, 
                               user, null);

            Iterator iter = avMap.iterator();
            while (iter.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(iter.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null )
                {
                    aval.startTransaction(transaction);
                    group.setProperties(aval);
                }                
            }

            // get issue type id = the child type of the current issue type
            issue.setTypeId(scarabR.getCurrentIssueType().getTemplateId());
            issue.save();

            // Save template info
            infoGroup.setProperties(info);
            info.setIssueId(issue.getIssueId());
            info.saveAndSendEmail(user, scarabR.getCurrentModule(),
                new ContextAdapter(context));
            data.getParameters().add("templateId", issue.getIssueId().toString());
        } 
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    /**
        Edits template's attribute values.
    */
    public void doEditvalues( RunData data, TemplateContext context )
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();

        SequencedHashMap avMap = issue.getModuleAttributeValuesMap();
        AttributeValue aval = null;
        Group group = null;
        Group issueGroup = intake.get("Issue", issue.getQueryKey() );
        issueGroup.setProperties(issue);

        if (intake.isAllValid() ) 
        {
            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(TransactionTypePeer.CREATE_ISSUE__PK, 
                               user, null);

            Iterator iter = avMap.iterator();
            while (iter.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(iter.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null )
                {
                    NumberKey newOptionId = null;
                    NumberKey oldOptionId = null;
                    String newValue = "";
                    String oldValue = "";
                    if (aval instanceof OptionAttribute) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    
                        if (!newValue.equals(""))
                        {
                            newOptionId = new NumberKey(newValue);
                            AttributeOption newAttributeOption = 
                              AttributeOptionManager
                              .getInstance(new NumberKey(newValue));
                            newValue = newAttributeOption.getName();
                        }
                        if (!oldValue.equals(""))
                        {
                            oldOptionId = aval.getOptionId();
                            AttributeOption oldAttributeOption = 
                              AttributeOptionManager
                              .getInstance(oldOptionId);
                            oldValue = oldAttributeOption.getName();
                        }
                        
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }

                    if (!newValue.equals("") && 
                        (oldValue == null  || !oldValue.equals(newValue)))
                    {
                        aval.startTransaction(transaction);
                        group.setProperties(aval);
                        aval.save();
                    }
                }                
            }
        } 
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }


    /**
        Edits templates's basic information.
    */
    public void doEdittemplateinfo( RunData data, TemplateContext context )
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();

        IssueTemplateInfo info = scarabR.getIssueTemplateInfo();
        Group infoGroup = intake.get("IssueTemplateInfo", info.getQueryKey() );
        Field name = infoGroup.get("Name");
        name.setRequired(true);

        if (intake.isAllValid() ) 
        {
            // Save template info
            infoGroup.setProperties(info);
            info.setIssueId(issue.getIssueId());
            info.saveAndSendEmail(user, scarabR.getCurrentModule(),
                new ContextAdapter(context));
            data.getParameters().add("templateId", issue.getIssueId().toString());
        } 
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    public void doDeletetemplates( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String templateId;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
               templateId = key.substring(7);
               Issue issue = IssueManager
                  .getInstance(new NumberKey(templateId), false);
               try
               {
                   issue.delete(user);
               }
               catch (Exception e)
               {
                   getScarabRequestTool(context).setAlertMessage(
                       ScarabConstants.NO_PERMISSION_MESSAGE);
               }
            }
        } 
    } 

    public void doUsetemplate(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        String template = getOtherTemplate(data);
        setTarget(data, template);
    }
    
    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        doEditvalues(data, context);
        doEdittemplateinfo(data, context);
    }

}
