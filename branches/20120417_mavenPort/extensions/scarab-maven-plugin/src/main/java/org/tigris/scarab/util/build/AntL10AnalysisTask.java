package org.tigris.scarab.util.build;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import org.tigris.scarab.util.build.l10nchecker.L10nInspector;
import org.tigris.scarab.util.build.l10nchecker.L10nIssue;
import org.tigris.scarab.util.build.l10nchecker.L10nIssueTemplates;
import org.tigris.scarab.util.build.l10nchecker.L10nMessage;

/**
 * 
 * Goal to check for localisation problems.
 *  
 * @author sreindl
 * @goal check-l10n
 *  
 */
public class AntL10AnalysisTask extends AbstractMojo
{
	/**
	 * Files to check
	 * @parameter
	 */
    private FileSet fileset;
    
    /**
     * Set the severity for a particular message.
	 *     This is done by setting the attributes <code>error</code> to the
	 *     issue that has to be used (e.g. <code>error="CantParseIssue"</code>)
	 *     and <code>severity</code> to one of the defined severity levels:
	 *     <ul>
	 *  	<li><strong>ERROR</strong>: This issue will be marked as error
	 *  	<li><strong>WARNING</strong>: This issue will be marked as error
	 *  	<li><strong>INFORMATION</strong>: This issue will be marked as error
	 *  	<li><strong>IGNORED</strong>: This issue will be marked to be 
	 *                 ignored. By default all issues are marked as "IGNORED".
	 *     </ul>
     * @parameter
     */
    private Map messageSettings;
    
    /**
     * Verbosity
     * <ul>
	 * <li>0: Errors only
	 * <li>1: Errors and warnings
	 * <li>2: Errors, warnings and information. 
	 * </ul>
     */
    private int verbose = 0;

    /**
     * Base file for reference.
     * @parameter
     */
    private String refFile;

    /**
     * Stop after first checked file in case of errors.
     */
    private boolean failonerr = false;

    /**
     * Name of output file.
     * @parameter
     */
    private String outFileName = null;

    /**
     * IF given, all output is written to this file
     */
    private BufferedWriter outFileBuffer;
    
    
    /*
     * Read all parameters and execute
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws MojoExecutionException
    {
        L10nInspector ins = null;

        if (outFileName != null)
        {
            try
            {
            	File outFile = new File(outFileName);
            	if(outFile.exists()){
            		outFile.delete();
            	}
        		outFile.createNewFile();
        		outFileBuffer = new BufferedWriter(new FileWriter(outFile));
            }
            catch (IOException eIO)
            {
                throw new MojoExecutionException(eIO.getMessage());
            }
        }
	output("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
	output("<document>");
	output("<properties><title>L10N status report</title></properties>");
	output("<body>");
        try
        {
            ins = new L10nInspector(getLog());
            // set severities
            Iterator it = messageSettings.keySet().iterator();
            while (it.hasNext())
            {
                Message msg = new Message();
                msg.setError((String)it.next());
                msg.setSeverity((String)messageSettings.get(msg.id));
                try
                {
                    Class _clazz = Class.forName("org.tigris.scarab.util.build.l10nchecker.issues." + msg.id + "Issue");
                    L10nIssueTemplates.setMessageType(_clazz, msg.severity);
                }
                catch (ClassNotFoundException ex_cnf)
                {
                    throw new MojoExecutionException ("Cannot locate issue " + msg.id);
                }
            }
	    output("<section name=\"Reference file "+refFile.substring(refFile.lastIndexOf("/")+1)+"\"><pre>");
            output("Loading reference file " + refFile, true);
            int refCount = ins.setReference(refFile);
            output("Loaded properties: " + refCount);
            if (ins.hasErrors())
            {
                output("Errors: " + ins.getErrors().size());
                it = ins.getErrors().iterator();
                while (it.hasNext())
                {
                    L10nMessage err = (L10nMessage) it.next();
                    output("Error at line " + err.getLineNumber() + ": "
                            + err.getMessageText());
                }
            }
	    output("</pre></section>");
            output(""); // empty line for readability
            FileSetManager fileSetManager = new FileSetManager();

                String[] files = fileSetManager.getIncludedFiles(fileset);
                for (int i = 0; i < files.length; i++)
                {
                    File f = new File (files[i]);
                    output("");
                    output("<section name=\"Checking bundle " + files[i] +"\"><pre>", true);
                    int transcount = ins.checkFile(fileset.getDirectory() + "/" + files[i]);
                    output("Translations found: " + transcount, true);
                    if (ins.getErrors().size() > 0)
                    {
                        output("Errors:             " + 
                                ins.getErrors().size(), true);
                    }
                    if (verbose > 0 && ins.getWarnings().size() > 0)
                    {
                        output("Warnings:           "
                                + ins.getWarnings().size());
                    }
                    if (verbose > 1 && ins.getInfos().size() > 0)
                    {
                        output("Information:        " 
                                + ins.getInfos().size());
                    }
                    output(""); // empty line for readability
                    if (ins.hasErrors())
                    {
                        it = ins.getErrors().iterator();
                        while (it.hasNext())
                        {
                            L10nMessage err = (L10nMessage) it.next();
                            output("Error at line " + err.getLineNumber()
                                    + ": " + err.getMessageText());
                        }
                        if (failonerr)
                        {
                            throw new MojoExecutionException(
                                    "Failed due to errors in reference bundle");
                        }
                    }
                    if (verbose > 0 && ins.getWarnings().size() > 0)
                    {
                        it = ins.getWarnings().iterator();
                        while (it.hasNext())
                        {
                            L10nMessage err = (L10nMessage) it.next();
                            output("Warning at line " + err.getLineNumber()
                                    + ": " + err.getMessageText());
                        }
                    }
                    if (verbose > 1 && ins.getInfos().size() > 0)
                    {
                        it = ins.getInfos().iterator();
                        while (it.hasNext())
                        {
                            L10nMessage err = (L10nMessage) it.next();

                            if (err.getLineNumber() < 0)
                            {
                                output("Information: "
                                        + err.getMessageText());
                            }
                            else
                            {
                                output("Information for line "
                                        + err.getLineNumber() + ": "
                                        + err.getMessageText());
                            }
                        }
                    }
		    output("</pre></section>");
                }

	    output("</body>");
	    output("</document>");
        }
        catch (Exception e)
        {
            getLog().error("Exception " + e + " raised");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Set the output file
     * 
     * @param filename the file to write
     */
    public void setOutfile(String filename)
    {
        this.outFileName = filename;
    }

