package org.tigris.scarab.baseom.peer;

// JDK classes
import java.util.*;

// Village classes
import com.workingdogs.village.*;

// Turbine classes
import org.apache.turbine.om.peer.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.db.map.*;
import org.apache.turbine.util.db.pool.DBConnection;

import org.tigris.scarab.baseom.ScarabModule;

// Local classes
import org.tigris.scarab.baseom.map.*;
import org.tigris.scarab.baseom.*;

/** This class was autogenerated by GenerateMapBuilder on: Tue Jan 02 21:50:40 PST 2001 */
public class ScarabModulePeer extends BasePeer
{
    /** the mapbuilder for this class */
    private static final ScarabModuleMapBuilder mapBuilder = 
        (ScarabModuleMapBuilder)getMapBuilder(ScarabModuleMapBuilder.CLASS_NAME);

    /** the table name for this class */
    public static final String TABLE_NAME = mapBuilder.getTable();

    /** the column name for the MODULE_ID field */
    public static final String MODULE_ID = mapBuilder.getScarabModule_ModuleId();
    /** the column name for the MODULE_NAME field */
    public static final String MODULE_NAME = mapBuilder.getScarabModule_Name();
    /** the column name for the MODULE_DESCRIPTION field */
    public static final String MODULE_DESCRIPTION = mapBuilder.getScarabModule_Description();
    /** the column name for the MODULE_URL field */
    public static final String MODULE_URL = mapBuilder.getScarabModule_Url();
    /** the column name for the PARENT_ID field */
    public static final String PARENT_ID = mapBuilder.getScarabModule_ParentId();
    /** the column name for the OWNER_ID field */
    public static final String OWNER_ID = mapBuilder.getScarabModule_OwnerId();
    /** the column name for the QA_CONTACT_ID field */
    public static final String QA_CONTACT_ID = mapBuilder.getScarabModule_QaContactId();
    /** the column name for the DELETED field */
    public static final String DELETED = mapBuilder.getScarabModule_Deleted();

    /** number of columns for this peer */
    public static final int numColumns =  8;;

    /** Method to do inserts */
    public static Object doInsert( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                        // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                      return BasePeer.doInsert( criteria );
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Object doInsert( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                        // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                      return BasePeer.doInsert( criteria, dbCon );
    }

    /** Add all the columns needed to create a new object */
    public static void addSelectColumns (Criteria criteria) throws Exception
    {
            criteria.addSelectColumn( MODULE_ID );
            criteria.addSelectColumn( MODULE_NAME );
            criteria.addSelectColumn( MODULE_DESCRIPTION );
            criteria.addSelectColumn( MODULE_URL );
            criteria.addSelectColumn( PARENT_ID );
            criteria.addSelectColumn( OWNER_ID );
            criteria.addSelectColumn( QA_CONTACT_ID );
            criteria.addSelectColumn( DELETED );
        }


    /** Create a new object of type cls from a resultset row starting
      * from a specified offset.  This is done so that you can select
      * other rows than just those needed for this object.  You may
      * for example want to create two objects from the same row.
      */
    public static ScarabModule row2Object (Record row, int offset, Class cls ) throws Exception
    {
        ScarabModule obj = (ScarabModule)cls.newInstance();
                                        obj.setModuleId(row.getValue(offset+0).asInt());
                                            obj.setName(row.getValue(offset+1).asString());
                                            obj.setDescription(row.getValue(offset+2).asString());
                                            obj.setUrl(row.getValue(offset+3).asString());
                                            obj.setParentId(row.getValue(offset+4).asInt());
                                            obj.setOwnerId(row.getValue(offset+5).asInt());
                                            obj.setQaContactId(row.getValue(offset+6).asInt());
                                            obj.setDeleted
                (1 == row.getValue(offset+7).asInt());
                                        obj.setModified(false);
            obj.setNew(false);
                return obj;
    }

    /** Method to do selects */
    public static Vector doSelect( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabModule", null);
    }

    /** 
     * Method to do selects.  This method is to be used during a transaction,
     * otherwise use the doSelect(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Vector doSelect( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabModule", dbCon);
    }

    /** Method to do selects. The returned vector will have object
      * of className
      */
    public static Vector doSelect( Criteria criteria, String className, DBConnection dbCon) throws Exception
    {
        addSelectColumns ( criteria );

                                                                                                                        // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
              
        // BasePeer returns a Vector of Value (Village) arrays.  The array
        // order follows the order columns were placed in the Select clause.
        Vector rows = null;
        if (dbCon == null)
        {
            rows = BasePeer.doSelect(criteria);
        }
        else
        {
            rows = BasePeer.doSelect(criteria, dbCon);
        }
        Vector results = new Vector();

        // populate the object(s)
        for ( int i=0; i<rows.size(); i++ )
        {
            Record row = (Record)rows.elementAt(i);
            results.add (row2Object (row,1,Class.forName (className)));
         }
         return results;
    }

    /**
     * Method to do updates. 
     *
     * @param Criteria object containing data that is used to create the UPDATE statement.
     */
    public static void doUpdate(Criteria criteria) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        Criteria selectCriteria = new Criteria(2);
                                selectCriteria.put( MODULE_ID, criteria.remove(MODULE_ID) );
                                                                                                                                                                         // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) ) 
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                                BasePeer.doUpdate( selectCriteria, criteria );
    }

