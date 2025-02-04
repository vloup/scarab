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

import org.apache.turbine.om.security.User;

import org.apache.velocity.context.Context;
import org.apache.turbine.util.RunData;
import org.apache.turbine.modules.actions.sessionvalidator.TemplateSessionValidator;

import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.LocalizationKey;

/**
 * Sets the home page to the current target
 *  
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class ScarabSessionValidator extends TemplateSessionValidator
{
    protected void processCounter(RunData data)
    {
        int userCounter = Integer.MAX_VALUE;
        User user = data.getUser();
        if (user != null)
        {
            Integer i = (Integer) user.getTemp(COUNTER);
            if (i != null)
            {
                userCounter = i.intValue() - 1;
            }
        }

        Context context = getContext(data);
        ScarabLocalizationTool l10n = 
            (ScarabLocalizationTool) context.get(ScarabConstants.LOCALIZATION_TOOL);

        LocalizationKey l10nKey = null;

        if (null == user)
        {
            Log.get().warn("User object was null in session validator");
            l10nKey = L10NKeySet.LostSessionStateError;
        }
        else if (userCounter == Integer.MAX_VALUE)
        {
            Log.get().debug("Could not determine " + COUNTER + 
                            ". This normally occurs during a session timeout.");
            l10nKey = L10NKeySet.LostSessionStateError;
        }
        else if (data.getParameters().getInt(COUNTER) < userCounter)
        {
            l10nKey = L10NKeySet.ResubmitError;
        }

        if (l10nKey != null) 
        {

            L10NMessage l10nMessage = new L10NMessage(l10nKey);
            String msg = l10nMessage.getMessage(l10n);
            ((ScarabRequestTool)context.get(ScarabConstants.SCARAB_REQUEST_TOOL))
                .setAlertMessage( msg );

            data.setAction("");
            data.setScreenTemplate(data.getParameters()
                .getString(ScarabConstants.CANCEL_TEMPLATE, null));
            
        }
    }
}
