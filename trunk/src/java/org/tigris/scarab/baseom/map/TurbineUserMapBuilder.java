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

/**
  *  This class was autogenerated by Torque on: 
  *
  * [Thu Feb 15 16:11:32 PST 2001]
  *
  */
public class TurbineUserMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.TurbineUserMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "TURBINE_USER";
    }


    /** TURBINE_USER.USER_ID */
    public static String getTurbineUser_UserId()
    {
        return getTable() + ".USER_ID";
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

        tMap.setPrimaryKeyMethod(TableMap.NONE);



                  tMap.addPrimaryKey ( getTurbineUser_UserId(), new Integer(0) );
          
    }

}
