package org.tigris.scarab.om;

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

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.util.ScarabException;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.UnknownEntityException;

/** 
 * This class manages ScarabUser objects.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class ScarabUserManager
    extends BaseScarabUserManager
{
    /**
     * Creates a new <code>ScarabUserManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ScarabUserManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }    

    /**
     * @return null if there is an UnknownEntityException
     */
    protected ScarabUser getInstanceImpl()
    {
        ScarabUser user = null;
        try
        {
            user = (ScarabUser) TurbineSecurity.getAnonymousUser();
        }
        catch (UnknownEntityException uee)
        {
        }
        return user;
    }

    /**
     * Return an instance of User based on username.  Domain is currently
     * unused.
     */
    public static ScarabUser getInstance(String username, String domainName) 
        throws Exception
    {
        return getManager().getInstanceImpl(username, domainName);
    }

    /**
     * Gets a list of ScarabUsers based on usernames.  Domain is currently
     * unused.
     *
     * @param usernames a <code>String[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public static List getUsers(String[] usernames, String domainName) 
        throws Exception
    {
        return getManager().getUsers(usernames, domainName);
    }

    /**
     * Return an instance of User based on username.  Domain is currently
     * unused.
     */
    protected ScarabUser getInstanceImpl(String username, String domainName) 
        throws Exception
    {
        ScarabUser user = null;
        if ( username != null ) 
        {
            Criteria crit = new Criteria();
            crit.add(ScarabUserImplPeer.USERNAME, username);
            List users = ScarabUserImplPeer.doSelect(crit);
            if ( users.size() == 1 ) 
            {
                user = (ScarabUser)users.get(0);
            }
            else if ( users.size() > 1 ) 
            {
                throw new ScarabException("duplicate usernames exist");
            }
        }
        return user;
    }

    /**
     * Gets a list of ScarabUsers based on usernames.  Domain is currently
     * unused.
     *
     * @param usernames a <code>String[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    protected List getUsersImpl(String[] usernames, String domainName) 
        throws Exception
    {
        List users = null;
        if ( usernames != null && usernames.length > 0 ) 
        {
            Criteria crit = new Criteria();
            crit.addIn(ScarabUserImplPeer.USERNAME, usernames);
            users = ScarabUserImplPeer.doSelect(crit);            
        }
        return users;
    }
}





