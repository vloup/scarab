package org.tigris.scarab.screens.base;

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

// Velocity Stuff 
import org.apache.velocity.*; 
import org.apache.velocity.context.*; 
// Turbine Stuff 
import org.apache.turbine.om.security.*;
import org.apache.turbine.modules.*; 
import org.apache.turbine.modules.screens.*;
import org.apache.turbine.services.velocity.*; 
import org.apache.turbine.util.*; 
// Scarab Stuff
import org.tigris.scarab.om.*;
import org.tigris.scarab.screens.base.*;
import org.tigris.scarab.util.*;

/**
    This class is responsible for building the Context up
    for Scarab system.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id$
*/
public abstract class ScarabContextLoginFirst extends RequireLoginFirst
{
    protected int cur_project_id = -1;
    
    /**
        Require people to implement this method
    */
    public abstract void doBuildTemplate( RunData data, Context context ) throws Exception;

    /**
        Ok, this is where we will implement a the cool pull model
    */
    protected void doBuildTemplate( RunData data )
        throws Exception
    {
        cur_project_id = data.getParameters().getInt(ModuleManager.CURRENT_PROJECT, 1);
        Context context = getContext(data);
        context.put ("ModuleManager", new ModuleManager());
        context.put ("link", new ScarabLink(data));
        super.doBuildTemplate(data);
    }
}
