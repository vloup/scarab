package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.*;
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;
import org.apache.turbine.services.cache.*;
import org.apache.turbine.services.*;


/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public abstract class AttributeValue 
    extends BaseAttributeValue
    implements Persistent
{
    
    /** Creates a new attribute. Do not do anything here.
     * All initialization should be performed in init().
     */
    protected AttributeValue()
    {
    }

    public String toString()
    {
        try{
            return getAttribute().getName() + "->" + super.toString();
        }catch (Exception e) {return "toString() threw Exception";}
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * option easier
     *
     * @param optionId a <code>NumberKey</code> value
     */
    public void setOptionId(NumberKey optionId)
        throws Exception
    {
        if ( optionId != null && optionId.getValue() != null ) 
        {
            List options = getIssue().getModule()
                .getRModuleOptions(getAttribute());
            for ( int i=options.size()-1; i>=0; i-- ) 
            {
                RModuleOption option = (RModuleOption)options.get(i);
                if ( option.getOptionId().equals(optionId) ) 
                {
                    setValue(option.getDisplayValue());
                    break;
                }
            }
        }

        super.setOptionId(optionId);
    }

    public boolean isRequired()
       throws Exception
    {
        RModuleAttribute rma = getIssue().getModule()
            .getRModuleAttribute(getAttribute());
        return rma.getRequired();
    }

    // need a local reference
    private Attribute aAttribute;                 
    public Attribute getAttribute() throws Exception
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = Attribute.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    public void setAttribute(Attribute v) throws Exception
    {
        aAttribute = v;
        super.setAttribute(v);
    }


    public AttributeOption getAttributeOption()
        throws Exception
    {
        return getAttribute().getAttributeOption(getOptionId());
    }
    //public abstract boolean isEquivalent(AttributeValue aval);


    /**
     * if the Attribute related to this value is marked as relevant
     * to checking for duplicates in the module related to the Issue
     * related to this value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isDedupeAttribute()
        throws Exception
    {
        boolean result = false;
        Attribute[] dedupeAttributes = getIssue().getModule()
            .getDedupeAttributes();
        for ( int i=dedupeAttributes.length-1; i>=0; i--) 
        {
            if ( dedupeAttributes[i].equals(getAttribute()) ) 
            {
                result = true;
                break;
            }
        }
        
        return result;
    }

    /**
     * if the Attribute related to this value is marked as relevant
     * to quick search in the module related to the Issue
     * related to this value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isQuickSearchAttribute()
        throws Exception
    {
        boolean result = false;
        Attribute[] qsAttributes = getIssue().getModule()
            .getQuickSearchAttributes();
        for ( int i=qsAttributes.length-1; i>=0; i--) 
        {
            if ( qsAttributes[i].equals(getAttribute()) ) 
            {
                result = true;
                break;
            }
        }
        
        return result;
    }

    private static String className = "AttributeValue";
    static String getCacheKey(ObjectKey key)
    {
        String keyString = key.getValue().toString();
        return new StringBuffer(className.length() + keyString.length())
            .append(className).append(keyString).toString();
    }


    /** Creates, initializes and returns a new AttributeValue.
     * @return new Attribute instance
     * @param issue Issue object which this attribute is associated with
     * @param intId This Attribute's Id
     */
    public static AttributeValue getNewInstance(
        RModuleAttribute rma, Issue issue) throws Exception
    {
        return getNewInstance(rma.getAttributeId(), issue);
    }

    /** Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static AttributeValue getNewInstance(
        ObjectKey attId, Issue issue) throws Exception
    {
        Attribute attribute = Attribute.getInstance(attId);
        return getNewInstance(attribute, issue);
    }

    /** Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static synchronized AttributeValue getNewInstance(
        Attribute attribute, Issue issue) throws Exception
    {
        String className = attribute
            .getAttributeType().getJavaClassName();
        AttributeValue attv = (AttributeValue)
            Class.forName(className).newInstance();
        attv.setAttribute(attribute);
        attv.setIssue(issue);

        String key = getCacheKey(attribute.getPrimaryKey());
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        Object resources = null;
        try
        {
            resources = tgcs.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            resources = attv.loadResources();
            tgcs.addObject(key, new CachedObject(resources));
        }

        attv.setResources(resources);
        attv.init();
        return attv;
    }

    /** 
     * Loads from database data specific for this Attribute including Name.
     * These are data common to all Attribute instances with same id.
     * Data retrieved here will then be used in setResources.
     * @return Object containing Attribute resources which will be 
     *         used in setResources.
     */
    protected abstract Object loadResources() throws Exception;
    
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
     * @throws Exception Generic Exception
     */
    public abstract void init() throws Exception;
    
    public boolean supportsVoting()
    {
        return false;
    }
    
    public AttributeValue copy() throws Exception
    {
        AttributeValue copyObj = AttributeValue
            .getNewInstance(getAttributeId(), getIssue());
        return copyInto(copyObj);
    }
}



