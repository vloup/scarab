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

/**
 * A L10nInspector represents a class that is reading a property file (see
 * #setReference(String)) containing l10n properties as a reference and then
 * running through a given list of other property files to check if all
 * localisations that are needed are given.
 * 
 * A property language files that need to be checked against can be passed the
 * reference file can be set by setLanguageFile(String).
 * 
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

    private int errCount = 0;

    private int warningCount = 0;

    private int infoCount = 0;

    /* messages generated during parsing */
    private List messages = null;

    /* errors, warnings and messages */
    private static String ERR_DEFINED_TWICE = "Key {0} defined twice (first at line {1})";

    private static String ERR_DEFINED_TWICE_DIFF = "Key {0} defined twice with different values (first at line {1})";

    private static String ERR_NOT_DEFINED = "Key {0} not defined in reference";
    
    private static String ERR_NO_TRANS_ALLOWED = "Key {0} is not supposed to be translated";

    private static String ERR_TRANS_REQUIRED = "Key {0} has to to be translated";

    private static String ERR_TRANS_DIFFERENT = "Key {0} translated twice with different texts (also at line {1})";
    
    private static String ERR_ILLEGAL_PATTERN = "Key {0} contains an illegal pattern";

    private static String WARN_TRANS_SAME = "Key {0} translated twice (also at line {1})";
    
    private static String WARN_DIFF_ATTR_COUNT = "Key {0} contains different number of attributes ({1}) than reference ({2})";

    private static String INFO_NOT_TRANS = "Key {0} has not been translated";

    private static String INFO_TRANS_MISSING = "Key {0} is missing in localisation";

    /**
     * Create a standard instance
     */
    public L10nInspector() throws MalformedPatternException
    {
        try
        {
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
        errCount = 0;
        warningCount = 0;
        infoCount = 0;
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
        List errs = new ArrayList(errCount);
        Iterator it = messages.iterator();

        while (it.hasNext())
        {
            L10nMessage msg = (L10nMessage) it.next();
            if (msg.isError())
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
            if (msg.isWarning())
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
            if (msg.isInfo())
            {
                infos.add(msg);
            }
        }
        return infos;
    }

    /**
     * Return the messages collected
     * 
     * @return messages
     */
    public List getMessages()
    {
        return messages;
    }

    /**
     * Check if the parsing/checking resulted in errors
     * 
     * @return true if errors have been seen during loading/parsing
     */
    public boolean hasErrors()
    {
        return errCount > 0;
    }

    /**
     * Check an language file
     * 
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

        messages.clear();
        errCount = 0;
        warningCount = 0;
        infoCount = 0;
        checkFileName = filename;
        try
        {
            inStream = new BufferedReader(new FileReader(filename));
            while ((inLine = inStream.readLine()) != null)
            {
                lineNo++;
                if (matcher.matches(inLine, commentPattern))
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
                            String errMsg = MessageFormat.format(ERR_ILLEGAL_PATTERN,
                                    new Object[]
                                    { key, new Integer(l10nKey.getLineNo()) });
                            addError(lineNo, errMsg, l10nKey);
                            continue;
                        }
                    }
                    // we've seen this key
                    if (seen.contains(l10nKey))
                    {
                        // same entry in translation twice
                        L10nKey orig = (L10nKey) seen.get(l10nKey);
                        if (orig.getValue().equals(l10nKey.getValue()))
                        {
                            // same entry with same translation -> info
                            String warnMsg = MessageFormat.format(
                                    WARN_TRANS_SAME, new Object[]
                                    { key, new Integer(orig.getLineNo()) });
                            addWarning(lineNo, warnMsg, l10nKey);
                        } else
                        {
                            // same key, different translation -> error
                            String errMsg = MessageFormat.format(
                                    ERR_TRANS_DIFFERENT, new Object[]
                                    { key, new Integer(orig.getLineNo()) });
                            addError(lineNo, errMsg, l10nKey);
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
                        String errMsg = MessageFormat.format(ERR_NOT_DEFINED,
                                new Object[]
                                { key });
                        addError(lineNo, errMsg, l10nKey);
                    } 
                    else
                    {
                        if (ref.isNoTrans())
                        {
                            // This entry is not supposed to be translated
                            String errMsg = MessageFormat.format(
                                    ERR_NO_TRANS_ALLOWED, new Object[]
                                                                     { key });
                            addError(lineNo, errMsg, l10nKey);
                        }
                        else if (ref.getValue().equals(value))
                        {
                            // not translated
                            String warnMsg = MessageFormat.format(
                                    INFO_NOT_TRANS, new Object[]
                                    { key });
                            addInfo(lineNo, warnMsg, l10nKey);
                        } 
                        else if (ref.getAttributeCount() != l10nKey.getAttributeCount()) 
                        {
                            // different number of attributes in reference and translation
                            String warnMsg = MessageFormat.format(
                                    WARN_DIFF_ATTR_COUNT, new Object[]
                                    { key, new Integer(l10nKey.getAttributeCount()), 
                                            new Integer (ref.getAttributeCount()) });
                            addWarning(lineNo, warnMsg, l10nKey);
                        }
                    }
                } else
                {
                    addError(lineNo, "Cannot parse line '" + inLine + "'", null);
                }
            }
        } catch (IOException exIO)
        {
            Log.get().error(exIO);
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
            if (!seen.contains(key))
            {
                if (((L10nKey)refProperties.get(key)).isNeedTrans())
                {
                    // info: Key not found in translation set
                    String errMsg = MessageFormat.format(ERR_TRANS_REQUIRED,
                            new Object[]
                                       { key });
                    // MUST BE TRANSLATED
                    addError(-1, errMsg, (L10nKey) refProperties.get(key));
                }
                else
                {
                    // info: Key not found in translation set
                    String infoMsg = MessageFormat.format(INFO_TRANS_MISSING,
                            new Object[]
                                       { key });
                    addInfo(-1, infoMsg, (L10nKey) refProperties.get(key));
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
                            String errMsg = MessageFormat.format(ERR_ILLEGAL_PATTERN,
                                    new Object[]
                                    { key, new Integer(l10nKey.getLineNo()) });
                            addError(lineNo, errMsg, l10nKey);
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
                            String errMsg = MessageFormat.format(ERR_DEFINED_TWICE,
                                    new Object[]
                                               { key, new Integer(orig.getLineNo()) });
                            addError(lineNo, errMsg, l10nKey);
                        } else {
                            // even worse: same key with different values
                            String errMsg = MessageFormat.format(ERR_DEFINED_TWICE_DIFF,
                                    new Object[]
                                               { key, new Integer(orig.getLineNo()) });
                            addError(lineNo, errMsg, l10nKey);
                        }
                        continue;
                    }
                    // add value
                    refProperties.put(key, l10nKey);
                } else
                {
                    addError(lineNo, "Cannot parse line '" + inLine + "'", null);
                }
            }
        } catch (IOException exIO)
        {
            Log.get().error(exIO);
            // cleanup resources
            refProperties.clear();
            messages.clear();
            throw exIO; // rethrow
        } catch (Exception e)
        {
            Log.get().error(e);
            // cleanup resources
            refProperties.clear();
            messages.clear();
            throw new IOException(e.getMessage());
        }
        this.linesRead = lineNo;
    }

    /* Add an error message */
    private void addError(int lineNo, String errString, L10nKey key)
    {
        L10nError err = new L10nError(lineNo, errString);
        if (key != null)
        {
            err.setL10nObject(key);
        }
        messages.add(err);
        errCount++;
    }

    /* Add a warning message */
    private void addWarning(int lineNo, String msg, L10nKey key)
    {
        L10nWarning warning = new L10nWarning(lineNo, msg);
        if (key != null)
        {
            warning.setL10nObject(key);
        }
        messages.add(warning);
        warningCount++;
    }

    /* Add a informational message */
    private void addInfo(int lineNo, String msg, L10nKey key)
    {
        L10nMessage info = new L10nMessage(lineNo, msg);
        if (key != null)
        {
            info.setL10nObject(key);
        }
        messages.add(info);
        infoCount++;
    }

    /**
     * Main line. 
     * 
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
            ins.setReference("../../src/scarab/src/conf/classes/ScarabBundle_en.properties");
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
                L10nError data = (L10nError) it.next();
                System.err.println("E " + data.getLineNumber() + ": "
                        + data.getMessageText());
            }
        }
        if (ins.getWarnings().size() > 0)
        {
            Iterator it = ins.getWarnings().iterator();
            while (it.hasNext())
            {
                L10nWarning data = (L10nWarning) it.next();
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
                    .checkFile("../../src/scarab/src/conf/classes/ScarabBundle_de.properties");
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
            if (msg.isError())
            {
                System.out.print('E');
            } else if (msg.isWarning())
            {
                System.out.print('W');
            } else if (msg.isInfo())
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
