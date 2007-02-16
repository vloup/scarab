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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.List;
import java.util.HashMap;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

/** 
 * This class manages AttributeOption objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class AttributeOptionManager
    extends BaseAttributeOptionManager
{
    /**
     * Creates a new <code>AttributeOptionManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public AttributeOptionManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
        validFields = new HashMap();
        validFields.put(AttributeOptionPeer.OPTION_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap.get(AttributeOptionPeer.OPTION_ID);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }

    /**
     * Instantiates a new AttributeOption
     *
     * @return an <code>AttributeOption</code> value
     */
    public AttributeOption getInstanceImpl()
    {
        return new AttributeOption();
    }

    /**
     * @see #getInstance(Attribute, String, Module, IssueType)
     */
    public static AttributeOption getInstance(final Attribute attribute, 
            final String name)
        throws TorqueException
    {
        return getInstance(attribute, name, (Module) null, (IssueType) null);
    }

    /**
     * Using some contextual information, get an instance of a
     * particular <code>AttributeOption</code>.
     *
     * @param attribute The attribute to which the named option
     * belongs.
     * @param name The module-specific alias or canonical value of the
     * option.
     * @param module May be <code>null</code>.
     * @param issueType May be <code>null</code>.
     */
    public static AttributeOption getInstance(final Attribute attribute, 
                                              final String name,
                                              final Module module,
                                              final IssueType issueType)
        throws TorqueException
    {
        AttributeOption ao = null;
        Criteria crit;
        // FIXME: Optimize this implementation!  It is grossly
        // inefficient, which is problematic given how often it may be
        // used.
        if (module != null && issueType != null)
        {
            // Look for a module-scoped alias.
            crit = new Criteria(4);
            crit.add(AttributeOptionPeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId());
            crit.addJoin(AttributeOptionPeer.OPTION_ID, 
                         RModuleOptionPeer.OPTION_ID); 
            crit.add(RModuleOptionPeer.MODULE_ID, module.getModuleId());
            crit.add(RModuleOptionPeer.ISSUE_TYPE_ID,
                     issueType.getIssueTypeId());
            crit.add(RModuleOptionPeer.DISPLAY_VALUE, name);
            final List rmos = RModuleOptionPeer.doSelect(crit);
            if (rmos.size() == 1)
            {
                final RModuleOption rmo = (RModuleOption) rmos.get(0);
                ao = rmo.getAttributeOption();
            }
        }

        if (ao == null)
        {
            // TODO It seems that we might not necessarily get the global option.
            // Do we want to add a criteria to limit to getting the global option?
            // This would be either "= 0" or "is null".
             
            crit = new Criteria(2);
            crit.add(AttributeOptionPeer.OPTION_NAME, name);
            crit.add(AttributeOptionPeer.ATTRIBUTE_ID,
                     attribute.getAttributeId());
            crit.setIgnoreCase(true);
            final List options = AttributeOptionPeer.doSelect(crit);
            if (options.size() == 1)
            {
                ao =  (AttributeOption) options.get(0);
            }
        }
        return ao;
    }
    
}
