package org.tigris.scarab.baseom.peer;

// JDK classes
import java.util.*;
import java.math.*;

// Village classes
import com.workingdogs.village.*;

// Turbine classes
import org.apache.turbine.om.peer.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.db.map.*;
import org.apache.turbine.util.db.pool.DBConnection;

import org.tigris.scarab.baseom.ScarabIssue;

// Local classes
import org.tigris.scarab.baseom.map.*;
import org.tigris.scarab.baseom.*;

/** This class was autogenerated by GenerateMapBuilder on: Wed Feb 07 17:08:09 PST 2001 */
public abstract class BaseScarabIssuePeer extends BasePeer
{
    /** the mapbuilder for this class */
    private static final ScarabIssueMapBuilder mapBuilder = 
        (ScarabIssueMapBuilder)getMapBuilder(ScarabIssueMapBuilder.CLASS_NAME);

    /** the table name for this class */
    public static final String TABLE_NAME = mapBuilder.getTable();

    /** the column name for the ISSUE_ID field */
    public static final String ISSUE_ID = mapBuilder.getScarabIssue_IssueId();
    /** the column name for the MODULE_ID field */
    public static final String MODULE_ID = mapBuilder.getScarabIssue_ModuleId();
    /** the column name for the MODIFIED_BY field */
    public static final String MODIFIED_BY = mapBuilder.getScarabIssue_ModifiedBy();
    /** the column name for the CREATED_BY field */
    public static final String CREATED_BY = mapBuilder.getScarabIssue_CreatedBy();
    /** the column name for the MODIFIED_DATE field */
    public static final String MODIFIED_DATE = mapBuilder.getScarabIssue_ModifiedDate();
    /** the column name for the CREATED_DATE field */
    public static final String CREATED_DATE = mapBuilder.getScarabIssue_CreatedDate();
    /** the column name for the DELETED field */
    public static final String DELETED = mapBuilder.getScarabIssue_Deleted();

    /** number of columns for this peer */
    public static final int numColumns =  7;;

    /** Method to do inserts */
    public static Object doInsert( Criteria criteria ) throws Exception
    {
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
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
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
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
            criteria.addSelectColumn( ISSUE_ID );
            criteria.addSelectColumn( MODULE_ID );
            criteria.addSelectColumn( MODIFIED_BY );
            criteria.addSelectColumn( CREATED_BY );
            criteria.addSelectColumn( MODIFIED_DATE );
            criteria.addSelectColumn( CREATED_DATE );
            criteria.addSelectColumn( DELETED );
        }


    /** Create a new object of type cls from a resultset row starting
      * from a specified offset.  This is done so that you can select
      * other rows than just those needed for this object.  You may
      * for example want to create two objects from the same row.
      */
    public static ScarabIssue row2Object (Record row, int offset, Class cls ) throws Exception
    {
        ScarabIssue obj = (ScarabIssue)cls.newInstance();
                                        obj.setIssueId(row.getValue(offset+0).asInt());
                                            obj.setModuleId(row.getValue(offset+1).asInt());
                                            obj.setModifiedBy(row.getValue(offset+2).asInt());
                                            obj.setCreatedBy(row.getValue(offset+3).asInt());
                                            obj.setModifiedDate(row.getValue(offset+4).asDate());
                                            obj.setCreatedDate(row.getValue(offset+5).asDate());
                                            obj.setDeleted
                (1 == row.getValue(offset+6).asInt());
                                        obj.setModified(false);
                obj.setNew(false);
        return obj;
    }

    /** Method to do selects */
    public static Vector doSelect( Criteria criteria ) throws Exception
    {
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabIssue", null);
    }

