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
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.om.Report;
import org.tigris.scarab.om.ReportPeer;
import org.tigris.scarab.om.ReportManager;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
    This class is responsible for report generation forms
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id$
*/
public class GenerateReport 
    extends RequireLoginFirstAction
{
    private static final String NO_PERMISSION_MESSAGE = 
        "NoPermissionToEditReport";

    public void doStep1( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( !intake.isAllValid() ) 
        {
            getScarabRequestTool(context).setAlertMessage(
                l10n.get("InvalidData"));
            setTarget(data, "reports,Step1.vm");            
        }
        else if (report.getType() == 1)
        {
            setTarget(data, "reports,Step3_2a.vm");
        }
        else if (report.getType() == 0)
        {
            setTarget(data, "reports,Step2.vm");
        }
        else 
        {
            getScarabRequestTool(context).setAlertMessage(
                l10n.get("InvalidData"));
            setTarget(data, "reports,Step1.vm");            
        }
    }

    public void doStep2agoto2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            //Group g = intake.get("Report", report.getQueryKey(), false);
            //g.setProperties(report);
            setTarget(data, "reports,Step2b.vm");
        }
        else 
        {
            setTarget(data, "reports,Step2.vm");            
        }
    }

    public void doStep2agoto3( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            //Group g = intake.get("Report", report.getQueryKey(), false);
            //g.setProperties(report);
            setTarget(data, "reports,Step3_1a.vm");
        }
        else 
        {
            setTarget(data, "reports,Step2.vm");            
        }
    }

    public void doStep2baddgroup( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            // add new option group
            List groups = report.getOptionGroups();
            if (groups == null) 
            {
                groups = new ArrayList();
                report.setOptionGroups(groups);
            }
            
            Report.OptionGroup group = report.getNewOptionGroup();
            Group intakeGroup = intake.get("OptionGroup", 
                                           group.getQueryKey(), false);
            if ( intakeGroup != null ) 
            {
                intakeGroup.setProperties(group);
                if ( group.getDisplayValue() != null 
                     && group.getDisplayValue().length() > 0 ) 
                {
                    group.setQueryKey(String.valueOf(groups.size()));
                    groups.add(group);
                }
            }
        }
        setTarget(data, "reports,Step2b.vm");
    }

    public void doStep2bdeletegroup( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List groups = report.getOptionGroups();
            for ( int i=groups.size()-1; i>=0; i-- ) 
            {
                if (((Report.OptionGroup)groups.get(i)).isSelected())
                {
                    groups.remove(i);
                }
            }
        }
        setTarget(data, "reports,Step2b.vm");
    }

    public void doStep2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            
            setTarget(data, "reports,Step3_1a.vm");
            intake.removeAll();
        }
        else 
        {
            setTarget(data, "reports,Step2b.vm");
        }
    }

    public void doStep3_1a( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        Group repGroup = intake.get("Report", report.getQueryKey(), false);
        repGroup.get("Axis1Category").setRequired(true);
        repGroup.get("Axis2Category").setRequired(true);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_1b.vm");
            intake.removeAll();
        }
        else 
        {
            setTarget(data, "reports,Step3_1a.vm"); 
        }
    }

    public void doStep3_1b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        Group repGroup = intake.get("Report", report.getQueryKey(), false);
        repGroup.get("Axis1Keys").setRequired(true);
        repGroup.get("Axis2Keys").setRequired(true);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Report_1.vm");
            intake.removeAll();
        }
        else 
        {
            setTarget(data, "reports,Step3_1b.vm"); 
        }
    }

    public void doStep3_2a( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        Group repGroup = intake.get("Report", report.getQueryKey(), false);
        repGroup.get("Axis1Category").setRequired(true);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_2b.vm");
            intake.removeAll();
        }
        else 
        {
            setTarget(data, "reports,Step3_2a.vm"); 
        }
    }


    public void doStep3_2badddate( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else 
        {
            setTarget(data, "reports,Step3_2b.vm");
        }
    }

    public void doStep3_2bdeletedate( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List dates = report.getReportDates();
            if (dates != null && dates.size() > 0) 
            {
                for ( int i=dates.size()-1; i>=0; i-- ) 
                {
                    if (((Report.ReportDate)dates.get(i)).isSelected())
                    {
                        dates.remove(i);
                    }
                }   
            }            
        }
        setTarget(data, "reports,Step3_2b.vm");
    }


    public void doStep3_2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        Group repGroup = intake.get("Report", report.getQueryKey(), false);
        repGroup.get("Axis1Keys").setRequired(true);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            if (report.getDates() == null || report.getDates().length == 0) 
            {
                Group intakeDate = intake.get("ReportDate", "0", false);
                intakeDate.get("Date")
                    .setMessage("intake_YouMustSupplyAtLeastOneDate");
            }
            else 
            {
                setTarget(data, "reports,Report_1.vm");
                intake.removeAll();
            }            
        }
        else 
        {
            setTarget(data, "reports,Step3_2b.vm"); 
        }
    }

    public void doSavereport( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if ( intake.isAllValid() ) 
        {
            // make sure report has a name
            if ( report.getName() == null || report.getName().length() == 0 ) 
            {
                getScarabRequestTool(context)
                    .setAlertMessage(l10n.get("SavedReportsMustHaveName"));
                setTarget(data, "reports,SaveReport.vm");
            }
            else 
            {
                // make sure name is unique
                Report savedReport = ReportPeer
                    .retrieveByName(report.getName());
                if (savedReport == null 
                    || savedReport.getReportId().equals(report.getReportId()))
                {
                    report.save();
                    getScarabRequestTool(context)
                        .setConfirmMessage(l10n.get("ReportSaved"));
                    setTarget(data, "reports,Report_1.vm");                    
                }
                else 
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("ReportNameNotUnique"));
                    setTarget(data, "reports,SaveReport.vm");
                }
            }
        }
        else 
        {
            getScarabRequestTool(context).setAlertMessage(
                l10n.get("ErrorPreventedSavingReport"));
        }
    }


    /**
        Edits the stored story.
    */
    public void doEditstoredreport( RunData data, TemplateContext context )
         throws Exception
    {
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        populateReport(data, context);
        setTarget(data, "reports,Step1.vm");
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredreport( RunData data, TemplateContext context )
         throws Exception
    {        
        populateReport(data, context);
        setTarget(data, "reports,Report_1.vm");
    }

    public void doDeletereport( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        report.setDeleted(true);
        report.save();
        ScarabRequestTool scarabR = getScarabRequestTool(context); 
        scarabR.setReport(null);
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        setTarget(data, "reports,Step1.vm");
    }

    public void doDeletestoredreport( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        if (user.hasPermission("Item | Delete", 
            getScarabRequestTool(context).getCurrentModule()))
        {
            String[] reportIds = data.getParameters().getStrings("report_id");
            for (int i=0;i<reportIds.length; i++)
            {
               String reportId = reportIds[i];
               if (reportId != null && reportId.length() > 0)
               {
                   Report report = ReportManager
                       .getInstance(new NumberKey(reportId), false);
                    report.setDeleted(true);
                    report.save();
               }
           }
       }
     }

    public void doPrint( RunData data, TemplateContext context )
        throws Exception
    {
        populateReport(data, context);
        setTarget(data, "reports,Report_1.vm");
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        getScarabRequestTool(context)
            .setInfoMessage(l10n.get("UseBrowserPrintReport"));
    }

    private Report populateReport(RunData data, TemplateContext context)
       throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context); 
        Report report = scarabR.getReport();
        report.populate(data.getParameters());
        return report;
    }

    private void setNoPermissionMessage(TemplateContext context)
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
    }
}
