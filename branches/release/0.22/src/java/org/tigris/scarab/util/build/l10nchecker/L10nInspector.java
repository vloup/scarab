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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.build.l10nchecker.issues.CantParseLineIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.DefinedTwiceIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.DifferentAttributeCountIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.IllegalPatternIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.NoTransAllowedIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.NotInReferenceIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.NotTranslatedIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.TranslatedTwiceDiffIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.TranslatedTwiceIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.TranslationMissingIssue;
import org.tigris.scarab.util.build.l10nchecker.issues.TranslationRequiredIssue;

/**
 * Scarab language checker.
 * 
 * <p>
 * A L10nInspector represents a class that is reading a property file (see
 * {@link #setReference(String)}) containing l10n properties as a reference and then
 * running through a given list of other property files to check if all
 * localisations that are needed are given.
 * 
 * <p>
 * A property language files that need to be checked against can be passed the
 * reference file can be set by {@link #checkFile(String)}.
 * 
 * <p>
 * The results can be retrieved by using the get* methods.
 * 
 * @author sreindl
 */
public class L10nInspector
{

    /* regular expression matching stuff */
    private static String COMMENT_TRANS = "^\\s*#(\\+|-)TRANS.*$";

    private static String COMMENT_REGEX = "^\\s*(#.*)?$";

    private static String COMMAND_REGEX = "^\\s*([^=\\s]+)\\s*=\\s*(.*)$";

    private static Perl5Compiler compiler = new Perl5Compiler();

    private static Perl5Matcher matcher = new Perl5Matcher();

    private static Pattern commentPattern = null;

    private static Pattern commandPattern = null;

    private static Pattern transPattern = null;
    
    /* properties of reference file */
    private Hashtable refProperties;

    /* filename of reference file */
    private String refFileName;

    /* filename of file to be checked */
    private String checkFileName;

    /* statistical information */
    private int linesRead = 0;

    /* messages generated during parsing */
    private List messages = null;
    
    /**
     * Create a standard instance
     */
    public L10nInspector() throws MalformedPatternException
    {
        try
        {
            L10nIssueTemplates.reset();
            commentPattern = compiler.compile(COMMENT_REGEX);
            commandPattern = compiler.compile(COMMAND_REGEX);
            transPattern = compiler.compile (COMMENT_TRANS);
        } catch (MalformedPatternException exMP)
        {
            Log.get().fatal(exMP);
            throw exMP; // rethrow
        }
        messages = new ArrayList();
    }

    /**
     * Set the reference file to be used. This call resets all internal
     * variables and resets all counters
     * 
     * @param aRefFile
     *            The file to be used as a reference
     * 
     * @throws IOException
     *             In case the file cannot be read.
     */
    public int setReference(String aRefFile) throws IOException
    {
        refFileName = aRefFile;

        refProperties = new Hashtable();
        File inFile = new File(refFileName);
        if (!inFile.canRead())
        {
            throw new IOException("Cannot read reference file " + aRefFile);
        }
        loadReferenceFile(inFile);
        return refProperties.size();
    }

    /**
     * Function to return only the errors that occured during parsing
     * 
     * @return Returns the errors.
     */
    public List getErrors()
    {
        List errs = new ArrayList(messages.size());
        Iterator it = messages.iterator();

        while (it.hasNext())
        {
            L10nMessage msg = (L10nMessage) it.next();
            if (msg.getIssue().isError())
            {
                errs.add(msg);
            }
        }
        return errs;
    }

    /**
     * Return a list of warnings generated during processing
     * 
     * @return Returns the warnings.
     */
    public List getWarnings()
    {
        List warnings = new ArrayList(messages.size());
        Iterator it = messages.iterator();

        while (it.hasNext())
        {
            L10nMessage msg = (L10nMessage) it.next();
            if (msg.getIssue().isWarning())
            {
                warnings.add(msg);
            }
        }
        return warnings;
    }

    /**
     * Generate a list of information messages generated during processing.
     * 
     * @return Returns the information messages.
     */
    public List getInfos()
    {
        List infos = new ArrayList(messages.size());
        Iterator it = messages.iterator();

        while (it.hasNext())
        {
            L10nMessage msg = (L10nMessage) it.next();
            if (msg.getIssue().isInfo())
            {
                infos.add(msg);
            }
        }
        return infos;
    }

    /**
     * Return the messages collected.
     * 
     * @return messages
     */
    public List getMessages()
    {
        return messages;
    }

    /**
     * Check if the parsing/checking resulted in errors.
     * 
     * @return true if errors have been seen during loading/parsing
     */
    public boolean hasErrors()
    {
        return getErrors().size() > 0;
    }

