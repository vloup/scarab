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
 * extended all references should be to ScarabAttributeType 
 */
public abstract class BaseScarabAttributeType extends BaseObject
{
    /** the value for the attribute_type_id field */
    private int attribute_type_id;
    /** the value for the attribute_class_id field */
    private int attribute_class_id;
    /** the value for the attribute_type_name field */
    private String attribute_type_name;
    /** the value for the java_class_name field */
    private String java_class_name;


    /**
     * Get the AttributeTypeId
     * @return int
     */
     public int getAttributeTypeId()
     {
          return attribute_type_id;
     }

                            
    /**
     * Set the value of AttributeTypeId
     */
     public void setAttributeTypeId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabAttribute
          if (collScarabAttributes != null )
          {
              for (int i=0; i<collScarabAttributes.size(); i++)
              {
                  ((ScarabAttribute)collScarabAttributes.get(i))
                      .setTypeId(v);
              }
          }
       

           if (this.attribute_type_id != v)
           {
              this.attribute_type_id = v;
              setModified(true);
          }
     }
    /**
     * Get the ClassId
     * @return int
     */
     public int getClassId()
     {
          return attribute_class_id;
     }

            
    /**
     * Set the value of ClassId
     */
     public void setClassId(int v ) throws Exception
     {
                  if ( aScarabAttributeClass != null && !aScarabAttributeClass.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

           if (this.attribute_class_id != v)
           {
              this.attribute_class_id = v;
              setModified(true);
          }
     }
    /**
     * Get the Name
     * @return String
     */
     public String getName()
     {
          return attribute_type_name;
     }

        
    /**
     * Set the value of Name
     */
     public void setName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attribute_type_name, v) )
           {
              this.attribute_type_name = v;
              setModified(true);
          }
     }
    /**
     * Get the JavaClassName
     * @return String
     */
     public String getJavaClassName()
     {
          return java_class_name;
     }

        
    /**
     * Set the value of JavaClassName
     */
     public void setJavaClassName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.java_class_name, v) )
           {
              this.java_class_name = v;
              setModified(true);
          }
     }

 
 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttributeClass object
     *
     * @param ScarabAttributeClass v
     */
    private ScarabAttributeClass aScarabAttributeClass;
    public void setScarabAttributeClass(ScarabAttributeClass v) throws Exception
    {
        aScarabAttributeClass = null;
           setClassId(v.getAttributeClassId());
           aScarabAttributeClass = v;
    }

                     
    public ScarabAttributeClass getScarabAttributeClass() throws Exception
    {
        if ( aScarabAttributeClass==null && (this.attribute_class_id>0) )
        {
            aScarabAttributeClass = ScarabAttributeClassPeer.retrieveByPK(this.attribute_class_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabAttributeClass obj = ScarabAttributeClassPeer.retrieveByPK(this.attribute_class_id);
            // obj.addScarabAttributeTypes(this);
        }
        return aScarabAttributeClass;
    }

    
                
      
    /**
     * Collection to store aggregation of collScarabAttributes
     */
    private Vector collScarabAttributes;
    /**
     * Temporary storage of collScarabAttributes to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabAttributes;

    public void initScarabAttributes()
    {
        if (collScarabAttributes == null)
            collScarabAttributes = new Vector();
    }

    /**
     * Method called to associate a ScarabAttribute object to this object
     * through the ScarabAttribute foreign key attribute
     *
     * @param ScarabAttribute l
     */
    public void addScarabAttributes(ScarabAttribute l) throws Exception
    {
        /*
        if (collScarabAttributes == null)
        {
            if (tempcollScarabAttributes == null)
            {
                tempcollScarabAttributes = new Vector();
            }
            tempcollScarabAttributes.add(l);
        }
        else
        {
            collScarabAttributes.add(l);
        }
        */
        getScarabAttributes().add(l);
        l.setScarabAttributeType((ScarabAttributeType)this);
    }

    /**
     * The criteria used to select the current contents of collScarabAttributes
     */
    private Criteria lastScarabAttributesCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabAttributes(new Criteria())
     */
    public Vector getScarabAttributes() throws Exception
    {
        if (collScarabAttributes == null)
        {
            collScarabAttributes = getScarabAttributes(new Criteria(10));
        }
        return collScarabAttributes;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeType is new, it will return
     * an empty collection; or if this ScarabAttributeType has previously
     * been saved, it will retrieve related ScarabAttributes from storage.
     */
    public Vector getScarabAttributes(Criteria criteria) throws Exception
    {
        if (collScarabAttributes == null)
        {
            if ( isNew() ) 
            {
               collScarabAttributes = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabAttributePeer.ATTRIBUTE_TYPE_ID, getAttributeTypeId() );               
                   collScarabAttributes = ScarabAttributePeer.doSelect(criteria);
            }
/*
            if (tempcollScarabAttributes != null)
            {
                for (int i=0; i<tempcollScarabAttributes.size(); i++)
                {
                    collScarabAttributes.add(tempcollScarabAttributes.get(i));
                }
                tempcollScarabAttributes = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabAttributePeer.ATTRIBUTE_TYPE_ID, getAttributeTypeId() );               
               if ( !lastScarabAttributesCriteria.equals(criteria)  )
            {
                collScarabAttributes = ScarabAttributePeer.doSelect(criteria);  
            }
        }
        lastScarabAttributesCriteria = criteria; 

        return collScarabAttributes;
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
            fieldNames_.add("AttributeTypeId");
            fieldNames_.add("ClassId");
            fieldNames_.add("Name");
            fieldNames_.add("JavaClassName");
          }
      return fieldNames_;
    }

    /**
     * Retrieves a field from the object by name passed in
     * as a String.
     */
    public Object getByName(String name)
    {
            if (name.equals("AttributeTypeId"))
	{
	  	    return new Integer(getAttributeTypeId());
	  	}
            if (name.equals("ClassId"))
	{
	  	    return new Integer(getClassId());
	  	}
            if (name.equals("Name"))
	{
	  	    return getName();
	  	}
            if (name.equals("JavaClassName"))
	{
	  	    return getJavaClassName();
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
                ScarabAttributeTypePeer.getMapBuilder()
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
                ScarabAttributeTypePeer.doInsert((ScarabAttributeType)this, dbCon);
            }
            else
            {
                ScarabAttributeTypePeer.doUpdate((ScarabAttributeType)this, dbCon);
                setNew(false);
            }
        }

                                    
                
          if (collScarabAttributes != null )
          {
              for (int i=0; i<collScarabAttributes.size(); i++)
              {
                  ((ScarabAttribute)collScarabAttributes.get(i)).save(dbCon);
              }
          }
                  alreadyInSave = false;
      }
      }

                                                
    /** 
     * Set the Id using pk values.
     *
     * @param int attribute_type_id
     */
    public void setPrimaryKey(
                      int attribute_type_id
                                                                 ) throws Exception
    {
                     setAttributeTypeId(attribute_type_id);
                                                    }

    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setAttributeTypeId( Integer.parseInt(st.nextToken()) );
                                                            }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getAttributeTypeId()
                                                                 ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabAttributeType[" + getPrimaryKey() + "]";
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabAttributeType copy() throws Exception
    {
        ScarabAttributeType copyObj = new ScarabAttributeType();
         copyObj.setAttributeTypeId(attribute_type_id);
         copyObj.setClassId(attribute_class_id);
         copyObj.setName(attribute_type_name);
         copyObj.setJavaClassName(java_class_name);
 
                                  
                
         List v = copyObj.getScarabAttributes();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.get(i)).setNew(true);
         }
         
                       
        copyObj.setAttributeTypeId(NEW_ID);
                                return copyObj;
    }             
}
