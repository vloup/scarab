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
    public AttachmentRule(ImportBean ib)
    {
        super(ib);
    }
    
    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Attributes attributes) throws Exception
    {
        log().debug("(" + getImportBean().getState() + 
            ") attachment begin");
        super.doInsertionOrValidationAtBegin(attributes);
    }
    
    protected void doInsertionAtBegin(Attributes attributes)
    {
        Attachment attachment = new Attachment();
        getDigester().push(attachment);
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
        super.doInsertionOrValidationAtEnd();
        log().debug("(" + getImportBean().getState() + ") attachment end");
    }
    
    protected void doInsertionAtEnd()
        throws Exception
    {
        // FIXME! I have probably messed this up, as I did not follow 
        // the logic. Are we taking an existing Attachment that has been
        // stored previously and assigning it to a new issue? -jdm
        Attachment attachment = (Attachment)getDigester().pop();
        String path = attachment.getFullPath();

        Issue issue = (Issue)getDigester().pop();
        attachment.setIssueId(issue.getIssueId());
        String newPath = attachment.getFullPath();

        if (path != null)
        {
            // copy the file into its new location
            FileReader in = null;
            FileWriter out = null;
            try
            {
                in = new FileReader(path);
                out = new FileWriter(newPath);
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
        }
        attachment.save();
        getDigester().push(issue);
    }
    
    protected void doValidationAtEnd()
    {
    }
}
