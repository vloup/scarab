package org.tigris.scarab.baseom;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.BaseObject;
import org.apache.turbine.om.peer.BasePeer;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** This class was autogenerated by GenerateMapBuilder on: Mon Jan 08 11:17:17 PST 2001 */
public class ScarabAttributeOption extends BaseObject
{
    /** the value for the option_id field */
    private int option_id;
    /** the value for the attribute_id field */
    private int attribute_id;
    /** the value for the display_value field */
    private String display_value;
    /** the value for the numeric_value field */
    private int numeric_value;


    /**
     * Get the OptionId
     * @return int
     */
     public int getOptionId()
     {
          return option_id;
     }

                            
    /**
     * Set the value of OptionId
     */
     public void setOptionId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabIssueAttributeValue
          if (collScarabIssueAttributeValues != null )
          {
              for (int i=0; i<collScarabIssueAttributeValues.size(); i++)
              {
                  ((ScarabIssueAttributeValue)collScarabIssueAttributeValues.elementAt(i))
                      .setOptionId(v);
              }
          }
            
        
                
          // update associated ScarabIssueAttributeVote
          if (collScarabIssueAttributeVotes != null )
          {
              for (int i=0; i<collScarabIssueAttributeVotes.size(); i++)
              {
                  ((ScarabIssueAttributeVote)collScarabIssueAttributeVotes.elementAt(i))
                      .setOptionId(v);
              }
          }
       

            if (this.option_id != v)
           {
              this.option_id = v;
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
     * Get the DisplayValue
     * @return String
     */
     public String getDisplayValue()
     {
          return display_value;
     }

        
    /**
     * Set the value of DisplayValue
     */
     public void setDisplayValue(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.display_value, v) )

