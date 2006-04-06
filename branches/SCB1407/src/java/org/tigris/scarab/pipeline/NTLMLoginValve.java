package org.tigris.scarab.pipeline;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.RunData;
import org.apache.turbine.TurbineException;
import org.apache.turbine.ValveContext;
import org.apache.turbine.pipeline.AbstractValve;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.Log;

import jcifs.Config;
import jcifs.UniAddress;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;

import jcifs.util.Base64;

import jcifs.http.NtlmSsp;
import jcifs.netbios.NbtAddress;
/*
 * This valve will try to automatically login a user from the NTLM domain. It will try:
 * <ul><li>First, try to get an NTLM authentication from the client. This should work fine with most IE,
 * though it might not work (or require configuration) in Firefox and others browsers.</li>
 * <li>If it doesn't get authenticated via NTLM, it will ask for a 'Basic' authentication. This is insecure,
 * because the password will be sent in plaintext (no so different from current form-authz, really).
 * </li></ul>
 * 
 * @author jorgeuriarte
 */
public class NTLMLoginValve extends AbstractValve
{
    private String defaultDomain;
    private String domainController;
    private boolean loadBalance;
    private boolean enableBasic;
    private boolean insecureBasic;
    private String realm;
    
    /* 
     * Invoked by the Turbine's pipeline, as defined in scarab-pipeline.xml
     * @see org.apache.turbine.pipeline.AbstractValve#invoke(org.apache.turbine.RunData, org.apache.turbine.ValveContext)
     */
    public void invoke(RunData data, ValveContext context) throws IOException, TurbineException
    {
        if ((null == data.getUserFromSession() && null == data.getUser())
        		|| ((ScarabUser)data.getUserFromSession()).isUserAnonymous())
        {
            authenticateNtlm(data);
        }
        context.invokeNext(data);       
    }

	public void initialize() throws Exception {
		// TODO: Read from scarab configuration
        Config.setProperty("jcifs.smb.client.soTimeout", "300000");
        Config.setProperty("jcifs.netbios.cachePolicy", "600");
        Config.setProperty("jcifs.http.enableBasic", "true");
        Config.setProperty("jcifs.http.insecureBasic", "true");		// TODO: To be configurable?
        Config.setProperty("jcifs.http.domainController", "IBACC");
        Config.setProperty("jcifs.http.loadBalance", "false");
        Config.setProperty("jcifs.http.basicRealm", "IBACC");

        defaultDomain = Config.getProperty("jcifs.smb.client.domain");
        domainController = Config.getProperty("jcifs.http.domainController");
        if( domainController == null ) {
            domainController = defaultDomain;
            loadBalance = Config.getBoolean( "jcifs.http.loadBalance", false );
        }
        enableBasic = Boolean.valueOf(
                Config.getProperty("jcifs.http.enableBasic")).booleanValue();
        insecureBasic = Boolean.valueOf(
                Config.getProperty("jcifs.http.insecureBasic")).booleanValue();
        realm = Config.getProperty("jcifs.http.basicRealm");
		super.initialize();
	}

	private void authenticateNtlm(RunData data) throws IOException {
        HttpServletRequest request = data.getRequest();
        HttpServletResponse response=data.getResponse();
		UniAddress dc;
		boolean offerBasic = enableBasic
				&& (insecureBasic || request.isSecure());
		String msg = request.getHeader("Authorization");
		if (msg != null
				&& (msg.startsWith("NTLM ") || (offerBasic && msg
						.startsWith("Basic ")))) {
			if (loadBalance) {
				dc = new UniAddress(NbtAddress.getByName(domainController,
						0x1C, null));
			} else {
				dc = UniAddress.getByName(domainController, true);
			}
			NtlmPasswordAuthentication ntlm = null;
			if (msg.startsWith("NTLM ")) {
				byte[] challenge = SmbSession.getChallenge(dc);
				try {
					ntlm = NtlmSsp.authenticate(request, response, challenge);
				} catch (IOException e) {
                    Log.get().error("authenticateNtlm: " + e);
				} catch (ServletException e) {
                    Log.get().error("authenticateNtlm: " + e);
				}
				if (ntlm == null)
					return;
			} else {
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
						: defaultDomain;
				user = (index != -1) ? user.substring(index + 1) : user;
				ntlm = new NtlmPasswordAuthentication(domain, user, password);
			}
			try {
				SmbSession.logon(dc, ntlm);
			} catch (SmbAuthException sae) {
				if (offerBasic)
				{
					response.addHeader("WWW-Authenticate", "Basic realm=\""
							+ realm + "\"");
				}
				else
				{
					response.setHeader("WWW-Authenticate", "NTLM");
				}
				response.setHeader("Connection", "close");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.flushBuffer();
				return;
			}
			
            try
            {
                AnonymousUserUtil.userLogin(data, TurbineSecurity.getUser(ntlm.getUsername()));
                // TODO: What to do once logged in?
                data.setTarget("SelectModule.vm");
            }
            catch (DataBackendException e)
            {
                Log.get().error("authenticateNtlm: " + e);
            }
            catch (UnknownEntityException e)
            {
                Log.get().error("authenticateNtlm: " + e);
            }
		} else {
			HttpSession ssn = request.getSession(false);
			if (ssn == null || ssn.getAttribute("NtlmHttpAuth") == null) {
				response.setHeader("WWW-Authenticate", "NTLM");
				if (offerBasic) {
					// TODO: Allow a maximum number of connection attempts
					// TODO: What to do once the connection is refused? Show login page anyway?
					response.addHeader("WWW-Authenticate", "Basic realm=\""
							+ realm + "\"");
				}
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.flushBuffer();
				return;
			}
		}
	}
  
}
