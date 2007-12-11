package org.tigris.scarab.util;

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

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.fulcrum.parser.StringValueParser;
import org.apache.turbine.RunData;
import org.tigris.scarab.om.Module;

/**
 * A Utility class for code that doesn't really go other places.
 *   
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id$
 */
public class ScarabUtil
{
    

    
    /**
     * System global initialization.
     * Useful for random stuff that needs initialization early.
     *
     */
    public static void initializeScarab()
    {
    }
        
    /**
     * Finds the first value for the named request parameter.  This is
     * useful to handle the case when there are more than one of the
     * named key fields present on a screen.
     *
     * @param runData Source of the export format information.
     * @param name The name of the request parameter to get a value
     * for.
     * @return The format type, or <code>null</code> if indeterminate.
     */
    public static String findValue(RunData runData, String name)
    {
        String[] possibilities = runData.getParameters().getStrings(name);
        if (possibilities != null)
        {
            for (int i = 0; i < possibilities.length; i++)
            {
                if (StringUtils.isNotEmpty(possibilities[i]))
                {
                    return possibilities[i];
                }
            }
        }
        return null;
    }

    /**
     * It uses the IssueIdParser to convert all issue id's into links.
     * The output is enclosed into a &lt,pre>...&lt,/pre> bracket pair so that
     * simple markup (line breaks, white spaces) is preserved.
     */
    public static String linkifyText(String input, ScarabLink link, Module module)
        throws Exception
    {
        StringBuffer sb = new StringBuffer(input.length() * 2);
        
        // THIS STUFF REALLY BELONGS IN ScarabGlobalTool.textToHtml
        //  ALSO REMOVED BECAUSE OF RADEOX
        // first get rid of any HTML crap
        String output = input;//ReferenceInsertionFilter.filter(input);
        //output = perlUtil.substitute(REGEX_NEWLINETOBR,output);
        //output = perlUtil.substitute(REGEX_MAILTO,output);
        //output = perlUtil.substitute(REGEX_URL,output);
        
        List result = IssueIdParser.tokenizeText(module, output);
        String engine = module.getCommentRenderingEngine();
        if(engine.equals("plaintext"))
        {
            sb.append("<pre>");
        }
        for (Iterator itr = result.iterator(); itr.hasNext();)
        {
            Object tmp = itr.next();
            if (tmp instanceof String)
            {
                sb.append(tmp);
            }
            else
            {
                List tmpList = (List)tmp;
                link.addPathInfo("id", (String)tmpList.get(1));
                link.setLabel((String)tmpList.get(0));
                String bar = link.setAlternateText((String)tmpList.get(0)).toString();
                sb.append(bar);
            }
        }
        if(engine.equals("plaintext"))
        {
            sb.append("</pre>");
        }
        return sb.toString();
    }

    /**
     * Check whether Object array contains passed in object.
     */
    public static final boolean contains(Object[] array, Object obj)
    {
        boolean contains = false;
        if (array != null && array.length > 0)
        {
            for (int i = 0; i < array.length; i++) 
            {
                Object element = array[i];
                if (obj.equals(element))
                {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    /**
     * Hack to replace the string "\n" with EOL characters...
     * string.replaceAll("\\n","\n") does not work.
     * 
     * Originally part of SimpleHandler but useful as utility method.
     */
    public static String fixEOLs(final String str)
    {
        final int idx = str.indexOf("\\n");
        if (idx != -1)
        {
            return str.substring(0, idx) + "\n"
                    + fixEOLs(str.substring(idx + "\\n".length()));
        }
        else
        {
            return str;
        }
    }

    /**
     * URL encodes <code>in</code>.     * 
     * @param in the String to encode.
     * @return the url-encoded string.
     */
    public static final String urlEncode(String in)
    {
        try
        {
            return URLEncoder.encode(in, "UTF-8");
        }
        catch ( Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static String filterNonXml(String input)
    {
        char output[] = new char[input.length()];
        for (int i=0; i<input.length(); i++)
        {
            char ch = input.charAt(i);
            if (isXMLCharacter(ch))
            {
                output[i] = ch;
            }
            else
            {
                output[i] = ' ';
            }
        }
        return new String(output);
    }
    
    private static boolean isXMLCharacter(int c) {

        if (c == '\n') return true;
        if (c == '\r') return true;
        if (c == '\t') return true;

        if (c < 0x20) return false;
        if (c <= 0xD7FF) return true;
        if (c < 0xE000) return false;
        if (c <= 0xFFFD) return true;
        if (c < 0x10000) return false;
        if (c <= 0x10FFFF) return true;

        return false;
    }

    public static StringValueParser parseURL(String url) throws Exception
    {
        StringValueParser parser = new StringValueParser();
        parser.setCharacterEncoding("UTF-8");
        parser.parse(url, '&', '=', true);
        return parser;
    }

}
