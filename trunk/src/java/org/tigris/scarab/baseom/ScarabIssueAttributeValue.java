package org.tigris.scarab.baseom;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.BaseObject;
import org.apache.turbine.om.peer.BasePeer;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** This class was autogenerated by GenerateMapBuilder on: Fri Dec 15 13:47:21 PST 2000 */
public class ScarabIssueAttributeValue extends BaseObject
{
    /** the value for the issue_id field */
    private int issue_id;
    /** the value for the attribute_id field */
    private int attribute_id;
    /** the value for the option_id field */
    private int option_id;
    /** the value for the visitor_id field */
    private int visitor_id;
    /** the value for the value field */
    private String value;
    /** the value for the deleted field */
    private String deleted;


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
  
  
       
          // update associated ScarabIssueAttributeVote
          if (collScarabIssueAttributeVotes != null )
          {
              for (int i=0; i<collScarabIssueAttributeVotes.size(); i++)
              {
                  ((ScarabIssueAttributeVote)collScarabIssueAttributeVotes.elementAt(i))
                      .setIssueId(v);
              }
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
  
  
       
          // update associated ScarabIssueAttributeVote
          if (collScarabIssueAttributeVotes != null )
          {
              for (int i=0; i<collScarabIssueAttributeVotes.size(); i++)
              {
                  ((ScarabIssueAttributeVote)collScarabIssueAttributeVotes.elementAt(i))
                      .setAttributeId(v);
              }
          }

 
           if (this.attribute_id != v)
           {
              this.attribute_id = v;
              setModified(true);
          }
     }
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
                  if ( aScarabAttributeOption != null && !aScarabAttributeOption.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  
           if (this.option_id != v)
           {
              this.option_id = v;
              setModified(true);
          }
     }
    /**
     * Get the VisitorId
     * @return int
     */
     public int getVisitorId()
     {
          return visitor_id;
     }

        
    /**
     * Set the value of VisitorId
     */
     public void setVisitorId(int v ) 
     {
  
  
           if (this.visitor_id != v)
           {
              this.visitor_id = v;
              setModified(true);
          }
     }
    /**
     * Get the Value
     * @return String
     */
     public String getValue()
     {
          return value;
     }

        
    /**
     * Set the value of Value
     */
     public void setValue(String v ) 
     {
  
  
           if ( !ObjectUtils.equals(this.value, v) )
           {
              this.value = v;
              setModified(true);
          }
     }
    /**
     * Get the Deleted
     * @return String
     */
     public String getDeleted()
     {
          return deleted;
     }

        
    /**
     * Set the value of Deleted
     */
     public void setDeleted(String v ) 
     {
  
  
           if ( !ObjectUtils.equals(this.deleted, v) )
           {
              this.deleted = v;
              setModified(true);
          }
     }

 
 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabIssue object
     *
     * @param ScarabIssue v
     */
    private ScarabIssue aScarabIssue;
    void setScarabIssue(ScarabIssue v) throws Exception
    {
        aScarabIssue = null;
           setIssueId(v.getIssueId());
           aScarabIssue = v;
    }

                     
    public ScarabIssue getScarabIssue() throws Exception
    {
        if (aScarabIssue != null)
        {
            return aScarabIssue;            
        }
        else if (this.issue_id>0)
        {
            ScarabIssue obj = ScarabIssuePeer.retrieveByPK(this.issue_id);
            // The following line can be added to guarantee the related
            // object contains a reference to this object, but this
            // level of coupling may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // obj.addScarabIssueAttributeValues(this);
            return obj;
        }
        return null;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttribute object
     *
     * @param ScarabAttribute v
     */
    private ScarabAttribute aScarabAttribute;
    void setScarabAttribute(ScarabAttribute v) throws Exception
    {
        aScarabAttribute = null;
           setAttributeId(v.getAttributeId());
           aScarabAttribute = v;
    }

                     
    public ScarabAttribute getScarabAttribute() throws Exception
    {
        if (aScarabAttribute != null)
        {
            return aScarabAttribute;            
        }
        else if (this.attribute_id>0)
        {
            ScarabAttribute obj = ScarabAttributePeer.retrieveByPK(this.attribute_id);
            // The following line can be added to guarantee the related
            // object contains a reference to this object, but this
            // level of coupling may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // obj.addScarabIssueAttributeValues(this);
            return obj;
        }
        return null;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttributeOption object
     *
     * @param ScarabAttributeOption v
     */
    private ScarabAttributeOption aScarabAttributeOption;
    void setScarabAttributeOption(ScarabAttributeOption v) throws Exception
    {
        aScarabAttributeOption = null;
           setOptionId(v.getOptionId());
           aScarabAttributeOption = v;
    }

                     
    public ScarabAttributeOption getScarabAttributeOption() throws Exception
    {
        if (aScarabAttributeOption != null)
        {
            return aScarabAttributeOption;            
        }
        else if (this.option_id>0)
        {
            ScarabAttributeOption obj = ScarabAttributeOptionPeer.retrieveByPK(this.option_id);
            // The following line can be added to guarantee the related
            // object contains a reference to this object, but this
            // level of coupling may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // obj.addScarabIssueAttributeValues(this);
            return obj;
        }
        return null;
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
        l.setScarabIssueAttributeValue(this);
    }

    /**
     * The criteria used to select the current contents of collScarabIssueAttributeVotes
     */
    private Criteria lastScarabIssueAttributeVotesCriteria = null;

    /**
     * If this collection has already been initialized, it returns
     * the collection. Otherwise if this ScarabIssueAttributeValue is new, it will return
     * an empty collection; or if this ScarabIssueAttributeValue has previously
     * been saved, it will retrieve related ScarabIssueAttributeVotes from storage.
     */
    public Vector getScarabIssueAttributeVotes() throws Exception
    {
        return getScarabIssueAttributeVotes(new Criteria(5));
    }

    /**
     * If this collection has already been initialized, it returns
     * the collection. Otherwise if this ScarabIssueAttributeValue is new, it will return
     * an empty collection; or if this ScarabIssueAttributeValue has previously
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
                   criteria.add(ScarabIssueAttributeVotePeer.ISSUE_ID, getIssueId() );               
                   criteria.add(ScarabIssueAttributeVotePeer.ATTRIBUTE_ID, getAttributeId() );               
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
                   criteria.add(ScarabIssueAttributeVotePeer.ISSUE_ID, getIssueId() );               
                   criteria.add(ScarabIssueAttributeVotePeer.ATTRIBUTE_ID, getAttributeId() );               
               if ( lastScarabIssueAttributeVotesCriteria.size() == criteria.size() )
            {
                newCriteria = false;
                for (Enumeration e=criteria.keys(); e.hasMoreElements(); )
                {
                    Object key = e.nextElement();
                    if ( lastScarabIssueAttributeVotesCriteria.containsKey(key) )
                    {
                        if ( !criteria.get(key).equals(
                            lastScarabIssueAttributeVotesCriteria.get(key)) )
                        {
                            newCriteria = true;
                            break;
                        }
                    }
                    else
                    {
                        newCriteria = true;
                        break;
                    }
                }
            }
            if (newCriteria)
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelect(criteria);  
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
                ScarabIssueAttributeValuePeer.getMapBuilder()
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
                ScarabIssueAttributeValuePeer.doInsert(this, dbCon);
            }
            else
            {
                ScarabIssueAttributeValuePeer.doUpdate(this, dbCon);
                setNew(false);
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
     * @param int issue_id
     * @param int attribute_id
     */
    public void setId(
                      int issue_id
                                      , int attribute_id
                                                                             ) throws Exception
    {
                     setIssueId(issue_id);
                             setAttributeId(attribute_id);
                                                                }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setId(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setIssueId( Integer.parseInt(st.nextToken()) );
                                          setAttributeId( Integer.parseInt(st.nextToken()) );
                                                                        }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getId() 
    {
        return ""
                      + getIssueId()
                                      + ":"  + getAttributeId()
                                                                             ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabIssueAttributeValue[" + getId() + "]";
    }


}




