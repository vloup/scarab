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

public class ModuleIssueType
    implements java.io.Serializable,
               Retrievable
{
    Integer moduleId;

    /**
     * Get the ModuleId value.
     * @return the ModuleId value.
     */
    public Integer getModuleId()
    {
        return moduleId;
    }

    /**
     * Set the ModuleId value.
     * @param newModuleId The new ModuleId value.
     */
    public void setModuleId(Integer newModuleId)
    {
        this.moduleId = newModuleId;
    }

    Integer issueTypeId;

    /**
     * Get the IssueTypeId value.
     * @return the IssueTypeId value.
     */
    public Integer getIssueTypeId()
    {
        return issueTypeId;
    }

    /**
     * Set the IssueTypeId value.
     * @param newIssueTypeId The new IssueTypeId value.
     */
    public void setIssueTypeId(Integer newIssueTypeId)
    {
        this.issueTypeId = newIssueTypeId;
    }

    public boolean equals(Object obj)
    {
        boolean result = obj == this;
        if (!result && obj instanceof ModuleIssueType) 
        {
            ModuleIssueType mit = (ModuleIssueType)obj;
            result = ObjectUtils.equals(moduleId, mit.getModuleId())
                && ObjectUtils.equals(issueTypeId, mit.getIssueTypeId());
        }
        return result;
    }

    public int hashCode()
    {
        int result = 0;
        if (moduleId != null) 
        {
            result = moduleId.intValue();
        }
        if (issueTypeId != null) 
        {
            result += issueTypeId.intValue();
        }
        return result;
    }


    private String queryKey;

    /**
     * Get the QueryKey value.
     * @return the QueryKey value.
     */ 
    public String getQueryKey()
    {
        if (queryKey == null) 
        {
            return "";
        }
        return queryKey;
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
