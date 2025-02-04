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

import java.util.ArrayList;
import java.util.List;

import org.apache.fulcrum.intake.Retrievable;
import org.apache.torque.TorqueException;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.util.ScarabException;

/** 
  * This class is used by Intake on the GlobalAttributeEdit page
  * to create combination of a ROptionOption and a AttributeOption
  *
  * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
  * @version $Id$
  */
public class ParentChildAttributeOption 
    implements Retrievable, java.io.Serializable
{

    private Integer attributeId = null;
    private Integer optionId = null;
    private Integer parentId = null;
    private boolean deleted = false;
    private String name = null;
    private int preferredOrder = 0;
    private int weight = 0;
    private String style = null;
    private List ancestors = null;
    private static final Integer ROOT_ID = new Integer(0);


    /**
     * Must call getInstance()
     */
    protected ParentChildAttributeOption()
    {
    }

    /**
     * Gets an instance of a new ParentChildAttributeOption
     */
    public static ParentChildAttributeOption getInstance()
    {
        return new ParentChildAttributeOption();
    }

    /**
     * Gets an instance of a new ROptionOption
     */
    public static ParentChildAttributeOption getInstance(
                                Integer parent, Integer child)
    {
    	ParentChildAttributeOption pcao = getInstance();
        pcao.setParentId(parent);
        pcao.setOptionId(child);
        return pcao;
    }

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public String getQueryKey()
    {
        if (parentId == null || optionId == null)
        {
            return "";
        }
        return getParentId().toString() + ":" + getOptionId().toString();
    }

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public void setQueryKey(String key)
    {
        int index = key.indexOf(":");
        String a = key.substring(0,index);
        String b = key.substring(index,key.length());
        setParentId(new Integer(a));
        setOptionId(new Integer(b));
    }

    public Integer getAttributeId()
    {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId)
    {
        this.attributeId = attributeId;
    }

    /**
     * The 'child' optionid
     */
    public Integer getOptionId()
    {
        return this.optionId;
    }

    /**
     * The 'child' optionid
     */
    public void setOptionId(Integer key)
    {
        this.optionId = key;
    }

    /**
     * The 'child' AttributeOption
     */
    public AttributeOption getChildOption()
        throws TorqueException
    {
        return AttributeOptionManager.getInstance(getOptionId());
    }

    public Integer getParentId()
    {
        if (this.parentId == null)
        {
            return new Integer(0);
        }
        return this.parentId;
    }

    public void setParentId(Integer id)
    {
        this.parentId = id;
    }

    public AttributeOption getParentOption()
        throws TorqueException
    {
        return AttributeOptionManager.getInstance(getParentId());
    }

    public List getAncestors()
        throws TorqueException, Exception
    {
        ancestors = new ArrayList();
        AttributeOption parent = getParentOption();
        if (!ROOT_ID.equals(parent.getOptionId()))
        {
            addAncestors(parent);
        }
        return ancestors;
    }

    /**
     * recursive helper method for getAncestors()
     */
    private void addAncestors(AttributeOption option)
        throws TorqueException, Exception
    {
        if (!ROOT_ID.equals(option.getParent().getOptionId()))
        {
            if (ancestors.contains(option.getParent()))
            {
                throw new TorqueException("Tried to add a recursive parent-child " +
                                    "attribute option relationship."); //EXCEPTION
            }
            else
            { 
                addAncestors(option.getParent());
            }
        }
        ancestors.add(option.getOptionId());
    }

    public boolean getDeleted()
    {
        return this.deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public String getName()
    {
        if (this.name == null)
        {
            return "";
        }
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPreferredOrder()
    {
        return this.preferredOrder;
    }

    public void setPreferredOrder(int preferredOrder)
    {
        this.preferredOrder = preferredOrder;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public String getStyle()
    {
        return this.style;
    }

    public void setStyle(String style)
    {
        this.style = style;
    }

    public String toString()
    {
        return getParentId() + ":" + getOptionId() + " -> " + getName();
    }
    
    /**
     * saves the according attribute option and option-option relation
     * @throws TorqueException
     * @throws ScarabException
     */
    public void save()
        throws TorqueException, ScarabException
    {
        AttributeOption ao = null;
        ROptionOption roo = null;

        final Attribute tmpAttr = AttributeManager.getInstance(getAttributeId());
        
        // if it is new, it won't already have an optionId
        if (getOptionId() == null)
        {
            // if it is new, check for duplicates.
            final AttributeOption duplicate = 
                AttributeOptionManager.getInstance(tmpAttr, getName().trim());
            final AttributeOption parent = 
                AttributeOptionManager.getInstance(getParentId());
            if (duplicate != null)
            {
                throw new ScarabException (new L10NKey("CannotCreateDuplicateOption")); //EXCEPTION
            }
            else if (parent.getDeleted())
            {
                throw new ScarabException (new L10NKey("CannotCreateChild")); //EXCEPTION
            }
        }

        // if getOptionId() is null, then it will just create a new instance
        final Integer optionId = getOptionId();
        if (optionId == null)
        {
            ao = AttributeOptionManager.getInstance();
        } 
        else
        {
            ao = AttributeOptionManager.getInstance(getOptionId());
        }

        
        ao.setName(getName());
        ao.setDeleted(getDeleted());
        ao.setAttribute(tmpAttr);
        ao.setStyle(getStyle());
        ao.save();

        // clean out the caches for the AO
        tmpAttr.doRemoveCaches();

        // now set our option id from the saved AO
        this.setOptionId(ao.getOptionId());

        // now create the ROO mapping
        try
        {
            // look for a cached ROptionOption
            roo = ROptionOption.getInstance(getParentId(), getOptionId());
        }
        catch (ScarabException se)
        {
            // could not find a cached instance create new one
            roo = ROptionOption.getInstance();
            roo.setOption1Id(getParentId());
            roo.setOption2Id(getOptionId());
        }
        roo.setPreferredOrder(getPreferredOrder());
        roo.setWeight(getWeight());
        roo.setRelationshipId(OptionRelationship.PARENT_CHILD);
        roo.save();
    }
}
