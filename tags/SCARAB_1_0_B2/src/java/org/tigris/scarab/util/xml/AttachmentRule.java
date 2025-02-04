package org.tigris.scarab.util.xml;

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
import java.io.FileReader;
import java.io.FileWriter;

import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;

import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Attachment;

/**
 * Handler for the xpath "scarab/module/issue/attachment"
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class AttachmentRule extends BaseRule
{
    public AttachmentRule(Digester digester, String state)
    {
        super(digester, state);
    }
    
    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Attributes attributes) throws Exception
    {
        log().debug("(" + getState() + ") attachment begin");
        super.doInsertionOrValidationAtBegin(attributes);
    }
    
    protected void doInsertionAtBegin(Attributes attributes)
    {
        Attachment attachment = new Attachment();
        digester.push(attachment);
    }
    
    protected void doValidationAtBegin(Attributes attributes)
    {
    }
    
    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end() throws Exception
    {
        log().debug("(" + getState() + ") attachment end");
        super.doInsertionOrValidationAtEnd();
    }
    
    protected void doInsertionAtEnd()
        throws Exception
    {
        Attachment attachment = (Attachment)digester.pop();
        Issue issue = (Issue)digester.pop();
        
        attachment.setIssueId(issue.getIssueId());
        
        if (attachment.getFilePath() != null)
        {
            String path = attachment.getFilePath();
            String sourceFileName = path.substring(path.lastIndexOf(File.separator)+1);
            attachment.setFilePath(null);
            attachment.save();
            String newFile = attachment.getRepositoryDirectory(issue.getModule().getCode())
                + File.separator + sourceFileName.substring(0, sourceFileName.lastIndexOf('.')) + "_"
                + attachment.getPrimaryKey().toString()
                + sourceFileName.substring(sourceFileName.lastIndexOf('.'));
            
            // copy the file into its new location
            FileReader in = null;
            FileWriter out = null;
            try
            {
                in = new FileReader(path);
                out = new FileWriter(newFile);
                int c;
                while ((c = in.read()) != -1)
                {
                    out.write(c);
                }
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
            }
            
            attachment.setFilePath(newFile);
        }
        attachment.save();
        digester.push(issue);
    }
    
    protected void doValidationAtEnd()
    {
    }
}
