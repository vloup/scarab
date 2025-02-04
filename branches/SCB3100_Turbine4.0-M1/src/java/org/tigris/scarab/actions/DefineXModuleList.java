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

import java.util.List;
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.velocity.context.Context;
import org.apache.turbine.util.RunData;

import org.apache.turbine.services.intake.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.TorqueException;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.reports.IncompatibleMITListException;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * This class is responsible for building a list of Module/IssueTypes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id$
 */
public class DefineXModuleList extends RequireLoginFirstAction
{
    public void doGotoquerywithinternallist(RunData data,
                                            Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String listId = data.getParameters().getString("pd_list_id");
        if (listId == null || listId.length()==0)
        {
            scarabR.setAlertMessage(L10NKeySet.NoPredefinedXModuleListSelected);
        }
        else 
        {
            MITList list = null;
            ScarabUser user = (ScarabUser)data.getUser();
            if ("allmits".equals(listId)) 
            {
                list = MITListManager.getAllModulesAllIssueTypesList(user);
            }
            else 
            {
                IssueType issueType = IssueTypeManager
                    .getInstance(new Integer(listId.substring(13)));
                if (issueType.getDeleted()) 
                {

                    scarabR.setAlertMessage(L10NKeySet.GlobalIssueTypesDeleted);
                }
                else 
                {
                    list = MITListManager.getAllModulesSingleIssueTypeList(
                        issueType, user);                    
                }
            }
            user.setCurrentMITList(list);
            // reset selected users map
            scarabR.resetSelectedUsers();
            data.setScreenTemplate(data.getParameters()
                      .getString(ScarabConstants.NEXT_TEMPLATE));
        }
    }        

