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
 * extended all references should be to ScarabActivity 
 */
public abstract class BaseScarabActivity extends BaseObject
{
    /** the value for the issue_id field */
    private int issue_id;
    /** the value for the attribute_id field */
    private int attribute_id;
    /** the value for the transaction_id field */
    private int transaction_id;
    /** the value for the old_value field */
    private String old_value;
    /** the value for the new_value field */
    private String new_value;


    /**
     * Get the IssueId
     * @return int
     */
     public int getIssueId()
     {
          return issue_id;
     }

            
    /**
     * Set the value of IssueId
     */
     public void setIssueId(int v ) throws Exception
     {
                  if ( aScarabIssue != null && !aScarabIssue.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

           if (this.issue_id != v)
           {
              this.issue_id = v;
              setModified(true);
          }
     }
    /**
     * Get the AttributeId
     * @return int
     */
     public int getAttributeId()
     {
          return attribute_id;
     }

            
    /**
     * Set the value of AttributeId
     */
     public void setAttributeId(int v ) throws Exception
     {
                  if ( aScarabAttribute != null && !aScarabAttribute.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

           if (this.attribute_id != v)
           {
              this.attribute_id = v;
              setModified(true);
          }
     }
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
                  if ( aScarabTransaction != null && !aScarabTransaction.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

           if (this.transaction_id != v)
           {
              this.transaction_id = v;
              setModified(true);
          }
     }
    /**
     * Get the OldValue
     * @return String
     */
     public String getOldValue()
     {
          return old_value;
     }

        
    /**
     * Set the value of OldValue
     */
     public void setOldValue(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.old_value, v) )
           {
              this.old_value = v;
              setModified(true);
          }
     }
    /**
     * Get the NewValue
     * @return String
     */
     public String getNewValue()
     {
          return new_value;
     }

        
    /**
     * Set the value of NewValue
     */
     public void setNewValue(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.new_value, v) )
           {
              this.new_value = v;
              setModified(true);
          }
     }

 
 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabIssue object
     *
     * @param ScarabIssue v
     */
    private ScarabIssue aScarabIssue;
    public void setScarabIssue(ScarabIssue v) throws Exception
    {
        aScarabIssue = null;
           setIssueId(v.getIssueId());
           aScarabIssue = v;
    }

                     
    public ScarabIssue getScarabIssue() throws Exception
    {
        if ( aScarabIssue==null && (this.issue_id>0) )
        {
            aScarabIssue = ScarabIssuePeer.retrieveByPK(this.issue_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabIssue obj = ScarabIssuePeer.retrieveByPK(this.issue_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabIssue;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttribute object
     *
     * @param ScarabAttribute v
     */
    private ScarabAttribute aScarabAttribute;
    public void setScarabAttribute(ScarabAttribute v) throws Exception
    {
        aScarabAttribute = null;
           setAttributeId(v.getAttributeId());
           aScarabAttribute = v;
    }

                     
    public ScarabAttribute getScarabAttribute() throws Exception
    {
        if ( aScarabAttribute==null && (this.attribute_id>0) )
        {
            aScarabAttribute = ScarabAttributePeer.retrieveByPK(this.attribute_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabAttribute obj = ScarabAttributePeer.retrieveByPK(this.attribute_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabAttribute;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabTransaction object
     *
     * @param ScarabTransaction v
     */
    private ScarabTransaction aScarabTransaction;
    public void setScarabTransaction(ScarabTransaction v) throws Exception
    {
        aScarabTransaction = null;
           setTransactionId(v.getTransactionId());
           aScarabTransaction = v;
    }

                     
    public ScarabTransaction getScarabTransaction() throws Exception
    {
        if ( aScarabTransaction==null && (this.transaction_id>0) )
        {
            aScarabTransaction = ScarabTransactionPeer.retrieveByPK(this.transaction_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabTransaction obj = ScarabTransactionPeer.retrieveByPK(this.transaction_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabTransaction;
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
            fieldNames_.add("IssueId");
            fieldNames_.add("AttributeId");
            fieldNames_.add("TransactionId");
            fieldNames_.add("OldValue");
            fieldNames_.add("NewValue");
          }
      return fieldNames_;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.
     */
    public Object getByName(String name)
    {
            if (name.equals("IssueId"))
	{
	  	    return new Integer(getIssueId());
	  	}
            if (name.equals("AttributeId"))
	{
	  	    return new Integer(getAttributeId());
	  	}
            if (name.equals("TransactionId"))
	{
	  	    return new Integer(getTransactionId());
	  	}
            if (name.equals("OldValue"))
	{
	  	    return getOldValue();
	  	}
            if (name.equals("NewValue"))
	{
	  	    return getNewValue();
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
                ScarabActivityPeer.getMapBuilder()
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
                ScarabActivityPeer.doInsert((ScarabActivity)this, dbCon);
            }
            else
            {
                ScarabActivityPeer.doUpdate((ScarabActivity)this, dbCon);
                setNew(false);
            }
        }

              alreadyInSave = false;
      }
      }

                                                                    
    /** 
     * Set the Id using pk values.
     *
     * @param int issue_id
     * @param int attribute_id
     * @param int transaction_id
     */
    public void setPrimaryKey(
                      int issue_id
                                      , int attribute_id
                                      , int transaction_id
                                                     ) throws Exception
    {
                     setIssueId(issue_id);
                             setAttributeId(attribute_id);
                             setTransactionId(transaction_id);
                                        }

    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setIssueId( Integer.parseInt(st.nextToken()) );
                                          setAttributeId( Integer.parseInt(st.nextToken()) );
                                          setTransactionId( Integer.parseInt(st.nextToken()) );
                                                }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getIssueId()
                                      + ":"  + getAttributeId()
                                      + ":"  + getTransactionId()
                                                     ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabActivity[" + getPrimaryKey() + "]";
    }

}