    /**
     * Handle the attribute "refFile"
     * @param aFile The reference file to be used
     */
    public void setRefFile(String aFile)
    {
        refFile = aFile;
    }

    
    /**
     * Handle the attribute "verbose".
     * 
     * @param verbose The verbosity to be set
     */
    public void setVerbose(int verbose)
    {
        this.verbose = verbose;
    }

    /**
     * Handle the attribute fileonerr
     * 
     * @param failonerr The failonerr to set.
     */
    public void setFailonerr(boolean failonerr)
    {
        this.failonerr = failonerr;
    }

    
    /* output routines */
    private void output(String what)
    {
        output (what, false);
    }

    /* print output. In case dumpToConsole is true and output is redirected
     * the line is printed to the console also
     */
    private void output(String what, boolean dumpToConsole)
    {
        if (outFileBuffer != null)
        {
            try
            {
            	outFileBuffer.write(what);
            	outFileBuffer.newLine();
            	outFileBuffer.flush();
            }
            catch (IOException eIO)
            {
            	getLog().error("Cannot write " + what + " to " + outFileName + " ("
                        + eIO.getMessage() + ")");
            }
        }
        if (null == outFileBuffer || dumpToConsole)
        {
            getLog().info(what);
        }
    }

    /**
     * sub Class that represents a severity setting message
     *  
     */
    public class Message 
    {
        private String id;
        private int severity;
        
        /** bean constructor */
        public Message() {}
        
        /** ant entrypoint to set the error name */
        public void setError (String _id) 
        {
            this.id = _id;
        }
    
        /** ant entrypoint to set the severity */
        public void setSeverity (String severity)
        {
            if (severity.equals("ERROR"))
            {
                this.severity = L10nIssue.MESSAGE_ERROR;
            }
            else if (severity.equals("WARNING"))
            {
                this.severity = L10nIssue.MESSAGE_WARNING;
            }
            else if (severity.equals("INFORMATION"))
            {
                this.severity = L10nIssue.MESSAGE_INFO;
            }
            else 
            {
                this.severity = L10nIssue.MESSAGE_IGNORE;
            }
        }
    }        
}