    public void doFinished(RunData data, Context context)
        throws Exception
    {
        // add any last minute additions
        ScarabUser user = (ScarabUser)data.getUser();
        user.setCurrentMITList(new MITList());
        addSelectedRMITs(data, context);
        MITList currentList = user.getCurrentMITList();
        // reset selected users map
        getScarabRequestTool(context).resetSelectedUsers();

        if (currentList != null && !currentList.isEmpty()) 
        {
            data.setScreenTemplate(user.getQueryTarget());
        }
        else
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(L10NKeySet.ListWithAtLeastOneMITRequired);
        }
    }

    public void doFinishedreportlist(RunData data, Context context)
        throws Exception
    {
        doFinished(data, context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            data.setScreenTemplate("reports,ReportList.vm");
            return;
        }

        MITList mitList = user.getCurrentMITList();
        try 
        {
            report.setMITList(mitList);
            scarabR.setConfirmMessage(DEFAULT_MSG);

            if (!mitList.isSingleModule() && 
                Scope.MODULE__PK.equals(report.getScopeId())) 
            {
                report.setScopeId(Scope.PERSONAL__PK);
                scarabR.setInfoMessage(L10NKeySet.ScopeChangedToPersonal);
            }
            data.setScreenTemplate("reports,Info.vm");
        }
        catch (IncompatibleMITListException e)
        {
            scarabR.setAlertMessage(L10NKeySet.IncompatibleMITListReport);
            data.setScreenTemplate("reports,XModuleList.vm");
        }
    }


    public void doRemoveSavedlist(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String listId = data.getParameters().getString("list_id");
        if (listId == null || listId.length()==0)
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(L10NKeySet.NoSavedXModuleQuerySelected);
        }
        else 
        {
            // TODO: implement
        }        
    }

    public void doRemoveitemsfromlist(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        String[] mitids = data.getParameters().getStrings("mitlistitem");
        if (mitids == null || mitids.length == 0) 
        {
            scarabR.setAlertMessage(L10NKeySet.NoItemsSelectedForRemoval);
        }
        else 
        {
            user.removeItemsFromCurrentMITList(mitids);
            scarabR.setConfirmMessage(
                l10n.format("NumberItemsRemoved", String.valueOf(mitids.length)));
        }
    }

    public void doGotosavelist(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        MITList list = user.getCurrentMITList();
        if (list == null) 
        {
            scarabR.setAlertMessage(L10NKeySet.ApplicationErrorListWasNull);
            Log.get().error("Current list was null in DefineXModuleList.doGotosavelist.");
        }
        else if (list.isAnonymous())
        {
            list.save();
            scarabR.setConfirmMessage(DEFAULT_MSG);
            String queryId = data.getParameters().getString("queryId");
            if (queryId != null && queryId.length() > 0) 
            {
                data.setScreenTemplate("EditQuery.vm");
            }
        }
        else
        {
            list.setName(null);
            data.setScreenTemplate("EditXModuleList.vm");
        }
    }

    public void doStartover(RunData data, Context context)
        throws Exception
    {
        ((ScarabUser)data.getUser()).setCurrentMITList(null);
    }

    public void doSavelist(RunData data, Context context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
       
        if (intake.isAllValid()) 
        {
            ScarabUser user = (ScarabUser)data.getUser();
            MITList list = user.getCurrentMITList();
            Group group = 
                intake.get("MITList", list.getQueryKey(), false);
            group.setProperties(list);
            // check if the name already exists and inactivate the old list
            MITList oldList = MITListManager
                .getInstanceByName(list.getName(), user);
            if (oldList != null) 
            {
                // oldList should not be the same as the new, but checking
                // will not hurt
                if (!list.equals(oldList)) 
                {                                    
                    oldList.setActive(false);
                    oldList.save();
                }
            }
            // save the new list
            list.save();

            // Setting the current list to null
            // So that on IssueTypeList screen can select a saved list
            user.setCurrentMITList(null);
 
            scarabR.setConfirmMessage(DEFAULT_MSG);
            data.setScreenTemplate(data.getParameters()
                      .getString(ScarabConstants.LAST_TEMPLATE));
        }
    }

    public void doAddselectedrmits(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String[] rmitIds = data.getParameters().getStrings("rmitid");
        String listId = data.getParameters().getString("list_id");
        if ( (listId == null || listId.length() == 0)
             && (rmitIds == null || rmitIds.length == 0)
             && !data.getParameters().getBoolean("allit"))
        {
            scarabR.setAlertMessage(L10NKeySet.MustSelectAtLeastOneIssueType);
            return;
        }
        else 
        {
            addSelectedRMITs(data, context);
        }
    }

    private void addToUsersList(ScarabUser user, MITList list)
        throws TorqueException
    {
        MITList currentList = user.getCurrentMITList();
        if (currentList == null) 
        {
            currentList = MITListManager.getInstance();
            user.setCurrentMITList(currentList);
        }
        
            currentList.addAll(list);
    }

    private void addSelectedRMITs(RunData data, Context context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        String listId = data.getParameters().getString("list_id");
        if (listId != null && listId.length() != 0)
        {
            setAndGetCurrentList(listId, data, context);   
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (data.getParameters().getBoolean("allit")) 
        {
            Module module = user.getCurrentModule();
            List issueTypes = module.getIssueTypes(false);
            if (issueTypes == null || issueTypes.isEmpty())
            {
                scarabR.setAlertMessage(L10NKeySet.IssueTypeUnavailable);
                return;
            }
            MITList list = MITListManager.getSingleModuleAllIssueTypesList(
                module, user);
            addToUsersList(user, list.copy());
        }

        String[] rmitIds = data.getParameters().getStrings("rmitid");
        if (rmitIds != null && rmitIds.length != 0) 
        {
            List rmits = new ArrayList(rmitIds.length);
            boolean isIssueTypeAvailable = true;
            for (int i=0; i<rmitIds.length && isIssueTypeAvailable; i++)
            {
                try
                {
                    RModuleIssueType rmit = RModuleIssueTypeManager
                        .getInstance(rmitIds[i]);
                    if (rmit == null || rmit.getIssueType().getDeleted())
                    {
                        isIssueTypeAvailable = false;
                    }
                    else
                    {
                        rmits.add(rmit);
                    }
                }
                catch (Exception e)
                {
                    // would probably be a hack of the form
                    scarabR.setAlertMessage(L10NKeySet.IssueTypeUnavailable);
                    Log.get().debug("", e);
                    return;
                }
            }
            if (isIssueTypeAvailable)
            {
                user.addRMITsToCurrentMITList(rmits);
            }
            else
            {
                scarabR.setAlertMessage(L10NKeySet.IssueTypeUnavailable);
            }
        }
        
        // Another oddity due to ScarabUserImpl not extending
        // AbstractScarabUser
        MITList mitlist = user.getCurrentMITList();
        if (mitlist != null) 
        {
            mitlist.setScarabUser(user);            
        }
    }

    private void setAndGetCurrentList(String listId, RunData data, 
                                      Context context)
        
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        try
        {
            MITList list = MITListManager.getInstance(new Long(listId));
            if (list == null) 
            {
                scarabR.setAlertMessage(L10NKeySet.InvalidId);
                Log.get().warn("An invalid id was entered: "+listId);
            }
            else 
            {
                addToUsersList((ScarabUser)data.getUser(), list.copy());
            }
        }
        catch (Exception e)
        {
            scarabR.setAlertMessage(L10NKeySet.InvalidId);
            Log.get().warn("An invalid id was entered: "+listId);
        }
    }

    public void doToggleothermodules(RunData data, Context context)
        throws Exception
    {
        String flag = data.getParameters()
            .getString("eventSubmit_doToggleothermodules");
        ((ScarabUser)data.getUser()).setShowOtherModulesInIssueTypeList(
            "show".equals(flag));
    }
}