    /** 
     * Method to do updates.  This method is to be used during a transaction,
     * otherwise use the doUpdate(Criteria) method.  It will take care of 
     * the connection details internally. 
     *
     * @param Criteria object containing data that is used to create the UPDATE statement.
     */
    public static void doUpdate(Criteria criteria, DBConnection dbCon) throws Exception
    {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
         Criteria selectCriteria = new Criteria(2);
                                selectCriteria.put( MODULE_ID, criteria.remove(MODULE_ID) );
                                                                                                                                                                         // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                                BasePeer.doUpdate( selectCriteria, criteria, dbCon );
     }

    /** 
     * Method to do deletes.
     *
     * @param Criteria object containing data that is used DELETE from database.
     */
     public static void doDelete(Criteria criteria) throws Exception
     {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                        // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                       BasePeer.doDelete ( criteria );
     }

    /** 
     * Method to do deletes.  This method is to be used during a transaction,
     * otherwise use the doInsert(Criteria) method.  It will take care of 
     * the connection details internally. 
     *
     * @param Criteria object containing data that is used DELETE from database.
     */
     public static void doDelete(Criteria criteria, DBConnection dbCon) throws Exception
     {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                        // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                       BasePeer.doDelete ( criteria, dbCon );
     }

    /** Method to do inserts */
    public static void doInsert( ScarabModule obj ) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj)));
    }

    /**
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabModule obj) throws Exception
    {
        doUpdate(buildCriteria(obj));
    }
    /**
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabModule obj) throws Exception
    {
        doDelete(buildCriteria(obj));
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(ScarabModule) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to insert into the database.
     */
    public static void doInsert( ScarabModule obj, DBConnection dbCon) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj), dbCon));
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(ScarabModule) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabModule obj, DBConnection dbCon) throws Exception
    {
        doUpdate(buildCriteria(obj), dbCon);
    }
    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ScarabModule) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabModule obj, DBConnection dbCon) throws Exception
    {
        doDelete(buildCriteria(obj), dbCon);
    }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( ScarabModule obj )
    {
        Criteria criteria = new Criteria();
                            if ( !obj.isNew() )
            criteria.add( MODULE_ID, obj.getModuleId() );
                                        criteria.add( MODULE_NAME, obj.getName() );
                                        criteria.add( MODULE_DESCRIPTION, obj.getDescription() );
                                        criteria.add( MODULE_URL, obj.getUrl() );
                                        criteria.add( PARENT_ID, obj.getParentId() );
                                        criteria.add( OWNER_ID, obj.getOwnerId() );
                                        criteria.add( QA_CONTACT_ID, obj.getQaContactId() );
                                        criteria.add( DELETED, obj.getDeleted() );
                            return criteria;
    }

    /** 
     * Retrieve a single object by pk where multiple PK's are separated
     * by colons
     *
     * @param int module_id
     */
    public static ScarabModule retrieveById(Object id) 
        throws Exception
    {
        StringTokenizer stok = new StringTokenizer((String)id, ":");
        if ( stok.countTokens() < 1 )
        {   
            throw new TurbineException(
                "id tokens did not match number of primary keys" );
        }
           int module_id = Integer.parseInt(stok.nextToken());;

       return retrieveByPK(
             module_id
              );
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param int module_id
     */
    public static ScarabModule retrieveByPK(
                      int module_id
                                                                                                                 ) throws Exception
    {
        Criteria criteria = new Criteria();
                       if( module_id > 0 )
                  criteria.add( ScarabModulePeer.MODULE_ID, module_id );
                                                                                Vector ScarabModuleVector = doSelect(criteria);
        if (ScarabModuleVector.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabModule) ScarabModuleVector.firstElement();
        }
    }


     
          

  


          
    /** 
     * Retrieve objects by fk
     *
     * @param int parent_id
     */
//    public static Vector retrieveByParentId(int parent_id)
//    {
        
        
}








