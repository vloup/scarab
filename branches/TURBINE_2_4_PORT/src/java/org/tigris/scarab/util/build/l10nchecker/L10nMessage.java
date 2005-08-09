package org.tigris.scarab.util.build.l10nchecker;


/* ================================================================
 * Copyright (c) 2005 CollabNet.  All rights reserved.
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

/**
 * Interface that contains a message (information, warning, error) that can be
 * created during initialisation

 * @author sreindl
 */
public class L10nMessage
{
    /* line number */
    private int lineNumber;

    /* message text */
    private String messageText;

    /* corresponding L10nObject */
    private L10nKey l10nObject;
    
    /* The corresponding issue */
    private L10nIssue issue;

    /**
     * INTERNAL-should not have been called
     */
    private L10nMessage()
    {
        throw new RuntimeException("This should not have been called");
    }

    /**
     * Create a message of type INFORMATION at line #lineNo with message
     * #issue.
     * 
     * @param lineNo The line where the message appeared
     * @param issue The issue to be created.
     */
    public L10nMessage(int lineNo, L10nIssue issue)
    {
        lineNumber = lineNo;
        this.issue = issue;
        this.messageText = issue.formatMessage();
    }

    /* getter setter methods */
    /**
     * Return the line number.
     * 
     * @return Returns the lineNumber.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Set the line number
     * 
     * @param lineNumber The lineNumber to set.
     */
    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    /**
     * Return the message text.
     * 
     * @return Returns the messageText.
     */
    public String getMessageText()
    {
        return messageText;
    }

    /**
     * Set the message text.
     * 
     * @return Returns the l10nObject.
     */
    public L10nKey getL10nObject()
    {
        return l10nObject;
    }

    /**
     * Return the corresponding L10nKey.
     * 
     * @param object The l10nKey to set.
     */
    public void setL10nObject(L10nKey object)
    {
        l10nObject = object;
    }
    
    /**
     * Return the issue assiciated to this message.
     * 
     * @return Returns the issue.
     */
    public L10nIssue getIssue()
    {
        return issue;
    }
}
