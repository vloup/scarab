package org.tigris.scarab.baseom.map;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.services.db.PoolBrokerService;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.util.db.map.MapBuilder;
import org.apache.turbine.util.db.map.DatabaseMap;
import org.apache.turbine.util.db.map.TableMap;

/** This class was autogenerated by GenerateMapBuilder on: Tue Jan 02 21:50:40 PST 2001 */
public class ScarabIssueMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.ScarabIssueMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "SCARAB_ISSUE";
    }


    /** SCARAB_ISSUE.ISSUE_ID */
    public static String getScarabIssue_IssueId()
    {
        return getTable() + ".ISSUE_ID";
    }

    /** SCARAB_ISSUE.MODULE_ID */
    public static String getScarabIssue_ModuleId()
    {
        return getTable() + ".MODULE_ID";
    }

    /** SCARAB_ISSUE.MODIFIED_BY */
    public static String getScarabIssue_ModifiedBy()
    {
        return getTable() + ".MODIFIED_BY";
    }

    /** SCARAB_ISSUE.CREATED_BY */
    public static String getScarabIssue_CreatedBy()
    {
        return getTable() + ".CREATED_BY";
    }

    /** SCARAB_ISSUE.MODIFIED_DATE */
    public static String getScarabIssue_ModifiedDate()
    {
        return getTable() + ".MODIFIED_DATE";
    }

    /** SCARAB_ISSUE.CREATED_DATE */
    public static String getScarabIssue_CreatedDate()
    {
        return getTable() + ".CREATED_DATE";
    }

    /** SCARAB_ISSUE.DELETED */
    public static String getScarabIssue_Deleted()
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
        dbMap = TurbineDB.getDatabaseMap("default");

        dbMap.addTable(getTable());
        TableMap tMap = dbMap.getTable(getTable());

        tMap.setPrimaryKeyMethod(tMap.IDBROKERTABLE);



                  tMap.addPrimaryKey ( getScarabIssue_IssueId(), new Integer(0) );
          
                  tMap.addForeignKey ( getScarabIssue_ModuleId(), new Integer(0) , "SCARAB_MODULE" , "MODULE_ID" );
          
                  tMap.addColumn ( getScarabIssue_ModifiedBy(), new Integer(0) );
          
                  tMap.addColumn ( getScarabIssue_CreatedBy(), new Integer(0) );
          
                  tMap.addColumn ( getScarabIssue_ModifiedDate(), new Date() );
          
                  tMap.addColumn ( getScarabIssue_CreatedDate(), new Date() );
          
                  tMap.addColumn ( getScarabIssue_Deleted(), new Integer(0) );
          
    }

}
