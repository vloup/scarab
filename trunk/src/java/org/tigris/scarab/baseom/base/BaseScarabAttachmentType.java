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
 * extended all references should be to ScarabAttachmentType 
 */
public abstract class BaseScarabAttachmentType extends BaseObject
{
    /** the value for the attachment_type_id field */
    private int attachment_type_id;
    /** the value for the attachment_type_name field */
    private String attachment_type_name;


    /**
     * Get the AttachmentTypeId
     * @return int
     */
     public int getAttachmentTypeId()
     {
          return attachment_type_id;
     }

                            
    /**
     * Set the value of AttachmentTypeId
     */
     public void setAttachmentTypeId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabAttachment
          if (collScarabAttachments != null )
          {
              for (int i=0; i<collScarabAttachments.size(); i++)
              {
                  ((ScarabAttachment)collScarabAttachments.get(i))
                      .setTypeId(v);
              }
          }
       

           if (this.attachment_type_id != v)
           {
              this.attachment_type_id = v;
              setModified(true);
          }
     }
    /**
     * Get the Name
     * @return String
     */
     public String getName()
     {
          return attachment_type_name;
     }

        
    /**
     * Set the value of Name
     */
     public void setName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attachment_type_name, v) )
           {
              this.attachment_type_name = v;
              setModified(true);
          }
     }

 
    
                
      
    /**
     * Collection to store aggregation of collScarabAttachments
     */
    private Vector collScarabAttachments;
    /**
     * Temporary storage of collScarabAttachments to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabAttachments;

    public void initScarabAttachments()
    {
        if (collScarabAttachments == null)
            collScarabAttachments = new Vector();
    }

    /**
     * Method called to associate a ScarabAttachment object to this object
     * through the ScarabAttachment foreign key attribute
     *
     * @param ScarabAttachment l
     */
    public void addScarabAttachments(ScarabAttachment l) throws Exception
    {
        /*
        if (collScarabAttachments == null)
        {
            if (tempcollScarabAttachments == null)
            {
                tempcollScarabAttachments = new Vector();
            }
            tempcollScarabAttachments.add(l);
        }
        else
        {
            collScarabAttachments.add(l);
        }
        */
        getScarabAttachments().add(l);
        l.setScarabAttachmentType((ScarabAttachmentType)this);
    }

    /**
     * The criteria used to select the current contents of collScarabAttachments
     */
    private Criteria lastScarabAttachmentsCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabAttachments(new Criteria())
     */
    public Vector getScarabAttachments() throws Exception
    {
        if (collScarabAttachments == null)
        {
            collScarabAttachments = getScarabAttachments(new Criteria(10));
        }
        return collScarabAttachments;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttachmentType is new, it will return
     * an empty collection; or if this ScarabAttachmentType has previously
     * been saved, it will retrieve related ScarabAttachments from storage.
     */
    public Vector getScarabAttachments(Criteria criteria) throws Exception
    {
        if (collScarabAttachments == null)
        {
            if ( isNew() ) 
            {
               collScarabAttachments = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabAttachmentPeer.ATTACHMENT_TYPE_ID, getAttachmentTypeId() );               
                   collScarabAttachments = ScarabAttachmentPeer.doSelect(criteria);
            }
/*
            if (tempcollScarabAttachments != null)
            {
                for (int i=0; i<tempcollScarabAttachments.size(); i++)
                {
                    collScarabAttachments.add(tempcollScarabAttachments.get(i));
                }
                tempcollScarabAttachments = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabAttachmentPeer.ATTACHMENT_TYPE_ID, getAttachmentTypeId() );               
               if ( !lastScarabAttachmentsCriteria.equals(criteria)  )
            {
                collScarabAttachments = ScarabAttachmentPeer.doSelect(criteria);  
            }
        }
        lastScarabAttachmentsCriteria = criteria; 

        return collScarabAttachments;
    }
    

        
      
      
          
                    
                
        
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttachmentType is new, it will return
     * an empty collection; or if this ScarabAttachmentType has previously
     * been saved, it will retrieve related ScarabAttachments from storage.
     */
    public Vector getScarabAttachmentsJoinScarabIssue(Criteria criteria) 
        throws Exception
    {
        if (collScarabAttachments == null)
        {
            if ( isNew() ) 
            {
               collScarabAttachments = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabAttachmentPeer.ATTACHMENT_TYPE_ID, getAttachmentTypeId() );               
                   collScarabAttachments = ScarabAttachmentPeer.doSelectJoinScarabIssue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabAttachmentPeer.ATTACHMENT_TYPE_ID, getAttachmentTypeId() );               
               if ( !lastScarabAttachmentsCriteria.equals(criteria)  )
            {
                collScarabAttachments = ScarabAttachmentPeer.doSelectJoinScarabIssue(criteria);
            }
        }
        lastScarabAttachmentsCriteria = criteria; 

        return collScarabAttachments;
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
            fieldNames_.add("AttachmentTypeId");
            fieldNames_.add("Name");
          }
      return fieldNames_;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.
     */
    public Object getByName(String name)
    {
            if (name.equals("AttachmentTypeId"))
	{
	  	    return new Integer(getAttachmentTypeId());
	  	}
            if (name.equals("Name"))
	{
	  	    return getName();
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
                ScarabAttachmentTypePeer.getMapBuilder()
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
                ScarabAttachmentTypePeer.doInsert((ScarabAttachmentType)this, dbCon);
            }
            else
            {
                ScarabAttachmentTypePeer.doUpdate((ScarabAttachmentType)this, dbCon);
                setNew(false);
            }
        }

                                    
                
          if (collScarabAttachments != null )
          {
              for (int i=0; i<collScarabAttachments.size(); i++)
              {
                  ((ScarabAttachment)collScarabAttachments.get(i)).save(dbCon);
              }
          }
                  alreadyInSave = false;
      }
      }

                                        
    /** 
     * Set the Id using pk values.
     *
     * @param int attachment_type_id
     */
    public void setPrimaryKey(
                      int attachment_type_id
                                         ) throws Exception
    {
                     setAttachmentTypeId(attachment_type_id);
                            }

    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setAttachmentTypeId( Integer.parseInt(st.nextToken()) );
                                    }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getAttachmentTypeId()
                                         ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabAttachmentType[" + getPrimaryKey() + "]";
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabAttachmentType copy() throws Exception
    {
        ScarabAttachmentType copyObj = new ScarabAttachmentType();
         copyObj.setAttachmentTypeId(attachment_type_id);
         copyObj.setName(attachment_type_name);
 
                                  
                
         List v = copyObj.getScarabAttachments();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.get(i)).setNew(true);
         }
         
                       
        copyObj.setAttachmentTypeId(NEW_ID);
                  return copyObj;
    }             
}