    /** 
     * Method to do selects.  This method is to be used during a transaction,
     * otherwise use the doSelect(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Vector doSelect( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabIssue", dbCon);
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
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
        Criteria selectCriteria = new
            Criteria(mapBuilder.getDatabaseMap().getName(), 2);
                                selectCriteria.put( ISSUE_ID, criteria.remove(ISSUE_ID) );
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
        criteria.setDbName(mapBuilder.getDatabaseMap().getName());
        Criteria selectCriteria = new
            Criteria(mapBuilder.getDatabaseMap().getName(), 2);
                                selectCriteria.put( ISSUE_ID, criteria.remove(ISSUE_ID) );
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
         criteria.setDbName(mapBuilder.getDatabaseMap().getName());
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
         criteria.setDbName(mapBuilder.getDatabaseMap().getName());
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
    public static void doInsert( ScarabIssue obj ) throws Exception
    {
                obj.setPrimaryKey(doInsert(buildCriteria(obj)));
                obj.setNew(false);
    }

    /**
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabIssue obj) throws Exception
    {
        doUpdate(buildCriteria(obj));
    }
    /**
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabIssue obj) throws Exception
    {
        doDelete(buildCriteria(obj));
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(ScarabIssue) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to insert into the database.
     */
    public static void doInsert( ScarabIssue obj, DBConnection dbCon) throws Exception
    {
                obj.setPrimaryKey(doInsert(buildCriteria(obj), dbCon));
                obj.setNew(false);
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(ScarabIssue) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabIssue obj, DBConnection dbCon) throws Exception
    {
        doUpdate(buildCriteria(obj), dbCon);
    }
    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ScarabIssue) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabIssue obj, DBConnection dbCon) throws Exception
    {
        doDelete(buildCriteria(obj), dbCon);
    }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( ScarabIssue obj )
    {
        Criteria criteria = new Criteria();
                            if ( !obj.isNew() )
	                criteria.add( ISSUE_ID, obj.getIssueId() );
                                criteria.add( MODULE_ID, obj.getModuleId() );
                                criteria.add( MODIFIED_BY, obj.getModifiedBy() );
                                criteria.add( CREATED_BY, obj.getCreatedBy() );
                                criteria.add( MODIFIED_DATE, obj.getModifiedDate() );
                                criteria.add( CREATED_DATE, obj.getCreatedDate() );
                                criteria.add( DELETED, obj.getDeleted() );
                return criteria;
    }

    /** 
     * Retrieve a single object by pk where multiple PK's are separated
     * by colons
     *
     * @param int issue_id
     */
    public static ScarabIssue retrieveById(Object pkid) 
        throws Exception
    {
        StringTokenizer stok = new StringTokenizer((String)pkid, ":");
        if ( stok.countTokens() < 1 )
        {   
            throw new TurbineException(
                "id tokens did not match number of primary keys" );
        }
           int issue_id = Integer.parseInt(stok.nextToken());;

       return retrieveByPK(
             issue_id
              );
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param int issue_id
     */
    public static ScarabIssue retrieveByPK(
                      int issue_id
                                                                                                     ) throws Exception
    {
        Criteria criteria = new Criteria();
                       if( issue_id > 0 )
                  criteria.add( ScarabIssuePeer.ISSUE_ID, issue_id );
                                                                       Vector ScarabIssueVector = doSelect(criteria);
        if (ScarabIssueVector.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabIssue) ScarabIssueVector.firstElement();
        }
    }


     
        
                       
     
          


   /**
    * selects a collection of ScarabIssue objects pre-filled with their
    * ScarabModule objects.
    */
    public static Vector doSelectJoinScarabModule(Criteria c)
        throws Exception
    {
         c.setDbName(mapBuilder.getDatabaseMap().getName());

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
            ScarabIssue obj1 = 
                row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            1, Class.forName("org.tigris.scarab.baseom.ScarabIssue") );
            ScarabModule obj2 = ScarabModulePeer
                .row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            offset, Class.forName("org.tigris.scarab.baseom.ScarabModule") );
            
            boolean newObject = true;
            for (int j=0; j<results.size(); j++)
            {
                ScarabIssue temp_obj1 = (ScarabIssue)results.elementAt(j);
                ScarabModule temp_obj2 = temp_obj1.getScarabModule();
                if ( temp_obj2.getPrimaryKey().equals(obj2.getPrimaryKey() ) )
                {
                    newObject = false;
                    temp_obj2.addScarabIssues(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initScarabIssues();
                obj2.addScarabIssues(obj1);
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
        
            
}








