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
public class TurbineUser extends BaseObject
{
    /** the value for the user_id field */
    private int user_id;


    /**
     * Get the UserId
     * @return int
     */
     public int getUserId()
     {
          return user_id;
     }

                            
    /**
     * Set the value of UserId
     */
     public void setUserId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabIssueAttributeValue
          if (collScarabIssueAttributeValues != null )
          {
              for (int i=0; i<collScarabIssueAttributeValues.size(); i++)
              {
                  ((ScarabIssueAttributeValue)collScarabIssueAttributeValues.elementAt(i))
                      .setUserId(v);
              }
          }
            
        
                
          // update associated ScarabRModuleUser
          if (collScarabRModuleUsers != null )
          {
              for (int i=0; i<collScarabRModuleUsers.size(); i++)
              {
                  ((ScarabRModuleUser)collScarabRModuleUsers.elementAt(i))
                      .setUserId(v);
              }
          }
            
        
                
          // update associated ScarabIssueAttributeVote
          if (collScarabIssueAttributeVotes != null )
          {
              for (int i=0; i<collScarabIssueAttributeVotes.size(); i++)
              {
                  ((ScarabIssueAttributeVote)collScarabIssueAttributeVotes.elementAt(i))
                      .setUserId(v);
              }
          }
       

            if (this.user_id != v)
           {
              this.user_id = v;
              setModified(true);
          }
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
        l.setTurbineUser(this);
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
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
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
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
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
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
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabIssue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
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
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabAttribute(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
     * been saved, it will retrieve related ScarabIssueAttributeValues from storage.
     */
    public Vector getScarabIssueAttributeValuesJoinScarabAttributeOption(Criteria criteria) 
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
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
                   collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabAttributeOption(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeValuePeer.USER_ID, getUserId() );               
               if ( !lastScarabIssueAttributeValuesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeValues = ScarabIssueAttributeValuePeer.doSelectJoinScarabAttributeOption(criteria);
            }
        }
        lastScarabIssueAttributeValuesCriteria = criteria; 

