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
    private static final String className = "Attribute";

    private static Criteria allOptionsCriteria;

    /** should be cloned to use */
    private static Criteria moduleOptionsCriteria;

    private HashMap optionsMap;

    static
    {
        allOptionsCriteria = new Criteria();
        allOptionsCriteria.addOrderByColumn(AttributeOptionPeer.NUMERIC_VALUE);
        allOptionsCriteria.addOrderByColumn(AttributeOptionPeer.OPTION_NAME);

        moduleOptionsCriteria = new Criteria();
        moduleOptionsCriteria
            .addOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        moduleOptionsCriteria
            .addOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);
    }

    public Attribute()
    {
        /*
        try
        {            
            buildOptionsMap();
        }
        catch (Exception e)
        {
            Log.error("Unable to setup attribute "+getName()+"'s options", e);
        }
        */
    }

    static String getCacheKey(ObjectKey key)
    {
         String keyString = key.getValue().toString();
         return new StringBuffer(className.length() + keyString.length())
             .append(className).append(keyString).toString();
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
            attribute.buildOptionsMap();
            tgcs.addObject(key, new CachedObject(attribute));
        }
        
        return attribute;
    }

    /**
     * return the options (for attributes that have them).  They are put
     * into order by the numeric value.
     */
    public List getAllAttributeOptions()
        throws Exception
    {
        return getAttributeOptions(new Criteria());  
        // return getAttributeOptions(allOptionsCriteria);  
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
        HashMap optionsMap = new HashMap();
        List options = getAllAttributeOptions();

        for ( int i=options.size()-1; i>=0; i-- ) 
        {
            AttributeOption option = (AttributeOption)options.get(i);
            optionsMap.put(option.getOptionId(), option);
        }
        
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

    /**
     * Get options associated with this attribute base on 
     * specified criteria.
     *
     * @param c a <code>Criteria</code> value
     * @return a <code>Vector</code> value
     * @exception Exception if an error occurs
     */
    public Vector getAttributeOptions(Criteria c)
        throws Exception
    {
        return super.getAttributeOptions(c);
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