           {
              this.display_value = v;
              setModified(true);
          }
     }
    /**
     * Get the NumericValue
     * @return int
     */
     public int getNumericValue()
     {
          return numeric_value;
     }

        
    /**
     * Set the value of NumericValue
     */
     public void setNumericValue(int v ) 
     {
  
  

            if (this.numeric_value != v)
           {
              this.numeric_value = v;
              setModified(true);
          }
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
            // obj.addScarabAttributeOptions(this);
        }
        return aScarabAttribute;
    }

    
                
      
    /**
     * Collection to store aggregation of collScarabIssueAttributeValues
     */
    private Vector collScarabIssueAttributeValues;
    /**
     * Temporary storage of collScarabIssueAttributeValues to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabIssueAttributeValues;

    public void initScarabIssueAttributeValues()
    {
        if (collScarabIssueAttributeValues == null)
            collScarabIssueAttributeValues = new Vector();
    }

    /**
     * Method called to associate a ScarabIssueAttributeValue object to this object
     * through the ScarabIssueAttributeValue foreign key attribute
     *
     * @param ScarabIssueAttributeValue l
     */
    public void addScarabIssueAttributeValues(ScarabIssueAttributeValue l) throws Exception
    {
        /*
        if (collScarabIssueAttributeValues == null)
        {
            if (tempcollScarabIssueAttributeValues == null)
            {
                tempcollScarabIssueAttributeValues = new Vector();
            }
            tempcollScarabIssueAttributeValues.add(l);
        }
        else
        {
            collScarabIssueAttributeValues.add(l);
        }
        */
        getScarabIssueAttributeValues().add(l);
        l.setScarabAttributeOption(this);
    }

    /**
     * The criteria used to select the current contents of collScarabIssueAttributeValues
     */
    private Criteria lastScarabIssueAttributeValuesCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabIssueAttributeValues(new Criteria())
     */
    public Vector getScarabIssueAttributeValues() throws Exception
    {
        if (collScarabIssueAttributeValues == null)
        {
            collScarabIssueAttributeValues = getScarabIssueAttributeValues(new Criteria(10));
        }
        return collScarabIssueAttributeValues;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeValues from storage.
     */
    public Vector getScarabIssueAttributeValues(Criteria criteria) throws Exception
    {
        if (collScarabIssueAttributeValues == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeValues = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelect(criteria);
            }
/*
            if (tempcollScarabIssueAttributeValues != null)
            {
                for (int i=0; i<tempcollScarabIssueAttributeValues.size(); i++)
                {
                    collScarabIssueAttributeValues.add(tempcollScarabIssueAttributeValues.get(i));
                }
                tempcollScarabIssueAttributeValues = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeValuesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelect(criteria);  
            }
        }
        lastScarabIssueAttributeValuesCriteria = criteria; 

        return collScarabIssueAttributeValues;
    }
      

        
      
      
          
                    
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeValues from storage.
     */
    public Vector getScarabIssueAttributeValuesJoinScarabIssue(Criteria criteria) 
        throws Exception
    {
        if (collScarabIssueAttributeValues == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeValues = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabIssue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeValuesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabIssue(criteria);
            }
        }
        lastScarabIssueAttributeValuesCriteria = criteria; 

        return collScarabIssueAttributeValues;
    }
      
      
      
          
                    
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeValues from storage.
     */
    public Vector getScarabIssueAttributeValuesJoinScarabAttribute(Criteria criteria) 
        throws Exception
    {
        if (collScarabIssueAttributeValues == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeValues = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabAttribute(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeValuesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabAttribute(criteria);
            }
        }
        lastScarabIssueAttributeValuesCriteria = criteria; 

        return collScarabIssueAttributeValues;
    }
      
      
         
          
                    
            
        
       
      
      
          
                    
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeValues from storage.
     */
    public Vector getScarabIssueAttributeValuesJoinTurbineUser(Criteria criteria) 
        throws Exception
    {
        if (collScarabIssueAttributeValues == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeValues = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinTurbineUser(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeValuesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinTurbineUser(criteria);
            }
        }
        lastScarabIssueAttributeValuesCriteria = criteria; 

        return collScarabIssueAttributeValues;
    }
     



             
      
    /**
     * Collection to store aggregation of collScarabIssueAttributeVotes
     */
    private Vector collScarabIssueAttributeVotes;
    /**
     * Temporary storage of collScarabIssueAttributeVotes to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabIssueAttributeVotes;

    public void initScarabIssueAttributeVotes()
    {
        if (collScarabIssueAttributeVotes == null)
            collScarabIssueAttributeVotes = new Vector();
    }

    /**
     * Method called to associate a ScarabIssueAttributeVote object to this object
     * through the ScarabIssueAttributeVote foreign key attribute
     *
     * @param ScarabIssueAttributeVote l
     */
    public void addScarabIssueAttributeVotes(ScarabIssueAttributeVote l) throws Exception
    {
        /*
        if (collScarabIssueAttributeVotes == null)
        {
            if (tempcollScarabIssueAttributeVotes == null)
            {
                tempcollScarabIssueAttributeVotes = new Vector();
            }
            tempcollScarabIssueAttributeVotes.add(l);
        }
        else
        {
            collScarabIssueAttributeVotes.add(l);
        }
        */
        getScarabIssueAttributeVotes().add(l);
        l.setScarabAttributeOption(this);
    }

    /**
     * The criteria used to select the current contents of collScarabIssueAttributeVotes
     */
    private Criteria lastScarabIssueAttributeVotesCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabIssueAttributeVotes(new Criteria())
     */
    public Vector getScarabIssueAttributeVotes() throws Exception
    {
        if (collScarabIssueAttributeVotes == null)
        {
            collScarabIssueAttributeVotes = getScarabIssueAttributeVotes(new Criteria(10));
        }
        return collScarabIssueAttributeVotes;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeVotes from storage.
     */
    public Vector getScarabIssueAttributeVotes(Criteria criteria) throws Exception
    {
        if (collScarabIssueAttributeVotes == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeVotes = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelect(criteria);
            }
/*
            if (tempcollScarabIssueAttributeVotes != null)
            {
                for (int i=0; i<tempcollScarabIssueAttributeVotes.size(); i++)
                {
                    collScarabIssueAttributeVotes.add(tempcollScarabIssueAttributeVotes.get(i));
                }
                tempcollScarabIssueAttributeVotes = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeVotesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelect(criteria);  
            }
        }
        lastScarabIssueAttributeVotesCriteria = criteria; 

        return collScarabIssueAttributeVotes;
    }
     

        
      
         
          
                    
            
        
       
      
      
                 
                                
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeVotes from storage.
     */
    public Vector getScarabIssueAttributeVotesJoinScarabIssueAttributeValue(Criteria criteria) 
        throws Exception
    {
        if (collScarabIssueAttributeVotes == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeVotes = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabIssueAttributeValue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeVotesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabIssueAttributeValue(criteria);
            }
        }
        lastScarabIssueAttributeVotesCriteria = criteria; 

        return collScarabIssueAttributeVotes;
    }
      
      
      
          
                    
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeOption is new, it will return
     * an empty collection; or if this ScarabAttributeOption has previously
     * been saved, it will retrieve related ScarabIssueAttributeVotes from storage.
     */
    public Vector getScarabIssueAttributeVotesJoinTurbineUser(Criteria criteria) 
        throws Exception
    {
        if (collScarabIssueAttributeVotes == null)
        {
            if ( isNew() ) 
            {
               collScarabIssueAttributeVotes = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
                   collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinTurbineUser(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeVotePeer.OPTION_ID, getOptionId() );               
               if ( !lastScarabIssueAttributeVotesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinTurbineUser(criteria);
            }
        }
        lastScarabIssueAttributeVotesCriteria = criteria; 

        return collScarabIssueAttributeVotes;
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
                ScarabAttributeOptionPeer.getMapBuilder()
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
                ScarabAttributeOptionPeer.doInsert(this, dbCon);
            }
            else
            {
                ScarabAttributeOptionPeer.doUpdate(this, dbCon);
                setNew(false);
            }
        }

                                    
                
          if (collScarabIssueAttributeValues != null )
          {
              for (int i=0; i<collScarabIssueAttributeValues.size(); i++)
              {
                  ((ScarabIssueAttributeValue)collScarabIssueAttributeValues.elementAt(i)).save(dbCon);
              }
          }
                                        
                
          if (collScarabIssueAttributeVotes != null )
          {
              for (int i=0; i<collScarabIssueAttributeVotes.size(); i++)
              {
                  ((ScarabIssueAttributeVote)collScarabIssueAttributeVotes.elementAt(i)).save(dbCon);
              }
          }
                  alreadyInSave = false;
      }
      }

                                                
    /** 
     * Set the Id using pk values.
     *
     * @param int option_id
     */
    public void setPrimaryKey(
                      int option_id
                                                                 ) throws Exception
    {
                     setOptionId(option_id);
                                                    }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                     setOptionId( Integer.parseInt(st.nextToken()) );
                  }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getOptionId()
                                                                 ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabAttributeOption[" + getPrimaryKey() + "]";
    }

    public static Vector getScarabAttributeOptions(ParameterParser pp)
        throws Exception
    {
        Vector v = new Vector();
        // look for classname in keys
        Enumeration keys = pp.keys();
        while ( keys.hasMoreElements() )
        {
            String ppKey = (String)keys.nextElement();
            String[] parsedString = StringUtils.parseObjectKey(ppKey);
            if ("ScarabAttributeOption".equalsIgnoreCase(
                     parsedString[StringUtils.PPKEY_CLASSNAME]))
            {
                ScarabAttributeOption obj = null;
                if (parsedString[StringUtils.PPKEY_ID].equals("PK") )
                {
                    obj = new ScarabAttributeOption();
                    v.addElement(obj);
  
                          obj.setOptionId(Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]));
                        }
                else
                {
  
                          int option_id = Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]);
        

              
                    for ( int i=0; i<v.size(); i++) 
                    {
                        ScarabAttributeOption tempObj = 
                            (ScarabAttributeOption)v.get(i);
                        if ( option_id == tempObj.getOptionId() ) 
                        {
                            obj = tempObj;
                            break;
                        }
                    }
                    if ( obj == null ) 
                    {
                        obj = new ScarabAttributeOption();
                        obj.setOptionId(option_id);
                        v.addElement(obj);
                    }
                }
                String property = parsedString[StringUtils.PPKEY_PROPERTY];

                        if ( property.equalsIgnoreCase("AttributeId") ) 
                {
                    obj.setAttributeId(pp.getInt(ppKey));
                }
                        if ( property.equalsIgnoreCase("DisplayValue") ) 
                {
                    obj.setDisplayValue(pp.getString(ppKey));
                }
                        if ( property.equalsIgnoreCase("NumericValue") ) 
                {
                    obj.setNumericValue(pp.getInt(ppKey));
                }
                }
        }
        return v;
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabAttributeOption copy() throws Exception
    {
        ScarabAttributeOption copyObj = new ScarabAttributeOption();
         copyObj.setOptionId(option_id);
         copyObj.setAttributeId(attribute_id);
         copyObj.setDisplayValue(display_value);
         copyObj.setNumericValue(numeric_value);
 
                                  
                
         Vector v = copyObj.getScarabIssueAttributeValues();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
                                         
                
         v = copyObj.getScarabIssueAttributeVotes();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
         
                        
        copyObj.setOptionId(NEW_ID);
                                return copyObj;
    }

}
