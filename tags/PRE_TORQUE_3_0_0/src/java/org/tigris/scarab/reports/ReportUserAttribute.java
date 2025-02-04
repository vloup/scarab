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
 */

package org.tigris.scarab.reports;

import org.apache.fulcrum.intake.Retrievable;
import org.apache.commons.lang.ObjectUtils;
import org.tigris.scarab.util.Log;

public class ReportUserAttribute
    implements java.io.Serializable,
               Retrievable
{
    Integer attributeId;

    /**
     * Get the AttributeId value.
     * @return the AttributeId value.
     */
    public Integer getAttributeId()
    {
        return attributeId;
    }

    /**
     * Set the AttributeId value.
     * @param newAttributeId The new AttributeId value.
     */
    public void setAttributeId(Integer newAttributeId)
    {
        this.attributeId = newAttributeId;
    }

    Integer userId;

    /**
     * Get the UserId value.
     * @return the UserId value.
     */
    public Integer getUserId()
    {
        return userId;
    }

    /**
     * Set the UserId value.
     * @param newUserId The new UserId value.
     */
    public void setUserId(Integer newUserId)
    {
        this.userId = newUserId;
    }

    public boolean equals(Object obj)
    {
        boolean result = obj == this;
        if (!result && obj instanceof ReportUserAttribute) 
        {
            ReportUserAttribute rua = (ReportUserAttribute)obj;
            result = ObjectUtils.equals(userId, rua.getUserId())
                && ObjectUtils.equals(attributeId, rua.getAttributeId());
        }
        Log.get().debug("Compare " + obj + " and " + this + " -> " + result);
        
        return result;
    }

    public int hashCode()
    {
        int result = userId == null ? 0 : userId.intValue();
        if (attributeId != null)
        {
            result += attributeId.intValue();
        }
        return result;
    }

    public String toString()
    {
        return super.toString() + " {a=" + attributeId + ", u=" + userId + "}";
    }

    private String queryKey;

    /**
     * Get the QueryKey value.
     * @return the QueryKey value.
     */ 
    public String getQueryKey()
    {
        return queryKey == null ? "" : queryKey;
    }
    
    /**
     * Set the QueryKey value.
     * @param newQueryKey The new QueryKey value.
     */
    public void setQueryKey(String newQueryKey)
    {
        this.queryKey = newQueryKey;
    }
}
