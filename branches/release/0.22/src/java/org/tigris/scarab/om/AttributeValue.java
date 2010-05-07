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

// JDK classes
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;

import org.apache.commons.lang.ObjectUtils;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.attribute.DateAttribute;
import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Module;

/**
 * This class is for dealing with Issue Attribute Values
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public abstract class AttributeValue 
    extends BaseAttributeValue
    implements Persistent
{
    private ActivitySet activitySet;
    private Integer oldOptionId;
    private Integer oldUserId;
    private String oldValue;
    private Integer oldNumericValue;
    private boolean oldOptionIdIsSet;
    private boolean oldUserIdIsSet;
    private boolean oldValueIsSet;
    private boolean oldNumericValueIsSet;
    private AttributeValue chainedValue;
    
    private Activity saveActivity = null;

    
    /** Creates a new attribute. Do not do anything here.
     * All initialization should be performed in init().
     */
    protected AttributeValue()
    {
        oldOptionIdIsSet = false;
        oldUserIdIsSet = false;
        oldValueIsSet = false;
        oldNumericValueIsSet = false;
    }

    /**
     * Get the value of chainedValue.
     * @return value of chainedValue.
     */
    public AttributeValue getChainedValue() 
    {
        return chainedValue;
    }
    
    /**
     * Set the value of chainedValue.
     * @param v  Value to assign to chainedValue.
     */
    public void setChainedValue(final AttributeValue  v)
        throws TorqueException, ScarabException
    {
        if (v == null)
        {
            this.chainedValue = null;
        }
        else 
        {        
            if (v.getAttributeId() == null && getAttributeId() != null) 
            {
                v.setAttributeId(getAttributeId());
            }
            else if (v.getAttribute() != null 
                     && !v.getAttribute().equals(getAttribute()))
            {
                throw new ScarabException(L10NKeySet.ExceptionCantChainAttributeValues,
                                          v.getAttributeId(),
                                          getAttributeId());
            }
            
            if (v.getIssueId() == null && getIssueId() != null) 
            {
                v.setIssueId(getIssueId());
            }
            else if (v.getIssue() != null 
                      && !v.getIssue().equals(getIssue()))
            {
                throw new ScarabException(L10NKeySet.ExceptionCantChainIssues,
                                          v.getIssueId(),
                                          getIssueId());
            }

            if (this.chainedValue == null) 
            {
                this.chainedValue = v;
            }
            else 
            {
                chainedValue.setChainedValue(v);
            }
            
            if (activitySet != null) 
            {
                v.startActivitySet(activitySet);
            }        
        }
    }

    /**
     * This method returns a flat List of all AttributeValues that might
     * be chained to this one. This AttributeValue will be first in the List.
     *
     * @return a <code>List</code> of AttributeValue's
     */
    public List getValueList()
    {
        List list = new ArrayList();
        list.add(this);
        AttributeValue av = getChainedValue();
        while (av != null) 
        {
            list.add(av);
            av = av.getChainedValue();
        }
        return list;
    }

    /**
     * sets the AttributeId for this as well as any chained values.
     */
    public void setAttributeId(Integer nk)
        throws TorqueException
    {
        super.setAttributeId(nk);
        if (chainedValue != null) 
        {
            chainedValue.setAttributeId(nk);
        }
    }

    /**
     * sets the IssueId for this as well as any chained values.
     */
    public void setIssueId(Long nk)
        throws TorqueException
    {
        super.setIssueId(nk);
        if (chainedValue != null) 
        {
            chainedValue.setIssueId(nk);
        }
    }

    /**
     * Enters this attribute value into a activitySet.  All changes to a
     * value must occur within a activitySet.  The activitySet is cleared
     * once the attribute value is saved.
     *
     * @param activitySet a <code>ActivitySet</code> value
     * @exception ScarabException if a new activitySet is set before
     * the value is saved.
     */
    public void startActivitySet(ActivitySet activitySet)
        throws ScarabException, TorqueException
    {
        if (activitySet == null) 
        {
            throw new ScarabException(L10NKeySet.ExceptionCanNotStartActivitySet);
        }
        
        if (this.activitySet == null) 
        {
            this.activitySet = activitySet;
        }
        else
        {
            throw new ScarabException(L10NKeySet.ExceptionActivitySetInProgress);
        }
/*
This is wrong. It prevented the old/new value stuff from working properly!
If we have an existing issue and we change some attributes, then when the
history was created, the data was not valid in it for some reason. I'm not
quite sure why this was added. (JSS)

Leaving here so that John can remove or fix.

        oldOptionIdIsSet = false;
        oldValueIsSet = false;
        oldOptionId = null;
        oldValue = null;
*/

        // Check for previous active activities on this attribute 
        // If they exist, set old value for this activity
        List result = null;
        final Issue issue = getIssue();
        if (issue != null)
        {
            result = issue
                .getActivitiesWithNullEndDate(getAttribute());
        }
        if (result != null && result.size() > 0)
        {
            for (int i=0; i<result.size(); i++)
            {
                final Activity a = (Activity)result.get(i);
                oldOptionId = a.getNewOptionId();
                oldValue = a.getNewValue();
            }
        }
        if (chainedValue != null) 
        {
            chainedValue.startActivitySet(activitySet);
        }
    }
    
    private void endActivitySet()
    {
        this.activitySet = null;
        this.saveActivity = null;
        oldOptionId = null;
        oldValue = null;
        oldOptionIdIsSet = false;
        oldValueIsSet = false;
        if (chainedValue != null) 
        {
            chainedValue.endActivitySet();
        }
    }

    private void checkActivitySet(LocalizationKey l10nKey)
        throws ScarabException
    {
        if (activitySet == null) 
        {
            throw new ScarabException(l10nKey);
        }
    }

    public String getQueryKey()
    {
        String key = super.getQueryKey();
        if (key == null || key.length() == 0) 
        {
            try
            {
                key = "__" + getAttribute().getQueryKey();
            }
            catch (Exception e)
            {
                key = "";
            }
        }
        
        return key;
    }

    public boolean equals(Object obj)
    {
        boolean b = false;
        if (obj instanceof AttributeValue) 
        {
            b = super.equals(obj);
            if (!b) 
            {
                //FIXME! this code will equate 2 AttributeValues that are
                // multivalued, but are in a flat list.  
                AttributeValue aval = (AttributeValue)obj;
                b = (getChainedValue() == null) && 
                    ObjectUtils.equals(aval.getAttributeId(), getAttributeId())
                    && ObjectUtils.equals(aval.getIssueId(), getIssueId());
            }
        }
        return b;
    }

    public int hashCode()
    {
        int retVal = 0;

        if (getChainedValue() != null || getPrimaryKey() != null)
        {
            // get the hash code from the primary key
            // field from BaseObject
            retVal = super.hashCode(); 
        }
        else 
        {
            int issueHashCode = 0;
            if (getIssueId() != null) 
            {
                issueHashCode = getIssueId().hashCode();
            }
            retVal = getAttributeId().hashCode() ^ issueHashCode;
        }
        return retVal;
    }

    public String toString()
    {
        try
        {
            String s = '{' + super.toString() + ": " + getAttribute().getName();
            if (getOptionId() != null) 
            {
                s += " optionId=" + getOptionId();  
            }
            if (getUserId() != null) 
            {
                s += " userId=" + getUserId();  
            }
            if (getValue() != null) 
            {
                s += " value=" + getValue();  
            }
            
            return s + '}';
        }
        catch (Exception e)
        {
            return super.toString();
        }
    }

    /**
     * Get the OptionId
     * @return String
     */
    public String getOptionIdAsString()
    {
        String optionIdString = "";
        if (getOptionId() != null)
        {
            optionIdString = getOptionId().toString();
        }
        return optionIdString;
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * option easier
     *
     * @param optionId a <code>Integer</code> value
     */
    public void setOptionId(Integer optionId)
        throws TorqueException
    {
        if ( optionId != null ) 
        {
            Module module = getIssue().getModule();
            IssueType issueType = getIssue().getIssueType();
            if (module == null || issueType == null)
            {
                AttributeOption option = AttributeOptionManager
                    .getInstance(optionId);
                setValueOnly(option.getName());
            }
            else 
            {
                // FIXME! create a key and get the instance directly from
                // the manager.
                List options = null;

                options = module
                        .getRModuleOptions(getAttribute(), issueType);
                if(options != null)
                {
                    for (int i = options.size() - 1; i >= 0; i--)
                    {
                        RModuleOption option = (RModuleOption) options.get(i);
                        if (option.getOptionId().equals(optionId))
                        {
                            setValueOnly(option.getDisplayValue());
                            break;
                        }
                    }
                }
            }            
        }
        else
        {
            // any reason to set a option_id to null, once its already set?
            setValueOnly(null);
        }
        
        setOptionIdOnly(optionId);
    }

    /**
     * Makes sure to set the Value as well
     *
     * @param v
     */
    public void setNumericValue(Integer v)
    {        
        setValueOnly(String.valueOf(v));
        if (v != getNumericValue())
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldNumericValueIsSet) 
            {
                oldNumericValue = getNumericValue();
                oldNumericValueIsSet = true;
            }
            super.setNumericValue(v);
        }  
    }

    protected void setOptionIdOnly(Integer optionId)
        throws TorqueException
    {
        if (!ObjectUtils.equals(optionId, getOptionId()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldOptionIdIsSet && getOptionId() != null) 
            {
                oldOptionId = getOptionId();
                oldOptionIdIsSet = true;
            }
            super.setOptionId(optionId);
        }  
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * user easier
     *
     * @param userId a <code>Integer</code> value
     */
    public void setUserId(Integer userId)
        throws TorqueException
    {
        if (userId != null) 
        {
            ScarabUser user = ScarabUserManager.getInstance(userId);
            setValueOnly(user.getUserName());
        }
        else
        {
            // any reason to set a user_id to null, once its already set?
            setValueOnly(null);
        }

        setUserIdOnly(userId);
    }

    protected void setUserIdOnly(Integer value)
        throws TorqueException
    {
        if (!ObjectUtils.equals(value, getUserId()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldUserIdIsSet) 
            {
                oldUserId = getUserId();
                oldUserIdIsSet = true;
            }
            super.setUserId(value);
        }
    }

    /**
     * Not implemented always throws an exception
     *
     * @return a <code>Integer[]</code> value
     * @exception Exception if an error occurs
     */
    public Integer[] getOptionIds()
    {
        List optionIds = new ArrayList();
        if (getOptionId() != null) 
        {
            optionIds.add(getOptionId());
        }
        AttributeValue chainedAV = getChainedValue();
        while (chainedAV != null) 
        {
            if (chainedAV.getOptionId() != null) 
            {
                optionIds.add(chainedAV.getOptionId());
            }
            chainedAV = chainedAV.getChainedValue();
        }
        if (Log.get().isDebugEnabled()) 
        {
            Log.get().debug(this + " optionIds: " + optionIds);
        }
        
        return (Integer[])optionIds.toArray(new Integer[optionIds.size()]);
    }

    public void setOptionIds(final Integer[] ids)
        throws TorqueException, ScarabException
    {
        if (ids != null && ids.length > 0) 
        {
            setOptionId(ids[0]);
        }
        if (ids != null && ids.length > 1) 
        {
            for (int i=1; i<ids.length; i++) 
            {            
                final AttributeValue av = AttributeValue                
                    .getNewInstance(getAttributeId(), getIssue());
                setChainedValue(av);
                av.setOptionId(ids[i]);
            }
        }
    }

    /**
     * Not implemented always throws an exception
     *
     * @return a <code>Integer[]</code> value
     * @exception Exception if an error occurs
     */
    public Integer[] getUserIds()
        throws ScarabException
    {
        throw new ScarabException(L10NKeySet.ExceptionGetUserIdsNotImplemented);
    }

    public void setUserIds(final Integer[] ids)
        throws TorqueException, ScarabException
    {
        if (ids != null && ids.length > 0) 
        {
            setUserId(ids[0]);
        }
        if (ids != null && ids.length > 1) 
        {
            for (int i=1; i<ids.length; i++) 
            {            
                final AttributeValue av = AttributeValue                
                    .getNewInstance(getAttributeId(), getIssue());
                setChainedValue(av);
                av.setUserId(ids[i]);
            }
        }
    }

    public void setValue(String value)
    {
        setValueOnly(value);
    }

    protected void setValueOnly(String value)
    {
        if (!ObjectUtils.equals(value, getValue()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldValueIsSet) 
            {
                oldValue = getValue();
                oldValueIsSet = true;
            }
            super.setValue(value);
        }
    }

    public boolean isSet()
    {
        return !(getOptionId() == null && getValue() == null
                 && getUserId() == null);
    }

    public boolean isRequired()
       throws TorqueException, ScarabException
    {
        return getRModuleAttribute().getRequired();
    }

    public RModuleAttribute getRModuleAttribute()
        throws TorqueException, ScarabException
    {
        final Issue issue = getIssue();
        RModuleAttribute rma = null;
        if (issue != null)
        {
            final Module module = issue.getModule();
            if (module != null)
            {
                rma = module.getRModuleAttribute(
                    getAttribute(), getIssue().getIssueType());
            }
            else
            {
                throw new ScarabException (L10NKeySet.ExceptionGeneral,
                        "Module is null: Please report this issue."); //EXCEPTION
            }
        }
        else
        {
            throw new ScarabException (L10NKeySet.ExceptionGeneral,
                    "Issue is null: Please report this issue."); //EXCEPTION
        }
        return rma;
    }
    
    /**
     * Returns value for attribute in display format.
     * @param l10n : Instance of ScarabLocalizationTool.
     * @return : (Formatted) value of attribute.
     * @throws TorqueException
     */
	public String getDisplayValue(ScarabLocalizationTool l10n)
	    throws TorqueException
	{
		String displayValue = null;
		
		if(getAttribute().isOptionAttribute())
	    {
			displayValue = getIssue().getModule().getRModuleOption( getAttributeOption(), getIssue().getIssueType()).getDisplayValue();
			//displayValue = getRModuleAttribute().getDisplayValue();
	    }
		else if(getAttribute().isUserAttribute())
		{
			displayValue = getScarabUser().getUserName();
		}
		else if(getAttribute().isDateAttribute())
		{
			displayValue = DateAttribute.dateFormat(getValue(), L10NKeySet.ShortDateDisplay.getMessage(l10n));
		}
	    else
	    {
	    	displayValue = getValue();
	    }
		return displayValue;
	}


    public AttributeOption getAttributeOption()
        throws TorqueException
    {
        return getAttribute()
            .getAttributeOption(getOptionId());
    }

    /**
     * if the Attribute related to this value is marked as relevant
     * to quick search in the module related to the Issue
     * related to this value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isQuickSearchAttribute()
        throws TorqueException
    {
        boolean result = false;
        List qsAttributes = getIssue().getIssueType()
            .getQuickSearchAttributes(getIssue().getModule());
        for (int i=qsAttributes.size()-1; i>=0; i--) 
        {
            if (((Attribute)qsAttributes.get(i)).equals(getAttribute())) 
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new Attribute instance
     * @param rma the Attribute's rma
     * @param issue Issue object which this attribute is associated with
     */
    public static AttributeValue getNewInstance(
        RModuleAttribute rma, Issue issue) throws TorqueException
    {
        return getNewInstance(rma.getAttributeId(), issue);
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static AttributeValue getNewInstance(
        Integer attId, Issue issue) throws TorqueException
    {
        Attribute attribute = AttributeManager.getInstance(attId);
        return getNewInstance(attribute, issue);
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param attribute the Attribute
     * @param issue Issue object which this attributeValue is associated
     */
    public static synchronized AttributeValue getNewInstance(
        Attribute attribute, Issue issue) throws TorqueException
    {
        AttributeValue attv = null;
        try
        {
            String className = attribute
                .getAttributeType().getJavaClassName();
            attv = (AttributeValue)
                Class.forName(className).newInstance();
            attv.setAttribute(attribute);
            attv.setIssue(issue);
    
            attv.init();
        }
        catch (Exception e)
        {
            throw new TorqueException(e); //EXCEPTION
        }
        return attv;
    }

    /** 
     * Loads from database data specific for this Attribute including Name.
     * These are data common to all Attribute instances with same id.
     * Data retrieved here will then be used in setResources.
     * @return Object containing Attribute resources which will be 
     *         used in setResources.
     */
    protected abstract Object loadResources() throws TorqueException;
    
    /** 
     * This method is used by an Attribute instance to obtain 
     * specific resources such as option list for SelectOneAttribute.
     * It may, for example put them into instance variables. Attributes
     * may use common resources as-is or create it's own resources
     * based on common, it should not, however, modify common resources
     * since they will be used by other Attribute instances.
     *
     * @param resources Resources common for Attributes with the specified id.
     */
    protected abstract void setResources(Object resources);
        
    /** Override this method if you need any initialization for this attr.
     * @throws TorqueException Generic Exception
     */
    public abstract void init() throws TorqueException;
    
    public boolean supportsVoting()
    {
        return false;
    }
    
    public AttributeValue copy() throws TorqueException
    {
        AttributeValue copyObj = AttributeValue
            .getNewInstance(getAttributeId(), getIssue());
        return copyInto(copyObj);
    }
    
    public AttributeValue copy(Connection con) throws TorqueException 
    {
        throw new RuntimeException("Unimplemented method AttributeValue:copy(Connection conn)");
        //return copy();
    }
    

    public void save(Connection dbcon)
        throws TorqueException
    {
        if (isModified() && !getAttribute().isUserAttribute())
        {
            try
            {
                checkActivitySet(L10NKeySet.ExceptionCanNotSaveAttributeValue);
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
            if (saveActivity == null)
            {            
                if (getDeleted())
                {
                    saveActivity = ActivityManager
                        .create(getIssue(), getAttribute(), activitySet, 
                                ActivityType.ATTRIBUTE_CHANGED, null, null, getNumericValue(), ScarabConstants.INTEGER_0,
                                getUserId(), null, getOptionId(), null, 
                                getValue(), null, dbcon);
                }
                else
                {
                    saveActivity = ActivityManager
                        .create(getIssue(), getAttribute(), activitySet, 
                                ActivityType.ATTRIBUTE_CHANGED, null, null, oldNumericValue, getNumericValue(),
                                oldUserId, getUserId(), oldOptionId, getOptionId(), 
                                oldValue, getValue(), dbcon);
                }
            }
        }
        super.save(dbcon);
        if (chainedValue != null) 
        {
            chainedValue.save(dbcon);
        }
        endActivitySet();
    }

    /**
     * Gets the Activity record associated with this AttributeValue
     * It can only be retrieved after the save() method has been called 
     * since that is when it is generated.
     */
    public Activity getActivity()
    {
        return this.saveActivity;
    }

    public void setActivity(Activity activity)
    {
        this.saveActivity = activity;
    }

    /**
     * Sets the properties of one attribute value based on another 
     * NOTE: Does not copy the deleted field
     */
    public void setProperties(final AttributeValue attVal1)
        throws TorqueException
    {
        setAttribute(attVal1.getAttribute());
        setIssue(attVal1.getIssue());
        setNumericValue(attVal1.getNumericValue());
        setOptionId(attVal1.getOptionId());
        setUserId(attVal1.getUserId());
        setValue(attVal1.getValue());
    }
}