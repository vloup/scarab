package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.services.db.om.*;
// import org.apache.turbine.services.db.om.peer.BasePeer;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.services.cache.*;
import org.apache.turbine.services.*;

import org.tigris.scarab.util.*;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public class Attribute 
    extends BaseAttribute
    implements Persistent
{
    public static final NumberKey STATUS__PK = new NumberKey("3");
    public static final NumberKey RESOLUTION__PK = new NumberKey("4");

    private static final String className = "Attribute";

    private static Criteria allOptionsCriteria;

    /** should be cloned to use */
    private static Criteria moduleOptionsCriteria;

    private HashMap optionsMap;
    private AttributeOption[] optionsArray;
    private AttributeOption[] currentOptions;
    private static HashMap optionAttributeMap = new HashMap();

    static
    {
        allOptionsCriteria = new Criteria();
        allOptionsCriteria.addAscendingOrderByColumn(AttributeOptionPeer.NUMERIC_VALUE);
        allOptionsCriteria.addAscendingOrderByColumn(AttributeOptionPeer.OPTION_NAME);

        moduleOptionsCriteria = new Criteria();
        moduleOptionsCriteria
            .addAscendingOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        moduleOptionsCriteria
            .addAscendingOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);
    }

    protected Attribute()
    {
    }

    static String getCacheKey(ObjectKey key)
    {
         String keyString = key.getValue().toString();
         return new StringBuffer(className.length() + keyString.length())
             .append(className).append(keyString).toString();
    }


    /**
     * A new Attribute
     */
    public static Attribute getInstance() 
    {
        return new Attribute();
    }

    /**
     * Return an instance of Attribute based on the passed in attribute id
     */
    public static Attribute getInstance(ObjectKey attId) 
        throws Exception
    {
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(attId);
        Attribute attribute = null;
        try
        {
            attribute = (Attribute)tgcs.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            attribute = AttributePeer.retrieveByPK(attId);
            if ( attribute == null) // is this check needed?
            {
                throw new ScarabException("Attribute with ID " + attId + 
                                          " can not be found");
            }
            if ( attribute.getAttributeType().getAttributeClass().getName()
                 .equals("select-one") ) 
            {
                attribute.buildOptionsMap();                
            }

            tgcs.addObject(key, new CachedObject(attribute));
        }
        
        return attribute;
    }

    public static Attribute getAttributeForOption(NumberKey optionId)
    {
        return (Attribute)optionAttributeMap.get(optionId);
    }

    /**
     * return the options (for attributes that have them).  They are put
     * into order by the numeric value.
     */
    public List getAllAttributeOptions()
        throws Exception
    {
        // return getAttributeOptions(new Criteria());  
        return getAttributeOptions(allOptionsCriteria);  
    }

    /**
     * Adds a new option.  The list is resorted.
     */
    public synchronized void addAttributeOption(AttributeOption option)
        throws Exception
    {        
        Vector v = getAttributeOptions();
        
        // Check that a duplicate name is not being added
        int size = v.size();
        for (int i=0; i<size; i++) 
        {
            AttributeOption opt = (AttributeOption)v.get(i);
            if ( option.getName()
                 .equalsIgnoreCase(opt.getName()) ) 
            {
                throw new ScarabException("Adding option " + 
                    option.getName() + 
                    " failed due to a non-unique name." );
            }
        }
        
        Vector sortedOptions = (Vector)v.clone();
        sortedOptions.add(option);
        option.setAttribute(this);
        sortOptions(sortedOptions);

    }

    public synchronized void buildOptionsMap()
        throws Exception
    {
        // synchronized method due to getAllAttributeOptions, this needs
        // further investigation !FIXME!
        List options = getAllAttributeOptions();
        HashMap optionsMap = new HashMap((int)(1.25*options.size()+1));
        AttributeOption[] optionsArray = new AttributeOption[options.size()];

        for ( int i=options.size()-1; i>=0; i-- ) 
        {
            AttributeOption option = (AttributeOption)options.get(i);
            optionsArray[i] = option;
            optionsMap.put(option.getOptionId(), option);
            optionAttributeMap.put(option.getOptionId(), this);
        }

        List optionsList = new ArrayList(optionsArray.length);
        for ( int i=0; i<optionsArray.length; i++ ) 
        {
            if ( !optionsArray[i].getDeleted() ) 
            {
                optionsList.add(optionsArray[i]);
            }
        }
        AttributeOption[] currentOptions = 
            new AttributeOption[optionsList.size()];
        optionsList.toArray(currentOptions);
        
        this.optionsArray = optionsArray;
        this.currentOptions = currentOptions;
        this.optionsMap = optionsMap;
    }

    /**
     * Gets one of the options belonging to this attribute. if the 
     * PrimaryKey does not belong to an option in this attribute
     * null is returned.
     *
     * @param pk a <code>NumberKey</code> value
     * @return an <code>AttributeOption</code> value
     */
    public AttributeOption getAttributeOption(NumberKey pk)
    {
        return (AttributeOption)optionsMap.get(pk);
    }

    public AttributeOption getAttributeOption(String optionID)
    {
        return getAttributeOption(new NumberKey(optionID));
    }

    public AttributeOption[] getAttributeOptions(boolean includeDeleted)
        throws Exception
    {
        if ( includeDeleted ) 
        {
            return optionsArray;
        }
        else 
        {
            return currentOptions; 
        }
    }

    /**
     * Sorts the options and renumbers any with duplicate numeric values
     */
    public synchronized void sortOptions(Vector v)
        throws Exception
    {
        Vector sortedOptions = (Vector)v.clone();
        Collections.sort( sortedOptions, AttributeOption.getComparator() );

        // set new numeric values in case any options duplicated
        // a numeric value
        int size = sortedOptions.size();
        for (int i=0; i<size; i++) 
        {
            AttributeOption opt = (AttributeOption)sortedOptions.get(i);
            opt.setNumericValue(i+1);
            opt.save();
        }

        collAttributeOptions = sortedOptions;
    }

    /**
     * Little method to return a List of all Attribute Type's.
     * It is here for convenience with regards to needing this
     * functionality from within a Template.
     */
    public static List getAllAttributeTypes()
        throws Exception
    {
        return AttributeTypePeer.doSelect(new Criteria());
    }
}

