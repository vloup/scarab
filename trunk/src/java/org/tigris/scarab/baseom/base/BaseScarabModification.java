package org.tigris.scarab.baseom.base;


// JDK classes
import java.util.*;
import java.math.*;

// Turbine classes
import org.apache.turbine.om.*;
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

import org.tigris.scarab.baseom.*;
import org.tigris.scarab.baseom.peer.*;

/** 
 * This class was autogenerated by Torque on:
 *
 * [Thu Feb 15 16:11:32 PST 2001]
 *
 * You should not use this class directly.  It should not even be
 * extended all references should be to ScarabModification 
 */
public abstract class BaseScarabModification extends BaseObject
{
    /** the value for the table_id field */
    private int table_id;
    /** the value for the column_id field */
    private int column_id;
    /** the value for the modified_by field */
    private int modified_by;
    /** the value for the created_by field */
    private int created_by;
    /** the value for the modified_date field */
    private Date modified_date;
    /** the value for the created_date field */
    private Date created_date;


    /**
     * Get the TableId
     * @return int
     */
     public int getTableId()
     {
          return table_id;
     }

        
    /**
     * Set the value of TableId
     */
     public void setTableId(int v ) 
     {
  
  

           if (this.table_id != v)
           {
              this.table_id = v;
              setModified(true);
          }
     }
    /**
     * Get the ColumnId
     * @return int
     */
     public int getColumnId()
     {
          return column_id;
     }

        
    /**
     * Set the value of ColumnId
     */
     public void setColumnId(int v ) 
     {
  
  

           if (this.column_id != v)
           {
              this.column_id = v;
              setModified(true);
          }
     }
    /**
     * Get the ModifiedBy
     * @return int
     */
     public int getModifiedBy()
     {
          return modified_by;
     }

        
    /**
     * Set the value of ModifiedBy
     */
     public void setModifiedBy(int v ) 
     {
  
  

           if (this.modified_by != v)
           {
              this.modified_by = v;
              setModified(true);
          }
     }
    /**
     * Get the CreatedBy
     * @return int
     */
     public int getCreatedBy()
     {
          return created_by;
     }

        
    /**
     * Set the value of CreatedBy
     */
     public void setCreatedBy(int v ) 
     {
  
  

           if (this.created_by != v)
           {
              this.created_by = v;
              setModified(true);
          }
     }
    /**
     * Get the ModifiedDate
     * @return Date
     */
     public Date getModifiedDate()
     {
          return modified_date;
     }

        
    /**
     * Set the value of ModifiedDate
     */
     public void setModifiedDate(Date v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.modified_date, v) )
           {
              this.modified_date = v;
              setModified(true);
          }
     }
    /**
     * Get the CreatedDate
     * @return Date
     */
     public Date getCreatedDate()
     {
          return created_date;
     }

        
    /**
     * Set the value of CreatedDate
     */
     public void setCreatedDate(Date v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.created_date, v) )
           {
              this.created_date = v;
              setModified(true);
          }
     }

 
    
        
    
    private static Vector fieldNames_ = null;

    /**
     * Generate a list of field names.
     */
    public static Vector getFieldNames()
    {
      if (fieldNames_ == null)
      {
        fieldNames_ = new Vector();
            fieldNames_.add("TableId");
            fieldNames_.add("ColumnId");
            fieldNames_.add("ModifiedBy");
            fieldNames_.add("CreatedBy");
            fieldNames_.add("ModifiedDate");
            fieldNames_.add("CreatedDate");
          }
      return fieldNames_;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.
     */
    public Object getByName(String name)
    {
            if (name.equals("TableId"))
	{
	  	    return new Integer(getTableId());
	  	}
            if (name.equals("ColumnId"))
	{
	  	    return new Integer(getColumnId());
	  	}
            if (name.equals("ModifiedBy"))
	{
	  	    return new Integer(getModifiedBy());
	  	}
            if (name.equals("CreatedBy"))
	{
	  	    return new Integer(getCreatedBy());
	  	}
            if (name.equals("ModifiedDate"))
	{
	  	    return getModifiedDate();
	  	}
            if (name.equals("CreatedDate"))
	{
	  	    return getCreatedDate();
	  	}
            return null; 
    }
     	

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     */
    public void save() throws Exception
    {
         DBConnection dbCon = null;
        try
        {
            dbCon = BasePeer.beginTransaction(
                ScarabModificationPeer.getMapBuilder()
                .getDatabaseMap().getName());
            save(dbCon);
        }
        catch(Exception e)
        {
            BasePeer.rollBackTransaction(dbCon);
            throw e;
        }
        BasePeer.commitTransaction(dbCon);

     }

      // flag to prevent endless save loop, if this object is referenced
    // by another object which falls in this transaction.
    private boolean alreadyInSave = false;
      /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     */
    public void save(DBConnection dbCon) throws Exception
    {
        if (!alreadyInSave)
      {
        alreadyInSave = true;
          if (isModified())
        {
            if (isNew())
            {
                ScarabModificationPeer.doInsert((ScarabModification)this, dbCon);
            }
            else
            {
                ScarabModificationPeer.doUpdate((ScarabModification)this, dbCon);
                setNew(false);
            }
        }

              alreadyInSave = false;
      }
      }

                                                
    /** 
     * Set the Id using pk values.
     *
     * @param int table_id
     * @param int column_id
     */
    public void setPrimaryKey(
                      int table_id
                                      , int column_id
                                                                             ) 
    {
                     setTableId(table_id);
                             setColumnId(column_id);
                                                                }

    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setTableId( Integer.parseInt(st.nextToken()) );
                                          setColumnId( Integer.parseInt(st.nextToken()) );
                                                                        }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getTableId()
                                      + ":"  + getColumnId()
                                                                             ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabModification[" + getPrimaryKey() + "]";
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabModification copy() throws Exception
    {
        ScarabModification copyObj = new ScarabModification();
         copyObj.setTableId(table_id);
         copyObj.setColumnId(column_id);
         copyObj.setModifiedBy(modified_by);
         copyObj.setCreatedBy(created_by);
         copyObj.setModifiedDate(modified_date);
         copyObj.setCreatedDate(created_date);
 
  
                       
        copyObj.setTableId(NEW_ID);
                             
        copyObj.setColumnId(NEW_ID);
                                       return copyObj;
    }             
}
