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
public class ScarabAttachmentMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.ScarabAttachmentMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "SCARAB_ATTACHMENT";
    }


    /** SCARAB_ATTACHMENT.ATTACHMENT_ID */
    public static String getScarabAttachment_AttachmentId()
    {
        return getTable() + ".ATTACHMENT_ID";
    }

    /** SCARAB_ATTACHMENT.ISSUE_ID */
    public static String getScarabAttachment_IssueId()
    {
        return getTable() + ".ISSUE_ID";
    }

    /** SCARAB_ATTACHMENT.ATTACHMENT_TYPE_ID */
    public static String getScarabAttachment_TypeId()
    {
        return getTable() + ".ATTACHMENT_TYPE_ID";
    }

    /** SCARAB_ATTACHMENT.ATTACHMENT_NAME */
    public static String getScarabAttachment_Name()
    {
        return getTable() + ".ATTACHMENT_NAME";
    }

    /** SCARAB_ATTACHMENT.ATTACHMENT_DATA */
    public static String getScarabAttachment_Data()
    {
        return getTable() + ".ATTACHMENT_DATA";
    }

    /** SCARAB_ATTACHMENT.ATTACHMENT_FILE_PATH */
    public static String getScarabAttachment_FilePath()
    {
        return getTable() + ".ATTACHMENT_FILE_PATH";
    }

    /** SCARAB_ATTACHMENT.ATTACHMENT_MIME_TYPE */
    public static String getScarabAttachment_MimeType()
    {
        return getTable() + ".ATTACHMENT_MIME_TYPE";
    }

    /** SCARAB_ATTACHMENT.MODIFIED_BY */
    public static String getScarabAttachment_ModifiedBy()
    {
        return getTable() + ".MODIFIED_BY";
    }

    /** SCARAB_ATTACHMENT.CREATED_BY */
    public static String getScarabAttachment_CreatedBy()
    {
        return getTable() + ".CREATED_BY";
    }

    /** SCARAB_ATTACHMENT.MODIFIED_DATE */
    public static String getScarabAttachment_ModifiedDate()
    {
        return getTable() + ".MODIFIED_DATE";
    }

    /** SCARAB_ATTACHMENT.CREATED_DATE */
    public static String getScarabAttachment_CreatedDate()
    {
        return getTable() + ".CREATED_DATE";
    }

    /** SCARAB_ATTACHMENT.DELETED */
    public static String getScarabAttachment_Deleted()
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

        tMap.setPrimaryKeyMethod(tMap.IDBROKERTABLE);



                  tMap.addPrimaryKey ( getScarabAttachment_AttachmentId(), integer );
          
                  tMap.addForeignKey ( getScarabAttachment_IssueId(), integer , "SCARAB_ISSUE" , "ISSUE_ID" );
          
                  tMap.addForeignKey ( getScarabAttachment_TypeId(), integer , "SCARAB_ATTACHMENT_TYPE" , "ATTACHMENT_TYPE_ID" );
          
                  tMap.addColumn ( getScarabAttachment_Name(), string );
          
                  tMap.addColumn ( getScarabAttachment_Data(), string );
          
                  tMap.addColumn ( getScarabAttachment_FilePath(), string );
          
                  tMap.addColumn ( getScarabAttachment_MimeType(), string );
          
                  tMap.addColumn ( getScarabAttachment_ModifiedBy(), integer );
          
                  tMap.addColumn ( getScarabAttachment_CreatedBy(), integer );
          
                  tMap.addColumn ( getScarabAttachment_ModifiedDate(), date );
          
                  tMap.addColumn ( getScarabAttachment_CreatedDate(), date );
          
                  tMap.addColumn ( getScarabAttachment_Deleted(), string );
          
    }

}
