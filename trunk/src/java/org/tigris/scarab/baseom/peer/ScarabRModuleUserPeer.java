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

import org.tigris.scarab.baseom.ScarabRModuleUser;

// Local classes
import org.tigris.scarab.baseom.map.*;
import org.tigris.scarab.baseom.*;

/** This class was autogenerated by GenerateMapBuilder on: Mon Jan 08 11:17:17 PST 2001 */
public class ScarabRModuleUserPeer extends BasePeer
{
    /** the mapbuilder for this class */
    private static final ScarabRModuleUserMapBuilder mapBuilder = 
        (ScarabRModuleUserMapBuilder)getMapBuilder(ScarabRModuleUserMapBuilder.CLASS_NAME);

    /** the table name for this class */
    public static final String TABLE_NAME = mapBuilder.getTable();

    /** the column name for the MODULE_ID field */
    public static final String MODULE_ID = mapBuilder.getScarabRModuleUser_ModuleId();
    /** the column name for the USER_ID field */
    public static final String USER_ID = mapBuilder.getScarabRModuleUser_UserId();
    /** the column name for the DELETED field */
    public static final String DELETED = mapBuilder.getScarabRModuleUser_Deleted();

    /** number of columns for this peer */
    public static final int numColumns =  3;;

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
            criteria.addSelectColumn( USER_ID );
            criteria.addSelectColumn( DELETED );
        }


    /** Create a new object of type cls from a resultset row starting
      * from a specified offset.  This is done so that you can select
      * other rows than just those needed for this object.  You may
      * for example want to create two objects from the same row.
      */
    public static ScarabRModuleUser row2Object (Record row, int offset, Class cls ) throws Exception
    {
        ScarabRModuleUser obj = (ScarabRModuleUser)cls.newInstance();
                                        obj.setModuleId(row.getValue(offset+0).asInt());
                                            obj.setUserId(row.getValue(offset+1).asInt());
                                            obj.setDeleted
                (1 == row.getValue(offset+2).asInt());
                                        obj.setModified(false);
            obj.setNew(false);
                return obj;
    }

    /** Method to do selects */
    public static Vector doSelect( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabRModuleUser", null);
    }

    /** 
     * Method to do selects.  This method is to be used during a transaction,
     * otherwise use the doSelect(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Vector doSelect( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabRModuleUser", dbCon);
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
                                         selectCriteria.put( USER_ID, criteria.remove(USER_ID) );
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
                                         selectCriteria.put( USER_ID, criteria.remove(USER_ID) );
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
    public static void doInsert( ScarabRModuleUser obj ) throws Exception
    {
        obj.setPrimaryKey(doInsert(buildCriteria(obj)));
    }

    /**
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabRModuleUser obj) throws Exception
    {
        doUpdate(buildCriteria(obj));
    }
    /**
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabRModuleUser obj) throws Exception
    {
        doDelete(buildCriteria(obj));
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(ScarabRModuleUser) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to insert into the database.
     */
    public static void doInsert( ScarabRModuleUser obj, DBConnection dbCon) throws Exception
    {
        obj.setPrimaryKey(doInsert(buildCriteria(obj), dbCon));
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(ScarabRModuleUser) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabRModuleUser obj, DBConnection dbCon) throws Exception
    {
        doUpdate(buildCriteria(obj), dbCon);
    }
    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ScarabRModuleUser) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabRModuleUser obj, DBConnection dbCon) throws Exception
    {
        doDelete(buildCriteria(obj), dbCon);
    }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( ScarabRModuleUser obj )
    {
        Criteria criteria = new Criteria();
                            if ( !obj.isNew() )
            criteria.add( MODULE_ID, obj.getModuleId() );
                                        if ( !obj.isNew() )
            criteria.add( USER_ID, obj.getUserId() );
                                        criteria.add( DELETED, obj.getDeleted() );
                            return criteria;
    }

    /** 
     * Retrieve a single object by pk where multiple PK's are separated
     * by colons
     *
     * @param int module_id
     * @param int user_id
     */
    public static ScarabRModuleUser retrieveById(Object pkid) 
        throws Exception
    {
        StringTokenizer stok = new StringTokenizer((String)pkid, ":");
        if ( stok.countTokens() < 2 )
        {   
            throw new TurbineException(
                "id tokens did not match number of primary keys" );
        }
           int module_id = Integer.parseInt(stok.nextToken());;
           int user_id = Integer.parseInt(stok.nextToken());;

       return retrieveByPK(
             module_id
              , user_id
              );
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param int module_id
     * @param int user_id
     */
    public static ScarabRModuleUser retrieveByPK(
                      int module_id
                                      , int user_id
                                         ) throws Exception
    {
        Criteria criteria = new Criteria();
                       if( module_id > 0 )
                  criteria.add( ScarabRModuleUserPeer.MODULE_ID, module_id );
                            if( user_id > 0 )
                  criteria.add( ScarabRModuleUserPeer.USER_ID, user_id );
                          Vector ScarabRModuleUserVector = doSelect(criteria);
        if (ScarabRModuleUserVector.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabRModuleUser) ScarabRModuleUserVector.firstElement();
        }
    }


      
        
                       
     
          


   /**
    * selects a collection of ScarabRModuleUser objects pre-filled with their
    * ScarabModule objects.
    */
    public static Vector doSelectJoinScarabModule(Criteria c)
        throws Exception
    {
        addSelectColumns(c);
        int offset = numColumns + 1;
        ScarabModulePeer.addSelectColumns(c);

                                                  // check for conversion from boolean to int
        if ( c.containsKey(DELETED) )
        {
            Object possibleBoolean = c.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    c.add(DELETED, 1);
                }
                else
                {   
                    c.add(DELETED, 0);
                }
            }                     
         }
                      
        Vector rows = BasePeer.doSelect(c);
        Vector results = new Vector();

        for (int i=0; i<rows.size(); i++)
        {
            ScarabRModuleUser obj1 = 
                row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            1, Class.forName("org.tigris.scarab.baseom.ScarabRModuleUser") );
            ScarabModule obj2 = ScarabModulePeer
                .row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            offset, Class.forName("org.tigris.scarab.baseom.ScarabModule") );
            
            boolean newObject = true;
            for (int j=0; j<results.size(); j++)
            {
                ScarabRModuleUser temp_obj1 = (ScarabRModuleUser)results.elementAt(j);
                ScarabModule temp_obj2 = temp_obj1.getScarabModule();
                if ( temp_obj2.getPrimaryKey().equals(obj2.getPrimaryKey() ) )
                {
                    newObject = false;
                    temp_obj2.addScarabRModuleUsers(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initScarabRModuleUsers();
                obj2.addScarabRModuleUsers(obj1);
            }
            results.add(obj1);

        }

        return results;
    }
         
                       
     
          


   /**
    * selects a collection of ScarabRModuleUser objects pre-filled with their
    * TurbineUser objects.
    */
    public static Vector doSelectJoinTurbineUser(Criteria c)
        throws Exception
    {
        addSelectColumns(c);
        int offset = numColumns + 1;
        TurbineUserPeer.addSelectColumns(c);

                                                  // check for conversion from boolean to int
        if ( c.containsKey(DELETED) )
        {
            Object possibleBoolean = c.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    c.add(DELETED, 1);
                }
                else
                {   
                    c.add(DELETED, 0);
                }
            }                     
         }
                      
        Vector rows = BasePeer.doSelect(c);
        Vector results = new Vector();

        for (int i=0; i<rows.size(); i++)
        {
            ScarabRModuleUser obj1 = 
                row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            1, Class.forName("org.tigris.scarab.baseom.ScarabRModuleUser") );
            TurbineUser obj2 = TurbineUserPeer
                .row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            offset, Class.forName("org.tigris.scarab.baseom.TurbineUser") );
            
            boolean newObject = true;
            for (int j=0; j<results.size(); j++)
            {
                ScarabRModuleUser temp_obj1 = (ScarabRModuleUser)results.elementAt(j);
                TurbineUser temp_obj2 = temp_obj1.getTurbineUser();
                if ( temp_obj2.getPrimaryKey().equals(obj2.getPrimaryKey() ) )
                {
                    newObject = false;
                    temp_obj2.addScarabRModuleUsers(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initScarabRModuleUsers();
                obj2.addScarabRModuleUsers(obj1);
            }
            results.add(obj1);

        }

        return results;
    }
    

  


  
    /** 
     * Retrieve objects by fk
     *
     * @param int module_id
     */
//    public static Vector retrieveByModuleId(int module_id)
//    {
        
    
    /** 
     * Retrieve objects by fk
     *
     * @param int user_id
     */
//    public static Vector retrieveByUserId(int user_id)
//    {
        
    
}








