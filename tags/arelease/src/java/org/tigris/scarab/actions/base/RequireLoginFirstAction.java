package org.tigris.scarab.actions.base;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

// JDK Imports
import java.util.Vector;

// Turbine/Village/ECS Imports
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.velocity.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.om.security.*;
import org.apache.turbine.om.security.peer.*;
import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

// Velocity Stuff
import org.apache.turbine.services.velocity.*;
import org.apache.velocity.*;
import org.apache.velocity.context.*; 

// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;

/**
    All you have to do is extend this screen to require someone
    to log in first. Eventually it will be made smart enough to
    also redirect you to the page you requested after having 
    logged in. That part isn't a priority yet though.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id$    
*/
public abstract class RequireLoginFirstAction extends VelocitySecureAction
{
    /**
        sets the template to Login.vm if the user hasn't logged in yet
    */
    protected boolean isAuthorized( RunData data ) throws Exception
    {
        if (!data.getUser().hasLoggedIn())
        {
            data.getParameters().add (ScarabConstants.NEXT_TEMPLATE,
                data.getTemplateInfo().getScreenTemplate());
            setTemplate(data, "Login.vm");
            return false;
        }
        return true;
    }
    
    /**
        Require people to implement this method
    */
    public abstract void doPerform( RunData data, Context context ) throws Exception;
}