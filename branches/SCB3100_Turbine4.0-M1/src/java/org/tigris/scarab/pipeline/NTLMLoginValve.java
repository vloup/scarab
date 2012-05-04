package org.tigris.scarab.pipeline;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.turbine.util.RunData;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineException;
import org.apache.turbine.ValveContext;
import org.apache.turbine.pipeline.AbstractValve;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.actions.Login;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.Log;

import jcifs.Config;
import jcifs.UniAddress;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;

import jcifs.util.Base64;

import jcifs.http.NtlmSsp;

/*
 * This valve will try to automatically login a user in an NTLM domain, using
 * the credentials provided by the browser.
 * 
 * @author jorgeuriarte
 */
public class NTLMLoginValve extends AbstractValve
{
    private boolean bNTLMActive;
    private String domainController;
    private static Logger log = Log.get(NTLMLoginValve.class.getName());    
    
    /* 
     * Invoked by the Turbine's pipeline, as defined in scarab-pipeline.xml
     * Will try to authenticate against a NTLM domain using info provided by the browser.
     * 
     * @see org.apache.turbine.pipeline.AbstractValve#invoke(org.apache.turbine.RunData, org.apache.turbine.ValveContext)
     */
    public void invoke(RunData data, ValveContext context) throws IOException, TurbineException
    {
        try
        {
    	   if (bNTLMActive &&
                   (
                   ((null == data.getUserFromSession() || data.getUserFromSession().getUserName().trim().length()==0) && null == data.getUser())
                   || ((ScarabUser)data.getUserFromSession()).isUserAnonymous()
                   )
                   && ( !data.getAction().equals("Logout")
                           && !data.getAction().equals("Login")
                           && !data.getTarget().equals("Register.vm")
                           && !data.getTarget().equals("ForgotPassword.vm")
                    ))
            {
                authenticateNtlm(data);
            }
        }
        catch(TorqueException e)
        {
        	throw new RuntimeException(e);
        }
    	   context.invokeNext(data);       
    }

    /*
     * This method will initialize the NTLM login system if the required properties are set.
     */
    public void initialize() throws Exception
    {
        bNTLMActive = Turbine.getConfiguration().getBoolean("scarab.login.ntlm.active", false);
        domainController = Turbine.getConfiguration().getString("scarab.login.ntlm.domain", "<check properties>");
        Config.setProperty("jcifs.util.loglevel", log.isDebugEnabled()?"10":"0");        
        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");
        Config.setProperty("jcifs.http.domainController", domainController);
        Config.setProperty("jcifs.smb.client.domain", domainController);
        Config.setProperty("http.auth.ntlm.domain", domainController);        

        if( domainController == null )
        {
            bNTLMActive = false;
            Log.get(this.getClass().getName()).debug("Domain controller must be specified.");
        }
        super.initialize();
    }

    private void authenticateNtlm(RunData data) throws IOException
    {
        HttpServletRequest request = data.getRequest();
        HttpServletResponse response=data.getResponse();
        UniAddress dc;
        String msg = request.getHeader("Authorization");
        if (msg != null && msg.startsWith("NTLM "))
        {
            dc = UniAddress.getByName(domainController, true);
            NtlmPasswordAuthentication ntlm = null;
            if (msg.startsWith("NTLM "))
            {
                byte[] challenge = SmbSession.getChallenge(dc);
                try
                {
                    ntlm = NtlmSsp.authenticate(request, response, challenge);
                }
                catch (IOException e)
                {
                    log.error("authenticateNtlm: " + e);
                }
                catch (ServletException e)
                {
                    log.error("authenticateNtlm: " + e);
                }
                if (ntlm == null)
                    return;
            }
            else
            {
                String auth = new String(Base64.decode(msg.substring(6)),
                        "US-ASCII");
                int index = auth.indexOf(':');
                String user = (index != -1) ? auth.substring(0, index) : auth;
                String password = (index != -1) ? auth.substring(index + 1)
                        : "";
                index = user.indexOf('\\');
                if (index == -1)
                    index = user.indexOf('/');
                String domain = (index != -1) ? user.substring(0, index)
                        : domainController;
                user = (index != -1) ? user.substring(index + 1) : user;
                ntlm = new NtlmPasswordAuthentication(domain, user, password);
            }
            try
            {
                SmbSession.logon(dc, ntlm);
            }
            catch (SmbAuthException sae)
            {
                response.setHeader("WWW-Authenticate", "NTLM");
                response.setHeader("Connection", "close");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.flushBuffer();
                return;
            }
            
            try
            {
                // Once the user has been authenticated, we'll try logging in Scarab.
                String creds[] = {ntlm.getUsername(), domainController};
                ScarabUser user = (ScarabUser)TurbineSecurity.getUser(creds[0]);
                
                if(user != null && user.isConfirmed())
                {
                	Login.simpleLogin(data, user);
                	
                	//Inform the user that s/he's been logged in using NTLM credentials!
                    L10NMessage mesg = new L10NMessage(L10NKeySet.AutomaticallyLoggedIn, creds);
                    ScarabLocalizationTool l10n = new ScarabLocalizationTool();
                    l10n.init(user.getLocale());
                    data.setMessage(mesg.getMessage(l10n));
                }
                data.setTarget("SelectModule.vm");
            }
            catch (DataBackendException e)
            {
                log.error("authenticateNtlm: " + e);
            }
            catch (UnknownEntityException e)
            {
                log.error("authenticateNtlm: " + e);
            }
        }
        else
        {
            HttpSession ssn = request.getSession(false);
            if (ssn == null || ssn.getAttribute("NtlmHttpAuth") == null)
            {
                response.setHeader("WWW-Authenticate", "NTLM");
                // TODO: Allow a maximum number of connection attempts?
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.flushBuffer();
                return;
            }
        }
    }
  
}
