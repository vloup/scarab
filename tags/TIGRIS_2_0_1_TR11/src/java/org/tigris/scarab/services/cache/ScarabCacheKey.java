package org.tigris.scarab.services.cache;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.io.Serializable;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Category;
import org.apache.fulcrum.pool.RecyclableSupport;

public class ScarabCacheKey
    extends RecyclableSupport
    implements Serializable
{
    private static final Category log = 
        Category.getInstance("org.apache.torque");

    int n;
    private Serializable instanceOrClass;
    private String method;
    private Serializable arg1;
    private Serializable arg2;
    private Serializable arg3;
    private Serializable[] moreThanThree;

    public ScarabCacheKey()
    {
    }
    
    public ScarabCacheKey(Serializable instanceOrClass, String method)
    {
        init(instanceOrClass, method);
    }

    public ScarabCacheKey(Serializable instanceOrClass, String method, 
                          Serializable arg1)
    {
        init(instanceOrClass, method, arg1);
    }

    public ScarabCacheKey(Serializable instanceOrClass, String method,
                          Serializable arg1, Serializable arg2)
    {
        init(instanceOrClass, method, arg1, arg2);
    }

    public ScarabCacheKey(Serializable instanceOrClass, String method,
                          Serializable arg1, Serializable arg2, 
                          Serializable arg3)
    {
        init(instanceOrClass, method, arg1, arg2, arg3);
    }

    public ScarabCacheKey(Serializable[] moreThanThree)
    {
        init(moreThanThree);
    }

    /**
     * Initialize key for method with no arguments.
     *
     * @param instanceOrClass the Object on which the method is invoked.  if 
     * the method is static, a String representing the class name is used.
     * @param method the method name
     */
    public void init(Serializable instanceOrClass, String method) 
    {
        n = 0;
        this.instanceOrClass = instanceOrClass;
        this.method = method;
    }

    /**
     * Initialize key for method with one argument.
     *
     * @param instanceOrClass the Object on which the method is invoked.  if 
     * the method is static, a String representing the class name is used.
     * @param method the method name
     * @param arg1 first method arg, may be null
     */
    public void init(Serializable instanceOrClass, String method, 
                     Serializable arg1)
    {
        n = 1;
        this.instanceOrClass = instanceOrClass;
        this.method = method;
        this.arg1 = arg1;
    }

    /**
     * Initialize key for method with two arguments.
     *
     * @param instanceOrClass the Object on which the method is invoked.  if 
     * the method is static, a String representing the class name is used.
     * @param method the method name
     * @param arg1 first method arg, may be null
     * @param arg2 second method arg, may be null
     */
    public void init(Serializable instanceOrClass, String method, 
                     Serializable arg1, Serializable arg2)
    {
        n = 2;
        this.instanceOrClass = instanceOrClass;
        this.method = method;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }


    /**
     * Initialize key for method with two arguments.
     *
     * @param instanceOrClass the Object on which the method is invoked.  if 
     * the method is static, a String representing the class name is used.
     * @param method the method name
     * @param arg1 first method arg, may be null
     * @param arg2 second method arg, may be null
     */
    public void init(Serializable instanceOrClass, String method, 
                     Serializable arg1, Serializable arg2,
                     Serializable arg3)
    {
        n = 3;
        this.instanceOrClass = instanceOrClass;
        this.method = method;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    /**
     * Initialize key for method with more than two arguments.
     *
     * @param Serializable[] where 
     * [0]=>the Object on which the method is invoked
     * if the method is static, a String representing the class name is used.
     * [1]=>the method name
     * [n] where n>1 are the method arguments
     */
    public void init(Serializable[] keys)
    {
        n = keys.length-2;
        this.instanceOrClass = keys[0];
        this.method = (String)keys[1];
        if (n>0) 
        {
            this.arg1 = keys[2];
            if (n>1) 
            {
                this.arg2 = keys[3];
                if (n>2) 
                {
                    this.arg2 = keys[4];
                    if (n>3) 
                    {
                        this.moreThanThree = keys;                
                    }
                }
            }
        }
    }

    public boolean equals(Object obj)
    {
        boolean equal = false;
        if ( obj instanceof ScarabCacheKey ) 
        {
            ScarabCacheKey sck = (ScarabCacheKey)obj;
            equal = sck.n == n;
            equal &= ObjectUtils.equals(sck.instanceOrClass, instanceOrClass);
            equal &= ObjectUtils.equals(sck.method, method);
            if (n > 0) 
            {
                equal &= ObjectUtils.equals(sck.arg1, arg1);
                if (n > 1) 
                {
                    equal &= ObjectUtils.equals(sck.arg2, arg2);
                    if (n > 2) 
                    {
                        equal &= ObjectUtils.equals(sck.arg3, arg3);
                        if (n > 3) 
                        {
                            for (int i=5; i<n+2; i++) 
                            {
                                equal &= ObjectUtils.equals(sck.moreThanThree[i], 
                                                            moreThanThree[i]);
                            }
                        }
                    }
                }
            }

            if (equal) 
            {
                log.debug("Saved db hit on " + instanceOrClass + "::" 
                          + method + ". YAY!");
            }
        }            
        return equal;
    }

        public int hashCode()
        {
            int h = instanceOrClass.hashCode();
            h += method.hashCode();
            if (n > 0) 
            {
                h += (arg1 == null ? 0 : arg1.hashCode());
                if (n > 1) 
                {
                    h += (arg2 == null ? 0 : arg2.hashCode());
                    if (n > 2) 
                    {
                        h += (arg3 == null ? 0 : arg3.hashCode());
                        if (n > 3) 
                        {
                            for (int i=5; i<n+2; i++) 
                            {
                                h+= (moreThanThree[i] == null ?
                                     0 : moreThanThree[i].hashCode());
                            }
                        }        
                    }    
                }
            }
            return h;
        }

    // ****************** Recyclable implementation ************************

    /**
     * Disposes the object after use. The method is called when the
     * object is returned to its pool.  The dispose method must call
     * its super.
     */
    public void dispose()
    {
        super.dispose();
        instanceOrClass = null;
        method = null;
        arg1 = null;
        arg2 = null;
        arg3 = null;
        moreThanThree = null;
    }
}
