package org.tigris.scarab.baseom.map;

// JDK classes
import java.util.*;
import java.math.*;

// Turbine classes
import org.apache.turbine.services.db.PoolBrokerService;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.util.db.map.MapBuilder;
import org.apache.turbine.util.db.map.DatabaseMap;
import org.apache.turbine.util.db.map.TableMap;

/** This class was autogenerated by GenerateMapBuilder on: Mon Jan 08 11:17:17 PST 2001 */
public class ScarabAttributeTypeMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.ScarabAttributeTypeMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "SCARAB_ATTRIBUTE_TYPE";
    }


    /** SCARAB_ATTRIBUTE_TYPE.ATTRIBUTE_TYPE_ID */
    public static String getScarabAttributeType_AttributeTypeId()
    {
        return getTable() + ".ATTRIBUTE_TYPE_ID";
    }

    /** SCARAB_ATTRIBUTE_TYPE.ATTRIBUTE_CLASS_ID */
    public static String getScarabAttributeType_ClassId()
    {
        return getTable() + ".ATTRIBUTE_CLASS_ID";
    }

    /** SCARAB_ATTRIBUTE_TYPE.ATTRIBUTE_TYPE_NAME */
    public static String getScarabAttributeType_Name()
    {
        return getTable() + ".ATTRIBUTE_TYPE_NAME";
    }

    /** SCARAB_ATTRIBUTE_TYPE.JAVA_CLASS_NAME */
    public static String getScarabAttributeType_JavaClassName()
    {
        return getTable() + ".JAVA_CLASS_NAME";
    }


    /**  the database map  */
    private DatabaseMap dbMap = null;

    /**
        tells us if this DatabaseMapBuilder is built so that we don't have
        to re-build it every time
    */
    public boolean isBuilt()
    {
        if ( dbMap != null )
            return true;
        return false;
    }

    /**  gets the databasemap this map builder built.  */
    public DatabaseMap getDatabaseMap()
    {
        return this.dbMap;
    }
    /** the doBuild() method builds the DatabaseMap */
    public void doBuild ( ) throws Exception
    {
        dbMap = TurbineDB.getDatabaseMap("default");

        dbMap.addTable(getTable());
        TableMap tMap = dbMap.getTable(getTable());

        tMap.setPrimaryKeyMethod(tMap.IDBROKERTABLE);



                  tMap.addPrimaryKey ( getScarabAttributeType_AttributeTypeId(), new Integer(0) );
          
                  tMap.addForeignKey ( getScarabAttributeType_ClassId(), new Integer(0) , "SCARAB_ATTRIBUTE_CLASS" , "ATTRIBUTE_CLASS_ID" );
          
                  tMap.addColumn ( getScarabAttributeType_Name(), new String() );
          
                  tMap.addColumn ( getScarabAttributeType_JavaClassName(), new String() );
          
    }

}
