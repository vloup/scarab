package org.tigris.scarab.tools;

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

import org.apache.turbine.services.pull.ApplicationTool;
import org.tigris.scarab.om.ScarabUser;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.AccessControlList;


/**
 * This scope is an object that is made available as a global
 * object within the system.
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $scarabG
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public interface ScarabGlobalScope extends ApplicationTool
{
    /**
     * holds the Scarab constants. it will be available to the template system
     * as $scarabG.Constant.CONSTANT_NAME.
     */
    public FieldMethodizer getConstant();

    /**
     * holds the Scarab permission constants.  It will be available to 
     * the template system as $scarabG.PERMISSION_NAME.
     */
    public FieldMethodizer getPermission();
        
    /**
     * Gets a List of all of user objects
     * By attribute Type : either user, or non-user.
     */
    public List getUserAttributes()
        throws Exception;

    /**
     * Gets a List of all of user Attribute objects.
     */
    public List getAttributes(String attributeType)
        throws Exception;

    /**
     * gets a list of all Issue Types 
     */
    public List getAllIssueTypes()
        throws Exception;
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getAllAttributes() 
        throws Exception;
    
    /**
     * Gets a List of users based on the specified search criteria.
     */
    public List getSearchUsers(String searchField, String searchCriteria)
        throws Exception;
    
    /**
     * Gets a List of users based on the specified search criteria and
     * orders the list on the specified field.
     */
    public List getSearchUsers(String searchField, String searchCriteria, 
                               String orderByField, String ascOrDesc)
        throws Exception;

    /**
     * Creates a new array with elements reversed from the given array.
     *
     * @param the orginal <code>Object[]</code> 
     * @return a new <code>Object[]</code> with values reversed from the 
     * original
     */
    public Object[] reverse(Object[] a);

    /**
     * Creates a new List with elements reversed from the given List.
     *
     * @param the orginal <code>List</code> 
     * @return a new <code>List</code> with values reversed from the 
     * original
     */
    public List reverse(List a);

    /**
     * Creates  a view of the portion of the given
     * List between the specified fromIndex, inclusive, and toIndex, exclusive
     * The list returned by this method is backed by the original, so changes
     * to either affect the other.
     *
     * @param the orginal <code>List</code> 
     * @return a derived <code>List</code> with a view of the original
     */
    public List subset(List a, Integer fromIndex, Integer toIndex);

    /**
     * Creates a new array with a view of the portion of the given array
     * between the specified fromIndex, inclusive, and toIndex, exclusive
     *
     * @param the orginal <code>Object[]</code> 
     * @return a new <code>Object[]</code> with a view of the original
     */
    public Object[] subset(Object[] a, Integer fromIndex, Integer toIndex);
}
