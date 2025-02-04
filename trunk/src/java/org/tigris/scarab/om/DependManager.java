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

import java.util.List;
import java.util.HashMap;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

/** 
 * This class manages Depend objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class DependManager
    extends BaseDependManager
{
    /**
     * Creates a new <code>DependManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public DependManager()
        throws TorqueException
    {
        super();
        validFields = new HashMap();
        validFields.put(DependPeer.DEPEND_ID, null);
        validFields.put(DependPeer.OBSERVER_ID, null);
        validFields.put(DependPeer.OBSERVED_ID, null);
        validFields.put(DependPeer.DEPEND_TYPE_ID, null);
        validFields.put(DependPeer.DELETED, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap.get(DependPeer.DEPEND_ID);
        notifyListeners(listeners, oldOm, om);
        listeners = (List)listenersMap.get(DependPeer.OBSERVER_ID);
        notifyListeners(listeners, oldOm, om);
        listeners = (List)listenersMap.get(DependPeer.OBSERVED_ID);
        notifyListeners(listeners, oldOm, om);
        listeners = (List)listenersMap.get(DependPeer.DEPEND_TYPE_ID);
        notifyListeners(listeners, oldOm, om);
        listeners = (List)listenersMap.get(DependPeer.DELETED);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }
}
