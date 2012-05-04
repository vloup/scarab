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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeValueManager;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.ScarabUser;
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
    private final Long issueId;
    private final List issueListAttributeColumns;
    private final Integer sortAttrId;
    private final Long sortValueId;
    private final ScarabLocalizationTool L10N;
    
    private List attributeValues;

    /**
     * Ctor. Should only be called by an IssueSearch.
     *
     * @param search the <code>IssueSearch</code> that created this result
     */
    QueryResult(Long issueId, List issueListAttributeColumns, Integer sortAttrId, Long sortValueId, ScarabLocalizationTool L10N )
    {
        this.issueId = issueId;
        this.issueListAttributeColumns = issueListAttributeColumns;
        this.sortAttrId=sortAttrId;
        this.sortValueId=sortValueId;
        this.L10N = L10N;        
    }

    public final Issue getIssue()
        throws TorqueException
        {
    	return IssueManager.getInstance(issueId);
    }
    /**
     * Get the IssueId value.
     * @return the IssueId value.
     */
    public final String getIssueId()
    {
        return issueId.toString();
    }

    /**
     * Combines getIdPrefix() and getIdCount()
     */
    public final String getUniqueId()
        throws TorqueException
    {
        return getIssue().getUniqueId();
    }

    /**
     * Get the AttributeValues value.
     * @return the AttributeValues value.
     */
    public final List getAttributeValues()
        throws TorqueException
    {
        if(attributeValues==null)
        {
            attributeValues = new ArrayList();
            for(int j=0;j<issueListAttributeColumns.size();j++)
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute)issueListAttributeColumns.get(j);

                List value = null;
                if(rmua.isInternal())
                {
                	String attributeId = rmua.getInternalAttribute();
                    value = getInternalAttributeValue(attributeId);
                }
                else
                {
                    Integer attributeId = rmua.getAttributeId();                
                    value = getAttributeValue(attributeId);
                }                    
                attributeValues.add(value);
            }                                
        }        
        return attributeValues;
    }

	/**
	 * @param attributeId
	 */
	private List getAttributeValue(Integer attributeId)
        throws TorqueException
	{
        List value = new ArrayList();
        List attributeValue = getIssue().getAttributeValues(attributeId);
        String singleValue = null;
        
        if(attributeId.equals(sortAttrId))
	    {
	    	if(sortValueId!=null)
	    	{
	    		AttributeValue sortValue = AttributeValueManager.getInstance(sortValueId);
	    		singleValue = sortValue.getDisplayValue(L10N);	    		
	    	}
	    	else
	    	{
	    		singleValue = "";
	    	}
        	value.add(singleValue);
	    }
	    else
	    {
            for(int i=0;i<attributeValue.size();i++)
		    {
		        singleValue = ((AttributeValue)attributeValue.get(i)).getDisplayValue(L10N);
		        value.add( singleValue );
		    }
		}
		return value;
	}

	/**
	 * @param attributeId
	 */
	private List getInternalAttributeValue(String attributeId)
        throws TorqueException
	{
        List value = new ArrayList();
        if (attributeId.equals(RModuleUserAttribute.CREATED_BY.getName()))
        {
            ScarabUser user = getIssue().getCreatedBy();
            value.add(user.getName());
        }
        else if (attributeId.equals(RModuleUserAttribute.CREATED_DATE.getName()))
        {
            DateFormat df = new SimpleDateFormat(L10NKeySet.ShortDatePattern.getMessage(L10N));
            value.add(df.format(getIssue().getCreatedDate()));
        }
        else if (attributeId.equals(RModuleUserAttribute.MODIFIED_BY.getName()))
        {
            ScarabUser user = getIssue().getModifiedBy();
            value.add(user.getName());
        }
        else if (attributeId.equals(RModuleUserAttribute.MODIFIED_DATE.getName()))
        {
            DateFormat df = new SimpleDateFormat(L10NKeySet.ShortDatePattern.getMessage(L10N));
            value.add(df.format(getIssue().getModifiedDate()));
        }
        else if (attributeId.equals(RModuleUserAttribute.MODULE.getName()))
        {
            value.add(getIssue().getModule().getRealName());
        }
        else if (attributeId.equals(RModuleUserAttribute.ISSUE_TYPE.getName()))
        {
            IssueType isueType = getIssue().getIssueType();
            value.add(isueType.getDisplayName(getIssue().getModule()));
        }
		return value;
	}
	
    /**
     * Get the AttributeValues value.
     * @return the AttributeValues value.
     */
    public final List getAttributeValuesAsCSV()
        throws TorqueException
    {
        List result = null;
        if (getAttributeValues() != null) 
        {
            result = new ArrayList(getAttributeValues().size());
            for (Iterator i = getAttributeValues().iterator(); i.hasNext();) 
            {
                List multiVal = (List)i.next();
                String csv = StringUtils.join(multiVal.iterator(), ", ");
                if(csv.indexOf('\n') > -1)
                {
                    csv = csv.replace('\n', '.');
                }
                if(csv.indexOf('\r') > -1)
                {
                    csv = csv.replace('\r', '.');
                }
                result.add(csv);
            }
        }            
        return result;
    }
}
