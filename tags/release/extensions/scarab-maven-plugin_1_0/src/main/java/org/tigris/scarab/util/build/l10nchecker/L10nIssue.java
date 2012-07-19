package org.tigris.scarab.util.build.l10nchecker;

import java.text.MessageFormat;

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
 * Class that represents an issue template
 */
public abstract class L10nIssue
{
    /** ignore message (default) */
    public final static int MESSAGE_IGNORE = -1;
    
    /** INFORMATIONAL message */
    public final static int MESSAGE_INFO = 0;

    /** ERROR message */
    public final static int MESSAGE_ERROR = 1;

    /** WARNING message */
    public final static int MESSAGE_WARNING = 2;

    /**
     * Utility function to perform a message translation for a specific issue
     * @return The formatted string. In case the string cannot be formatted
     *  (i.e. MessageFormat.format () throws an exception), null is returned. 
     */
    public String formatMessage ()
    {
        try 
        {
            String out = MessageFormat.format(getMessageTemplate(), getParameters());;
            return out;
        }
        catch (IllegalArgumentException ex_iae)
        {
            System.err.println("Error processing " + getMessageTemplate() + ": " + ex_iae.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Return true in case the current issue is an error message
     * 
     * @return true, if the underlying issue is an issue representing 
     * an error.
     */
    public final boolean isError()
    {
        return MESSAGE_ERROR == getMessageType();
    }

    /**
     * Return true in case the current issue is a warning
     * 
     * @return true, if the underlying issue is an issue representing 
     * a warning.
     */
    public final boolean isWarning()
    {
        return MESSAGE_WARNING == getMessageType();
    }

    /**
     * Return true in case the current issue is an informational message
     * 
     * @return true, if the underlying issue is an issue representing 
     * an information.
     */
    public final boolean isInfo()
    {
        return MESSAGE_INFO == getMessageType();
    }
    
    /**
     * Return the message template that is used to display the error text.
     * 
     * @return Returns the messageTemplate.
     */
    abstract public String getMessageTemplate();

    /**
     * Get the parameters for a message
     * 
     * @return the parameters. The parameters have to be an array 
     * representing objects.
     */
    abstract public Object[] getParameters();
    
    /**
     * Return the message type for the current object. The message type
     * is retrieved from the corresponding entry in 
     * {@link L10nIssueTemplates}
     * 
     * @return Returns the messageType.
     */
    public final int getMessageType()
    {
        return L10nIssueTemplates.getMessageType(this.getClass());
    }
    
    /**
     * Set the severity of the current message
     * 
     * @param messageType The messageType to set.
     */
    public final void setMessageType(int messageType)
    {
        L10nIssueTemplates.setMessageType(this.getClass(), messageType);
    }
}
