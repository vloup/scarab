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

import java.util.HashMap;
import java.util.Map;

/**
 * Static utility class to hold the issue message types as defined by the 
 * task
 */
public class L10nIssueTemplates
{
    private static Map issueMessageTypes = null;
        
    /**
     * Set the severity of an issue.
     * 
     * In case this function is called the first time, 
     * issueMessageTypes is initialized.
     * 
     * @param _clazz The class to set the severity for.
     * 
     * @param messageType The new severity
     */
    public static void setMessageType (Class _clazz, int messageType)
    {
        if (issueMessageTypes == null)
        {
            issueMessageTypes = new HashMap ();
        }
        issueMessageTypes.put (_clazz, new Integer (messageType)); 
    }
    
    /**
     * Retrive the message type of the class representing this issue.
     * 
     * @param _clazz The class representing the issue (usually one
     * of org.tigris.scarab.util.build.l10nchecker.issues.*Issue
     * 
     * @return The message type (see {@link L10nIssue} for details.
     * In case the class is not represented in issueMessageTypes, the 
     * function returns null. 
     */
    public static int getMessageType (Class _clazz)
    {
        if (issueMessageTypes == null || !issueMessageTypes.containsKey(_clazz))
        {
            return L10nIssue.MESSAGE_IGNORE;
        }
        return ((Integer)issueMessageTypes.get(_clazz)).intValue();
    }
    
    /**
     * Clear all definitions
     */
    public static void reset ()
    {
        issueMessageTypes = null;
    }
}
