package org.tigris.scarab.util;

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

/**
 * A model that provides for an application to present a list of possible
 * options.  Can be used along with a velocity macro to create option tags.
 * 
 * #macro ( option $optionModel )
 *     <option #selected($optionModel.isSelected()) 
 *         value="$optionModel.Value">$optionModel.Name</option>
 * #end
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id$
 */
public class OptionModel
{
    protected String name;
    protected String value;
    protected boolean selected;
    
    public OptionModel()
    {
    }

    public OptionModel(String value, String name)
    {
        this(value, name, false);
    }
    
    public OptionModel(int value, String name)
    {
        this(String.valueOf(value), name);
    }

    public OptionModel(int value, String name, boolean selected)
    {
        this(String.valueOf(value), name, selected);
    }

    public OptionModel(String value, String name, boolean selected)
    {
        this.name = name;
        this.value = value;
        this.selected = selected;
    }
    
    /**
     * Get the name which is useful for ui.
     * @return value of name.
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     * Set the name which is useful for ui.
     * @param v  Value to assign to name.
     */
    public void setName(String  v) 
    {
        this.name = v;
    }
    
    /**
     * Get the value used by the application.
     * @return value of value.
     */
    public String getValue() 
    {
        return value;
    }
    
    /**
     * Set the value used by the application.
     * @param v  Value to assign to value.
     */
    public void setValue(String  v) 
    {
        this.value = v;
    }
    
    /**
     * Get the value of selected.
     * @return value of selected.
     */
    public boolean isSelected() 
    {
            return selected;
    }
    
    /**
     * Set the value of selected.
     * @param v  Value to assign to selected.
     */
    public void setSelected(boolean  v) 
    {
        this.selected = v;
    }        
}
