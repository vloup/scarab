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

import org.apache.turbine.util.RunData;
import org.apache.turbine.modules.actions.AccessController;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.security.TurbineSecurityException;
import org.apache.turbine.util.security.AccessControlList;

/**
 * The ScarabAccessController class is a replacement for
 * AccessController of Turbine3, which Scarab used before.
 * It's mainly a copy of AccessController of turbine 2.3.
 * In Turbine 3 AccessController.doPerform does always load the ACL from the DB.
 * It tries to load it first from the session, but this never happens,
 * because the statament is missing, which stores the ACL in the session.
 * In the implementation of Turbine 2.3 (and so in this implementation)
 * this works properly and so doPerform does not always access the DB, when called.
 * @version $Id: $
 */
public class ScarabAccessController
    extends AccessController
{

    /**
     * If there is a user and the user is logged in, doPerform will
     * set the RunData ACL.  The list is first sought from the current
     * session, otherwise it is loaded through
     * <code>TurbineSecurity.getACL()</code> and added to the current
     * session.
     *
     * @see org.apache.turbine.services.security.TurbineSecurity
     * @param data Turbine information.
     * @exception TurbineSecurityException problem with the security service.
     */
    public void doPerform( RunData data )
        throws TurbineSecurityException
    {
        User user = data.getUser();

        if (user.hasLoggedIn() && user.isConfirmed())
        {
            AccessControlList acl = (AccessControlList)
                    data.getSession().getAttribute(
                            AccessControlList.SESSION_KEY);

            if (acl == null)
            {
                acl = TurbineSecurity.getACL(user);
                data.getSession().setAttribute(
                        AccessControlList.SESSION_KEY, acl);
            }
            data.setACL(acl);
        }
    }
}
