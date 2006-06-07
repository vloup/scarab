package org.tigris.scarab.util.build;

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

import java.io.File;
import java.io.FileWriter;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tigris.scarab.util.RegexProcessor;

/**
 * This class is used as ant task backend for the generation
 * of a property file by use of a template file.
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id: AntPropertyFileGenerator.java 9421 2005-02-20 22:32:38Z jorgeuriarte $
 */

public class AntSchemaFix extends Task implements PropertyGetter
{
    File sourceFile;
    File targetFile;
    String dbtype;
    
    /**
     * Source schema file to be fixed.
     * @param theSourceFileName
     */
    public void setSource(String theSourceFileName)
    {
        sourceFile = new File(adjust(theSourceFileName));
        if(!sourceFile.exists())
        {
            System.out.println("wd=["+System.getProperty("user.dir")+"]");
            System.out.println("bd=["+this.getProject().getBaseDir()+"]");
            throw new BuildException("the source file ["
                    + theSourceFileName
                    + "] does not exist.");            
        }
        
        if(!sourceFile.canWrite())
        {
            throw new BuildException("the source file["
                    + theSourceFileName
                    + "] is not writable.");           
        }
        
    }

    /**
     * @param theFileName
     * @return
     */
    private String adjust(String theFileName)
    {
        String baseDir = this.getProject().getBaseDir().getAbsolutePath();
        System.setProperty("user.dir",baseDir);
        
        String result = theFileName;
        if(   !theFileName.startsWith("/")   // this is for unix
           && !theFileName.startsWith("\\")  // this is for windows
           &&  theFileName.charAt(1)!=(':')  // this is for windows
         )
        {
            result = baseDir + File.separator + theFileName;
        }
        
        return result;
    }

    /**
     * target schema file to be created.
     * @param theTargetFileName
     */
    public void setTarget(String theTargetFileName)
    {

        targetFile = new File(adjust(theTargetFileName));
        if (targetFile.exists())
        {
            targetFile.delete();
        }
        
    }

    /**
     * database type for which to run the fix.
     * currently only hypersonic needs a fix.
     * @param theDbtype
     */
    public void setDbtype(String theDbtype)
    {
        dbtype = theDbtype;
    }

    /**
     * fix schema-file if dbtype is hypersonic.
     * due to an error in the hypersonic-torque generator.
     */
    public void execute() 
    {
        if(dbtype.equals("hypersonic"))
        {
            System.out.println("dbtype \""+dbtype+"\" needs fixes ...");
            fixHsqlSchema();
        }
        else
        {
            System.out.println("dbtype \""+dbtype+"\" is clean.");
        }
    }


    
    /**
     * replace "integer(...)" by "integer"
     * replace "DELETED ... ," by "DELETED integer DEFAULT 0,"
     * @param sourceFile
     * @param targetFile
     */
    private void fixHsqlSchema() 
    {
        try
        {
            
            boolean modifySelf = false;

            if(targetFile==null)
            {
                String targetFileName = sourceFile.getPath()+"tmp";
                setTarget(targetFileName);
                modifySelf=true;
            }
            
            java.io.BufferedReader rdr = new java.io.BufferedReader(new java.io.FileReader(sourceFile));
            FileWriter fw = new java.io.FileWriter(targetFile);
            java.io.BufferedWriter wrtr = new java.io.BufferedWriter(fw);
            
            String str;
            RegexProcessor processor = new RegexProcessor();
            while ((str = rdr.readLine()) != null) 
            {
                
                String fstr = processor.process(str,"integer \\(\\d+\\)", "integer");
                fstr        = processor.process(fstr,"DELETED\\s+integer[^,]*,", "DELETED integer DEFAULT 0,");

                //if(!fstr.equals(str))
                //{
                //    System.out.println("old: "+str);
                //    System.out.println("new: "+fstr);
                //}
                
                wrtr.write(fstr);
                wrtr.newLine();
            }
            rdr.close();
            wrtr.close();
            
            if(modifySelf)
            {
                sourceFile.delete();
                targetFile.renameTo(sourceFile);
                System.out.println("replaced ["+sourceFile.getPath()+"]...");
            }
            else
            {
                System.out.println("created ["+targetFile.getPath()+"]...");
            }
            
        } 

        catch (java.io.IOException e1) 
        {
            throw new BuildException("IOException while processing Hsql-schema.["+e1.getMessage()+"]");
        }

        catch (MalformedPatternException e2)
        {
            throw new BuildException("Regex Error while processing Hsql-schema.["+e2.getMessage()+"]");            
        }           
        
    }

    /**
     * dummy method. Not used.
     * @param name
     * @return
     */
    public Object getProperty(String name, Object def)
    {
        throw new BuildException("Tried to getProperty() from non implemented method.");
    }
}
