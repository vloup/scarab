package org.tigris.scarab.baseom;

// JDK classes
import java.util.*;
import java.math.*;

// Turbine classes
import org.apache.turbine.om.BaseObject;
import org.apache.turbine.om.peer.BasePeer;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** 
 * This class was autogenerated by Torque on: Wed Feb 07 17:08:09 PST 2001
 * You should not use this class directly.  It should not even be
 * extended all references should be to ScarabTransaction 
 */
public abstract class BaseScarabTransaction extends BaseObject
{
    /** the value for the transaction_id field */
    private int transaction_id;
    /** the value for the created_by field */
    private int created_by;
    /** the value for the created_date field */
    private Date created_date;


    /**
     * Get the TransactionId
     * @return int
     */
     public int getTransactionId()
     {
          return transaction_id;
     }

                            
    /**
     * Set the value of TransactionId
     */
     public void setTransactionId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabActivity
          if (collScarabActivitys != null )
          {
              for (int i=0; i<collScarabActivitys.size(); i++)
              {
                  ((ScarabActivity)collScarabActivitys.get(i))
                      .setTransactionId(v);
              }
          }
       

           if (this.transaction_id != v)
           {
              this.transaction_id = v;
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

 
    
                
      
    /**
     * Collection to store aggregation of collScarabActivitys
     */
    private Vector collScarabActivitys;
    /**
     * Temporary storage of collScarabActivitys to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabActivitys;

    public void initScarabActivitys()
    {
        if (collScarabActivitys == null)
            collScarabActivitys = new Vector();
    }

    /**
     * Method called to associate a ScarabActivity object to this object
     * through the ScarabActivity foreign key attribute
     *
     * @param ScarabActivity l
     */
    public void addScarabActivitys(ScarabActivity l) throws Exception
    {
        /*
        if (collScarabActivitys == null)
        {
            if (tempcollScarabActivitys == null)
            {
                tempcollScarabActivitys = new Vector();
            }
            tempcollScarabActivitys.add(l);
        }
        else
        {
            collScarabActivitys.add(l);
        }
        */
        getScarabActivitys().add(l);
        l.setScarabTransaction((ScarabTransaction)this);
    }

    /**
     * The criteria used to select the current contents of collScarabActivitys
     */
    private Criteria lastScarabActivitysCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabActivitys(new Criteria())
     */
    public Vector getScarabActivitys() throws Exception
    {
        if (collScarabActivitys == null)
        {
            collScarabActivitys = getScarabActivitys(new Criteria(10));
        }
        return collScarabActivitys;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabTransaction is new, it will return
     * an empty collection; or if this ScarabTransaction has previously
     * been saved, it will retrieve related ScarabActivitys from storage.
     */
    public Vector getScarabActivitys(Criteria criteria) throws Exception
    {
        if (collScarabActivitys == null)
        {
            if ( isNew() ) 
            {
               collScarabActivitys = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
                   collScarabActivitys = ScarabActivityPeer.doSelect(criteria);
            }
/*
            if (tempcollScarabActivitys != null)
            {
                for (int i=0; i<tempcollScarabActivitys.size(); i++)
                {
                    collScarabActivitys.add(tempcollScarabActivitys.get(i));
                }
                tempcollScarabActivitys = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
               if ( !lastScarabActivitysCriteria.equals(criteria)  )
            {
                collScarabActivitys = ScarabActivityPeer.doSelect(criteria);  
            }
        }
        lastScarabActivitysCriteria = criteria; 

        return collScarabActivitys;
    }
     

        
      
      
          
                    
                
        
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabTransaction is new, it will return
     * an empty collection; or if this ScarabTransaction has previously
     * been saved, it will retrieve related ScarabActivitys from storage.
     */
    public Vector getScarabActivitysJoinScarabIssue(Criteria criteria) 
        throws Exception
    {
        if (collScarabActivitys == null)
        {
            if ( isNew() ) 
            {
               collScarabActivitys = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
                   collScarabActivitys = ScarabActivityPeer.doSelectJoinScarabIssue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
               if ( !lastScarabActivitysCriteria.equals(criteria)  )
            {
                collScarabActivitys = ScarabActivityPeer.doSelectJoinScarabIssue(criteria);
            }
        }
        lastScarabActivitysCriteria = criteria; 

        return collScarabActivitys;
    }
      
      
      
          
                    
                
        
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabTransaction is new, it will return
     * an empty collection; or if this ScarabTransaction has previously
     * been saved, it will retrieve related ScarabActivitys from storage.
     */
    public Vector getScarabActivitysJoinScarabAttribute(Criteria criteria) 
        throws Exception
    {
        if (collScarabActivitys == null)
        {
            if ( isNew() ) 
            {
               collScarabActivitys = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
                   collScarabActivitys = ScarabActivityPeer.doSelectJoinScarabAttribute(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabActivityPeer.TRANSACTION_ID, getTransactionId() );               
               if ( !lastScarabActivitysCriteria.equals(criteria)  )
            {
                collScarabActivitys = ScarabActivityPeer.doSelectJoinScarabAttribute(criteria);
            }
        }
        lastScarabActivitysCriteria = criteria; 

        return collScarabActivitys;
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
            fieldNames_.add("TransactionId");
            fieldNames_.add("CreatedBy");
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
            if (name.equals("TransactionId"))
	{
	  	    return new Integer(getTransactionId());
	  	}
            if (name.equals("CreatedBy"))
	{
	  	    return new Integer(getCreatedBy());
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
                ScarabTransactionPeer.getMapBuilder()
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
                ScarabTransactionPeer.doInsert((ScarabTransaction)this, dbCon);
            }
            else
            {
                ScarabTransactionPeer.doUpdate((ScarabTransaction)this, dbCon);
                setNew(false);
            }
        }

                                    
                
          if (collScarabActivitys != null )
          {
              for (int i=0; i<collScarabActivitys.size(); i++)
              {
                  ((ScarabActivity)collScarabActivitys.get(i)).save(dbCon);
              }
          }
                  alreadyInSave = false;
      }
      }

                                            
    /** 
     * Set the Id using pk values.
     *
     * @param int transaction_id
     */
    public void setPrimaryKey(
                      int transaction_id
                                                     ) throws Exception
    {
                     setTransactionId(transaction_id);
                                        }

    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setTransactionId( Integer.parseInt(st.nextToken()) );
                                                }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getTransactionId()
                                                     ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabTransaction[" + getPrimaryKey() + "]";
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabTransaction copy() throws Exception
    {
        ScarabTransaction copyObj = new ScarabTransaction();
         copyObj.setTransactionId(transaction_id);
         copyObj.setCreatedBy(created_by);
         copyObj.setCreatedDate(created_date);
 
                                  
                
         List v = copyObj.getScarabActivitys();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.get(i)).setNew(true);
         }
         
                       
        copyObj.setTransactionId(NEW_ID);
                         return copyObj;
    }             
}
