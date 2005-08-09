package org.tigris.scarab.actions.base;

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
 
// Java Stuff

 // Turbine Stuff
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.fulcrum.template.TemplateContext;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.template.TemplateInfo;
import org.apache.turbine.util.velocity.VelocityActionEvent;
import org.apache.turbine.modules.screens.TemplateScreen;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.velocity.context.Context;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;

/**
 *  This is a helper class that extends TemplateAction to add
 *  a couple methods useful for Scarab.
 *   
 *  @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 *  @version $Id$
 */
public abstract class ScarabTemplateAction extends VelocityActionEvent
{
    private static final Logger LOG = Logger.getLogger("org.tigris.scarab");

    protected static final String ERROR_MESSAGE = 
        "MoreInformationWasRequired";
    protected static final String NO_PERMISSION_MESSAGE = 
        "YouDoNotHavePermissionToAction";
    protected static final String DEFAULT_MSG = "YourChangesWereSaved";
    protected static final String EMAIL_ERROR = "CouldNotSendEmail";

    /**
     * Helper method to retrieve the IntakeTool from the Context
     */
    public IntakeTool getIntakeTool(Context context)
    {
        return (IntakeTool) context.get(ScarabConstants.INTAKE_TOOL);
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    public ScarabRequestTool getScarabRequestTool(Context context)
    {
        return (ScarabRequestTool)
            context.get(ScarabConstants.SCARAB_REQUEST_TOOL);
    }

    /**
     * Helper method to retrieve the ScarabLocalizationTool from the Context
     */
    protected final ScarabLocalizationTool getLocalizationTool(Context context)
    {
        return (ScarabLocalizationTool)
            context.get(ScarabConstants.LOCALIZATION_TOOL);
    }

    /**
     * Returns the current template that is being executed, otherwisse
     * it returns null
     */
    public String getCurrentTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.TEMPLATE, null);
    }

    /**
     * Returns the current template that is being executed, otherwisse
     * it returns defaultValue.
     */
    public String getCurrentTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.TEMPLATE, defaultValue);
    }

    /**
     * Returns the nextTemplate to be executed. Otherwise returns null.
     */
    public String getNextTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.NEXT_TEMPLATE, null);
    }

    /**
     * Returns the nextTemplate to be executed. Otherwise returns defaultValue.
     */
    public String getNextTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.NEXT_TEMPLATE, defaultValue);
    }

    /**
     * Returns the last template to be cancelled back to.
     */
    public String getLastTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.LAST_TEMPLATE, null);
    }

    /**
     * Returns the cancelTemplate to be executed. Otherwise returns null.
     */
    public String getCancelTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.CANCEL_TEMPLATE, null);
    }

    /**
     * Returns the cancelTemplate to be executed. 
     * Otherwise returns defaultValue.
     */
    public String getCancelTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.CANCEL_TEMPLATE, 
                              defaultValue);
    }

    /**
     * Returns the backTemplate to be executed. Otherwise returns null.
     */
    public String getBackTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.BACK_TEMPLATE, null);
    }

    /**
     * Returns the backTemplate to be executed. 
     * Otherwise returns defaultValue.
     */
    public String getBackTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.BACK_TEMPLATE, defaultValue);
    }

    /**
     * This is a default "do nothing" implementation of the method so
     * that sub-classes don't have to implement it themselves.
     */
    protected void initialize() throws Exception
    {
    }

    /**
     * This is a default "do nothing" implementation of the method so
     * that sub-classes don't have to implement it themselves.
     */
    public void doPerform(RunData data) throws Exception
    {
    }

    /**
     * Returns the other template that is being executed, otherwise
     * it returns null.
     */
    public String getOtherTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.OTHER_TEMPLATE);
    }

    public void doSave(RunData data, Context context)
        throws Exception
    {
    }

    public void doGonext(RunData data, Context context)
        throws Exception
    {
        TemplateScreen.setTemplate(data, getNextTemplate(data));
    }

    public void doGotoothertemplate(RunData data, 
                                    Context context)
        throws Exception
    {
        data.getParameters().setString(ScarabConstants.CANCEL_TEMPLATE,
                                       getCurrentTemplate(data));
        TemplateScreen.setTemplate(data, getOtherTemplate(data));
    }

    public void doRefresh(RunData data, Context context)
        throws Exception
    {
        TemplateScreen.setTemplate(data, getCurrentTemplate(data));
    }

    public void doReset(RunData data, Context context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        TemplateScreen.setTemplate(data, getCurrentTemplate(data));
    }
        
    public void doCancel(RunData data, Context context)
        throws Exception
    {
        TemplateScreen.setTemplate(data, getCancelTemplate(data));
    }

    public void doDone(RunData data, Context context)
        throws Exception
    {
        doSave(data, context);
        doCancel(data, context);
    }

    protected Logger log()
    {
        return LOG;
    }

    public void doRefreshresultsperpage(RunData data, Context context) 
        throws Exception
    {
        ParameterParser params = data.getParameters();
        int oldResultsPerPage = params.getInt("oldResultsPerPage");
        int newResultsPerPage = params.getInt("resultsPerPage");
        int oldPageNum = params.getInt("pageNum");
        
        //
        // We want to display whichever page contains the first issue
        // on the old page.
        //
        int firstItem = (oldPageNum - 1) * oldResultsPerPage + 1;
        int newPageNum = (firstItem / newResultsPerPage) + 1;
        params.remove("oldResultsPerPage");
        params.remove("pageNum");
        params.add("pageNum", newPageNum);
        TemplateScreen.setTemplate(data, getCurrentTemplate(data));
    }

    /**
     * Creates a read-only adapter to the given Velocity context. If any
     * mutator methods are called on the returned object, an 
     * UnsupportedOperationException is thrown.
     * @param velocityContext
     * @return A TemplateContext that provides access to the given
     * velocity context.
     */
    protected static TemplateContext createTemplateContext(final Context velocityContext)
    {
        return new TemplateContext() {
            public void put(String arg0, Object arg1)
            {
                throw new UnsupportedOperationException("This Context is immutable");
            }
    
            public Object get(String arg0)
            {
                return velocityContext.get(arg0);
            }
    
            public boolean containsKey(Object arg0)
            {
                return velocityContext.containsKey(arg0);
            }
    
            public Object[] getKeys()
            {
                return (Object[]) velocityContext.getKeys().clone();
            }
    
            public Object remove(Object arg0)
            {
                throw new UnsupportedOperationException("This context is immutable");
            }};
    }
}
