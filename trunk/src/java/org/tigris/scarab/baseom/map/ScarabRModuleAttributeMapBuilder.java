package org.tigris.scarab.baseom.map;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.services.db.PoolBrokerService;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.util.db.map.MapBuilder;
import org.apache.turbine.util.db.map.DatabaseMap;
import org.apache.turbine.util.db.map.TableMap;

/** This class was autogenerated by GenerateMapBuilder on: Fri Dec 15 13:47:21 PST 2000 */
public class ScarabRModuleAttributeMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.ScarabRModuleAttributeMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "SCARAB_R_MODULE_ATTRIBUTE";
    }


    /** SCARAB_R_MODULE_ATTRIBUTE.ATTRIBUTE_ID */
    public static String getScarabRModuleAttribute_AttributeId()
    {
        return getTable() + ".ATTRIBUTE_ID";
    }

    /** SCARAB_R_MODULE_ATTRIBUTE.MODULE_ID */
    public static String getScarabRModuleAttribute_ModuleId()
    {
        return getTable() + ".MODULE_ID";
    }

    /** SCARAB_R_MODULE_ATTRIBUTE.DELETED */
    public static String getScarabRModuleAttribute_Deleted()
    {
        return getTable() + ".DELETED";
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
        String string = new String("");
        Integer integer = new Integer(0);
        java.util.Date date = new Date();

        dbMap = TurbineDB.getDatabaseMap("default");

        dbMap.addTable(getTable());
        TableMap tMap = dbMap.getTable(getTable());

        tMap.setPrimaryKeyMethod(tMap.NONE);



                  tMap.addForeignPrimaryKey ( getScarabRModuleAttribute_AttributeId(), integer , "SCARAB_ATTRIBUTE" , "ATTRIBUTE_ID" );
          
                  tMap.addForeignPrimaryKey ( getScarabRModuleAttribute_ModuleId(), integer , "SCARAB_MODULE" , "MODULE_ID" );
          
                  tMap.addColumn ( getScarabRModuleAttribute_Deleted(), string );
          
    }

}
