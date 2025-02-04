package org.tigris.scarab.services.user;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;

import org.apache.torque.om.ObjectKey;
import java.util.List;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.AbstractOMService;

/**
 * This class contains common functionality of a UserService.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public abstract class AbstractUserService 
    extends AbstractOMService 
    implements UserService
{
    /**
     * This method should be called in the constructor of concrete
     * implementations
     */
    protected void doInit()
        throws InitializationException
    {
        setRegion("org_tigris_scarab_om_ScarabUser");
    }


    /**
     * Get a fresh instance of a User
     */
    public ScarabUser getInstance()
        throws Exception
    {
        return (ScarabUser) getOMInstance();
    }

    /**
     * Get a user with the given id.
     */
    public ScarabUser getInstance(ObjectKey userId)
        throws Exception
    {
        return (ScarabUser) getOMInstance(userId);
    }

    /**
     * Gets a list of ScarabUsers based on id's.
     *
     * @param userIds a <code>NumberKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getUsers(ObjectKey[] userIds) 
        throws Exception
    {
        return getOMs(userIds);
    }
}
