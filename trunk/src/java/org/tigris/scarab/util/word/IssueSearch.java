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

// JDK classes
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.workingdogs.village.Record;
import com.workingdogs.village.DataSetException;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.fulcrum.localization.Localization;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.SqlEnum;
import org.apache.fulcrum.intake.Retrievable;
import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.StringAttribute;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.ActivitySetPeer;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.search.CachedQuery;
import org.tigris.scarab.search.CachedResultList;

/** 
 * A utility class to build up and carry out a search for 
 * similar issues.  It subclasses Issue for functionality, it is 
 * not a more specific type of Issue.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class IssueSearch
    implements Retrievable
{
    private static final String STATE_CHANGE_DATE_TABLE = "STATE_CHANGE_DATE_TABLE";

	private static final String STATE_CHANGE_TABLE = "STATE_CHANGE_TABLE";

	private static final String SORT_HELP_TABLE = "SORT_HELP_TABLE";

	private static final String SORT_TABLE = "SORT_TABLE";


    public static final String ASC = "asc";
    public static final String DESC = "desc";

    public static final String CREATED_BY_KEY = "created_by";
    public static final String ANY_KEY = "any";
    public static final String SEARCHING_USER_KEY = "$me";
    private static ScarabUser searchingUserPlaceholder;

    private static final Integer NUMBERKEY_0 = new Integer(0);
    
    private static final char DOT_REPLACEMENT_IN_JOIN_CONDITION = '#';

	private static final Pattern RELATIVE_DATE_PATTERN = Pattern.compile(
        "\\s*now\\s*(([+-])\\s*(\\d+)|)\\s*",
		Pattern.CASE_INSENSITIVE
    );
	
	private SimpleDateFormat formatter;

    private ScarabUser searchingUser;
    private String searchWords;
    private String commentQuery;
    private Integer[] textScope;
    private String minId;
    private String maxId;
    private String minCreationDate;
    private String maxCreationDate;
    private String minChangeDate;
    private String maxChangeDate;
    
    private Integer stateChangeAttributeId;
    private Integer stateChangeFromOptionId;
    private Integer stateChangeToOptionId;
    private String stateChangeFromDate;
    private String stateChangeToDate;

    /**
     * sortAttributeId and sortInternalAttribute hold the attribute to be used
     * for sorting. Only one of them must be setted.
     */
    private Integer sortAttributeId;
    private String sortInternalAttribute;
    private String sortPolarity;
    private MITList mitList;
    private Map searchUsers = new HashMap();
    private boolean mergePartialTextQueries = false;
    
    private List issueListAttributeColumns;

    private boolean isSearchAllowed = true;
    
    /**
     * This is the locale that the search is currently running
     * under. We need it to parse the date attributes. It defaults
     * to the US locale as that was the behaviour before.
     * @todo Ideally, the minCreationDate, maxCreationDate and others should
     * be Date objects, with the user of this class doing the
     * parsing itself. However, the intake tool is currently
     * configured to use this class directly. Hopefully when
     * (if?) intake supports dates natively, we can drop the
     * date parsing from this class and use Dates instead. 
     */
    private Locale locale = Locale.US;
    
    private ScarabLocalizationTool L10N = null;
    
    private Issue searchIssue;
    private Module singleModule;
    private IssueType singleIssueType;
    
    IssueSearch(Module module, IssueType issueType, ScarabUser searcher)
    	throws Exception
    {
    	this( MITListManager.getSingleItemList(module, issueType, searcher), searcher);
    }

	IssueSearch(Issue issue, ScarabUser searcher)
        throws Exception
    {
        this(issue.getModule(), issue.getIssueType(), searcher);
        
        // Make copies of the issue's attribute values so that
        // we can modify them later without affecting the issue
        // itself.
        List searchAttributes = searchIssue.getAttributeValues();
        
        for (Iterator iter = issue.getAttributeValues().iterator(); iter.hasNext(); ) {
            AttributeValue value = (AttributeValue) iter.next();
            searchAttributes.add(value.copy());
        }
    }

    IssueSearch(MITList mitList, ScarabUser searcher)
        throws Exception
    {
        searchingUser = searcher;
        searchIssue = new Issue();
        if (mitList == null || mitList.size() == 0) 
        {
            throw new IllegalArgumentException("A non-null list with at" +
               " least one item is required.");
        }

        String[] perms = {ScarabSecurity.ISSUE__SEARCH};
        MITList searchableList = mitList
            .getPermittedSublist(perms, searcher);

        isSearchAllowed = searchableList.size() > 0;

        this.mitList = searchableList;   
        if (searchableList.isSingleModule()) 
        {
        	singleModule = searchableList.getModule();
        	searchIssue.setModule(singleModule);
        }
        if (searchableList.isSingleIssueType()) 
        {
        	singleIssueType = searchableList.getIssueType();
        	searchIssue.setIssueType(singleIssueType);
        }
    }
    
    public static ScarabUser getSearchingUserPlaceholder()
        throws TorqueException
    {
        if(searchingUserPlaceholder==null)
        {
            searchingUserPlaceholder = ScarabUserManager.getInstance();
            searchingUserPlaceholder.setFirstName("");
            searchingUserPlaceholder.setLastName(SEARCHING_USER_KEY);
            searchingUserPlaceholder.setUserName(SEARCHING_USER_KEY);
            searchingUserPlaceholder.setEmail("");
        }
        return searchingUserPlaceholder;
    }
    
    public Locale getLocale() {
        return this.locale;
    }
    
    public void setLocale(Locale newLocale) {
        this.locale = newLocale;
    }

    public boolean isXMITSearch()
    {
        return !mitList.isSingleModuleIssueType();
    }

    /**
     * Remove special characters from the text values to be searched for
     * to make the search less specific.
     *
     */
    public void removeSpecialCharacters()
        throws Exception
    {
        for (Iterator textAVs = getTextAttributeValues().iterator(); textAVs.hasNext();)
        {
            AttributeValue av = (AttributeValue)textAVs.next();
            String s = av.getValue();
            if (s!=null) 
            {
                av.setValue( s.replaceAll("[\\+\\-\\&{2}\\|\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\]+", " ").toLowerCase());       
            }
        }
    }

    /**
     * List of attributes to show with each issue.
     *
     * @param rmuas a <code>List</code> of RModuleUserAttribute objects
     */
    public void setIssueListAttributeColumns(List rmuas)
    {
        issueListAttributeColumns = rmuas;
    }

    public List getIssueListAttributeColumns()
    {
        return issueListAttributeColumns;
    }
        
    /**
     * returns the list of attribute values
     * for all attributes in the current module.
     * 
     * if(commonOnly==true) 
     * return only attribute values which are
     * commonly defined for all searched issueTypes.
     * @return
     * @throws Exception
     */
    public Map getAttributeValuesMap(boolean commonOnly) throws Exception
    {
       	Map result = null;

        List attributes;
        if(commonOnly)
        {
            attributes = mitList.getCommonAttributes(false);
        }
        else
        {
            attributes = mitList.getAttributes(false,false);
        }

        Map siaValuesMap = searchIssue.getAttributeValuesMap();
        if (attributes != null) 
        {
            result = new LinkedMap((int)(1.25*attributes.size() + 1));
            Iterator i = attributes.iterator();
            while (i.hasNext()) 
            {
                Attribute attribute = (Attribute)i.next();
                String key = attribute.getName().toUpperCase();
                if (siaValuesMap.containsKey(key)) 
                {
                    result.put(key, siaValuesMap.get(key));
                }
                else 
                {
                    AttributeValue aval = AttributeValue
                        .getNewInstance(attribute, searchIssue);
                    searchIssue.addAttributeValue(aval);
                    result.put(key, aval);
                }
            }
        }
        return result;
    }   

    /**
     * @return The list of attributes of type "user" for the module(s)
     * to search in.
     */
    public List getUserAttributes()
        throws Exception
    {
        return mitList.getCommonUserAttributes(false);
    } 

    public List getLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        return mitList.getCommonLeafRModuleOptions(attribute);
    } 

    public List getCommonOptionTree(Attribute attribute)
        throws Exception
    {
        return mitList.getCommonRModuleOptionTree(attribute);
    }

    public List getAllOptionTree(Attribute attribute)
        throws Exception
    {
        return mitList.getAllRModuleOptionTree(attribute);
    }

    /**
     * Get the words for which to search.
     *
     * @return Value of {@link #searchWords}.
     */
    public String getSearchWords() 
    {
        return searchWords;
    }
    
    /**
     * Set the words for which to search.
     *
     * @param v Value to assign to {@link #searchWords}.
     */
    public void setSearchWords(String  v) 
    {
        this.searchWords = v;
    }

    /**
     * Get the value of commentQuery.
     * @return value of commentQuery.
     */
    public String getCommentQuery() 
    {
        return commentQuery;
    }
    
    /**
     * Set the value of commentQuery.
     * @param v  Value to assign to commentQuery.
     */
    public void setCommentQuery(String  v) 
    {
        this.commentQuery = v;
    }
    
    /**
     * Get the value of textScope.  if the scope is not set then all
     * text attributes are returned.  if there are no relevant text
     * attributes null will be returned.
     * @return value of textScope.
     */
    public Integer[] getTextScope()
        throws Exception
    {
        if (textScope == null) 
        {
            textScope = getTextScopeForAll();
        }
        else
        {
            for (int i = textScope.length - 1; i >= 0; i--)
            {
                if (NUMBERKEY_0.equals(textScope[i])) 
                {
                    textScope = getTextScopeForAll();
                    break;
                }       
            }
        }
        return textScope;
    }


    /**
     * Sets the text search scope to all quick search text attributes.
     */
    private Integer[] getTextScopeForAll()
        throws Exception
    {
        Integer[] textScope = null;
        List textAttributes = getQuickSearchTextAttributeValues();
        if (textAttributes != null) 
        {
            textScope = new Integer[textAttributes.size()];
            for (int j=textAttributes.size()-1; j>=0; j--) 
            {
                textScope[j] = ((AttributeValue)
                                textAttributes.get(j)).getAttributeId();
            }
        }
        return textScope;
    }

    /**
     * Set the value of textScope.
     * @param v  Value to assign to textScope.
     */
    public void setTextScope(Integer[] v) 
        throws Exception
    {
        this.textScope = v;            
    }

    /**
     * Get the value of minId.
     * @return value of minId.
     */
    public String getMinId() 
    {
        return minId;
    }
    
    private static String emptyString2null(String s)
    {
        if (s != null && s.length() == 0) 
        {
            return null;
        }
        else
        {
        	return s;
        }
    }
    
    /**
     * Set the value of minId.
     * @param v  Value to assign to minId.
     */
    public void setMinId(String  v) 
    {
        this.minId = emptyString2null(v);
    }
    
    /**
     * Get the value of maxId.
     * @return value of maxId.
     */
    public String getMaxId() 
    {
        return maxId;
    }
    
    /**
     * Set the value of maxId.
     * @param v  Value to assign to maxId.
     */
    public void setMaxId(String  v) 
    {
        this.maxId = emptyString2null(v);
    }
        
    /**
     * Get the value of minCreationDate.
     * @return value of minCreationDate.
     */
    public String getMinCreationDate() 
    {
        return this.minCreationDate;
    }
    
    /**
     * Set the value of minCreationDate.
     * @param newMinCreationDate  Value to assign to minCreationDate.
     */
    public void setMinCreationDate(String newMinCreationDate) 
    {
        this.minCreationDate = emptyString2null(newMinCreationDate);
    }

    /**
     * Get the value of maxCreationDate.
     * @return value of maxCreationDate.
     */
    public String getMaxCreationDate() 
    {
        return this.maxCreationDate;
    }
    
    /**
     * Set the value of maxCreationDate.
     * @param newMaxCreationDate Value to assign to maxCreationDate.
     */
    public void setMaxCreationDate(String newMaxCreationDate) 
    {
        this.maxCreationDate = emptyString2null(newMaxCreationDate);
    }
    
    /**
     * Get the value of minCreationDate.
     * @return value of minCreationDate.
     */
    public String getMinChangeDate() 
    {
        return this.minChangeDate;
    }
    
    /**
     * Set the value of minChangeDate.
     * @param newMinChangeDate  Value to assign to minChangeDate.
     */
    public void setMinChangeDate(String newMinChangeDate) 
    {
        this.minChangeDate = emptyString2null(newMinChangeDate);
    }

    
    /**
     * Get the value of maxChangeDate.
     * @return value of maxChangeDate.
     */
    public String getMaxChangeDate() 
    {
        return this.maxChangeDate;
    }
    
    /**
     * Set the value of maxChangeDate.
     * @param newMaxChangeDate Value to assign to maxChangeDate.
     */
    public void setMaxChangeDate(String newMaxChangeDate) 
    {
        this.maxChangeDate = emptyString2null(newMaxChangeDate);
    }


    /**
     * Get the value of stateChangeAttributeId.
     * @return value of stateChangeAttributeId.
     */
    public Integer getStateChangeAttributeId() 
    {
        return stateChangeAttributeId;
    }
    
    /**
     * Set the value of stateChangeAttributeId.
     * @param v  Value to assign to stateChangeAttributeId.
     */
    public void setStateChangeAttributeId(Integer  v) 
    {
        this.stateChangeAttributeId = v;
    }
        
    /**
     * Get the value of stateChangeFromOptionId.
     * @return value of stateChangeFromOptionId.
     */
    public Integer getStateChangeFromOptionId() 
    {
        return stateChangeFromOptionId;
    }
    
    /**
     * Set the value of stateChangeFromOptionId.
     * @param v  Value to assign to stateChangeFromOptionId.
     */
    public void setStateChangeFromOptionId(Integer  v) 
    {
        this.stateChangeFromOptionId = v;
    }
    
    /**
     * Get the value of stateChangeToOptionId.
     * @return value of stateChangeToOptionId.
     */
    public Integer getStateChangeToOptionId() 
    {
        return stateChangeToOptionId;
    }
    
    /**
     * Set the value of stateChangeToOptionId.
     * @param v  Value to assign to stateChangeToOptionId.
     */
    public void setStateChangeToOptionId(Integer  v) 
    {
        this.stateChangeToOptionId = v;
    }

    
    /**
     * Get the value of stateChangeFromDate.
     * @return value of stateChangeFromDate.
     */
    public String getStateChangeFromDate() 
    {
        return this.stateChangeFromDate;
    }
    
    /**
     * Set the value of stateChangeFromDate.
     * @param fromDate Value to assign to stateChangeFromDate.
     */
    public void setStateChangeFromDate(String fromDate) 
    {
        this.stateChangeFromDate = emptyString2null(fromDate);
    }
    
    
    /**
     * Get the value of stateChangeToDate.
     * @return value of stateChangeToDate.
     */
    public String getStateChangeToDate()
    {
        return this.stateChangeToDate;
    }
    
    /**
     * Set the value of stateChangeToDate.
     * @param toDate Value to assign to stateChangeToDate.
     */
    public void setStateChangeToDate(String toDate) 
    {
        this.stateChangeToDate = emptyString2null(toDate);
    }
    
    /**
     * Get the value of sortAttributeId.
     * @return value of SortAttributeId.
     */
    public Integer getSortAttributeId() 
    {
        return sortAttributeId;
    }
    
    /**
     * Set the value of sortAttributeId.
     * @param v  Value to assign to sortAttributeId.
     */
    public void setSortAttributeId(Integer v) 
    {
        this.sortAttributeId = v;
    }
    
    public void setSortInternalAttribute(String internal)
    {
        this.sortInternalAttribute = internal;
    }
    
    public String getSortInternalAttribute()
    {
        return this.sortInternalAttribute;
    }

    /**
     * Whether to do SQL sorting in <code>DESC</code> or
     * <code>ASC</code> order (the default being the latter).
     * @return value of sortPolarity.
     */
    public String getSortPolarity() 
    {
        return (DESC.equals(sortPolarity) ? DESC : ASC);
    }
    
    /**
     * Set the value of sortPolarity.
     * @param v  Value to assign to sortPolarity.
     */
    public void setSortPolarity(String  v) 
    {
        this.sortPolarity = v;
    }

    public Map getSearchUsers()
    {
    	return searchUsers;
    }
    
    /**
     * Describe <code>addUserSearch</code> method here.
     *
     * @param userId a <code>String</code> represention of the PrimaryKey
     * @param attributeId a <code>String</code> either a String 
     * representation of an Attribute PrimaryKey, or the Strings "created_by" 
     * "any"
     */
    public void addUserSearch(String userId, String attributeId)
    {        
        Set attributeIds = (Set)searchUsers.get(userId);
        
        if(attributeIds==null)
        {
            attributeIds=new HashSet();
            searchUsers.put(userId, attributeIds);        
        }
        
        attributeIds.add(attributeId);
    }

    public Integer getALL_TEXT()
    {
        return NUMBERKEY_0;
    }

    public List getQuickSearchTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(true);
    }

    public List getTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(false);
    }

    private List getTextAttributeValues(boolean quickSearchOnly)
        throws Exception
    {
    	Map searchValues = getAttributeValuesMap(true);
        List searchAttributes = new ArrayList(searchValues.size());

        for (Iterator i=searchValues.values().iterator(); i.hasNext();) 
        {
            AttributeValue searchValue = (AttributeValue)i.next();
            if ((!quickSearchOnly || searchValue.isQuickSearchAttribute())
                 && searchValue.getAttribute().isTextAttribute()) 
            {
                searchAttributes.add(searchValue);
            }
        }

        return searchAttributes;
    }

    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getQuickSearchOptionAttributeValues()
        throws Exception
    {
        Map searchValues = getAttributeValuesMap(true);
		List searchAttributeValues = new ArrayList(searchValues.size());
		
		for (Iterator i= searchValues.values().iterator();i.hasNext();) 
		{
		    AttributeValue searchValue = (AttributeValue)i.next();
		    if (searchValue.isQuickSearchAttribute()
		         && searchValue instanceof OptionAttribute) 
		    {
		        searchAttributeValues.add(searchValue);
		    }
		}
		
		return searchAttributeValues;
    }

    /**
     * remove unset AttributeValues.
     *
     * @param attValues a <code>List</code> value
     */
    private List removeUnsetValues(List attValues)
    {
        int size = attValues.size();
        List setAVs = new ArrayList(size);
        for (int i=0; i<size; i++) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if (attVal.isSet())
            {
                setAVs.add(attVal);
            }
        }
        return setAVs;
    }

    /**
     * give reasonable defaults if module code was not specified
     */
    private void setDefaults(Issue.FederatedId minFid, 
                             Issue.FederatedId maxFid)
        throws Exception
    {
        Module module = singleModule;
        if (module != null) 
        {
            if (minFid != null && minFid.getDomain() == null) 
            {
                minFid.setDomain(module.getScarabInstanceId());
            }
            if (maxFid != null && maxFid.getDomain() == null) 
            {
                maxFid.setDomain(module.getScarabInstanceId());
            }
            if (minFid != null && minFid.getPrefix() == null) 
            {
                minFid.setPrefix(module.getCode());
            }            
        }
        if (maxFid != null && maxFid.getPrefix() == null) 
        {
            if (minFid == null) 
            {
                maxFid.setPrefix(module.getCode());                
            }
            else 
            {
                maxFid.setPrefix(minFid.getPrefix());        
            }
        }
    }

    /**
     * Attempts to parse a atring as a date, first using the locale-sepcific
     * short date format, and then the ISO standard "yyyy-mm-dd". If it sees
     * a ':' character in the date string then the string will be interpreted
     * as a date <b>and</b> time. Throws a ParseException if the String does
     * not contain a suitable format.
     *
     * @param dateString a <code>String</code> value
     * @param locale the locale to use when determining the date patterns
     * to try.
     * @param addTwentyFourHours if no time is given in the date string and
     * this flag is true, then 24 hours - 1 msec will be added to the date.
     * @return a <code>Date</code> value
     */
    public Date parseDate(String dateString, boolean addTwentyFourHours)
        throws ParseException
    {
        Date date = null;
        if (dateString != null) 
        {
            Matcher m = RELATIVE_DATE_PATTERN.matcher(dateString);
        	   if(m.matches())
        	   {
                date = new Date();

        		    String dateDifference = m.group(3);
        		    String sign = m.group(2);
        		    if(dateDifference!=null)
        	       {
                    long hours = Long.parseLong(dateDifference);  
                    if (sign.equals("-")) hours = hours * -1;
                    date.setTime(date.getTime() + hours * 3600000 );
                }
        	   }
            else if (dateString.indexOf(':') == -1)
            {
                String[] patterns = {
                    Localization.getString(this.locale, "ShortDatePattern"),
                    ScarabConstants.ISO_DATE_PATTERN };
                date = parseDate(dateString, patterns);
                
                if (addTwentyFourHours) 
                {                
                    date.setTime(date.getTime() + 86399999);
                }
            }
            else
            {
                String[] patterns = {
                    Localization.getString(this.locale, "ShortDateTimePattern"),
                    ScarabConstants.ISO_DATETIME_PATTERN };
                date = parseDate(dateString, patterns);        
            }
        }
        
        return date;
    }
    
    /**
     * Attempts to parse a String as a Date, trying each pattern in
     * turn until the string is successfully parsed or all patterns
     * have been tried.
     *
     * @param s a <code>String</code> value that should be converted
     * to a <code>Date</code>.
     * @param patterns patterns to be used for conversion
     * @return the equivalent <code>Date</code> if the string could
     * be parsed. 
     * @throws ParseException if input String is null, or the string
     * could not be parsed.
     */
    private Date parseDate(String s, String[] patterns)
        throws ParseException
    {
        if (s == null) 
        {
            throw new ParseException("Input string was null", -1);
        }

        if (formatter == null) 
        {
            formatter = new SimpleDateFormat();
        }
        
        for (int i = 0; i < patterns.length; i++) 
        {
        	Date date = null;
        	formatter.applyPattern(patterns[i]);
            try
            {
            	date = formatter.parse(s);
            }
            catch (ParseException ex)
            {
                // ignore, because we have to try all patterns
            }            
            if (date != null) 
            {
                return date;
            }
        }
        
        throw new ParseException("Date could not be parsed with any"
                                 + " of the provided date patterns.", -1);
    }

    /**
     * <p>This method builds a Criterion for a single attribute value.
     * It is used in the addOptionAttributes method.</p>
     * <p>The attribute value is basically the attribute name/id
     * + its value. Since some option (picklist) attributes are
     * hierachical, we need to add any child values of the given
     * attribute value. For example, assume we have an attribute named
     * "Operating System". This might have values in a hierarchy like
     * so:</p>
     * <pre>
     *   All
     *     Windows
     *       NT
     *       2000
     *       XP
     *     Unix
     *       Linux
     *       Solaris
     *       Tru64
     * </pre>
     * <p>If the user selects the "Windows" value in a query, we want
     * to include any issues that have "Windows" as this attribute's
     * value, and also "NT", "2000", and "XP".</p>
     * <p>All the appropriate attribute values are added to the 'options'
     * list as RModuleOption objects.</p>
     *
     * @param aval an <code>AttributeValue</code> value
     * @return a <code>Criteria.Criterion</code> value
     */
    private void buildOptionList(List options, AttributeValue aval)
        throws Exception
    {
        List descendants =  mitList.getDescendantsUnion(aval.getAttributeOption());
        
        // Include the selected attribute value as one of the options
        // to search for.
        options.add(aval.getOptionId());
        
        if (descendants != null && !descendants.isEmpty())
        {
            // Add all applicable child attribute options to the list as well.
            for (Iterator i = descendants.iterator(); i.hasNext();) 
            {
                options.add(((RModuleOption)i.next())
                    .getOptionId());
            }
        }
    }

    private Long[] getTextMatches()
        throws Exception
    {
        List setAttValues = getSetAttributeValues();
    	boolean searchCriteriaExists = false;
        Long[] matchingIssueIds = null;
        SearchIndex searchIndex = SearchFactory.getInstance();

        if (getSearchWords() != null && getSearchWords().length() != 0)
        {
            searchIndex.addQuery(getTextScope(), getSearchWords());
            searchCriteriaExists = true;
        }
        else 
        {
            for (int i=0; i<setAttValues.size(); i++) 
            {
                AttributeValue aval = (AttributeValue)setAttValues.get(i);

                Integer[] ids = {aval.getAttributeId()};

                //FIXME remove auxDate workaround for date ranges
                if (aval instanceof DateAttribute)
                {
                    searchCriteriaExists = true;
                    AttributeValue auxAval = aval.getChainedValue();

                    Date date = parseDate(aval.getValue(), false); 
                    Date auxDate = date;
                    if(auxAval != null &&
                       emptyString2null(auxAval.getValue()) != null)
                    {
                    	auxDate = parseDate(auxAval.getValue(), true);                        	
                    }                        
                    if (date.equals(auxDate))
                    {    
                    	searchIndex.addQuery(ids, DateAttribute.internalDateFormat(date));
                    }
                    else
                    {
                        searchIndex.addQuery(ids, 
                            SearchIndex.TEXT + ":["
                            +DateAttribute.internalDateFormat(date) 
                            + " TO " 
                            + DateAttribute.internalDateFormat(auxDate) 
                            + "]"
                        );
                    }
                }
                else if (aval instanceof StringAttribute)
                {
                    searchCriteriaExists = true;
                    searchIndex.addQuery(ids, aval.getValue());
                }
            }
        }

        // add comment attachments
        String commentQuery = getCommentQuery();
        if (commentQuery != null && commentQuery.trim().length() > 0) 
        {
            Integer[] id = {AttachmentTypePeer.COMMENT_PK};
            searchIndex.addAttachmentQuery(id, commentQuery);            
            searchCriteriaExists = true;
        }

        if (searchCriteriaExists) 
        {
            try 
            {
                matchingIssueIds = searchIndex.getRelatedIssues(getMergePartialTextQueries());    
            }
            catch (Exception e)
            {
                SearchFactory.releaseInstance(searchIndex);
                throw e;
            }
        }

        SearchFactory.releaseInstance(searchIndex);
        
        return matchingIssueIds;
    }

    private static String useAlias(String alias, String tableColumn)
    {
        int dot = tableColumn.indexOf('.');
        if(dot!=-1)
        {
            return alias + tableColumn.substring(dot);
        }
            else
        {
            return tableColumn;
        }
    }
    
    private void addMITCriteria(Criteria crit)
        throws org.apache.torque.TorqueException
    {   
        mitList.addToCriteria(crit);
    }

    private void addMatchedTextCriteria(Criteria crit, Long[] issueIdsMatchedText)
    {
        if(issueIdsMatchedText!=null)
        {
    	    crit.andIn(IssuePeer.ISSUE_ID, issueIdsMatchedText);
        }
    }

    private void addSortColumn(Criteria crit, String sortColumn)
    {
        crit.addSelectColumn(sortColumn);
        
        if(getSortPolarity().equals(ASC))
        {
           crit.addAscendingOrderByColumn(sortColumn);
        }
        else
        {
           crit.addDescendingOrderByColumn(sortColumn);
        }
    }

    private void setupSortColumn(Criteria crit, Integer sortAttrId)
        throws TorqueException
    {           
        crit.addAlias(SORT_TABLE, AttributeValuePeer.TABLE_NAME);

        crit.addJoin(
            IssuePeer.ISSUE_ID,
            useAlias(SORT_TABLE, AttributeValuePeer.ISSUE_ID) 
             + ( " AND "  
             + useAlias(SORT_TABLE, AttributeValuePeer.DELETED) + " = 0 AND " +
            useAlias(SORT_TABLE, AttributeValuePeer.ATTRIBUTE_ID) + " = " + sortAttrId
            ).replace('.', DOT_REPLACEMENT_IN_JOIN_CONDITION ), 
            Criteria.LEFT_JOIN
        );
        crit.addSelectColumn(useAlias(SORT_TABLE, AttributeValuePeer.VALUE_ID));

        String sortColumn;
        Attribute att = AttributeManager.getInstance(sortAttrId); 

        if (att.isOptionAttribute())
        {
            crit.addJoin(
                IssuePeer.MODULE_ID, 
                RModuleOptionPeer.MODULE_ID
                + ( " AND " 
                +  IssuePeer.TYPE_ID + " = " + RModuleOptionPeer.ISSUE_TYPE_ID +
                " AND " + RModuleOptionPeer.OPTION_ID + " = " + useAlias(SORT_TABLE, AttributeValuePeer.OPTION_ID)
                ).replace('.', DOT_REPLACEMENT_IN_JOIN_CONDITION ), 
                Criteria.LEFT_JOIN
            );
            sortColumn = RModuleOptionPeer.PREFERRED_ORDER;
        }
        else 
        {
            sortColumn = useAlias(SORT_TABLE, AttributeValuePeer.VALUE);
        }
        
        addSortColumn(crit, sortColumn);
    }

    private void setupInternalSortColumn(Criteria crit, String sortInternal)
    {
        String sortColumn = null;
        String joinColumn = null;

        if (sortInternal.equals(RModuleUserAttribute.MODULE.getName()))
        {
            sortColumn = useAlias(SORT_TABLE, ScarabModulePeer.MODULE_NAME);

            crit.addAlias(SORT_TABLE, ScarabModulePeer.TABLE_NAME);
            crit.addJoin( 
                IssuePeer.MODULE_ID, 
                useAlias(SORT_TABLE, ScarabModulePeer.MODULE_ID), 
                Criteria.LEFT_JOIN 
            );
        }
        else if (sortInternal.equals(RModuleUserAttribute.ISSUE_TYPE.getName()))
        {
            sortColumn = useAlias(SORT_TABLE, IssueTypePeer.NAME);

            crit.addAlias(SORT_TABLE, IssueTypePeer.TABLE_NAME);
            crit.addJoin( 
                IssuePeer.TYPE_ID, 
                useAlias(SORT_TABLE, IssueTypePeer.ISSUE_TYPE_ID), 
                Criteria.LEFT_JOIN 
            );
        }
        if (sortInternal.equals(RModuleUserAttribute.MODIFIED_DATE.getName()) ||
            sortInternal.equals(RModuleUserAttribute.CREATED_DATE.getName()))
        {
            if (sortInternal.equals(RModuleUserAttribute.CREATED_DATE.getName()))
            {
                joinColumn  = IssuePeer.CREATED_TRANS_ID;
            }
            else
            {
                joinColumn  = IssuePeer.LAST_TRANS_ID;
            }
            sortColumn = useAlias(SORT_TABLE, ActivitySetPeer.CREATED_DATE);

            crit.addAlias(SORT_TABLE, ActivitySetPeer.TABLE_NAME);
            crit.addJoin( 
                joinColumn, 
                useAlias(SORT_TABLE,ActivitySetPeer.TRANSACTION_ID), 
                Criteria.LEFT_JOIN 
            );
        }
        else if (sortInternal.equals(RModuleUserAttribute.MODIFIED_BY.getName()) ||
            sortInternal.equals(RModuleUserAttribute.CREATED_BY.getName()))
        {   
            if (sortInternal.equals(RModuleUserAttribute.CREATED_BY.getName()))
            {
                joinColumn  = IssuePeer.CREATED_TRANS_ID;
            }
            else
            {
                joinColumn  = IssuePeer.LAST_TRANS_ID;
            }
            sortColumn = useAlias(SORT_TABLE, ScarabUserImplPeer.LOGIN_NAME);

            crit.addAlias(SORT_TABLE, ScarabUserImplPeer.TABLE_NAME);
            crit.addAlias(SORT_HELP_TABLE, ActivitySetPeer.TABLE_NAME);
            crit.addJoin( 
                joinColumn, 
                useAlias(SORT_HELP_TABLE, ActivitySetPeer.TRANSACTION_ID), 
                Criteria.LEFT_JOIN 
            );
            crit.addJoin( 
                useAlias(SORT_HELP_TABLE, ActivitySetPeer.CREATED_BY), 
                useAlias(SORT_TABLE, ScarabUserImplPeer.USER_ID), 
                Criteria.LEFT_JOIN 
            );
        }

        addSortColumn(crit, sortColumn);
    }

    private void addSortCriteria(Criteria crit)
        throws org.apache.torque.TorqueException
    {
        Integer sortAttrId = getSortAttributeId();
        String sortInternal = getSortInternalAttribute();
        
        if (sortAttrId != null) 
        {
            setupSortColumn(crit, sortAttrId);
        }
        else if (sortInternal != null)
        {
            setupInternalSortColumn(crit, sortInternal);
        }
        else
        {
        	addSortColumn(crit, IssuePeer.ISSUE_ID);
        }
        crit.addAscendingOrderByColumn(IssuePeer.ISSUE_ID);
    }
    
    private Criteria getCoreSearchCriteria(Long[] issueIdsMatchedText)
        throws java.lang.Exception
    {
        Criteria crit = new Criteria();

        crit.addSelectColumn(IssuePeer.ISSUE_ID)
            .and(IssuePeer.DELETED, false)
            .and(IssuePeer.MOVED, false);

        crit.setDistinct();

        addMITCriteria(crit);
        
        addIssueIdRangeCriteria(crit);

        addAttributeOptionCriteria(crit);

        addMatchedTextCriteria(crit, issueIdsMatchedText);

        addDateCriteria(crit);

        addUserCriteria(crit);

        addStateChangeCriteria(crit);

        addSortCriteria(crit);
        
        return crit;
    }

    private void addStateChangeCriteria(Criteria crit)
        throws Exception
    {
        Integer oldOptionId = getStateChangeFromOptionId();
        Integer newOptionId = getStateChangeToOptionId();

        if ((oldOptionId != null &&  !oldOptionId.equals(NUMBERKEY_0))
            || (newOptionId != null && !newOptionId.equals(NUMBERKEY_0))
            || getStateChangeFromDate() != null 
            || getStateChangeToDate() != null)
        {
            crit.addAlias(STATE_CHANGE_TABLE, ActivityPeer.TABLE_NAME);
            crit.addJoin( 
                IssuePeer.ISSUE_ID, 
                useAlias(STATE_CHANGE_TABLE,ActivityPeer.ISSUE_ID), 
                Criteria.INNER_JOIN 
            );

            if (oldOptionId == null && newOptionId == null)
            {
                crit.and(useAlias(STATE_CHANGE_TABLE,ActivityPeer.ATTRIBUTE_ID), 
                         getStateChangeAttributeId(),
                         Criteria.EQUAL);
            }
            else
            {
                if (newOptionId != null && !newOptionId.equals(NUMBERKEY_0)) 
                {
                    crit.and(useAlias(STATE_CHANGE_TABLE,ActivityPeer.NEW_OPTION_ID), 
                             newOptionId,
                             Criteria.EQUAL);
                }
                if (oldOptionId != null && !oldOptionId.equals(NUMBERKEY_0))
                {
                    crit.and(useAlias(STATE_CHANGE_TABLE,ActivityPeer.OLD_OPTION_ID), 
                             oldOptionId,
                             Criteria.EQUAL);
                }
            }
            if (getStateChangeFromDate() != null || getStateChangeToDate() != null)
            {
                crit.addAlias(STATE_CHANGE_DATE_TABLE, ActivitySetPeer.TABLE_NAME);

                crit.addJoin( 
                    useAlias(STATE_CHANGE_TABLE, ActivityPeer.TRANSACTION_ID), 
                    useAlias(STATE_CHANGE_DATE_TABLE, ActivitySetPeer.TRANSACTION_ID), 
                    Criteria.INNER_JOIN 
                );
               addDateRangeCriteria( crit, useAlias(STATE_CHANGE_DATE_TABLE, ActivitySetPeer.CREATED_DATE), 
                                getStateChangeFromDate(), getStateChangeToDate());
            }
        }
    }

            
            
    private void addDateRangeCriteria( Criteria crit, String column, 
                                  String minDateStr, String maxDateStr)
        throws java.text.ParseException
    {        
        Date minDate = parseDate(minDateStr, false);
        Date maxDate = parseDate(maxDateStr, true);
    
        if(minDate!=null)
        {
            crit.and(column, minDate, Criteria.GREATER_EQUAL);
        }
        if(maxDate!=null)
        {
            crit.and(column, maxDate, Criteria.LESS_EQUAL);
        }
    }
    
    private void addTransDateRange(Criteria crit, 
                                 String alias,
                                 String baseColumn,
                                 String minDateStr, String maxDateStr)
        throws java.text.ParseException
    {
        if(minDateStr != null || maxDateStr != null)
        {
            crit.addAlias(alias, ActivitySetPeer.TABLE_NAME);
            crit.addJoin( 
                baseColumn, 
                useAlias(alias,ActivitySetPeer.TRANSACTION_ID), 
                Criteria.INNER_JOIN 
            );
            addDateRangeCriteria(crit,useAlias(alias, ActivitySetPeer.CREATED_DATE),
                                 minDateStr,maxDateStr);    
        } 
    }
    
    private void addDateCriteria(Criteria crit)
        throws Exception
    {        
        addTransDateRange(crit, "CHANGE_DATE_TABLE", IssuePeer.LAST_TRANS_ID,
                          getMinChangeDate(), getMaxChangeDate());
        addTransDateRange(crit, "CREATE_DATE_TABLE", IssuePeer.CREATED_TRANS_ID,
                          getMinCreationDate(), getMaxCreationDate());
    }
        
    private void addUserCriteria(Criteria crit)
        throws Exception
    {        
        List anyUsers = new ArrayList();
        List creatorUsers = new ArrayList();
        Map attrUsers = new HashMap();

        for (Iterator i = searchUsers.entrySet().iterator(); i.hasNext();)
        {                
            Map.Entry entry = (Map.Entry)i.next();
            String userId = (String)entry.getKey();
            Set attrIds = (Set)entry.getValue();

            for ( Iterator i2 = attrIds.iterator(); i2.hasNext();)
            {
                String attrId = (String)i2.next();

    	        if(SEARCHING_USER_KEY.equalsIgnoreCase(userId))
    	        {
    	            userId=searchingUser.getUserId().toString();
    	        }
            
    	        if(ANY_KEY.equals(attrId))
    	        {
    	        	anyUsers.add(userId);
    	        }
    	        else if (CREATED_BY_KEY.equals(attrId)) 
    	        {
    	            creatorUsers.add(userId);
    	        }
    	        else 
    	        {
    	            List userIds = (List)attrUsers.get(attrId);
    	            if (userIds == null) 
    	            {
    	                userIds = new ArrayList();
    	                attrUsers.put(attrId, userIds);
    	            }
    	            userIds.add(userId);
    	        }

            }
        }

        if (anyUsers.size()>0 || attrUsers.size()>0 || creatorUsers.size()>0)
        {
            crit.addAlias("USER_TABLE", ActivityPeer.TABLE_NAME);
            crit.addJoin( 
                IssuePeer.ISSUE_ID, 
                useAlias("USER_TABLE",ActivityPeer.ISSUE_ID), 
                Criteria.INNER_JOIN 
            );
        }                

        if (anyUsers.size()>0)
        {
            crit.addIn(useAlias("USER_TABLE",ActivityPeer.NEW_USER_ID), anyUsers);
        }                                     
        if (attrUsers.size()>0) 
        {
            for (Iterator i = attrUsers.entrySet().iterator(); i.hasNext();)
            {                
                Map.Entry entry = (Map.Entry)i.next();
                String attrId = (String)entry.getKey();
                List userIds = (List)entry.getValue();
                
                Criteria.Criterion userIds4attrId = crit.getNewCriterion(
                    useAlias("USER_TABLE",ActivityPeer.NEW_USER_ID),
                    userIds,
                    Criteria.IN
                );
                userIds4attrId.and(crit.getNewCriterion(
                    useAlias("USER_TABLE",ActivityPeer.ATTRIBUTE_ID),
                    attrId,
                    Criteria.EQUAL
                ));
                
                Criteria.Criterion c = crit.getCriterion(useAlias("USER_TABLE",ActivityPeer.NEW_USER_ID));
                if(c!=null)
                {
                	c.or(userIds4attrId);
                }
                else
                {
                	crit.add(userIds4attrId);
                }
            }
        }
        if (anyUsers.size()>0 || creatorUsers.size()>0)
        {
            List anyAndCreators = new ArrayList();
            anyAndCreators.addAll(anyUsers);
            anyAndCreators.addAll(creatorUsers);

            crit.addAlias("CREATOR_USER_TABLE", ActivitySetPeer.TABLE_NAME);
            crit.addJoin( 
                IssuePeer.CREATED_TRANS_ID, 
                useAlias("CREATOR_USER_TABLE",ActivitySetPeer.TRANSACTION_ID), 
                Criteria.INNER_JOIN 
            );

            Criteria.Criterion userIds4anyAndCreators = crit.getNewCriterion(
                useAlias("CREATOR_USER_TABLE",ActivitySetPeer.CREATED_BY),
                anyAndCreators,
                Criteria.IN
            );

            Criteria.Criterion c = crit.getCriterion(useAlias("USER_TABLE",ActivityPeer.NEW_USER_ID));
            if(c!=null)
            {
            	c.or(userIds4anyAndCreators);
            }
            else
            {
            	crit.add(userIds4anyAndCreators);
            }
        }
    }
        
    private void addIssueIdRangeCriteria(Criteria crit)
        throws java.lang.Exception
    {
        Issue.FederatedId minFid = null;
        Issue.FederatedId maxFid = null;

        if(minId != null)
        {
            minFid = new Issue.FederatedId(minId);
        }
        if(maxId != null)
        {
            maxFid = new Issue.FederatedId(maxId);
        }
        setDefaults(minFid, maxFid);

        if ( minFid != null
          && maxFid != null
          && (   minFid.getCount() > maxFid.getCount() 
              || !StringUtils.equals(minFid.getPrefix(), maxFid.getPrefix())
              || !StringUtils.equals(minFid.getDomain(), maxFid.getDomain())))
        {
            throw new ScarabException(L10NKeySet.ExceptionIncompatibleIssueIds, minId, maxId);
        }
        else 
        {
            addIssueIdCrit(crit, minFid, Criteria.GREATER_EQUAL );
            addIssueIdCrit(crit, maxFid, Criteria.LESS_EQUAL );
        }        
    }
        
    private void addIssueIdCrit(Criteria crit, Issue.FederatedId fId, SqlEnum comparisionType)
    {
        if (fId != null)
        {
            crit.and(IssuePeer.ID_COUNT, fId.getCount(), comparisionType)
                .and(IssuePeer.ID_DOMAIN, fId.getDomain())
                .and(IssuePeer.ID_PREFIX, fId.getPrefix());
        }
    }

    private void addAttributeOptionCriteria(Criteria crit)
        throws java.lang.Exception
    {
        List setAttValues = getSetAttributeValues();

        Map attrMap = new HashMap((int)(setAttValues.size()*1.25));
        for (int j=0; j<setAttValues.size(); j++) 
        {
            AttributeValue multiAV = (AttributeValue)setAttValues.get(j);
            if (multiAV instanceof OptionAttribute)
            {                
                //pull any chained values out to create a flat list
                List flatOptions = new ArrayList();
                List chainedValues = multiAV.getValueList();
                for (int i=0; i<chainedValues.size(); i++) 
                {
                    AttributeValue aval = (AttributeValue)chainedValues.get(i);
                    Integer optionId = aval.getOptionId();
                    if(optionId == null)
                    {
                        continue;
                    }
                    if (optionId.intValue() != 0) // Empty value is 0
                    {
                        buildOptionList(flatOptions, aval);
                    }
                    else
                    {
                        flatOptions.add(optionId);
                    }
                }

                Integer attributeId = multiAV.getAttributeId();
                attrMap.put(attributeId, flatOptions);
            }
        }

        for (Iterator i=attrMap.entrySet().iterator(); i.hasNext();) 
        {
            Map.Entry options4Attribute = (Map.Entry)i.next();
            List options = (List) options4Attribute.getValue();
            Integer attributeId = (Integer) options4Attribute.getKey();
            String alias = "av" + attributeId;
                        
            crit.addAlias(alias, AttributeValuePeer.TABLE_NAME);
            crit.addJoin(
                   IssuePeer.ISSUE_ID, 
                   useAlias(alias, AttributeValuePeer.ISSUE_ID), 
                   Criteria.INNER_JOIN
                )
                .and(useAlias(alias, AttributeValuePeer.DELETED), false)
                .and(useAlias(alias, AttributeValuePeer.ATTRIBUTE_ID), attributeId)
                .andIn(useAlias(alias, AttributeValuePeer.OPTION_ID), options );

            if (options.contains(NUMBERKEY_0)) //is 'empty' option selected?
            {                
                crit.or(useAlias(alias, AttributeValuePeer.OPTION_ID), Criteria.ISNULL);
            }
        }
    }

    /**
     * Get a List of Issues that match the criteria given by this
     * SearchIssue's searchWords and the quick search attribute values.
     * Perform a logical AND on partial queries (in text searches)
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getQueryResults()
        throws Exception
    {
    	List queryResults = Collections.EMPTY_LIST;            
        Criteria c = getSearchCriteria();
        if(c!=null)
        {
            queryResults = getQueryResults(c);
        }
        return queryResults;
    }

    public int getIssueCount()
        throws Exception
    {
        int count = 0;
        Criteria c = getSearchCriteria();
        if(c!=null)
        {
            count = (new CachedQuery(c, true)).getRowCount();
        }
        return count;
    }
    
    private Criteria getSearchCriteria()
        throws Exception
    {
        Criteria c = null;
    	if (isSearchAllowed) 
        {
            Long[] issueIdsMatchedText = getTextMatches();
            
            if (issueIdsMatchedText==null || issueIdsMatchedText.length > 0) 
            {            
            	c = getCoreSearchCriteria(issueIdsMatchedText);
            }
        }
        return c;
    }

    private List getQueryResults(Criteria c)
        throws Exception
    {
        return new QueryResultList(new CachedQuery(c, false));
    }

    /**
     * Allows setting the L10N tool for using when is needed to know
     * the user's locale (example, when parsing date parameters) 
     * @param l10nTool
     */
    public void setLocalizationTool(ScarabLocalizationTool l10nTool)
    {
        this.L10N = l10nTool;
    }
   
    public void addAttributeValue(Attribute attribute, String value)
    	throws TorqueException
    {
        AttributeValue av = AttributeValue.getNewInstance(attribute,searchIssue);
        av.setValue(value);
        searchIssue.addAttributeValue(av);
    }

    public AttributeValue addAttributeValue(Attribute attribute, AttributeOption option)
        throws TorqueException
    {
        AttributeValue av = AttributeValue.getNewInstance(attribute, searchIssue);
        av.setAttributeOption(option);
        searchIssue.addAttributeValue(av);
        return av;
    }
    
    public Module getModule()
    {
	    return singleModule;
    }

    public IssueType getIssueType()
    {
	    return singleIssueType;
    }

    public List getAttributeValues()
        throws TorqueException
    {
    	return searchIssue.getAttributeValues();
    }

    public void setQueryKey(String key)
        throws Exception
    {
    }

    public String getQueryKey()
    {
    	return "";
    }
    
    public void setMergePartialTextQueries(boolean mergePartialTextQueries)
    {
		this.mergePartialTextQueries = mergePartialTextQueries;
	}

    public boolean getMergePartialTextQueries()
    {
		return mergePartialTextQueries;
	}

	/**
	 * @return
	 * @throws TorqueException
	 */
	public List getSetAttributeValues() throws TorqueException {
		return removeUnsetValues(searchIssue.getAttributeValues());
	}
    
    private class QueryResultList extends AbstractList
    {
        private final CachedResultList queryResult;
        
        private QueryResultList(CachedQuery query)
        {            
            this.queryResult = (CachedResultList)query.getResults();
        }

        public int size()
        {
            return queryResult.size();
        }
        
        public Object get(int index)
        {
            Record next = (Record) queryResult.get(index);

            try
            {
	            Long issueId = new Long(next.getValue(1).asString());
	            Integer sortAttrId = getSortAttributeId();
	            Long sortValueId = null;
	            if(sortAttrId != null)
	            { 
	                String s = next.getValue(2).asString();
	                if(s!=null)
	                {
	                    sortValueId = new Long(s);	 
	                }                	 
	            }
	            return new QueryResult(issueId, issueListAttributeColumns, sortAttrId, sortValueId, L10N );
            }
            catch (DataSetException e)
            {
                throw new RuntimeException(e);	
            }
        }
    }
}