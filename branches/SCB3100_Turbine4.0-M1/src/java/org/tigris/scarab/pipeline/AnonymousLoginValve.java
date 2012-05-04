package org.tigris.scarab.pipeline;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.pipeline.AbstractValve;
import org.tigris.scarab.actions.Login;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.Log;

/*
 * This valve will try to automatically login an Anonymous user if there is no user authenticated.
 * The user and password will be set in scarab.user.anonymous and scarab.anonymous.password
 * If scarab.anonymous.userid does not exists, the valve will just pass control to the following
 * through the pipeline.
 * 
 */
public class AnonymousLoginValve extends AbstractValve
{
    private final static Set nonAnonymousTargets = new HashSet();
    private boolean anonymousAccessAllowed = false;
    
    /**
     * Initilizes the templates that will not make an automatical
     * anonymous login.
     */
    public void initialize() throws Exception
    {
        anonymousAccessAllowed = ScarabUserManager.anonymousAccessAllowed();
        if (anonymousAccessAllowed) {
            Log.get().info("anonymous Login enabled.");
	        nonAnonymousTargets.add("Register.vm");
	        nonAnonymousTargets.add("ForgotPassword.vm");
        }
        else
        {
            Log.get().info("anonymous Login disabled.");
        }
    }
    
    /** 
     * Invoked by the Turbine's pipeline, as defined in scarab-pipeline.xml
     * If anonymous access is allowed and current user is null the anomynous user is logged in.
     * @see org.apache.turbine.pipeline.AbstractValve#invoke(org.apache.turbine.RunData, org.apache.turbine.ValveContext)
     */
    public void invoke(RunData data, ValveContext context) throws IOException, TurbineException
    {
        String target = data.getTarget();
        
        // Only try this if accessing an authenticated page:
        if (!nonAnonymousTargets.contains(target) && target.indexOf("help,") == -1)
        {
            ScarabUser user = (ScarabUser)data.getUserFromSession();
            // If there's no user, we will try login:

            if (null == user || user.getUserId() == null || !user.hasLoggedIn())
            {
                boolean isLoggedIn = false;
                String username   = data.getParameters().get("userid");
                // Maybe a User has been provided in the parameter list ?    
                if(username != null)
                {
                    String password = data.getParameters().get("password");
                    final ScarabRequestTool scarabR = null;
                    try
                    {
                        // Try logging in. Note No ScarabRequestTool available here,
                        // so no screen action can be prepared here.
                        isLoggedIn = Login.authentifyWithCredentials(data, scarabR, username, password);
                    }
                    catch(Exception e)
                    {
                        // login failed (user unknown, not confirmed, wrong password, ...) no action taken here
                        // Maybe even anonymous login should be forbidden now ?
                        // See below
                    }
                }

                // We were not able to log in an authenticated user (as possibly tried above) but anonymous login is enabled
                if(!isLoggedIn && anonymousAccessAllowed)
                {
                    // So perform anonymous login here
                    Login.anonymousLogin(data);
                }
                
            }
        }
        context.invokeNext(data);        
    }

}
