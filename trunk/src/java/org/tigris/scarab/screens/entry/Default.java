package org.tigris.scarab.screens.entry;

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
import org.apache.turbine.services.template.TurbineTemplate;

// Scarab Stuff
import org.tigris.scarab.security.ScarabSecurityPull;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.pages.ScarabPage;

/**
    This class is responsible for building the Context up
    for the Issue Entry templates.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id$
*/
public class Default extends TemplateSecureScreen
{
    /**
        builds up the context for display of variables on the page.
    */
    public void doBuildTemplate( RunData data, TemplateContext context ) 
        throws Exception 
    {   
    }

    /**
     * sets the template to Login.vm if the user hasn't logged in yet
     */
    protected boolean isAuthorized( RunData data ) throws Exception
    {
        TemplateContext context = getTemplateContext(data);
        ScarabSecurityPull security = (ScarabSecurityPull)context
            .get(ScarabConstants.SECURITY_TOOL);
        ScarabRequestTool scarab = (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
        
        if ( !(scarab.getUser().hasLoggedIn()
               && security.hasPermission(ScarabSecurityPull.EDIT_ISSUE, 
                                         scarab.getUser().getCurrentModule())))
        {
            // Note: we need to replace '/' with ',' so that 
            //       the hidden input field will have the right
            //       value for ParameterParser to parse.
            context.put( ScarabConstants.NEXT_TEMPLATE, 
               ScarabPage.getScreenTemplate(data).replace('/',',') );
            setTarget(data, "Login.vm");
            return false;
        }
        return true;
    }
}
