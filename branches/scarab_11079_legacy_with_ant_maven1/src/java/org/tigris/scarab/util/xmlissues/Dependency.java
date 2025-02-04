package org.tigris.scarab.util.xmlissues;

/* ================================================================
 * Copyright (c) 2000-2006 CollabNet.  All rights reserved.
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

public class Dependency implements java.io.Serializable
{
    /** @deprecated no point in using this anymore. **/
    private String id = null;
    private String type = null;
    private String child = null;
    private String parent = null;
    private boolean deleted = false;

    public Dependency()
    {
    }

    /** @deprecated no point in using this anymore. **/
    public void setId(String id)
    {
        this.id = id;
    }

    /** @deprecated no point in using this anymore. **/
    public String getId()
    {
        return this.id;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public void setChild(String child)
    {
        this.child = child;
    }

    public String getChild()
    {
        return this.child;
    }

    public void setParent(String parent)
    {
        this.parent = parent;
    }

    public String getParent()
    {
        return this.parent;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public boolean getDeleted()
    {
        return this.deleted;
    }

    public boolean equals(Object obj)
    {
        if( obj instanceof Dependency ){
            final Dependency dependency = (Dependency)obj;
            // Check the opposing matching dependency as well.
            //  It makes the presumption that immediate cyclic dependencies are not permitted.
            return ((child.equals(dependency.getChild()) && parent.equals(dependency.getParent())) 
                    || (child.equals(dependency.getParent()) && parent.equals(dependency.getChild())))
                    && type.equals(dependency.getType());
        }else{
            return super.equals(obj);
        }
    }

    public int hashCode()
    {
        int hash = 37;
        hash += child.hashCode() + parent.hashCode(); // Allows parent-child to be swapped around & give same hashcode.
        hash *= 17;
        hash += type.hashCode();
        return hash;
    }

    public String toString()
    {
        return ("Type: " + type + " Parent: " + parent + " Child: " + child);
    }
}
