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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.apache.oro.util.Cache;
import org.apache.oro.util.CacheLRU;

/**
 * This is a utility class for processing regular expressions
 * based on oro.
 *
 * @author <a href="mailto:dabboussaxess.com">Hussayn Dabbous</a>
 * @version $Id: ReferenceInsertionFilter.java 7365 2003-03-15 21:56:59Z jon $
 */
public class RegexProcessor extends Object
{

    protected static Cache patterns      = new CacheLRU();
    protected static Cache substitutions = new CacheLRU();

    private PatternCompiler compiler;

    public RegexProcessor()
    {
        compiler = new Perl5Compiler();
    }

    public String process(String input, String regex, String substitution) throws MalformedPatternException
    {
        String result = input;
        Pattern pattern = fetchPattern(regex);

        Substitution subst = (Substitution) substitutions.getElement(substitution);
        if (subst == null)
        {
            subst = new Perl5Substitution(substitution);
            substitutions.addElement(substitution, subst);
        }

        Perl5Matcher matcher = new Perl5Matcher();

        result = Util.substitute(matcher, pattern, subst, input, Util.SUBSTITUTE_ALL);

        return result;
    }

    /**
     * Returns true, if the string exactly matches the given pattern.
     * @param input
     * @param regex
     * @return
     * @throws MalformedPatternException
     */
    public boolean matches(String input, String regex) throws MalformedPatternException
    {
        boolean result;
        Pattern pattern = fetchPattern(regex);
        Perl5Matcher matcher = new Perl5Matcher();
        result = matcher.matches(input, pattern);
        return result;
    }
    
    private Pattern fetchPattern(String regex) throws MalformedPatternException {
        Pattern pattern = (Pattern) patterns.getElement(regex);
        if (pattern == null)
        {
            pattern = compiler.compile(regex);
            patterns.addElement(regex, pattern);
        }
        return pattern;
    }

}