    /**
     * Check an language file.
     * 
     * <p>
     * TODO: Sophisticated exception handling
     * 
     * @return Number of (unique)translation entries
     */
    public int checkFile(String filename) throws IOException
    {
        BufferedReader inStream = null;
        String inLine;
        Hashtable seen = new Hashtable();
        int lineNo = 0;
        boolean doNotTrans = false;        

        messages.clear();
        checkFileName = filename;
        try
        {
            inStream = new BufferedReader(new FileReader(filename));
            while ((inLine = inStream.readLine()) != null)
            {
                lineNo++;
                if (matcher.matches(inLine, transPattern))
                {
                    MatchResult result = matcher.getMatch();
                    if (result.group(1).equals("-"))
                    {
                        doNotTrans = true;
                    }
                }                
                else if (matcher.matches(inLine, commentPattern))
                {
                    // skip comment lines
                    continue;
                }
                else if (matcher.contains(inLine, commandPattern))
                {
                    // extract key and value and insert them into
                    // the reference pattern list
                    MatchResult result = matcher.getMatch();
                    String key = result.group(1);
                    String value = result.group(2);
                    L10nKey l10nKey = new L10nKey(key, value, lineNo);
                    if (value.indexOf('{') >= 0) {
                        // handle attributes
                        try
                        {
                            MessageFormat fmt = new MessageFormat (value);
                            int attributeCount = fmt.getFormats().length;
                            l10nKey.setAttributeCount(attributeCount);
                        }
                        catch (IllegalArgumentException exIAE) 
                        {
                            addMessage (lineNo, new IllegalPatternIssue(key), null);
                            continue;
                        }
                    }
                    l10nKey.setNoTrans(doNotTrans);
                    doNotTrans = false;
                    
                    // we've seen this key
                    if (seen.contains(l10nKey))
                    {
                        // same entry in translation twice
                        L10nKey orig = (L10nKey) seen.get(l10nKey);
                        if (orig.getValue().equals(l10nKey.getValue()))
                        {
                            // same entry with same translation -> info
                            addMessage (lineNo, new TranslatedTwiceIssue(key, orig.getLineNo()),
                                    l10nKey);
                        } 
                        else
                        {
                            // same key, different translation -> error
                            addMessage (lineNo, new TranslatedTwiceDiffIssue(key, orig.getLineNo()), 
                                    l10nKey);
                        }
                        seen.remove(orig); // remove original key
                        seen.put(l10nKey, l10nKey);
                        continue; // do not create other errors here
                    }
                    seen.put(l10nKey, l10nKey);
                    L10nKey ref = (L10nKey) refProperties.get(key);
                    if (ref == null)
                    {
                        // error: key not in reference
                        addMessage (lineNo, new NotInReferenceIssue (key), l10nKey);
                    } 
                    else
                    {
                        if (ref.isNoTrans())
                        {
                            // This entry is not supposed to be translated
                            addMessage (lineNo, new NoTransAllowedIssue(key), l10nKey);
                        }
                        else if (ref.getValue().equals(value))
                        {
                            if (ref.isNeedTrans())
                            {
                                // info: Key not found in translation set. but required
                                addMessage (lineNo, new TranslationRequiredIssue(key), ref);
                            }
                            else
                            {
                                // not translated. Will only add the warning if the
                                // key has not been marked as NOTRANS in the target language
                                // bundle.
                                if (!l10nKey.isNoTrans())
                                    addMessage (lineNo, new NotTranslatedIssue (key), l10nKey);
                            }
                        } 
                        else if (ref.getAttributeCount() != l10nKey.getAttributeCount()) 
                        {
                            // different number of attributes in reference and translation
                            addMessage (lineNo, 
                                    new DifferentAttributeCountIssue(key, 
                                            	l10nKey.getAttributeCount(), 
                                            	ref.getAttributeCount()),
                                            l10nKey);
                        }
                    }
                } else
                {
                    addMessage(lineNo, new CantParseLineIssue(inLine), null);
                }
            }
        } catch (IOException exIO)
        {
            Log.get().error(exIO);
            exIO.printStackTrace();
            // cleanup resources
            refProperties.clear();
            messages.clear();
            throw exIO; // rethrow
        } catch (Exception e)
        {
            Log.get().error(e);
            e.printStackTrace();
            // cleanup resources
            refProperties.clear();
            messages.clear();
            throw new IOException(e.getMessage());
        }

        // look for missing messages
        Iterator it = refProperties.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            L10nKey refKey = (L10nKey)refProperties.get(key); 
            if (!seen.contains(refKey))
            {
                if (refKey.isNeedTrans())
                {
                    // info: Key not found in translation set. but required
                    addMessage (-1, new TranslationRequiredIssue(key), refKey);
                }
                else
                {
                    // info: Key not found in translation set
                    addMessage (-1, new TranslationMissingIssue (key), refKey);
                }
            }
        }
        this.linesRead = lineNo;
        return seen.size();
    }

    /* private functions */

    /**
     * Load a the reference file.
     * 
     * <p>
     * TODO: Sophisticated exception handling
     */
    private void loadReferenceFile(File inFile) throws IOException
    {
        BufferedReader inStream = null;
        String inLine;
        int lineNo = 0;
        boolean transNeeded = false;
        boolean doNotTrans = false;
        
        try
        {
            inStream = new BufferedReader(new FileReader(inFile));
            while ((inLine = inStream.readLine()) != null)
            {
                lineNo++;
                if (matcher.matches(inLine, transPattern))
                {
                    MatchResult result = matcher.getMatch();
                    if (result.group(1).equals("+"))
                    {
                        transNeeded = true;
                    }
                    else 
                    {
                        doNotTrans = true;
                    }
                }
                else if (matcher.matches(inLine, commentPattern))
                {
                    // skip comment lines
                    continue;
                }
                else if (matcher.contains(inLine, commandPattern))
                {
                    // extract key and value and insert them into
                    // the reference pattern list
                    MatchResult result = matcher.getMatch();
                    String key = result.group(1);
                    String value = result.group(2);
                    L10nKey l10nKey = new L10nKey(key, value, lineNo);
                    if (value.indexOf('{') >= 0)
                    {
                        // handle attributes
                        try
                        {
                            MessageFormat fmt = new MessageFormat (value);
                            int attributeCount = fmt.getFormats().length;
                            l10nKey.setAttributeCount(attributeCount);
                        }
                        catch (IllegalArgumentException exIAE) 
                        {
                            addMessage (lineNo, new IllegalPatternIssue(key), null);
                            continue;
                        }
                    }
                    l10nKey.setNeedTrans(transNeeded);
                    l10nKey.setNoTrans(doNotTrans);
                    // reset values 
                    transNeeded = false;
                    doNotTrans = false;
                    if (refProperties.get(key) != null)
                    {
                        // error: key already there
                        L10nKey orig = (L10nKey) refProperties.get(key);
                        if (orig.getValue().equals(l10nKey.getValue()))
                        {
                            addMessage(lineNo, new DefinedTwiceIssue(key, orig.getLineNo()),
                                    l10nKey);
                        } else {
                            // even worse: same key with different values
                            addMessage(lineNo, new DefinedTwiceIssue (key, orig.getLineNo()),
                                    l10nKey);
                        }
                        continue;
                    }
                    // add value
                    refProperties.put(key, l10nKey);
                } else
                {
                    addMessage(lineNo, new CantParseLineIssue (inLine), null);
                }
            }
        } catch (IOException exIO)
        {
            exIO.printStackTrace();
            Log.get().error(exIO);
            // cleanup resources
            refProperties.clear();
            messages.clear();
            throw exIO; // rethrow
        } catch (Exception e)
        {
            Log.get().error(e);
            // cleanup resources
            e.printStackTrace();
            refProperties.clear();
            messages.clear();
            throw new IOException(e.getMessage());
        }
        this.linesRead = lineNo;
    }

    /* Add an error message */
    private void addMessage(int lineNo, L10nIssue issue, L10nKey l10nKey)
    {
        L10nMessage err = new L10nMessage(lineNo, issue);
        if (l10nKey != null)
        {
            err.setL10nObject(l10nKey);
        }
        messages.add(err);
    }

    /**
     * Main line. 
     * 
     * <p>
     * The pathes here are hardcoded, please change accordingly if you want to 
     * use the test main code
     */
    public static void main(String[] args)
    {
        L10nInspector ins = null;
        System.err.println ("This is only used for internal tests");
        try
        {
            ins = new L10nInspector();
        } catch (MalformedPatternException exMP)
        {
            System.exit(1); // we cannot continue
        }
        try
        {
            File f = new File(".");
            System.err.println("We are here:" + f.getAbsolutePath());
            ins.setReference("src/conf/classes/ScarabBundle_en.properties");
        } catch (IOException exIO)
        {
            exIO.printStackTrace();
            System.exit(1);
        }
        if (ins.getErrors().size() > 0)
        {
            Iterator it = ins.getErrors().iterator();
            while (it.hasNext())
            {
                L10nMessage data = (L10nMessage) it.next();
                System.err.println("E " + data.getLineNumber() + ": "
                        + data.getMessageText());
            }
        }
        if (ins.getWarnings().size() > 0)
        {
            Iterator it = ins.getWarnings().iterator();
            while (it.hasNext())
            {
                L10nMessage data = (L10nMessage) it.next();
                System.err.println("W " + data.getLineNumber() + ": "
                        + data.getMessageText());
            }
        }
        if (ins.hasErrors())
        {
            //System.exit (1);
        }
        System.out.println("--- checking de");
        try
        {
            int lines = ins
                    .checkFile("src/conf/classes/ScarabBundle_es.properties");
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        List msgs = ins.getMessages();
        Iterator it = msgs.iterator();
        while (it.hasNext())
        {
            L10nMessage msg = (L10nMessage) it.next();
            if (msg.getIssue().isError())
            {
                System.out.print('E');
            } else if (msg.getIssue().isWarning())
            {
                System.out.print('W');
            } else if (msg.getIssue().isInfo())
            {
                System.out.print('I');
            }
            if (msg.getLineNumber() > 0)
            {
                System.out.print(" " + msg.getLineNumber());
            }
            System.out.println(": " + msg.getMessageText());
        }
    }
}
