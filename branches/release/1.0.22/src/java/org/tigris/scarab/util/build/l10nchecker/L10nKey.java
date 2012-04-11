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

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Localisation key. This key holds the information that is representing a
 * single l10n property key.
 * 
 * @author sreindl
 */
public class L10nKey
{
    /**
     * The key name
     */
    private String key;

    /**
     * The actual value
     */
    private String value;

    /**
     * The line number where this key has been defined first.
     */
    private int lineNo;

    /**
     * Number of attributes this key processes
     */
    private int attributeCount = 0;

    /**
     * If this property is set to true, the key has to be translated in every
     * translation file. This might be used for example in case the translator
     * or the translation version might be used. In case a translation is
     * missing and this flag is set in the reference file, an error will be
     * reported.
     */
    private boolean needTrans = false;

    /**
     * Indicates that the value is part of the translated property file but is
     * not going to be translated.
     */
    private boolean noTrans = false;

    /**
     * create a key
     * 
     * @param key
     *            The key itself
     * @param value
     *            it's value
     * @param lineNo
     *            The line number where the key has been defined
     */
    public L10nKey(String key, String value, int lineNo)
    {
        this.key = key;
        this.value = value;
        this.lineNo = lineNo;
    }

    /**
     * @return Returns the key.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return Returns the lineNo.
     */
    public int getLineNo()
    {
        return lineNo;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @return Returns the attributeCount.
     */
    public int getAttributeCount()
    {
        return attributeCount;
    }

    /**
     * @param attributeCount
     *            The attributeCount to set.
     */
    public void setAttributeCount(int attributeCount)
    {
        this.attributeCount = attributeCount;
    }

    /**
     * @return Returns the needTrans.
     */
    public boolean isNeedTrans()
    {
        return needTrans;
    }

    /**
     * @param needTrans
     *            The needTrans to set.
     */
    public void setNeedTrans(boolean needTrans)
    {
        this.needTrans = needTrans;
    }

    /**
     * @return Returns the noTrans.
     */
    public boolean isNoTrans()
    {
        return noTrans;
    }

    /**
     * @param noTrans
     *            The noTrans to set.
     */
    public void setNoTrans(boolean noTrans)
    {
        this.noTrans = noTrans;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(-995326837, 526330937).append(this.key)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof L10nKey))
        {
            return false;
        }
        L10nKey rhs = (L10nKey) object;
        return this.key.equals(rhs.key);
    }
}
