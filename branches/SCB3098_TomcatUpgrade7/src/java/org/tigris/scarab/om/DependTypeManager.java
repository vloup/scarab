package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.localization.L10NKey;

/** 
 * This class manages DependType objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class DependTypeManager
    extends BaseDependTypeManager
{
    public static final Integer BLOCKING__PK = new Integer(1);
    public static final Integer DUPLICATE__PK = new Integer(2);
    public static final Integer NON_BLOCKING__PK = new Integer(3);

    // the following Strings are method names that are used in caching results
    private static final String GET_ALL = 
        "getAll";

    private static final String DEPENDTYPE = 
        "DependType";
    private static final String FIND_DEPENDTYPE_BY_NAME = 
        "findDependTypeByName";
    private static Map l10nKeys = new HashMap();

    /**
     * Creates a new <code>DependTypeManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public DependTypeManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    /**
     * Creates a new <code>DependType</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public static DependType getInstance(String dependTypeName)
        throws TorqueException
    {
        DependType result = null;
        Object obj = ScarabCache.get(DEPENDTYPE, FIND_DEPENDTYPE_BY_NAME, 
                                     dependTypeName); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(DependTypePeer.DEPEND_TYPE_NAME, dependTypeName);
            List dependTypes = DependTypePeer.doSelect(crit);
            if (dependTypes == null || dependTypes.size() == 0)
            {
                throw new TorqueException("Invalid issue depend type: " + 
                                    dependTypeName); //EXCEPTION
            }
            result = (DependType)dependTypes.get(0);
            ScarabCache.put(result, DEPENDTYPE, FIND_DEPENDTYPE_BY_NAME, 
                            dependTypeName);
        }
        else 
        {
            result = (DependType)obj;
        }
        return result;
    }
    
    /**
     * Creates a new <code>DependType</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public static DependType getInstanceById(String dependTypeId)
        throws TorqueException
    {
        DependType result = null;
        List dependTypes = getAll();
        for(int index=0; index < dependTypes.size(); index++)
        {
            DependType entry = (DependType)dependTypes.get(index);
            if(entry.getQueryKey().equals(dependTypeId))
            {
                result = entry;
                break;
            }
        }
        return result;
    }
    
    public static List getAll()
        throws TorqueException
    {
        return getManager().getAllImpl();
    }

    public List getAllImpl()
        throws TorqueException
    {
        List result = null;
        Object obj = getMethodResult().get(this.toString(), GET_ALL); 
        if (obj == null) 
        {        
            result = DependTypePeer.doSelect(new Criteria());
            getMethodResult().put(result, this.toString(), GET_ALL);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        getMethodResult().remove(this, GET_ALL);
        return oldOm;
    }
    
    static {
        l10nKeys.put( DependTypePeer.BLOCKING__PK, new L10NKey("depend_blocking"));
        l10nKeys.put( DependTypePeer.DUPLICATE__PK, new L10NKey("depend_duplicate"));
        l10nKeys.put( DependTypePeer.NON_BLOCKING__PK, new L10NKey("depend_nonblocking"));
    }
    
    public L10NKey getL10nKey( String dependTypeName ) 
        throws TorqueException 
    {
        return (L10NKey)l10nKeys.get( getInstance(dependTypeName).getDependTypeId());
    }
}
