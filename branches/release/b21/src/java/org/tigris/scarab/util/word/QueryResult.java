package org.tigris.scarab.util.word;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * This class is created by the IssueSearch object to contain a single result.
 * It represents a row on the IssueList.vm screen.  It is mostly
 * a data structure with some additional functionality to format a 
 * multivalued attribute as CSV. 
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 */
public class QueryResult
{
    /** the search that created this QueryResult */
    private final IssueSearch search;
    private String issueId;
    private String idPrefix;
    private String idCount;
    private String uniqueId;
    private List attributeValues;
    private Integer moduleId;
    private Integer issueTypeId;
    private Integer createdBy;
    private Date createdDate;
    private Date modifiedDate;
    private Integer modifiedBy;

    /**
     * Ctor. Should only be called by an IssueSearch.
     *
     * @param search the <code>IssueSearch</code> that created this result
     */
    QueryResult(IssueSearch search)
    {
        this.search = search;
    }

    /**
     * Get the IssueId value.
     * @return the IssueId value.
     */
    public final String getIssueId()
    {
        return issueId;
    }

    /**
     * Set the IssueId value.
     * @param newIssueId The new IssueId value.
     */
    public final void setIssueId(String newIssueId)
    {
        this.issueId = newIssueId;
    }

    /**
     * Get the IdPrefix value.
     * @return the IdPrefix value.
     */
    public final String getIdPrefix()
    {
        return idPrefix;
    }

    /**
     * Set the IdPrefix value.
     * @param newIdPrefix The new IdPrefix value.
     */
    public final void setIdPrefix(String newIdPrefix)
    {
        this.idPrefix = newIdPrefix;
    }

    /**
     * Get the IdCount value.
     * @return the IdCount value.
     */
    public final String getIdCount()
    {
        return idCount;
    }

    /**
     * Set the IdCount value.
     * @param newIdCount The new IdCount value.
     */
    public final void setIdCount(String newIdCount)
    {
        this.idCount = newIdCount;
    }


    /**
     * Combines getIdPrefix() and getIdCount()
     */
    public final String getUniqueId()
    {
        if (uniqueId == null) 
        {
            uniqueId = getIdPrefix() + getIdCount();
        }
            
        return uniqueId;
    }

    /**
     * Get the AttributeValues value.
     * @return the AttributeValues value.
     */
    public final List getAttributeValues()
    {
        return attributeValues;
    }

    /**
     * Get the AttributeValues value.
     * @return the AttributeValues value.
     */
    public final List getAttributeValuesAsCSV()
    {
        List result = null;
        if (attributeValues != null) 
        {
            result = new ArrayList(attributeValues.size());
            for (Iterator i = attributeValues.iterator(); i.hasNext();) 
            {
                String csv = null;
                List multiVal = (List)i.next();
                if (multiVal.size() == 1) 
                {
                    csv = (String)multiVal.get(0);    
                    if (csv == null) 
                    {
                        csv = "";
                    }
                }
                else 
                {
                    StringBuffer sb = new StringBuffer();
                    boolean addComma = false;
                    for (Iterator j = multiVal.iterator(); j.hasNext();) 
                    {
                        if (addComma) 
                        {
                            sb.append(", ");
                        }
                        else 
                        {
                            addComma = true;
                        }
                            
                        sb.append(j.next().toString());
                    }
                    csv = sb.toString();
                }
                result.add(csv);
            }
        }
            
        return result;
    }

    /**
     * Populate any attribute considered 'internal' with the proper value. To decide
     * which should be filled, it will use the list 'preferences', which shares the same
     * order than the attribute list.
     * 
     * @param preferences
     */
    public void populateInternalAttributes(List preferences)
    {
        this.populateInternalAttributes(preferences, null);
    }
    
