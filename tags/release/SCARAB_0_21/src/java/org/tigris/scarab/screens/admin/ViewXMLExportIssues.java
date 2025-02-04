package org.tigris.scarab.screens.admin;

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
 
// Jaav stuff
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.velocity.VelocityContext;
import org.apache.fulcrum.velocity.TurbineVelocity;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.tigris.scarab.actions.Search;
import org.tigris.scarab.om.Module;

import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.screens.Default;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.util.word.QueryResult;
import org.tigris.scarab.util.xmlissues.ImportIssues;

/**
 * Sends XML Export issues contents directly to the output stream.
 *
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id$
 */
public class ViewXMLExportIssues extends Default
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);

        // probably should use intake, but i'm being lazy for now cause
        // this is only three form variables and not worth the trouble...
        // [hair] it is actually very useful that data.getParameters() is used
        // because it means that the xml results can be fetched through a Http GET
        // as well as the normal POST.
        String filename = data.getParameters().getString("filename");
        if (filename == null 
            || filename.length() == 0 
            || filename.indexOf('/') > 0
            || filename.indexOf(':') > 0
            || filename.indexOf(';') > 0)
        {
            filename = "scarab-issues-export.xml";
        }

        final ScarabRequestTool scarabR = getScarabRequestTool(context);
        final Module currentModule = scarabR.getCurrentModule();
        final ScarabLocalizationTool l10n = getLocalizationTool(context);
        
        // new functionality allows query parameter to select which issues to print
        final String query = data.getParameters().getString("go");
        String ids = null;
        final List allIdList = new ArrayList();
        if( query != null )
        {
            final StringBuffer sb = new StringBuffer();
            final Search searchAction = new Search();
            searchAction.doSelectquery(data, context);
            final Iterator it = scarabR.getCurrentSearchResults();
            while(it.hasNext())
            {
                final QueryResult issue = (QueryResult)it.next();
                sb.append(issue.getIdPrefix()+issue.getIdCount());
                allIdList.add(issue.getIdPrefix()+issue.getIdCount());
                if(it.hasNext())
                {
                    sb.append(',');
                }
            }
            ids = sb.toString();
        }else{
            // issues are defined manually
            ids = data.getParameters().getString("exportissues");
        }
        context.put("exportissues", ids);
        if (ids == null || ids.length() == 0)
        {
            data.setTarget("admin,XMLExportIssues.vm");
            scarabR.setAlertMessage(l10n.get("EnterIssues"));
            return;
        }
        else
        {
            // manually specified ids we need parse & verify
            if( allIdList.isEmpty() )
            {
                try
                {
                    // FIXME! we need this method to return valid ids, if the range
                    // is thousands of issues, we cannot post verify the issues.
                   allIdList.addAll( Issue.parseIssueList(currentModule, ids) );
                }
                catch (Exception e)
                {
                    data.setTarget("admin,XMLExportIssues.vm");
                    scarabR.setAlertMessage(l10n.getMessage(e));
                    return;
                }
            }
            final List issueIdList = new ArrayList();
            final List badIdList = new ArrayList();
            final Integer currentModuleId = currentModule.getModuleId();
            final String defaultCode = currentModule.getCode();
            for (Iterator itr = allIdList.iterator(); itr.hasNext();)
            {
                final String tmp = (String) itr.next();
                final Issue issue = IssueManager.getIssueById(tmp, defaultCode);
                // check that the issue is in the current module, don't allow
                // exporting of issues other than those in the current
                // module for security reasons
                if (issue != null && !issue.getDeleted() )
                {
                    issueIdList.add(tmp);
                }
                else
                {
                    badIdList.add(tmp);
                }
            }
            if (issueIdList.isEmpty())
            {
                data.setTarget("admin,XMLExportIssues.vm");
                scarabR.setAlertMessage(l10n.get("NoValidIssuesCouldBeLocated"));
                return;
            }
            else if (!badIdList.isEmpty())
            {
                data.setTarget("admin,XMLExportIssues.vm");
                scarabR.setAlertMessage(
                    l10n.format("FollowingIssueIdsAreInvalid", 
                    badIdList.toString()));
                return;
            }

            String contentType;
            String contentDisposition;
            if ("1".equals(data.getParameters().getString("downloadtype")))
            {
                // To browser window.
                contentType = "text/xml";
                contentDisposition = "inline";
            }
            else
            {
                // Save to file.  Unforunately, not all browsers are
                // created equal, and some fail to fully heed the
                // Content-disposition header.  We hack around this by
                // using a Content-type header which indicates binary
                // data.
                //contentType = "text/xml";
                contentType = "application/octet-stream";
                contentDisposition = "attachment";
            }
            data.getResponse().setContentType(contentType);
            data.getParameters().add("content-type", contentType);
            contentDisposition += "; filename=" + filename;
            data.getParameters().add("content-dispostion", contentDisposition);
            data.getResponse().setHeader("Content-Disposition",
                                         contentDisposition);
    
            context.put("issueIdList", issueIdList);
            final VelocityContext vc = new VelocityContext();
            for (Iterator keys = context.keySet().iterator(); keys.hasNext(); )
            {
                final String key = (String) keys.next();
                vc.put(key, context.get(key));
            }
            vc.put("dtdURI", ImportIssues.SYSTEM_DTD_URI);

            final String encoding = Turbine.getConfiguration()
                .getString("scarab.dataexport.encoding");
            if (encoding != null && !encoding.equals(""))
            {
                TurbineVelocity.handleRequest
                    (vc, "macros/XMLExportIssuesMacro.vm",
                     data.getResponse().getOutputStream(), encoding, encoding);
            }
            else
            {
                TurbineVelocity.handleRequest
                    (vc, "macros/XMLExportIssuesMacro.vm",
                     data.getResponse().getOutputStream());
            }
    
            // we already sent the response, there is no target to render
            data.setTarget(null);
        }
    }
}
