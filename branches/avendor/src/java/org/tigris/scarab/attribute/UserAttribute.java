package org.tigris.scarab.attribute;

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

import org.apache.turbine.util.RunData;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserPeer;
import org.tigris.scarab.om.*;
import java.util.*;
import org.apache.turbine.util.db.Criteria;

/**
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Revision$ $Date$
 */
public class UserAttribute extends AttributeValue
{
    private Hashtable usersById;
    private Vector users;
    private ScarabUser user;

    /**
     * Looks for users using prefix and suffix wildcards on the
     * username.
     */
    public List getMatchingUsers(Module module, String partialUserName)
    {
        // exclude users with Roles Guest or Observer 
        // !FIXME! these roles need to be defined
        Role[] excludeRoles = null;

        List matches = module.getUsers(partialUserName, null, excludeRoles);
        
        return matches;
    }

    

    /** Gets the Value attribute of the Attribute object
     *
     * @return    The Value value
     */
    public String getUserName()
    {
        return getValue();
    }

    public void init() throws Exception
    {
    }

    public void setResources(Object resources)
    {
    }
    
    /** displays the attribute.
     * @return Object to display the property. May be a String containing HTML
     * @param data app data. may be needed to render control
     * differently in different circumstances.
     * Not sure about this though. It may be a better
     * idea to handle this on the UI level.
     */
    public Object loadResources() throws Exception
    {
        return null;
    }


}


