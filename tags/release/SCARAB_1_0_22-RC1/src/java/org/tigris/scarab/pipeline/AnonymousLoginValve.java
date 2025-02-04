package org.tigris.scarab.pipeline;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.turbine.RunData;
import org.apache.turbine.TurbineException;
import org.apache.turbine.ValveContext;
import org.apache.turbine.pipeline.AbstractValve;
import org.tigris.scarab.actions.Login;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
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
        if (anonymousAccessAllowed && !nonAnonymousTargets.contains(target) && target.indexOf("help,") == -1)
        {
	        // If there's no user, we will login as Anonymous.
	        ScarabUser user = (ScarabUser)data.getUserFromSession();
	        if (null == user || user.getUserId() == null || !user.hasLoggedIn())
	        {
	            Login.anonymousLogin(data);
	        }	        
        }
        context.invokeNext(data);        
    }

}
