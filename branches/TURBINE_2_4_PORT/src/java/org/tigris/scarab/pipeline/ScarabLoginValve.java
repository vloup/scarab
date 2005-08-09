package org.tigris.scarab.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.pipeline.AbstractValve;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;

/**
 * Handles the Login and Logout actions in the request process
 * cycle. It is basically a copy of the Turbine 2.4 DefaultLoginValve
 * without the code that cleans the context. This ensures that
 * changes to any of the tools by the login/logout actions are
 * preserved for the page creation. If we used the default login
 * valve, we would not be able to set any messages on the request
 * tool in the login or logout actions (because the tool would
 * cleared out and recreated).
 * 
 * @author pledbrook
 * @version $Id: DefaultLoginValve.java,v 1.4 2004/12/06 17:47:11 painter Exp $
 */
public class ScarabLoginValve extends AbstractValve
{
    /**
     * Here we can setup objects that are thread safe and can be
     * reused. We setup the session validator and the access
     * controller.
     */
    public ScarabLoginValve() throws Exception
    {
    }

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(PipelineData pipelineData, ValveContext context)
        throws IOException, TurbineException
    {
        try
        {             
            process(pipelineData);
        }
        catch (Exception e)
        {
            throw new TurbineException(e);
        }

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(pipelineData);
    }

    /**
     * Handles user sessions, parsing of the action from the query
     * string, and access control.
     *
     * @param data The run-time data.
     */
    protected void process(PipelineData pipelineData)
        throws Exception
    {
        RunData data = (RunData)getRunData(pipelineData);
        // Special case for login and logout, this must happen before the
        // session validator is executed in order either to allow a user to
        // even login, or to ensure that the session validator gets to
        // mandate its page selection policy for non-logged in users
        // after the logout has taken place.
        String actionName = data.getAction();
        if (data.hasAction() &&
            actionName.equalsIgnoreCase
            (Turbine.getConfiguration().getString(TurbineConstants.ACTION_LOGIN_KEY)) ||
            actionName.equalsIgnoreCase
            (Turbine.getConfiguration().getString(TurbineConstants.ACTION_LOGOUT_KEY)))
        {
            // If a User is logging in, we should refresh the
            // session here.  Invalidating session and starting a
            // new session would seem to be a good method, but I
            // (JDM) could not get this to work well (it always
            // required the user to login twice).  Maybe related
            // to JServ?  If we do not clear out the session, it
            // is possible a new User may accidently (if they
            // login incorrectly) continue on with information
            // associated with the previous User.  Currently the
            // only keys stored in the session are "turbine.user"
            // and "turbine.acl".
            if (actionName.equalsIgnoreCase
                (Turbine.getConfiguration().getString(TurbineConstants.ACTION_LOGIN_KEY)))
            {
                Enumeration names = data.getSession().getAttributeNames();
                if (names != null)
                {
                    // copy keys into a new list, so we can clear the session
                    // and not get ConcurrentModificationException
                    List nameList = new ArrayList();
                    while (names.hasMoreElements())
                    {
                        nameList.add(names.nextElement());
                    }

                    HttpSession session = data.getSession();
                    Iterator nameIter = nameList.iterator();
                    while (nameIter.hasNext())
                    {
                        try
                        {
                            session.removeAttribute((String)nameIter.next());
                        }
                        catch (IllegalStateException invalidatedSession)
                        {
                            break;
                        }
                    }
                }
            }

            // Save the existing user's login name.
            ActionLoader.getInstance().exec(pipelineData, data.getAction());
            data.setAction(null);
        }
    }
}