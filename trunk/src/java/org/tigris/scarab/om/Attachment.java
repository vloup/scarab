package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.*;
// import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
// import org.apache.turbine.util.Log;
import org.apache.turbine.util.upload.FileItem;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class Attachment 
    extends BaseAttachment
    implements Persistent
{
    public static final NumberKey FILE__PK = new NumberKey("1");
    public static final NumberKey COMMENT__PK = new NumberKey("2");
    public static final NumberKey URL__PK = new NumberKey("3");

    /**
     * Returns the data field converted to a string
     */
    public String getDataAsString() throws Exception
    {
        byte[] data = getData();
        String dataString = null;
        if ( data != null ) 
        {
            dataString = new String(data);
        }
        
        return dataString;
    }

    /**
     * Converts a String comment into a byte[]
     */
    public void setDataAsString(String data) throws Exception
    {
        setData(data.getBytes());
    }
    

    /**
     * There is no reason to reconstruct the FileItem, always returns null.
     * @return value of file.
     */
    public FileItem getFile() 
    {
        return null;
    }
    
    /**
     * Set the value of file.
     * @param v  Value to assign to file.
     */
    public void setFile(FileItem  v) 
    {
        setData(v.get());
        if ( getMimeType() == null ) 
        {
            setMimeType(v.getContentType());
        }
    }    
}



