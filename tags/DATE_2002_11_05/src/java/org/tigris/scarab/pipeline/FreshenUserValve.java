package org.tigris.scarab.pipeline;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.TurbineException;
import org.apache.turbine.Valve;
import org.apache.turbine.pipeline.AbstractValve;
import org.apache.turbine.ValveContext;
import org.apache.torque.om.NumberKey;
import org.apache.torque.TorqueException;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;

/**
 * This valve clears any stale data out of the user due to aborted wizards.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class FreshenUserValve 
    extends AbstractValve
{
    private static final Map xmitScreens = new HashMap();

    static
    {
        xmitScreens.put("home,XModuleList.vm", null);
        xmitScreens.put("AdvancedQuery.vm", null);
        xmitScreens.put("IssueList.vm", null);
        xmitScreens.put("ViewIssue.vm", null);
        xmitScreens.put("QueryList.vm", null);
        xmitScreens.put("SaveQuery.vm", null);
        xmitScreens.put("EditQuery.vm", null);
        xmitScreens.put("ConfigureIssueList.vm", null);
        xmitScreens.put("EditXModuleList.vm", null);
        //xmitScreens.put(, null);
    }


    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke( RunData data, ValveContext context )
        throws IOException, TurbineException
    {
        ScarabUser user = (ScarabUser)data.getUser();
        setCurrentModule(user, data);
        setCurrentIssueType(user, data);

        // set the thread key 
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.THREAD_QUERY_KEY);
        if (key != null) 
        {
            user.setThreadKey(new Integer(key));
        }
        else
        {
            user.setThreadKey(null);
        }
        
        // remove any report that was aborted
        String reportKey = parameters.getString(ScarabConstants.REMOVE_CURRENT_REPORT);
        if (reportKey != null && reportKey.length() > 0)
        {
            user.setCurrentReport(reportKey, null);
        }

        // remove the current module/issuetype list, if needed
        String removeMitKey = 
            parameters.getString(ScarabConstants.REMOVE_CURRENT_MITLIST_QKEY);
        if (removeMitKey != null 
            || !xmitScreens.containsKey(data.getTarget()) )
        {
            user.setCurrentMITList(null);
        }

        // override the current module/issuetype list if one is given
        // in the url.
        String mitid = parameters.getString(ScarabConstants.CURRENT_MITLIST_ID);
        if (mitid != null) 
        {
            MITList mitList = null;
            try
            {
                mitList = MITListManager.getInstance(new NumberKey(mitid));
                user.setCurrentMITList(mitList);
                mitList.setScarabUser(user);
            }
            catch (TorqueException e)
            {
                throw new TurbineException(e);
            }
        }

        // should add the currently reporting issue here as well

        // Pass control to the next Valve in the Pipeline
        context.invokeNext( data );
    }

    private void setCurrentModule(ScarabUser user, RunData data)
        throws TurbineException
    {
        Module module = null;
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.CURRENT_MODULE);
        if (key != null) 
        {
            try
            {
                module = ModuleManager.getInstance(new NumberKey(key));
            }
            catch (TorqueException e)
            {
                throw new TurbineException(e);
            }
        }
        else if (parameters.getString("id") != null) 
        {
            try  
            {
                module = Issue.getIssueById(parameters.getString("id")).getModule();
                parameters.setString(ScarabConstants.CURRENT_MODULE, 
                             module.getQueryKey());
            }
            catch (Exception e)
            {
                // ignore
                Log.get().debug("'id' parameter was available, "
                    + parameters.getString("id") + 
                    ", but did not contain enough info to create issue.");
            }
        }
        user.setCurrentModule(module);
    }

    private void setCurrentIssueType(ScarabUser user, RunData data)
        throws TurbineException
    {
        IssueType issueType = null;
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.CURRENT_ISSUE_TYPE);
        if (key != null) 
        {
            try
            {
                issueType = IssueTypeManager.getInstance(new NumberKey(key));
            }
            catch (TorqueException e)
            {
                throw new TurbineException(e);
            }
        }
        else if (parameters.getString("id") != null) 
        {
            try  
            {
                issueType = 
                    Issue.getIssueById(parameters.getString("id")).getIssueType();
                parameters.setString(ScarabConstants.CURRENT_ISSUE_TYPE, 
                             issueType.getQueryKey());
            }
            catch (Exception e)
            {
                // ignore
                Log.get().debug("'id' parameter was available, " 
                    + parameters.getString("id") + 
                    ", but did not contain enough info to create issue.");
            }
        }
        user.setCurrentIssueType(issueType);
    }
    
}
