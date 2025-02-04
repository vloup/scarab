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

/**
 * This interface declared the methods that any object subjected to Conditions
 * should implement. It's meant to enforce the way this objects behave, so the
 * Condition-related actions could evolve if necessary.
 *  
 * @author Jorge
 *
 */
public interface Conditioned
{
    public static final Integer OR  = 0; 
    public static final Integer AND = 1; 
    
    /**
     * The logical operator to combine conditions.
     * Currently AttributeOptions within one Attribute are always combined with OR,
     * simply because only one option can be active within an optionAttibute.
     * The operator returned here is meant to work between different attributes.
     * @author hdab
     *
     */
    public Integer getConditionOperator();
    
    /**
     * Returns the array of Ids of the conditions that will force the requirement 
     * of this attribute if set. Used by templates to load the combo.
     * @return
     */
    public Integer[] getConditionsArray();
    /**
     * Load the attribute options' IDs from the template combo.
     * operator is 0 for "or" and 1 for "and" We can not use enums here,
     * because we will get data from velocity. Hence the 'hack' to use Integers here.
     * @param aOptionId
     * @throws TorqueException
     */
    public void setConditionsArray(Integer aOptionId[], Integer operator)
            throws TorqueException;
    /**
     * Return true if the given attributeOptionId will make the current
     * attribute required.
     * @param optionID
     * @return
     * @throws TorqueException
     */
    public boolean isRequiredIf(Integer optionID)
            throws TorqueException;
    
    /**
     * 
     * @return true if there's any condition associated to this object, false if there's not.
     */
    public boolean isConditioned();
}