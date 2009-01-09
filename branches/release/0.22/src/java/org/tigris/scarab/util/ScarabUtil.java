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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.fulcrum.parser.StringValueParser;
import org.apache.turbine.RunData;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.Localizable;
/**
 * A Utility class for code that doesn't really go other places.
 *   
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id$
 */
public class ScarabUtil
{
    
	
	 private static final Pattern RELATIVE_DATE_PATTERN = Pattern.compile(
	            "\\s*now\\s*(([+-])\\s*(\\d+)|)\\s*",
	    		Pattern.CASE_INSENSITIVE
	        );

    
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
     */
    public static String linkifyText(String input, ScarabLink link, Module module)
        throws Exception
    {
        StringBuffer sb = new StringBuffer(input.length() * 2);
        List result = IssueIdParser.tokenizeText(module, input);
        for (Iterator itr = result.iterator(); itr.hasNext();)
        {
            Object tmp = itr.next();
            if (tmp instanceof String)
            {
                sb.append(tmp);
            }
            else
            {
                final List tmpList = (List)tmp;
                final String id = (String)tmpList.get(1);
                final String defaultText = IssueManager.getIssueById(id).getDefaultText();
                final String attributes = "alt=\"" + defaultText + "\" title=\"" + defaultText + "\"";

                link.addPathInfo("id", id);
                link.addPathInfo("tab", "1");
                link.setLabel((String)tmpList.get(0));
                final String bar = link.setAttributeText(attributes).toString();
                sb.append(bar);
            }
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
    
    private static Date parseDate(String dateString, Localizable dateFormat, Locale locale) 
    throws ParseException
    {
    	Date date;
    	
    	ScarabLocalizationTool l10n = new ScarabLocalizationTool();
        l10n.init(locale);
    	
    	String[] patterns = {
                            l10n.get(dateFormat),
                            ScarabConstants.ISO_DATETIME_PATTERN };
    	date = parseDate(dateString, patterns);
    	return date;
    	
    }
    
    /**
     * Attempts to parse a String as a Date, trying each pattern in
     * turn until the string is successfully parsed or all patterns
     * have been tried.
     *
     * @param s a <code>String</code> value that should be converted
     * to a <code>Date</code>.
     * @param patterns patterns to be used for conversion
     * @return the equivalent <code>Date</code> if the string could
     * be parsed. 
     * @throws ParseException if input String is null, or the string
     * could not be parsed.
     */
    private static Date parseDate(String s, String[] patterns)
        throws ParseException
    {
        if (s == null) 
        {
            throw new ParseException("Input string was null", -1);
        }

        SimpleDateFormat formatter = new SimpleDateFormat();

        for (int i = 0; i < patterns.length; i++) 
        {
        	Date date = null;
        	formatter.applyPattern(patterns[i]);
            try
            {
            	date = formatter.parse(s);
            }
            catch (ParseException ex)
            {
                // ignore, because we have to try all patterns
            }            
            if (date != null) 
            {
                return date;
            }
        }
        
        throw new ParseException("Date could not be parsed with any"
                                 + " of the provided date patterns.", -1);
    }
    
    
    /**
     * Attempts to parse a atring as a date, first using the locale-sepcific
     * short date format, and then the ISO standard "yyyy-mm-dd". If it sees
     * a ':' character in the date string then the string will be interpreted
     * as a date <b>and</b> time. Throws a ParseException if the String does
     * not contain a suitable format.
     *
     * @param dateString a <code>String</code> value
     * @param locale the locale to use when determining the date patterns
     * to try.
     * @param addTwentyFourHours if no time is given in the date string and
     * this flag is true, then 24 hours - 1 msec will be added to the date.
     * @return a <code>Date</code> value
     */
    public static Date parseDate(String dateString, boolean addTwentyFourHours, Locale locale)
        throws ParseException
    {
        Date date = null;
        if (dateString != null) 
        {
            Matcher m = RELATIVE_DATE_PATTERN.matcher(dateString);
        	if(m.matches())
        	{
                date = new Date();

        		    String dateDifference = m.group(3);
        		    String sign = m.group(2);
        		    if(dateDifference!=null)
        		{
                    long hours = Long.parseLong(dateDifference);  
                    if (sign.equals("-")) hours = hours * -1;
                    date.setTime(date.getTime() + hours * 3600000 );
        		}
        	}
            else if (dateString.indexOf(':') == -1)
            {
                date = parseDate(dateString, L10NKeySet.ShortDatePattern, locale);
                
                if (addTwentyFourHours) 
                {                
                    date.setTime(date.getTime() + 86399999);
                }
            }
            else
            {
                date = parseDate(dateString, L10NKeySet.ShortDateTimePattern, locale);        
            }
        }
        
        return date;
    }
    
    /**
     * Attempts to parse a date passed in the query page.
    */
    public static boolean validateDateFormat(String date, Locale locale)
    {
        boolean valid = true;
        try
        {
        	
            parseDate(date, false, locale);
            
        }
        catch (Exception e)
        {
        	
            valid = false;
       
        }
        return valid;
    }

}
