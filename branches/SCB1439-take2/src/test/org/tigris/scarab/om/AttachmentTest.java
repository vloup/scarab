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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.torque.om.NumberKey;
import org.tigris.scarab.test.BaseTurbineTestCase;


/**
 * A Testing Suite for the om.Attachment class.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id$
 */
public class AttachmentTest extends BaseTurbineTestCase
{
    private Attachment comment = null;
    private Attachment fileAttachment = null;
    private Issue issue = null;
    private ScarabUserTestObjectFactory testUsers = new ScarabUserTestObjectFactory();

    public void setUp()throws Exception {
    	super.setUp();
    	comment = AttachmentManager.getInstance();
    	fileAttachment = AttachmentManager.getInstance();
    	issue = IssueManager.getInstance(new NumberKey("1"));
    	
    }


    public void testSaveComment() throws Exception
    {
        // save comment
        comment.setName("comment");
        comment.setData("Test comment");
        comment.setTextFields(testUsers.getUser1(),issue, Attachment.COMMENT__PK);
        comment.save();

        //
        // Make sure the comment was persisted correctly.
        //
        Attachment comment2 = AttachmentManager.getInstance(comment.getAttachmentId());
        assertEquals(comment2.getName(), comment.getName());

    }

    public void saveFile() throws Exception
    {
        //FileItem fileItem = new DefaultFileItem(scarab/images/", "logo.gif", "image/jpeg", 6480, 10000);
        FileItemFactory factory = new DiskFileItemFactory(6480, null);
        String textFieldName = "textField";

        FileItem fileItem = factory.createItem(
                textFieldName,
                "image/jpeg",
                true,
                "images/logo.gif"
        );
        fileItem.getOutputStream();
        fileAttachment.setFile(fileItem);
        fileAttachment.setName(fileItem.getName());
        fileAttachment.setMimeType("image/jpeg");
        fileAttachment.setCreatedBy(testUsers.getUser1().getUserId());
        issue.addFile(fileAttachment, testUsers.getUser1());      
        issue.save();
        // need to save the attachments AFTER the issue has been created
        issue.doSaveFileAttachments(testUsers.getUser1());
    }

    public void testGetRepositoryDirectory() throws Exception
    {
        
        String attachmentDir = new String("attachments");
        File testPath = new File(Attachment.getRepositoryDirectory());
        assertTrue(testPath.getPath(),testPath.getPath().endsWith(attachmentDir));
    }

    public void testGetRelativePath() throws Exception
    {
        saveFile();
        File control = new File("mod" + issue.getModuleId().toString() 
                      + "/" + issue.getIdCount()/1000 + "/" 
                      + issue.getUniqueId() + "_" 
                      + fileAttachment.getQueryKey() 
                      + "_" + fileAttachment.getFileName());
        File testPath = new File(fileAttachment.getRelativePath());
        assertEquals(control.getPath(), testPath.getPath());
    }

    public void testGetFullPath() throws Exception
    {
        saveFile();
        File control = new File(fileAttachment.getFullPath());
        File testPath = new File(Attachment.getRepositoryDirectory(),
                                 fileAttachment.getRelativePath());
        assertEquals(control.getPath(), testPath.getPath());
    }

    public void testSaveUrl() throws Exception
    {
        // save comment
        Attachment url = AttachmentManager.getInstance();
        url.setIssue(issue);
        url.setTypeId(AttachmentTypePeer.URL_PK);
        url.setMimeType("");
        url.setName("foo");
        url.setData("www.foo.com");
        url.save();
        assertEquals(url.getName(),"foo");
        assertEquals(url.getData(),"http://www.foo.com");

        url.setData("mailto:admin@foo.com");
        url.save();
        assertEquals(url.getData(),"mailto:admin@foo.com");
    }
}
