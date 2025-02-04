package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import org.apache.turbine.Turbine;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renders templates in the templates/snippets directory.  Its primary
 * use is within actions, if the messaging is more complex than a simple
 * string.  One cannot use the renderer that might be in the context as
 * that renderer could output directly to the response, while the action
 * message should not be sent until a later time.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 */
public class SnippetRenderer
    implements SkipFiltering
{
    private static final Log LOG = LogFactory.getLog(SnippetRenderer.class);
    private static final String SNIPPETS = "snippets";
    /**
     * RunData of the request this SnippetRenderer is for.
     */
    private RunData data;
    private String template;

    /**
     * Construct a renderer for the given RunData.
     */
    public SnippetRenderer(RunData data, String template)
    {
        this.data = data;
        this.template = template;
    }

    /**
     * Render the given template. TemplateContext to use will be extracted from
     * the RunData with which this SnippetRenderer was constructer. 
     *
     * @param template name/path of template in the format expected by the
     *                 appropriate template engine service
     * @return Result of template merge
     */
    public String toString()
    {
        String result = null;
        LOG.debug("Rendering snippet " + template);
        try 
        {
            StringBuffer templatePath = new StringBuffer(
                    SNIPPETS.length() + template.length() + 1);
            templatePath.append(SNIPPETS).append('/');
            templatePath.append(template.replace(',', '/'));
            
            result = TurbineVelocity.handleRequest(
                TurbineVelocity.getContext(data), 
                templatePath.toString());
        }
        catch (Exception e)
        {
            LOG.error("Error rendering " + template + ". ", e);
            result = "ERROR!";
        }
        return result;
    }
}