        return collScarabIssueAttributeValues;
    }
      
      
         
          
                    
            
        
      



             
      
    /**
     * Collection to store aggregation of collScarabRModuleUsers
     */
    private Vector collScarabRModuleUsers;
    /**
     * Temporary storage of collScarabRModuleUsers to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabRModuleUsers;

    public void initScarabRModuleUsers()
    {
        if (collScarabRModuleUsers == null)
            collScarabRModuleUsers = new Vector();
    }

    /**
     * Method called to associate a ScarabRModuleUser object to this object
     * through the ScarabRModuleUser foreign key attribute
     *
     * @param ScarabRModuleUser l
     */
    public void addScarabRModuleUsers(ScarabRModuleUser l) throws Exception
    {
        /*
        if (collScarabRModuleUsers == null)
        {
            if (tempcollScarabRModuleUsers == null)
            {
                tempcollScarabRModuleUsers = new Vector();
            }
            tempcollScarabRModuleUsers.add(l);
        }
        else
        {
            collScarabRModuleUsers.add(l);
        }
        */
        getScarabRModuleUsers().add(l);
        l.setTurbineUser(this);
    }

    /**
     * The criteria used to select the current contents of collScarabRModuleUsers
     */
    private Criteria lastScarabRModuleUsersCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabRModuleUsers(new Criteria())
     */
    public Vector getScarabRModuleUsers() throws Exception
    {
        if (collScarabRModuleUsers == null)
        {
            collScarabRModuleUsers = getScarabRModuleUsers(new Criteria(10));
        }
        return collScarabRModuleUsers;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
     * been saved, it will retrieve related ScarabRModuleUsers from storage.
     */
    public Vector getScarabRModuleUsers(Criteria criteria) throws Exception
    {
        if (collScarabRModuleUsers == null)
        {
            if ( isNew() ) 
            {
               collScarabRModuleUsers = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabRModuleUserPeer.USER_ID, getUserId() );               
                   collScarabRModuleUsers = ScarabRModuleUserPeer.doSelect(criteria);
            }
/*
            if (tempcollScarabRModuleUsers != null)
            {
                for (int i=0; i<tempcollScarabRModuleUsers.size(); i++)
                {
                    collScarabRModuleUsers.add(tempcollScarabRModuleUsers.get(i));
                }
                tempcollScarabRModuleUsers = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabRModuleUserPeer.USER_ID, getUserId() );               
               if ( !lastScarabRModuleUsersCriteria.equals(criteria)  )
            {
                collScarabRModuleUsers = ScarabRModuleUserPeer.doSelect(criteria);  
            }
        }
        lastScarabRModuleUsersCriteria = criteria; 

        return collScarabRModuleUsers;
    }
    

        
      
      
          
                    
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
     * been saved, it will retrieve related ScarabRModuleUsers from storage.
     */
    public Vector getScarabRModuleUsersJoinScarabModule(Criteria criteria) 
        throws Exception
    {
        if (collScarabRModuleUsers == null)
        {
            if ( isNew() ) 
            {
               collScarabRModuleUsers = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabRModuleUserPeer.USER_ID, getUserId() );               
                   collScarabRModuleUsers = ScarabRModuleUserPeer.doSelectJoinScarabModule(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabRModuleUserPeer.USER_ID, getUserId() );               
               if ( !lastScarabRModuleUsersCriteria.equals(criteria)  )
            {
                collScarabRModuleUsers = ScarabRModuleUserPeer.doSelectJoinScarabModule(criteria);
            }
        }
        lastScarabRModuleUsersCriteria = criteria; 

        return collScarabRModuleUsers;
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
        l.setTurbineUser(this);
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
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
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
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
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
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
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
     * been saved, it will retrieve related ScarabIssueAttributeVotes from storage.
     */
    public Vector getScarabIssueAttributeVotesJoinScarabAttributeOption(Criteria criteria) 
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
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
                   collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabAttributeOption(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
               if ( !lastScarabIssueAttributeVotesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabAttributeOption(criteria);
            }
        }
        lastScarabIssueAttributeVotesCriteria = criteria; 

        return collScarabIssueAttributeVotes;
    }
      
      
      
                 
                                
            
        
   
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this TurbineUser is new, it will return
     * an empty collection; or if this TurbineUser has previously
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
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
                   collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabIssueAttributeValue(criteria);
            }
            
            
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabIssueAttributeVotePeer.USER_ID, getUserId() );               
               if ( !lastScarabIssueAttributeVotesCriteria.equals(criteria)  )
            {
                collScarabIssueAttributeVotes = ScarabIssueAttributeVotePeer.doSelectJoinScarabIssueAttributeValue(criteria);
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
                TurbineUserPeer.getMapBuilder()
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
                TurbineUserPeer.doInsert(this, dbCon);
            }
            else
            {
                TurbineUserPeer.doUpdate(this, dbCon);
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
                                        
                
          if (collScarabRModuleUsers != null )
          {
              for (int i=0; i<collScarabRModuleUsers.size(); i++)
              {
                  ((ScarabRModuleUser)collScarabRModuleUsers.elementAt(i)).save(dbCon);
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
     * @param int user_id
     */
    public void setPrimaryKey(
                      int user_id
                             ) throws Exception
    {
                     setUserId(user_id);
                }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                     setUserId( Integer.parseInt(st.nextToken()) );
                  }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getUserId()
                             ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "TurbineUser[" + getPrimaryKey() + "]";
    }

    public static Vector getTurbineUsers(ParameterParser pp)
        throws Exception
    {
        Vector v = new Vector();
        // look for classname in keys
        Enumeration keys = pp.keys();
        while ( keys.hasMoreElements() )
        {
            String ppKey = (String)keys.nextElement();
            String[] parsedString = StringUtils.parseObjectKey(ppKey);
            if ("TurbineUser".equalsIgnoreCase(
                     parsedString[StringUtils.PPKEY_CLASSNAME]))
            {
                TurbineUser obj = null;
                if (parsedString[StringUtils.PPKEY_ID].equals("PK") )
                {
                    obj = new TurbineUser();
                    v.addElement(obj);
  
                          obj.setUserId(Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]));
                        }
                else
                {
  
                          int user_id = Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]);
        

              
                    for ( int i=0; i<v.size(); i++) 
                    {
                        TurbineUser tempObj = 
                            (TurbineUser)v.get(i);
                        if ( user_id == tempObj.getUserId() ) 
                        {
                            obj = tempObj;
                            break;
                        }
                    }
                    if ( obj == null ) 
                    {
                        obj = new TurbineUser();
                        obj.setUserId(user_id);
                        v.addElement(obj);
                    }
                }
                String property = parsedString[StringUtils.PPKEY_PROPERTY];

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
    public TurbineUser copy() throws Exception
    {
        TurbineUser copyObj = new TurbineUser();
         copyObj.setUserId(user_id);
 
                                  
                
         Vector v = copyObj.getScarabIssueAttributeValues();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
                                         
                
         v = copyObj.getScarabRModuleUsers();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
                                         
                
         v = copyObj.getScarabIssueAttributeVotes();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
         
                        
        copyObj.setUserId(NEW_ID);
           return copyObj;
    }

}
