package org.tigris.scarab.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.AttributeValue;

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

/** 
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision$ $Date$
 */
public class DateAttribute extends StringAttribute
{
    private static SimpleDateFormat internalFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
    
    /**
     * Receives the value in yyyyMMddHHmmssSS format and returns it
     * formatted according to the mask parameter.
     * If the value is not parseable, it will be returned unchanged. 
     * @param value
     * @param mask
     * @return
     */
    public static String dateFormat(String value, String mask)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(mask);
        String val = value;
        try
        {
            if (val == null)
                val = "";
            else
                val = sdf.format(internalFormat.parse(value));
        }
        catch (ParseException e)
        {
            // Will return the same value
        }
        return val;
    }
    /**
     * Receives the value in the format defined bu 'mask' and
     * returns it formatted in internal (yyyyMMddHHmmssSS) format.
     * If the value is not parseable, it will be returned unchanged.
     * @param value
     * @param mask
     * @return
     */
    public static String internalDateFormat(String value, String mask)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(mask);
        String val = value;
        try
        {
            val = internalFormat.format(sdf.parse(value));
        }
        catch (ParseException e)
        {
            // Will return the same value
        }
        return val;
    }
    
    /**
     * Utility method that will convert every DateAttribute in a list from the user's
     * locale format to the internal (yyyyMMddHHmmssSS) format.
     * @param issue
     * @param mask
     * @throws TorqueException
     */
    public static void convertDateAttributes(Collection attributeValues, String mask) throws TorqueException
    {
        for (Iterator iter = attributeValues.iterator(); iter.hasNext(); )
        {
            AttributeValue av = (AttributeValue)iter.next();
            if (av instanceof DateAttribute)
            {
                av.setValue(internalDateFormat(av.getValue(), mask));
            }
        }
    }
}
