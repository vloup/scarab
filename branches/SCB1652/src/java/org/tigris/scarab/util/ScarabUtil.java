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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.perl.Perl5Util;
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
    private static final String REGEX_URL =
        "s%\\b(?:[hH][tT]{2}[pP]|[fF][tT][pP]):[^ \\t\\n<>\"]+[\\w/]*%<a href=\"$0\">$0</a>%g";
    private static final String REGEX_MAILTO =
        "s%\\b(?:([mM][aA][iI][lL][tT][oO])):([^ \\t\\n<>\"]+[\\w/])*%<a href=\"$0\">$2</a>%g";
    private static final String REGEX_NEWLINETOBR =
        "s%\\n%<br />%g";

    private static Perl5Util perlUtil = new Perl5Util();

    
    /**
     * System global initialization.
     * Useful for random stuff that needs initialization early.
     *
     */
    public static void initializeScarab()
    {
        initializeXsltFactory();
    }
        
    /**
     *   REVERT TO JAVA 5 XSLT
     *    Quartz by default uses the Xalan xslt library.
     *     And this doesn't get bundled into the war, 
     *      and under Java5 will result in a ClassNotFoundException.
     *    Java5 provides it's own xslt engine so will use it instead.
     *         Really very messy, please show me a better way...
     *         This might be done through editing properties files,
     *         but then it requires manually changing for every jvm-version change!
     **/
    public static void initializeXsltFactory()
    {
            
        final StringBuffer info = new StringBuffer(
                "Checking java version... "+ System.getProperty("java.version"));
        try
        {
            final String version = System.getProperty("java.version").substring(0,3);
            final float v = Float.parseFloat(version);
            if( v > 1.49 )
            {
                info.append("   using Java5 TransformerFactory!");
                System.setProperty("javax.xml.transform.TransformerFactory",
                        "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
            }
            
        }
        catch(NumberFormatException nfe)
        {
            Log.get().error(nfe);
            info.append("    failed to parse! "+nfe.getLocalizedMessage());
        }
        finally
        {
            Log.get().info(info);
        }
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
     * First, it converts all HTML markup into entities.
     * Then convert http:// ftp:// mailto: links into URL's.
     * Lastly, it uses the IssueIdParser to convert all issue id's into links.
     * The output is enclosed into a &lt,pre>...&lt,/pre> bracket pair so that
     * simple markup (line breaks, white spaces) is preserved.
     */
    public static String linkifyText(String input, ScarabLink link, Module module)
        throws Exception
    {
        StringBuffer sb = new StringBuffer(input.length() * 2);
        // first get rid of any HTML crap
        String output = ReferenceInsertionFilter.filter(input);
        //output = perlUtil.substitute(REGEX_NEWLINETOBR,output);
        output = perlUtil.substitute(REGEX_MAILTO,output);
        output = perlUtil.substitute(REGEX_URL,output);
        List result = IssueIdParser.tokenizeText(module, output);
        //sb.append("<pre>");
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
        //sb.append("</pre>");
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
     * URL encodes <code>in</code>. If the string is null, nothing will be
     * written. This method is faster than urlEncodeSlow if the string to
     * encode does not contain any characters needing encoding. It adds some
     * penalty for strings which actually need to be encoded. for short strings
     * ~20 characters the upside is a 75% decrease. while the penalty is a 10%
     * increase. As many query parameters do not need encoding even in i18n
     * applications it should be much better to delay the byte conversion.
     * 
     * @param in the String to encode.
     * @return the url-encoded string.
     */
    public static final String urlEncode(String in)
    {
        if (in == null)
        {
            return null;
        }

        if (in.length() == 0)
        {
            return "";
        }

        StringBuffer out = new StringBuffer(in.length());
        char[] chars = in.toCharArray();

        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];

            if (c < 128 && safe[c])
            {
                out.append(c);
            }
            else if (c == ' ')
            {
                out.append('+');
            }
            else
            {
                // since we need to encode we will give up on
                // doing it the fast way and convert to bytes.
                return out
                    .append(urlEncodeSlow(in.substring(i).getBytes()))
                    .toString();
            }
        }
        return out.toString();
    }

    /**
     * URL encodes <code>in</code>. Code 'borrowed' from DynamicURI.java in
     * the Jakarta Turbine 3 package. We use this code instead of
     * java.net.Encoder because Encoder.encode is deprecated and we don't feel
     * like putting a dependency on JDK 1.4.1. This should work fine for our
     * purposes.
     * 
     * @param in a non-empty String to encode.
     * @return the url-encoded string.
     */
    private static final String urlEncodeSlow(byte[] bytes)
    {
        StringBuffer out = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++)
        {
            char c = (char)bytes[i];

            if (c < 128 && safe[c])
            {
                out.append(c);
            }
            else if (c == ' ')
            {
                out.append('+');
            }
            else
            {
                byte toEscape = bytes[i];
                out.append('%');
                int low = (toEscape & 0x0f);
                int high = ((toEscape & 0xf0) >> 4);
                out.append(HEXADECIMAL[high]);
                out.append(HEXADECIMAL[low]);
            }
        }
        return out.toString();
    }
	
    /**
     * Array mapping hexadecimal values to the corresponding ASCII characters.
     */
    private static final char[] HEXADECIMAL =
        {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
        };

    /**
     * Characters that need not be encoded. This is much faster than using a
     * BitSet, and for such a small array the space cost seems justified.
     */
    private static boolean[] safe = new boolean[ 128 ];

    /** Static initializer for {@link #safe} */
    static
    {
        for (int i = 'a'; i <= 'z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = 'A'; i <= 'Z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = '0'; i <= '9'; i++)
        {
            safe[ i ] = true;
        }

        safe['-'] = true;
        safe['_'] = true;
        safe['.'] = true;
        safe['!'] = true;
        safe['~'] = true;
        safe['*'] = true;
        safe['\''] = true;
        safe['('] = true;
        safe[')'] = true;
    }
}
