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
 * Attachments contain data associated with an issue.  It used to be that
 * an issue could have multiple attachments of a given type but only one
 * value for a given Attribute.  Attributes are now multi-valued, so the
 * difference is blurred in some cases.  A comment given as a reason for a 
 * modification to attribute values is considered an Attachment.  
 * Notes and urls are also considered attachments, though these two could 
 * probably be implemented as attributes (with some ui redesign).     
 * The obvious form of attachment is a file uploaded and associated with
 * an issue, such as a screenshot showing an error or a patch.
 */
public class Attachment 
    extends BaseAttachment
    implements Persistent
{
    /** ObjectKey for a file type attachment */
    public static final NumberKey FILE__PK = new NumberKey("1");
    /** ObjectKey for a note/comment type attachment */
    public static final NumberKey COMMENT__PK = new NumberKey("2");
    /** ObjectKey for a url type attachment */
    public static final NumberKey URL__PK = new NumberKey("3");
    /** ObjectKey for a reason for modification type attachment */
    public static final NumberKey MODIFICATION__PK = new NumberKey("4");
    
    /** Path to the base location for storing files */
    private static String fileRepo = null;

    /** 
     * The FileItem that is associated with the attachment as it is uploaded
     * from an html form.
     */
    private FileItem fileItem;
    
    /**
     * Returns the data field converted to a string for attachments that
     * have their data stored within the database (all except files).
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
     * Makes sure to only save the simple filename which is the part
     * following the last path separator.  This is appended as the last
     * part of the the path returned by getRelativePath() following the 
     * It would generally be set to original filename as given on the 
     * client that uploaded the file.  Spaces are replaced by underscores.
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
     * This is not used, but required by the bean introspector used by intake.
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
    
    /**
     * creates the directory given by path, if it does not already exist
     */
    synchronized private static void mkdirs(File path)
    {
        if ( !path.exists() ) 
        {
            path.mkdirs();
        }
    }

    /** 
     * The path to an attachment file relative to the base file repository.
     * Files are saved according to: 
     * <code>moduleId/(issue_IdCount/1000)/issueID_attID_filename</code>
     * where moduleId and attId are primary keys of the related module and
     * this attachment.  issueID is the unique id generally used to specify
     * the issue within the ui.  issue_IdCount is the numerical suffix of
     * the unique id.  So if the pk of module PACS is 201 and this attachment
     * pk is 123 the path would be: 201/0/PACS5_123_diff.txt or if the issue 
     * count were higher: 201/2/PACS2115_123_diff.txt.  The first two 
     * directories are used to keep the number of files per directory
     * reasonable while the issue unique id and the final textual filename
     * allow someone browsing the file system to be better able to pick
     * out relevant files.
     */ 
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

    /**
     * Prepends the base repository path to the path returned 
     * by getRelativePath(). 
     */
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
     * Get the repository path info as given in the configuration.  if the
     * path begins with a '/', it is assumed to be absolute.  Otherwise
     * the path is constructed relative to the webapp directory.
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
