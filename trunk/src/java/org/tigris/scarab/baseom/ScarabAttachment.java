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
public class ScarabAttachment extends BaseObject
{
    /** the value for the attachment_id field */
    private int attachment_id;
    /** the value for the issue_id field */
    private int issue_id;
    /** the value for the attachment_type_id field */
    private int attachment_type_id;
    /** the value for the attachment_name field */
    private String attachment_name;
    /** the value for the attachment_data field */
    private byte[] attachment_data;
    /** the value for the attachment_file_path field */
    private String attachment_file_path;
    /** the value for the attachment_mime_type field */
    private String attachment_mime_type;
    /** the value for the modified_by field */
    private int modified_by;
    /** the value for the created_by field */
    private int created_by;
    /** the value for the modified_date field */
    private Date modified_date;
    /** the value for the created_date field */
    private Date created_date;
    /** the value for the deleted field */
    private boolean deleted;


    /**
     * Get the AttachmentId
     * @return int
     */
     public int getAttachmentId()
     {
          return attachment_id;
     }

        
    /**
     * Set the value of AttachmentId
     */
     public void setAttachmentId(int v ) 
     {
  
  

            if (this.attachment_id != v)
           {
              this.attachment_id = v;
              setModified(true);
          }
     }
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
     * Get the TypeId
     * @return int
     */
     public int getTypeId()
     {
          return attachment_type_id;
     }

            
    /**
     * Set the value of TypeId
     */
     public void setTypeId(int v ) throws Exception
     {
                  if ( aScarabAttachmentType != null && !aScarabAttachmentType.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
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
          return attachment_name;
     }

        
    /**
     * Set the value of Name
     */
     public void setName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attachment_name, v) )

           {
              this.attachment_name = v;
              setModified(true);
          }
     }
    /**
     * Get the Data
     * @return byte[]
     */
     public byte[] getData()
     {
          return attachment_data;
     }

        
    /**
     * Set the value of Data
     */
     public void setData(byte[] v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attachment_data, v) )

           {
              this.attachment_data = v;
              setModified(true);
          }
     }
    /**
     * Get the FilePath
     * @return String
     */
     public String getFilePath()
     {
          return attachment_file_path;
     }

        
    /**
     * Set the value of FilePath
     */
     public void setFilePath(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attachment_file_path, v) )

           {
              this.attachment_file_path = v;
              setModified(true);
          }
     }
    /**
     * Get the MimeType
     * @return String
     */
     public String getMimeType()
     {
          return attachment_mime_type;
     }

        
    /**
     * Set the value of MimeType
     */
     public void setMimeType(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attachment_mime_type, v) )

           {
              this.attachment_mime_type = v;
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
    /**
     * Get the Deleted
     * @return boolean
     */
     public boolean getDeleted()
     {
          return deleted;
     }

        
    /**
     * Set the value of Deleted
     */
     public void setDeleted(boolean v ) 
     {
  
  

           if (this.deleted != v)
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
            // obj.addScarabAttachments(this);
        }
        return aScarabIssue;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttachmentType object
     *
     * @param ScarabAttachmentType v
     */
    private ScarabAttachmentType aScarabAttachmentType;
    public void setScarabAttachmentType(ScarabAttachmentType v) throws Exception
    {
        aScarabAttachmentType = null;
           setTypeId(v.getAttachmentTypeId());
           aScarabAttachmentType = v;
    }

                     
    public ScarabAttachmentType getScarabAttachmentType() throws Exception
    {
        if ( aScarabAttachmentType==null && (this.attachment_type_id>0) )
        {
            aScarabAttachmentType = ScarabAttachmentTypePeer.retrieveByPK(this.attachment_type_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabAttachmentType obj = ScarabAttachmentTypePeer.retrieveByPK(this.attachment_type_id);
            // obj.addScarabAttachments(this);
        }
        return aScarabAttachmentType;
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
                ScarabAttachmentPeer.getMapBuilder()
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
                ScarabAttachmentPeer.doInsert(this, dbCon);
            }
            else
            {
                ScarabAttachmentPeer.doUpdate(this, dbCon);
                setNew(false);
            }
        }

              alreadyInSave = false;
      }
      }

                                                            
    /** 
     * Set the Id using pk values.
     *
     * @param int attachment_id
     */
    public void setPrimaryKey(
                      int attachment_id
                                                                                                                                                                 ) 
    {
                     setAttachmentId(attachment_id);
                                                                                                                                                    }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                     setAttachmentId( Integer.parseInt(st.nextToken()) );
                  }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getAttachmentId()
                                                                                                                                                                 ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabAttachment[" + getPrimaryKey() + "]";
    }

    public static Vector getScarabAttachments(ParameterParser pp)
        throws Exception
    {
        Vector v = new Vector();
        // look for classname in keys
        Enumeration keys = pp.keys();
        while ( keys.hasMoreElements() )
        {
            String ppKey = (String)keys.nextElement();
            String[] parsedString = StringUtils.parseObjectKey(ppKey);
            if ("ScarabAttachment".equalsIgnoreCase(
                     parsedString[StringUtils.PPKEY_CLASSNAME]))
            {
                ScarabAttachment obj = null;
                if (parsedString[StringUtils.PPKEY_ID].equals("PK") )
                {
                    obj = new ScarabAttachment();
                    v.addElement(obj);
  
                          obj.setAttachmentId(Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]));
                        }
                else
                {
  
                          int attachment_id = Integer
                        .parseInt(parsedString[StringUtils.PPKEY_ID]);
        

              
                    for ( int i=0; i<v.size(); i++) 
                    {
                        ScarabAttachment tempObj = 
                            (ScarabAttachment)v.get(i);
                        if ( attachment_id == tempObj.getAttachmentId() ) 
                        {
                            obj = tempObj;
                            break;
                        }
                    }
                    if ( obj == null ) 
                    {
                        obj = new ScarabAttachment();
                        obj.setAttachmentId(attachment_id);
                        v.addElement(obj);
                    }
                }
                String property = parsedString[StringUtils.PPKEY_PROPERTY];

                        if ( property.equalsIgnoreCase("IssueId") ) 
                {
                    obj.setIssueId(pp.getInt(ppKey));
                }
                        if ( property.equalsIgnoreCase("TypeId") ) 
                {
                    obj.setTypeId(pp.getInt(ppKey));
                }
                        if ( property.equalsIgnoreCase("Name") ) 
                {
                    obj.setName(pp.getString(ppKey));
                }
                        if ( property.equalsIgnoreCase("Data") ) 
                {
                    obj.setData(pp.getBytes(ppKey));
                }
                        if ( property.equalsIgnoreCase("FilePath") ) 
                {
                    obj.setFilePath(pp.getString(ppKey));
                }
                        if ( property.equalsIgnoreCase("MimeType") ) 
                {
                    obj.setMimeType(pp.getString(ppKey));
                }
                        if ( property.equalsIgnoreCase("ModifiedBy") ) 
                {
                    obj.setModifiedBy(pp.getInt(ppKey));
                }
                        if ( property.equalsIgnoreCase("CreatedBy") ) 
                {
                    obj.setCreatedBy(pp.getInt(ppKey));
                }
                        if ( property.equalsIgnoreCase("ModifiedDate") ) 
                {
                    obj.setModifiedDate(pp.getDate(ppKey));
                }
                        if ( property.equalsIgnoreCase("CreatedDate") ) 
                {
                    obj.setCreatedDate(pp.getDate(ppKey));
                }
                        if ( property.equalsIgnoreCase("Deleted") ) 
                {
                    obj.setDeleted(pp.getBoolean(ppKey));
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
    public ScarabAttachment copy() throws Exception
    {
        ScarabAttachment copyObj = new ScarabAttachment();
         copyObj.setAttachmentId(attachment_id);
         copyObj.setIssueId(issue_id);
         copyObj.setTypeId(attachment_type_id);
         copyObj.setName(attachment_name);
         copyObj.setData(attachment_data);
         copyObj.setFilePath(attachment_file_path);
         copyObj.setMimeType(attachment_mime_type);
         copyObj.setModifiedBy(modified_by);
         copyObj.setCreatedBy(created_by);
         copyObj.setModifiedDate(modified_date);
         copyObj.setCreatedDate(created_date);
         copyObj.setDeleted(deleted);
 
  
                        
        copyObj.setAttachmentId(NEW_ID);
                                                                                        return copyObj;
    }

}
