package org.tigris.scarab.util.xmlissues;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Activity implements java.io.Serializable
{
    private final static Log log = LogFactory.getLog(Activity.class);

    private String id = null;
    private String attribute = null;
    private String oldNumericValue = null;
    private String newNumericValue = null;
    private String oldUser = null;
    private String newUser = null;
    private String oldOption = null;
    private String newOption = null;
    private String oldValue = null;
    private String newValue = null;
    private String description = null;
    private Attachment attachment = null;

    private boolean isNewActivity = true;
    
    public Activity()
    {
    }

    public boolean isNewActivity()
    {
        return isNewActivity;
    }
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setAttribute(String attribute)
    {
        this.attribute = attribute;
    }

    public String getAttribute()
    {
        return this.attribute;
    }

    public void setOldNumericValue(String oldNumericValue)
    {
        this.oldNumericValue = oldNumericValue;
        isNewActivity = false;
    }

    public String getOldNumericValue()
    {
        return this.oldNumericValue;
    }

    public void setNewNumericValue(String newNumericValue)
    {
        this.newNumericValue = newNumericValue;
    }

    public String getNewNumericValue()
    {
        return this.newNumericValue;
    }

    public void setOldUser(String oldUser)
    {
        this.oldUser = oldUser;
        isNewActivity = false;
    }

    public String getOldUser()
    {
        return this.oldUser;
    }

    public void setNewUser(String newUser)
    {
        this.newUser = newUser;
    }

    public String getNewUser()
    {
        return this.newUser;
    }

    public void setOldOption(String oldOption)
    {
        this.oldOption = oldOption;
        isNewActivity = false;
    }

    public String getOldOption()
    {
        return this.oldOption;
    }

    public void setNewOption(String newOption)
    {
        this.newOption = newOption;
    }

    public String getNewOption()
    {
        return this.newOption;
    }

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
        isNewActivity = false;
    }

    public String getOldValue()
    {
        return this.oldValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String getNewValue()
    {
        return this.newValue;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    public Attachment getAttachment()
    {
        return this.attachment;
    }
    
    public String toString()
    {
        return ("Activity Id: " + id);
    }
}