    /**
     * Populate, including localization of dates, any attribute considered 'internal' with
     * the proper value. To decide which should be filled, it will use the list 'preferences', which shares the same
     * order than the attribute list.
     * 
     * @param preferences
     */    
    public void populateInternalAttributes(List preferences, ScarabLocalizationTool l10n)
    {
        if (preferences == null)
        {
            // No preferences, no need to do anything
            return;
        }
        
        if (l10n == null)
        {
            l10n = new ScarabLocalizationTool();
        }

        for (int i=0; i<preferences.size(); i++)
        {
            RModuleUserAttribute rmua = (RModuleUserAttribute)preferences.get(i);
            if (rmua.isInternal())
            {
                List list = new ArrayList();
                if (rmua.getInternalAttribute().equals(RModuleUserAttribute.CREATED_BY.getName()))
                {
                    ScarabUser user = this.getCreatedByUser();
                    if (user != null)
                    {
                        list.add(user.getName());
                    }
                }
                else if (rmua.getInternalAttribute().equals(RModuleUserAttribute.CREATED_DATE.getName()))
                {
                    Date date = this.getCreatedDate();
                    if (date != null)
                    {
                        DateFormat df = new SimpleDateFormat(L10NKeySet.ShortDatePattern.getMessage(l10n));
                        list.add(df.format(this.getCreatedDate()));
                    }
                }
                else if (rmua.getInternalAttribute().equals(RModuleUserAttribute.MODIFIED_BY.getName()))
                {
                    ScarabUser user = this.getModifiedByUser();
                    if (user != null)
                    {
                        list.add(user.getName());
                    }
                }
                else if (rmua.getInternalAttribute().equals(RModuleUserAttribute.MODIFIED_DATE.getName()))
                {
                    Date date = this.getModifiedDate();
                    if (date != null)
                    {
                        DateFormat df = new SimpleDateFormat(L10NKeySet.ShortDatePattern.getMessage(l10n));
                        list.add(df.format(this.getModifiedDate()));
                    }
                }
                attributeValues.set(i, list);
            }
        }
    }
    
    /**
     * Set the AttributeValues value.
     * @param newAttributeValues The new AttributeValues value.
     */
    public final void setAttributeValues(List newAttributeValues)
    {
        this.attributeValues = newAttributeValues;
    }
        

    /**
     * Get the ModuleId value.
     * @return the ModuleId value.
     */
    public final Integer getModuleId()
    {
        return moduleId;
    }

    /**
     * Set the ModuleId value.
     * @param newModuleId The new ModuleId value.
     */
    public final void setModuleId(Integer newModuleId)
    {
        this.moduleId = newModuleId;
    }

    /**
     * Get the <code>Module</code> related thru getModuleId().  This method
     * provides caching so that multiple QueryResult objects in the same 
     * resultset save db hits.
     */
    public final Module getModule()
        throws TorqueException
    {
        return search.getModule(moduleId);
    }

    /**
     * Get the IssueTypeId value.
     * @return the IssueTypeId value.
     */
    public final Integer getIssueTypeId()
    {
        return issueTypeId;
    }

    /**
     * Set the IssueTypeId value.
     * @param newIssueTypeId The new IssueTypeId value.
     */
    public final void setIssueTypeId(Integer newIssueTypeId)
    {
        this.issueTypeId = newIssueTypeId;
    }

    /**
     * Get the <code>RModuleIssueType</code> related thru getModuleId() and 
     * getIssueTypeId().  This method provides caching so that multiple 
     * QueryResult objects in the same resultset save db hits.
     */
    public final RModuleIssueType getRModuleIssueType()
        throws TorqueException
    {
        return search.getRModuleIssueType(moduleId, issueTypeId);
    }

    public void setCreatedBy(Integer createdBy)
    {
        this.createdBy = createdBy;
        
    }
    
    public Integer getCreatedBy()
    {
        return this.createdBy;
    }
    
    public ScarabUser getCreatedByUser()
    {
        ScarabUser user = null;
        try
        {
            user = ScarabUserImplPeer.retrieveScarabUserImplByPK(this.createdBy);
        }
        catch (Exception e)
        {
        }
        return user;
    }
    public void setCreatedDate(Date created)
    {
        this.createdDate = created;
    }
    public Date getCreatedDate()
    {
        return this.createdDate;
    }
    public void setModifiedBy(Integer modified)
    {
        this.modifiedBy = modified;
    }
    public Integer getModifiedBy()
    {
        return this.modifiedBy;
    }
    
    public ScarabUser getModifiedByUser()
    {
        ScarabUser user = null;
        try
        {
            user = ScarabUserImplPeer.retrieveScarabUserImplByPK(this.modifiedBy);
        }
        catch (Exception e)
        {
        }
        return user;
    }
    public void setModifiedDate(Date modifiedDate )
    {
        this.modifiedDate = modifiedDate;
    }
    public Date getModifiedDate()
    {
        return this.modifiedDate;
    }
}
