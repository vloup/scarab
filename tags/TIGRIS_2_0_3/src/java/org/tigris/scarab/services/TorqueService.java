package org.tigris.scarab.services;

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

import org.apache.log4j.Category;
import org.apache.turbine.Turbine;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.TurbineServices;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;

/**
 * Turbine does not yet have a way to initialize components directly, and
 * use of fulcrum's DatabaseService causes fulcrum to try to treat all the
 * Managers as services.  So this service is used to initialize Torque.
 * It is not used directly by scarab's application code.  And can be removed
 * along with its Scarab.properties entries once another method of
 * initializing components is available.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class TorqueService
    extends BaseService
{
    /**
     * Initializes the service by setting up Torque.
     */
    public void init()
        throws InitializationException
    {
        try
        {
            Torque.init(Turbine.getConfiguration());
        }
        catch (Exception e)
        {
            throw new InitializationException("Can't initialize Torque!", e);
        }

        // indicate that the service initialized correctly
        setInit(true);
    }

    /**
     * Shuts down the service.
     *
     * This method halts the IDBroker's daemon thread in all of
     * the DatabaseMap's.
     */
    public void shutdown()
    {
        Torque.shutdown();
    }
}
