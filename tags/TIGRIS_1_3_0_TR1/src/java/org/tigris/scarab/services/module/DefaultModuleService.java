package org.tigris.scarab.services.module;

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

import java.util.List;

import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;

import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.util.ScarabException;

/**
 * This is an implementation of the ModuleService that will return 
 * <code>ScarabModule</code>'s. 
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jon@collab.net">John McNally</a>
 * @version $Id$
 */
public class DefaultModuleService 
    extends AbstractModuleService 
{
    /**
     * check for a duplicate project name
     */
    public boolean exists(ModuleEntity module)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add (ScarabModulePeer.MODULE_NAME, module.getRealName());
        crit.add (ScarabModulePeer.PARENT_ID, module.getParentId());
        return ScarabModulePeer.doSelect(crit).size() > 0;
    }

    /**
     * Get the user classname that this implementation will instantiate
     */
    protected String getClassName()
    {
        return "org.tigris.scarab.om.ScarabModule";
    }

    protected Object retrieveStoredOM(ObjectKey id)
        throws Exception
    {
        return ScarabModulePeer.retrieveByPK(id);
    }

    /**
     * Gets a list of ModuleEntities based on id's.
     *
     * @param moduleIds a <code>NumberKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    protected List retrieveStoredOMs(List moduleIds) 
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.addIn(ScarabModulePeer.MODULE_ID, moduleIds);
        return ScarabModulePeer.doSelect(crit);            
    }
}
