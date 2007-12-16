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

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Group;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

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
     * This manages clicking the "Register" button in the Register.vm
     * template. As a result, the user will go to the 
     * RegisterConfirm.vm screen.
     */
    public void doRegister(RunData data, TemplateContext context) 
        throws Exception
    {
        String template     = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if(!intake.isAllValid())
            return;
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        Group register = intake.get("Register", IntakeTool.DEFAULT_KEY, false);

        String password = register.get("Password").toString();
        String passwordConfirm = register.get("PasswordConfirm").toString();

        if (!password.equals(passwordConfirm))
        {
            setTarget(data, template);
            scarabR.setAlertMessage(L10NKeySet.PasswordsDoNotMatch);
            return;
        }

        ScarabUser su = ScarabUserManager.getInstance();
        register.setProperties(su);
        
        if(!su.hasValidEmailAddress())
        {
            setTarget(data,template);
            scarabR.setAlertMessage(L10NKeySet.EnterValidEmailAddress);
            return;
        }
        
        ScarabUser existingUser=ScarabUserManager.getInstance(su.getUserName());
        if (existingUser!=null)
        {
            setTarget(data, template);
            scarabR.setAlertMessage(L10NKeySet.UsernameExistsAlready);
            return;
        }

        data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, su);
        setTarget(data, nextTemplate);
    }

    public void doConfirmregistration(RunData data, TemplateContext context)
        throws Exception
    {
        String nextTemplate = getNextTemplate(data);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        ScarabUser su = (ScarabUser) data.getUser()
            .getTemp(ScarabConstants.SESSION_REGISTER);

        if (su == null)
        {
            setTarget(data, "Register.vm");
            return;
        }
        
        try
        {
            su.createNewUser();
        }
        catch (org.apache.fulcrum.security.util.EntityExistsException e)
        {
            su = ScarabUserManager.reactivateUserIfDeleted(su);
            if (su == null)
            {
                ScarabUser existingUser = ScarabUserManager.getInstance(su.getUserName());
                if(existingUser.isConfirmed())
                {
                    Localizable msg = new L10NMessage(L10NKeySet.UsernameExistsAlready);
                    scarabR.setAlertMessage(msg);
                    setTarget(data, "Login.vm");
                    return;
                }
                else
                {
                    su = existingUser;
                }
            }
        }
        data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, null);

        sendConfirmationEmail(su, context);

        setTarget(data, nextTemplate);
    }

    /**
     * returns you to Register.vm
     */
    public void doBack(RunData data, TemplateContext context) 
        throws Exception
    {
        setTarget(data, data.getParameters().getString(
                ScarabConstants.CANCEL_TEMPLATE, "Register.vm"));
    }

    public void doPerform(RunData data, TemplateContext context) 
        throws Exception
    {
        doConfirmregistration(data, context);
    }

    /**
     * This manages clicking the Confirm button in the Confirm.vm
     * template. As a result, this will end up sending
     * the user to the Confirm screen.
     */
    public void doConfirm(RunData data, TemplateContext context) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if(!intake.isAllValid())
            return;
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        Group register = intake.get("Register", IntakeTool.DEFAULT_KEY, false);


        String username = register.get("UserName").toString();
        String confirm = register.get("Confirm").toString();

        ScarabUser u = ScarabUserManager.getInstance(username);

        if (u.isConfirmed())
        {
            scarabR.setAlertMessage(L10NKeySet.AccountConfirmedSuccess);
            setTarget(data, nextTemplate);
        }
        else
        {
            if (u.confirm(confirm))
            {
                u.save();
                data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, null);

                scarabR.setConfirmMessage(L10NKeySet.AccountConfirmedSuccess);
                setTarget(data, nextTemplate);
            }
            else
            {
                scarabR.setAlertMessage(L10NKeySet.InvalidConfirmationCode);
                setTarget(data, template);
            }
        }
    }

    /**
     * This manages clicking the "Resend code" button 
     * in the Confirm.vm template.
     */
    public void doResendconfirmationcode(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
 
        IntakeTool intake = getIntakeTool(context);
        Group register = intake.get("Register", IntakeTool.DEFAULT_KEY, false);
   
        String username = register.get("UserName").toString();
        ScarabUser user = ScarabUserManager.getInstance(username);

        if(user==null)
        {
            String template = getCurrentTemplate(data, null);
            scarabR.setAlertMessage(L10NKeySet.InvalidUsername);
            setTarget(data, template);
            return;
        }
    
        if(user.isConfirmed())
        {
            String template = getCurrentTemplate(data, null);
            scarabR.setAlertMessage(L10NKeySet.AccountConfirmedSuccess);
            setTarget(data, template);
            return;
        }

        sendConfirmationEmail(user, context);
        scarabR.setConfirmMessage(L10NKeySet.ConfirmationCodeSent);

        setTarget(data, "Confirm.vm");
    }

    /**
     * Send the confirmation code to the given user.
     */
    private void sendConfirmationEmail(ScarabUser su, TemplateContext context)
        throws Exception
    {
        Email te = new Email();

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Locale locale = l10n.getPrimaryLocale();
        String charset = Email.getCharset(locale);
        te.setCharset(charset);

        te.setContext(new ContextAdapter(context));
        te.addTo(su.getEmail(), su.getFirstName() + " " + su.getLastName());
        te.setFrom(
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromAddress",
                           "register@localhost"),
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromName",
                           "Scarab System"));
        te.setSubject(L10NKeySet.ConfirmationSubject.getMessage(l10n));
        te.setTemplate(
            Turbine.getConfiguration()
                .getString("scarab.email.register.template",
                           "email/Confirmation.vm"));
        te.send();        
    }
}
