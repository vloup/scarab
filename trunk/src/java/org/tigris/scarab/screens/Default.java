package org.tigris.scarab.screens;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.TemplateSecureScreen;
import org.apache.turbine.Turbine;

// Scarab Stuff
import org.tigris.scarab.pages.ScarabPage;
import org.tigris.scarab.security.ScarabSecurityPull;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.IssueType;

/**
 * This class is responsible for building the Context up
 * for the Default Screen as well as validating Security information
 * for all of the Screens. Please note that the Actions also may depend
 * on the checkAuthorized() method in order to prevent the need for
 * duplication of code.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class Default extends TemplateSecureScreen
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate( RunData data, TemplateContext context )
        throws Exception 
    {
        // Nothing needed here
    }

    /**
     * sets the template to Login.vm if the user hasn't logged in yet
     * or if the user does not have the base permissions.
     */
    protected boolean isAuthorized( RunData data ) throws Exception
    {
        return checkAuthorized(data);
    }

    /**
     * Public static access to the isAuthorized() method so that
     * an Action can use this same method to do authorization.
     */
    public static boolean checkAuthorized(RunData data)
        throws Exception
    {
        String template = ScarabPage.getScreenTemplate(data);
        if (template != null)
        {
            template = template.replace('/','.');

            String perm = Turbine.getConfiguration()
                .getString("scarab.security." + template);

            ScarabSecurityPull security = 
                (ScarabSecurityPull)getTemplateContext(data)
                .get(ScarabConstants.SECURITY_TOOL);
            ScarabRequestTool scarabR = 
                (ScarabRequestTool)getTemplateContext(data)
                .get(ScarabConstants.SCARAB_REQUEST_TOOL);

            ModuleEntity currentModule = scarabR.getCurrentModule();
            IssueType currentIssueType = scarabR.getCurrentIssueType();
            
            if (perm != null)
            {
                if (currentModule == null)
                {
                    data.setMessage("Please select the Module " +
                                    "that you would like to work " +
                                    "in.");
                    setTargetSelectModule(data);
                    return false;
                }
                if (currentIssueType == null)
                {
                    data.setMessage("Please select the Issue Type " +
                                    "that you would like to work " +
                                    "in.");
                    setTargetSelectIssueType(data);
                    return false;
                }
                else if (! data.getUser().hasLoggedIn() &&
                         ! security
                           .hasPermission(perm, currentModule))
                {
                    data.setMessage("Please log in with an account " +
                                    "that has permissions to " +
                                    "access this page.");
                    setTargetLogin(data);
                    return false;
                }
            }
            // does the user at least have a role in the module?
            // we don't check user.hasLoggedIn() here because guest
            // users could have a role in a module.
            else if (currentModule != null && 
                    security.getRoles((ScarabUser)data.getUser(),
                        currentModule).size() == 0)
            {
                
                scarabR.setCurrentModule(null);
                data.getParameters().remove(ScarabConstants.CURRENT_MODULE);
                data.setMessage("Sorry, you do not have permission to " + 
                                "work in the selected module.");
                setTargetSelectModule(data);
                return false;
            }
        }
        return true;
    }

    private static void setTargetSelectModule(RunData data)
    {
        // Note: we need to replace '/' with ',' so that 
        //       the hidden input field will have the right
        //       value for ParameterParser to parse.
        getTemplateContext(data).put( ScarabConstants.NEXT_TEMPLATE,
                                      ScarabPage.getScreenTemplate(data)
                                          .replace('/',',') );

        setTarget(data, Turbine.getConfiguration()
                .getString("scarab.CurrentModuleTemplate", "SelectModule.vm"));        
    }

    private static void setTargetSelectIssueType(RunData data)
    {
        // Note: we need to replace '/' with ',' so that 
        //       the hidden input field will have the right
        //       value for ParameterParser to parse.
        getTemplateContext(data).put( ScarabConstants.NEXT_TEMPLATE,
                                      ScarabPage.getScreenTemplate(data)
                                          .replace('/',',') );

        setTarget(data, Turbine.getConfiguration()
                .getString("scarab.CurrentArtifactTypeTemplate", "SelectModule.vm"));        
    }

    private static void setTargetLogin(RunData data)
    {
        // Note: we need to replace '/' with ',' so that 
        //       the hidden input field will have the right
        //       value for ParameterParser to parse.
        getTemplateContext(data).put( ScarabConstants.NEXT_TEMPLATE,
                                      ScarabPage.getScreenTemplate(data)
                                          .replace('/',',') );
        setTarget(data, "Login.vm");        
    }
}
