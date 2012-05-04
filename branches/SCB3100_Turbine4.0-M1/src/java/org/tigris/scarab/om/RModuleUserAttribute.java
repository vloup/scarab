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

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.apache.turbine.util.security.TurbineSecurityException;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

/** 
 * This class is for dealing with Attributes associated to Users and Modules.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id$
 */
public  class RModuleUserAttribute 
    extends BaseRModuleUserAttribute
    implements Persistent
{
    /**
     * Delete the record. Must have USER__EDIT_PREFERENCES to be
     * able to execute this method.
     */
    public void delete(final ScarabUser user) throws TorqueException, TurbineSecurityException 
    { 
        boolean hasPermission = true;
        if (getModule() != null)
        {
            hasPermission = user.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES,getModule());
        }
        else //getModule() is null for X Project queries, so get the modules from the MIT List
        {
            final List moduleList = user.getCurrentMITList().getModules();
            for (Iterator iter = moduleList.iterator(); iter.hasNext(); )
            {
                hasPermission = user.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES,(Module)iter.next());
                if (!hasPermission)
                {
                    break;
                }
            }
        }
        if (hasPermission)
        {
            final Criteria c = new Criteria()
                .add(RModuleUserAttributePeer.MODULE_ID, getModuleId())
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, getIssueTypeId())
                .add(RModuleUserAttributePeer.LIST_ID, getListId())
                .add(RModuleUserAttributePeer.ATTRIBUTE_ID, getAttributeId());
            RModuleUserAttributePeer.doDelete(c);
        }
        else
        {
            throw new TurbineSecurityException(ScarabConstants.NO_PERMISSION_MESSAGE); //EXCEPTION
        }
    }

    public void setInternalAttribute(String v) 
    {
        try
        {
            if (v != null)
            {
                this.setAttributeId(new Integer(0));
            }
        }
        catch (TorqueException e)
        {
            Log.get().error("setInternalAttribute(): " + e);
        }
        super.setInternalAttribute(v);
    }
    
    public static Attribute MODIFIED_BY = new Attribute();
    public static Attribute MODIFIED_DATE = new Attribute();
    public static Attribute CREATED_BY = new Attribute();
    public static Attribute CREATED_DATE = new Attribute();
    public static Attribute MODULE = new Attribute();
    public static Attribute ISSUE_TYPE = new Attribute();
    
    public static Set internalAttributes = new HashSet();
    static
    {
        MODIFIED_BY.setName(L10NKeySet.ModifiedBy.toString());
        MODIFIED_DATE.setName(L10NKeySet.ModifiedDate.toString());
        CREATED_BY.setName(L10NKeySet.CreatedBy.toString());
        CREATED_DATE.setName(L10NKeySet.CreatedDate.toString());
        MODULE.setName(L10NKeySet.Module.toString());
        ISSUE_TYPE.setName( L10NKeySet.IssueType.toString());
        internalAttributes.add(MODIFIED_BY);
        internalAttributes.add(MODIFIED_DATE);
        internalAttributes.add(CREATED_BY);
        internalAttributes.add(CREATED_DATE);
        internalAttributes.add(MODULE);
        internalAttributes.add(ISSUE_TYPE);
    }

    public boolean isInternal()
    {
        boolean bInternal = false;
        if (this.getAttributeId() == null || this.getAttributeId().equals(new Integer(0)))
        {
            for (Iterator it = internalAttributes.iterator(); it.hasNext() && !bInternal; )
            {
                Attribute at = (Attribute)it.next();
                bInternal = at.getName().equals(this.getInternalAttribute());
            }
        }
        return bInternal;
    }
    
    /**
     * Returns the proper name for the subyacent attribute, be it
     * internal or a regular attribute.
     * 
     * @return
     */
    public String getName()
    {
        String name = this.getInternalAttribute(); 
        try
        {
            if (this.getAttributeId().intValue() != 0)
                name = this.getAttribute().getName();
        }
        catch (TorqueException e)
        {
            getLog().error("getName(): " + e);
        }

        return name;
    }

    /**
     * Returns a localized (if possible) version of the
     * attribute name. It only applies to internal attributes, so
     * any other attribute name will be returned unchanged.
     * 
     * @param l10n
     * @return
     */
    public String getName(ScarabLocalizationTool l10n)
    {
        String attrName = this.getName();
        if (this.isInternal())
        {
            // Internal attribute names are localizable
            L10NKey key = new L10NKey(attrName);
            attrName = (new L10NMessage(key)).getMessage(l10n);
        }
        return attrName;
    }

}
