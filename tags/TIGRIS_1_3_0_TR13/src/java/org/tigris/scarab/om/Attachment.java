package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.io.File;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.pool.DBConnection;

import org.apache.turbine.Log;
import org.apache.turbine.Turbine;

import org.apache.fulcrum.upload.FileItem;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.upload.UploadService;

//import org.apache.commons.collections.ExtendedProperties;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

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
    public static final NumberKey MODIFICATION__PK = new NumberKey("4");
    
    private static String fileRepo = null;

    private FileItem fileItem;
    
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
     * Makes sure to only save the simple filename.
     *
     * @param name a <code>String</code> value
     */
    public void setFileName(String name)
    {
        if ( name == null ) 
        {
            super.setFileName(null);
        }
        else 
        {
            // look for both '/' and '\' as path separators
            int start = name.lastIndexOf('/')+1;
            if ( start == 0 ) 
            {
                start = name.lastIndexOf('\\')+1;                
            }
            // don't allow spaces
            super.setFileName(name.substring(start).replace(' ', '_'));    
        }
    }
    
    /**
     * There is no reason to reconstruct the FileItem, always returns null.
     * @return value of file.
     */
    public FileItem getFile() 
    {
        return fileItem;
    }
    
    /**
     * Set the value of file.
     * @param v  Value to assign to file.
     */
    public void setFile(FileItem  v) 
    {
        fileItem = v;
        if ( getMimeType() == null ) 
        {
            setMimeType(v.getContentType());
        }
        setFileName(v.getFileName());
    }    
    
    /**
     * Populates fields for a text (non-file) type of attachment.
     */
    public void setTextFields(ScarabUser user, Issue issue, NumberKey typeId) 
        throws Exception
    {
        setIssue(issue);
        setTypeId(new NumberKey(typeId));
        setMimeType("text/plain");
        //setCreatedDate(new Date());
        setCreatedBy(user.getUserId());
    }
        
    /**
     * Calls super.save(DBConnection) and also checks for a FileItem.  if one
     * exists the file is moved to its final location.
     *
     * @param dbCon a <code>DBConnection</code> value
     * @exception Exception if an error occurs
     */
    public void save(DBConnection dbCon)
        throws TorqueException
    {
        if ( getIssue().isNew() ) 
        {
            throw new TorqueException("Cannot save an attachment before saving"
                                      + " the issue to which it is attached.");
        }
        super.save(dbCon);
        
        try
        {
        FileItem file = getFile();
        if ( file != null ) 
        {        
            File uploadFile = 
                new File(getRepositoryDirectory(),getRelativePath());
            File parent = uploadFile.getParentFile();
            if ( !parent.exists() ) 
            {
            mkdirs(parent);
            }                
            file.write(uploadFile.getPath());
        }
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
    }        

    synchronized private static void mkdirs(File path)
    {
        if ( !path.exists() ) 
        {
            path.mkdirs();
        }
    }

    public String getRelativePath()
        throws ScarabException, Exception
    {
        if ( isNew() ) 
        {
            throw new ScarabException("Path is not set prior to saving.");
        }        
        String path = null;
        String filename = getFileName();
        if ( filename != null ) 
        {
            // moduleId/(issue_IdCount/1000)/issueID_attID_filename
            StringBuffer sb = new StringBuffer(30+filename.length());
            Issue issue = getIssue();
            sb.append("mod").append(issue.getModule().getQueryKey());
            sb.append('/');
            int count = issue.getIdCount();
            sb.append(count/1000).append('/').append(issue.getUniqueId())
                .append('_').append(getQueryKey()).append('_')
                .append(filename);
            path = sb.toString();
        }
        return path;
    }

    public String getFullPath()
        throws Exception
    {
        String path = null;
        String prefix = getRepositoryDirectory();
        String suffix = getRelativePath();
        if ( suffix != null ) 
        {
            path = new StringBuffer(prefix.length() + suffix.length() + 1)
            .append(prefix).append(File.separator).append(suffix).toString();
        }
        
        return path;
    }


    /**
     * Get the repository path info
     * @return String
     */
    public static String getRepositoryDirectory()
        throws Exception
    {
        if ( fileRepo == null ) 
        {
            String testPath = Turbine.getConfiguration()
                .getString(ScarabConstants.ATTACHMENTS_REPO_KEY);
            if ( testPath.startsWith("/") ) 
            {
                File testDir = new File(testPath);                
                if ( !testDir.exists() ) 
                {
                    mkdirs(testDir);
                }
                fileRepo = testPath;
            }
            else 
            {                
                // test for existence within the webapp directory.
                String testPath2 = Turbine.getRealPath(testPath);
                File testDir = new File(testPath2);
                if ( !testDir.exists() ) 
                {
                    mkdirs(testDir);
                }
                fileRepo = testPath2;
            }
        }
        return fileRepo;
    }
}
