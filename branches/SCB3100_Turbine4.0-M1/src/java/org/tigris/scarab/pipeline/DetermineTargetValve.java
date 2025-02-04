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

import java.io.IOException;
import java.util.Enumeration;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.torque.TorqueException;
import org.apache.turbine.util.RunData;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.modules.Module;
import org.apache.turbine.pipeline.AbstractValve;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This valve determines the target template.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class DetermineTargetValve 
    extends AbstractValve
{
    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(PipelineData pipelineData, ValveContext context)
        throws IOException, TurbineException
    {
    	RunData data = getRunData(pipelineData);
        ParameterParser parameters = data.getParameters();
        if (! data.hasTarget())
        {
            String target = parameters.getString("template");
            String query  = parameters.getString("query");
            if(query==null)
            {
                query = parameters.getString("queryId");
            }

            if (query != null && getEventKey(parameters)==null)
            {
                parameters.setString("eventSubmit_doSelectquery", "foo");
                // Allows short link to public/personal queries
                // $scarabRoot/issues/query/<queryId>/curmodule/<moduleId>                
                if (target == null) target="IssueList.vm";
                data.setScreenTemplate(target);
                if (parameters.getString("tqk")      == null) parameters.setString("tqk", ""+0);
                if (parameters.getString("action")   == null) data.setAction("Search");
                if (parameters.getString("go")       == null) parameters.setString("go",query);
            }
            else if (target != null)
            {
                data.setScreenTemplate(target);
                Log.get().debug("Set target from request parameter");
            }
            else if (parameters.getString("id") != null)
            {
                // Allows short link to issue
                // $scarabRoot/issues/id/<issueId>
                data.setScreenTemplate("ViewIssue.vm");
            }
            else
            {
                //data.getResponse().sendError(404);
                //return;
                ScarabUser user = (ScarabUser)data.getUserFromSession();
                if(user!= null && user.getUserId() != null && user.getUserId().longValue() > 0)
                {
                    ScarabRequestTool scarabR = ((ScarabRequestTool)Module.getTemplateContext(data)
                            .get(ScarabConstants.SCARAB_REQUEST_TOOL));
                    org.tigris.scarab.om.Module module = scarabR.getCurrentModule();
                    
                    try
                    {
                        if(module != null)
                        {
                            target = user.getHomePage(module);
                        }
                        else
                        {
                            target = user.getHomePage();
                        }
                    }
                    catch (TorqueException e)
                    {
                        target = null;
                    }
                }
                if(target == null)
                {
                    target = Turbine.getConfiguration().getString(
                            Turbine.TEMPLATE_HOMEPAGE);                    
                }
                data.setScreenTemplate(target);
                Log.get().debug("Set target to ["+target+"]");

            }
        }
        
        if (Log.get().isDebugEnabled())
        {
            Log.get().debug("Target is now: " + data.getScreenTemplate());
        }

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(pipelineData);
    }

    private Object getEventKey(ParameterParser parameters) 
    {
        Enumeration en = parameters.keys();
        while(en.hasMoreElements())
        {
            Object key = en.nextElement();
            if(key.toString().startsWith("event"))
            {
                return key;
            }
        }
        return null;
    }
}
