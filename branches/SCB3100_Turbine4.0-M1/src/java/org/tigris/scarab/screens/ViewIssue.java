package org.tigris.scarab.screens;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.turbine.RunData;
import org.apache.velocity.context.Context;

// Scarab Stuff
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.ScarabModule;

/**
 * Handles dynamic title
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class ViewIssue extends Default
{
    private static final String ADDITIONAL_DISPLAY_ATTRIBUTES = "scarab.issue.dependencies.additionalDisplayAttributes";
    /**
     * Checks the validity of the issue before displaying the ViewIssue page, and
     * sets the proper alert messages for the cases of invalid, moved or deleted issues.
     */
    protected void doBuildTemplate(RunData data, Context context) throws Exception
    {
        super.doBuildTemplate(data, context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = null;
        
        String id = data.getParameters().getString("id");
		try{
			issue = getReferredIssue(id == null ? null : id.trim(), (ScarabModule)scarabR.getCurrentModule());
		}
		catch(Exception e){
			//ignore, this case will be handled later: issue == null
		}
        boolean hasViewPermission = false;
        boolean hasDeletePermission = false;
        // Deleted issues will appear to not have existed before
        if (issue == null || issue.getDeleted())
        {
             L10NMessage msg = new L10NMessage(L10NKeySet.IssueIdNotValid, id);
             scarabR.setAlertMessage(msg);
        }
        else
        {
            hasViewPermission = scarabR.hasPermission(ScarabSecurity.ISSUE__VIEW, issue.getModule());
            hasDeletePermission=scarabR.hasPermission(ScarabSecurity.ISSUE__DELETE, issue.getModule());
            context.put("currentIssue", issue);
            context.put("hasViewPermission", hasViewPermission?Boolean.TRUE:Boolean.FALSE);
            context.put("hasDeletePermission", hasDeletePermission?Boolean.TRUE:Boolean.FALSE);
            context.put("additionalDisplayAttributes", getAdditionalDisplayAttributes());
            if (!hasViewPermission)
            {
                L10NMessage msg = new L10NMessage(L10NKeySet.NoPermissionToViewIssue, id);
                scarabR.setAlertMessage(msg);
            }
            else if (issue.getMoved())
            {
                ScarabLink link = (ScarabLink)context.get("link");
                Issue newIssue = scarabR.getIssueIncludingDeleted(issue.getIssueNewId());
                L10NMessage msg = new L10NMessage(L10NKeySet.IssueIsNowLocatedIn,
                        link.getIssueIdLink(newIssue), newIssue.getUniqueId());
                scarabR.setAlertMessage(msg);
            }
        }
    }
    
    /**
     * Returns issue according to given issue id,
     * which can be a simple number or an id including module's prefix.
     * @param id : id of issue
     * @param module : module for issue
     * @return : Issue. Value is null if no issue was found.
     */
    private Issue getReferredIssue(String id, ScarabModule module)
    {
    	Issue issue = null;
    	
    	//get simple number from id
    	StringBuffer idCount = new StringBuffer();
    	for (int i = 0; i < id.length(); i++)
        {
            char c = id.charAt(i);
            if (c >= '0' && c <= '9')
            {
            	idCount.append(c);
            }
        }
    	
        if(!idCount.toString().equals(id)){
        	issue = IssueManager.getIssueById(id);
        }
        else if(module != null && idCount.toString().equals(id)){
        	//try if id is number only
            issue = IssueManager.getIssueById(id, module.getCode());
        }
        else{
        	//business of hope
        	issue = IssueManager.getIssueById(id);
        }

        return issue;
    }

    protected String getTitle(ScarabRequestTool scarabR,
                              ScarabLocalizationTool l10n,
                              RunData data, Context context)
        throws Exception
    {
        String title = (new L10NMessage(L10NKeySet.ViewIssue).getMessage(l10n));
        String currentIssueId = data.getParameters().getString("id");
        Issue issue = null;
        if (currentIssueId != null)
        {
            issue = scarabR.getIssue(currentIssueId);
        }
        if (issue != null) 
        {
            String name = issue.getModule().getRModuleIssueType(issue.getIssueType()).getDisplayName();
            String id = (new L10NMessage(L10NKeySet.ID).getMessage(l10n));
            String unique = issue.getUniqueId();
            title = name + " " + id + ": " + unique;
        }            
        return title;
    }
    /**
     * returns a list of all attributes which should be displayed 
     * in the dependency tab.
     * @return
     */
    private List getAdditionalDisplayAttributes()
        throws Exception
    {
        List displayAttributeNames = GlobalParameterManager.getStringList(
            ADDITIONAL_DISPLAY_ATTRIBUTES
        );

        List displayAttributes = new ArrayList();
        for(Iterator names=displayAttributeNames.iterator();names.hasNext();)
        {
            final String displayAttributeName = (String)names.next();
            
            for(Iterator attributes = AttributePeer.getAttributes().iterator();attributes.hasNext();)
            {
                Attribute attribute = (Attribute)attributes.next();
                if(displayAttributeName.equals(attribute.getName())){
                    displayAttributes.add(attribute);
                }
            }
        }
        return displayAttributes;
    }
}
