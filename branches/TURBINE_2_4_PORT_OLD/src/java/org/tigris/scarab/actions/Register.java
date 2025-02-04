package org.tigris.scarab.actions;

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

// Turbine Stuff 
import java.util.Locale;

import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.template.DefaultTemplateContext;
import org.apache.fulcrum.template.TemplateContext;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.template.TemplateInfo;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.screens.TemplateScreen;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.intake.IntakeTool;

import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.security.TurbineSecurityException;
import org.apache.velocity.context.Context;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabRuntimeException;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

// FIXME: remove the methods that reference this
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.TurbineUserPeer;

import org.xbill.DNS.Record;
import org.xbill.DNS.dns;
import org.xbill.DNS.Type;

/**
 * This class is responsible for dealing with the Register
 * Action.
 *   
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class Register extends ScarabTemplateAction
{
    /**
     * calls doRegisterConfirm()
     */
    public void doPerform(PipelineData data) 
        throws Exception
    {
        doConfirmregistration(getRunData(data), TurbineVelocity.getContext(data));
    }

    private boolean checkRFC2505(String email)
    {
        // try just the end portion of the domain
        String domain = parseDomain(email);
        if (domain != null)
        {
            // try to find any A records for the domain
            Record[] records = dns.getRecords(domain, Type.A);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try just the domain after the @
            // this is for domains like foo.co.uk
            String fullDomain = email.substring(email.indexOf('@')+1);
            records = dns.getRecords(fullDomain, Type.A);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try to find any MX records for the domain
            records = dns.getRecords(domain, Type.MX);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try to find any MX records for the fullDomain
            records = dns.getRecords(fullDomain, Type.MX);
            if (records != null || records.length > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This manages clicking the "Register" button in the Register.vm
     * template. As a result, the user will go to the 
     * RegisterConfirm.vm screen.
     */
    public void doRegister(RunData data, Context context) 
        throws Exception
    {
        String template     = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            Object user = data
                            .getUser()
                            .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register", 
                    ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                    IntakeTool.DEFAULT_KEY, false);
            }

            // not quite sure why this happens, but it does, so case
            // for it and deal with it.
            if (register == null)
            {
                TemplateScreen.setTemplate(data,"Register.vm");
                scarabR.setAlertMessage(L10NKeySet.RegisterSessionError);
                return;
            }

            String password = register.get("Password").toString();
            String passwordConfirm = register.get("PasswordConfirm").toString();

            // check to make sure the passwords match
            if (!password.equals(passwordConfirm))
            {
                TemplateScreen.setTemplate(data, template);
                scarabR.setAlertMessage(L10NKeySet.PasswordsDoNotMatch);
                return;
            }

            // get an anonymous user
            ScarabUser su = (ScarabUser) (ScarabUser) AnonymousUserUtil.getAnonymousUser();
            try
            {
                register.setPropertiesNoOverwrite(su);
            }
            catch (Exception e)
            {
                TemplateScreen.setTemplate(data, template);
                Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                scarabR.setAlertMessage(msg);
                return;
            }

            String email = su.getEmail();
            if (email == null)
            {
                TemplateScreen.setTemplate(data,"Register.vm");
                scarabR.setAlertMessage(L10NKeySet.EnterValidEmailAddress);
                return;
            }

            // check to see if the email is a valid domain (has A records)
            if (Turbine.getConfiguration()
                    .getBoolean("scarab.register.email.checkRFC2505", false))
            {
                if (!checkRFC2505(email))
                {
                    TemplateScreen.setTemplate(data, template);
                    Localizable msg = new L10NMessage(L10NKeySet.EmailHasBadDNS,email);
                    scarabR.setAlertMessage(msg);
                    return;
                }
            }
            String[] badEmails = Turbine
                                .getConfiguration()
                                .getStringArray("scarab.register.email.badEmails");
            if (badEmails != null && badEmails.length > 0)
            {
                for (int i=0;i<badEmails.length;i++)
                {
                    if (email.equalsIgnoreCase(badEmails[i]))
                    {
                        TemplateScreen.setTemplate(data, template);
                        Localizable msg = new L10NMessage(L10NKeySet.InvalidEmailAddress,email);
                        scarabR.setAlertMessage(msg);
                        return;
                    }
                }
            }
            
            // check to see if the user already exists
            if (TurbineSecurity.accountExists(su))
            {
                TemplateScreen.setTemplate(data, template);
                scarabR.setAlertMessage(L10NKeySet.UsernameExistsAlready);
                return;
            }

            // put the user object into the context so that it can be
            // used on the nextTemplate
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, su);
            TemplateScreen.setTemplate(data, nextTemplate);
        }
    }

    public void doConfirmregistration(RunData data, Context context)
        throws Exception
    {
        String template = getCurrentTemplate(data);
        String nextTemplate = getNextTemplate(data);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        try
        {
            // pull the user object from the session
            ScarabUser su = (ScarabUser) data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            if (su == null)
            {
                // assign the template to the cancel template, not the 
                // current template
                template = getCancelTemplate(data, "Register.vm");
                throw new ScarabRuntimeException(L10NKeySet.UserObjectNotInSession);
            }

            try
            {
                // attempt to create a new user!
                su.createNewUser();
            }
            catch (org.apache.turbine.util.security.EntityExistsException e)
            {
                Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
                scarabR.setAlertMessage(msg);
                TemplateScreen.setTemplate(data, "Confirm.vm");
                return;
            }

            // grab the ScarabRequestTool object so that we can populate the  
            // User object for redisplay of the form data on the screen
            if (scarabR != null)
            {
                scarabR.setUser(su);
            }
            
            // send an email that is for confirming the registration
            sendConfirmationEmail(su, context);

            // set the next template on success
            TemplateScreen.setTemplate(data, nextTemplate);
        }
        catch (Exception e)
        {
            TemplateScreen.setTemplate(data, template);
            Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
            scarabR.setAlertMessage(msg);
            Log.get().error(e);
            return;
        }
    }

    /**
     * returns you to Register.vm
     */
    public void doBack(RunData data, Context context) 
        throws Exception
    {
        // set the template to the template that we should be going back to
        TemplateScreen.setTemplate(data, data.getParameters().getString(
                ScarabConstants.CANCEL_TEMPLATE, "Register.vm"));
    }

    /**
     * This manages clicking the Confirm button in the Confirm.vm
     * template. As a result, this will end up sending
     * the user to the Confirm screen.
     */
    public void doConfirm(RunData data, Context context) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            Object user = data
                            .getUser()
                            .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register", 
                    ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                    IntakeTool.DEFAULT_KEY, false);
            }

            if (register == null)
            {
                // This is often triggered by self-host issue SCB825.
                scarabR.setAlertMessage(L10NKeySet.RegisterGroupIsNullError);
                String msg = "Register group is null: user="
                    + (user != null && user instanceof ScarabUser ?
                       ((ScarabUser) user).getQueryKey() : "[none]")
                    + " IntakeTool.DEFAULT_KEY=" + IntakeTool.DEFAULT_KEY;
                Log.get().warn(msg);
                return;
            }
            String username = null;
            String confirm = null;
            Field usernameField = register.get("UserName");
            Field confirmField = register.get("Confirm");
            if (usernameField == null)
            {
                scarabR.setAlertMessage(L10NKeySet.UsernameGroupIsNullError);
                return;
            }
            else if (confirmField == null)
            {
                scarabR.setAlertMessage(L10NKeySet.ConfirmFieldIsNullError);
                return;
            }
            username = usernameField.toString();
            confirm = confirmField.toString();

            // This reference to ScarabUserImpl is ok because this action
            // is specific to use with that implementation.
            if (ScarabUserImpl.checkConfirmationCode(username, confirm))
            {
                // update the database to confirm the user
                if(ScarabUserImpl.confirmUser(username))
                {
                    // NO PROBLEMS! :-)
                    ScarabUser confirmedUser = (ScarabUser)
                                    TurbineSecurity.getUser(username);
                    // we set this to false and make people login again
                    // because of this issue:
                    // http://scarab.tigris.org/issues/show_bug.cgi?id=115
                    // there may be a better way, but given that on the confirm
                    // screen, we aren't asking for a password and checkConfirmationCode
                    // will return true if someone is already confirmed, 
                    // we need to do this for security purposes.
                    confirmedUser.setHasLoggedIn(Boolean.FALSE);
                    data.setUser(confirmedUser);
                    data.save();
    
                    scarabR.setConfirmMessage(L10NKeySet.AccountConfirmedSuccess);
                    TemplateScreen.setTemplate(data, nextTemplate);
                }
                else
                {
                    scarabR.setAlertMessage(L10NKeySet.AccountConfirmedFailure);
                    TemplateScreen.setTemplate(data, template);
                }
            }
            else // we don't have confirmation! :-(
            {
                scarabR.setAlertMessage(L10NKeySet.InvalidConfirmationCode);
                TemplateScreen.setTemplate(data, template);
            }
        }
    }

    /**
     * This manages clicking the "Resend code" button 
     * in the Confirm.vm template.
     */
    public void doResendconfirmationcode(RunData data, Context context)
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        try
        {
            Object user = data
                            .getUser()
                            .getTemp(ScarabConstants.SESSION_REGISTER);
 
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            IntakeTool intake = getIntakeTool(context);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register", 
                    ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                    IntakeTool.DEFAULT_KEY, false);
            }
        
            if (register == null)
            {
                scarabR.setAlertMessage(L10NKeySet.RegisterGroupIsNullError);
                return;
            }
            String username = register.get("UserName").toString();
            try
            {
                // Authenticate the user and get the object.
                user = TurbineSecurity.getUser(username);

                // grab the ScarabRequestTool object so that we can 
                // populate the User object for redisplay of the form 
                // data on the screen
                if (scarabR != null)
                {
                    scarabR.setUser((ScarabUser) user);
                }
            }
            catch (TurbineSecurityException e)
            {
                scarabR.setAlertMessage(L10NKeySet.InvalidUsername);
                Log.get().error ("RegisterConfirm: ", e);
                return;
            }
        
            // send an email that is for confirming the registration
            sendConfirmationEmail((ScarabUser) user, context);
            scarabR.setConfirmMessage(L10NKeySet.ConfirmationCodeSent);

            // set the next template on success
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, user);
            intake.remove(register);

            TemplateScreen.setTemplate(data, "Confirm.vm");
        }
        catch (Exception e)
        {
            TemplateScreen.setTemplate(data, template);
            Localizable msg = new L10NMessage(L10NKeySet.ExceptionGeneric,e);
            scarabR.setAlertMessage(msg);
            Log.get().error(e);
            return;
        }
    }

    /**
     * For an email address like <code>jon@foo.bar.com</code>, parse
     * and return the TLD <code>bar.com</code>.
     */
    String parseDomain(String email)
    {
        String result = null;
        char[] emailChars = email.toCharArray();
        int dotCount = 0;
        for (int i = emailChars.length - 1; i >= 0; i--)
        {
            if (emailChars[i] == '.')
            {
                dotCount++;
            }
            if (dotCount == 2 || emailChars[i] == '@')
            {
                result = new String(emailChars, i + 1,
                                    emailChars.length - (i + 1));
                break;
            }
        }
        return result;
    }
    
    /**
     * Send the confirmation code to the given user.
     */
    private void sendConfirmationEmail(ScarabUser su, Context context)
        throws Exception
    {
        Email te = new Email();

        // Retrieve the charset to be used for the Email.
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Locale locale = l10n.getPrimaryLocale();
        String charset = Email.getCharset(locale);
        te.setCharset(charset);
        
        te.setContext(createTemplateContext(context));
        te.setTo(su.getFirstName() + " " + su.getLastName(), su.getEmail());
        te.setFrom(
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromName",
                           "Scarab System"), 
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromAddress",
                           "register@localhost"));
        te.setSubject(L10NKeySet.ConfirmationSubject.getMessage(l10n));
        te.setTemplate(
            Turbine.getConfiguration()
                .getString("scarab.email.register.template",
                           "email/Confirmation.vm"));
        te.send();        
    }
}
