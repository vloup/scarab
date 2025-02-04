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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.codehaus.plexus.util.FileUtils;

/**
 * This class is used as ant task backend for the generation
 * of a property file by use of a template file.
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id$
 */

public class PropertyFileGenerator
{
    /**
     * This file will be read in  and all property values
     * in it will be replaced by the actual values as they
     * are in the online environment.
     */
    private File templateFile;

    /**
     * This is the file, which shall contain the
     * final property name/value pairs. It's overall
     * layout is similar to the templateFile, but the
     * property values are generated from the current 
     * online property settings.
     */
    private File customFile;
    
    private File propertyTypes;

    /**
     * 
     */
    private File emptyCustomFile;
    
    /**
     * This Map contains the user specified properties
     * defined in the list of property files given 
     * when the method setProperties() is called.
     **/
    
    private Map userProperties;
    
    /**
     * Setter: set the path to the template file.
     * Throws an exception, if the template file does not exist.
     * @param theTemplatePath
     * @return
     */
    public boolean setTemplate(String theTemplatePath)
    {
        boolean status = true;
        templateFile = new File(theTemplatePath);
        if(!templateFile.exists())
        {
            status = false;
        }
        return status;
    }
    
    public boolean setEmptyCustom(String theCustomPath)
    {
        boolean status = true;
        emptyCustomFile = new File(theCustomPath);
        if(emptyCustomFile.exists())
        {
            status = emptyCustomFile.delete();
        }
        return status;
    }
    
    public boolean setPropertyTypes(String thePropertyTypePath)
    {
        boolean status = true;
        propertyTypes = new File(thePropertyTypePath);
        if(propertyTypes.exists())
        {
            status = propertyTypes.delete();
        }
        return status;
    }
    
    /**
     * Return the absolute path to the templateFile, or null, if
     * no template file has been set.
     * @return
     */
    public String getTemplate()
    {
        return (templateFile==null)? null:templateFile.getAbsolutePath();
    }

    /**
     * Setter: set the path to the final property file.
     * Throws an exception, if the customFile exist, 
     * but can't be overwritten (due to permission settings).
     * @param theCustomPath
     */
    public boolean setCustom(String theCustomPath)
    {
        boolean status = true;
        customFile = new File(theCustomPath);
        if(!customFile.getParentFile().exists())
        {
            customFile.getParentFile().mkdir();
        }
        
        if(customFile.exists() &&
           !customFile.canWrite())
        {
            status = false;
        }
        
        return status;
    }

    /**
     * Return the absoute path to the customFile, or null, if no path
     * has been set.
     * @return
     */
    public String getCustom()
    {
        String result = null;
        if ( customFile != null )
        {
            result = customFile.getAbsolutePath();
        }
        return result;
    }

    /**
     * Setter: Create a Map of unresolved properties from
     * the files defined in theUserpathes.
     * Throws an exception, if a customFile exist, 
     * but can't be read (due to permission settings).
     * First definition of a property wins.
     * @param theUserPath
     */
    public boolean setProperties(String theUserPathes)
    {
        List filePathes = createPathList(theUserPathes);
        userProperties = new Hashtable();
        Iterator iterFilePathes = filePathes.iterator();
        while(iterFilePathes.hasNext()){
            System.out.println("read");
        	String filePath = (String)iterFilePathes.next();
        	File file = new File(filePath);
                if(file.exists())
                {
                    if(!file.canRead())
                    {
                        throw new RuntimeException("No Read permission for file ["+filePath+"]");
                    }
                    try
                    {
                        addUnresolvedProperties(file,userProperties);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException("Could not read file ["+filePath+"]");
                    }
                }

        }
        return true;
    }

    /**
     * @param file
     * @param properties
     */
    private void addUnresolvedProperties(File file, Map properties) throws IOException
    {
        Reader reader     = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);

        String line;

        while((line=br.readLine()) != null)
        {
            String trimmedLine = line.trim();
            if (  trimmedLine.equals("")
                ||trimmedLine.startsWith("#") )
            {
                continue; // forget comment lines and empty lines.
            }
            else 
            {
                String name  = null;
                String value = null;
                int index = line.indexOf("=");
                if(index >=0)
                {
                    name  = line.substring(0,index).trim();
                    value = line.substring(index+1).trim();
                }
                else
                {   
                    name  = line.trim();
                    value = "";
                }
                
                if(properties.get(name) == null)
                {
                    properties.put(name,value);
                }
            }
        }
        br.close();
    }

    /**
     * Convert a unix style pathlist to a List of pathes.
     * E.g. the String "path1:path2:path3" is converted into
     * a three component vector containing "path1", "path2" and
     * "path3"
     * If theUserpathes contains no path, this method returns null
     * @param theUserPathes
     * @return
     */
    private List createPathList(String theUserPathes)
    {
        List result = new Vector();
        StringTokenizer stok = new StringTokenizer(theUserPathes,":");
        while(stok.hasMoreTokens())
        {
            String path = stok.nextToken();
            if(path.length()==1 && stok.hasMoreTokens())
            {
             // deal with windows drive letters, e.g. "c:scarab/build.properties"
             path+=":"+stok.nextToken();
            }
            
            result.add(path);
        }
        return result;
    }
    
    
    /**
     * Read the templateFile and behave according to 
     * following rule set:
     * <ul>
     * <li> rule 1: Copy every line, which does NOT contain
     *      a property verbatim to the customFile.</li>
     * 
     * <li> rule 2: Retrieve the current online value of each 
     *      property found in the templateFile and generate an 
     *      appropriate name/value pair in the customFile.</li>
     * 
     * <li> rule 3: If a property value starts with a "${" in 
     *      the templateFile, keep the value as is. By this we 
     *      can propagate ${variables} to the customFile, which 
     *      will be resolved during startup of Scarab.</li>
     * </ul>
     * 
     */
    public void execute() throws IOException {
        
    		createEmptyCustomPropertyFile();
    		
            Reader reader = new FileReader(templateFile);
            Writer writer = new FileWriter(customFile);
            BufferedReader br = new BufferedReader(reader);
            PrintWriter    pw = new PrintWriter(writer);

            String line;

            while((line=br.readLine()) != null)
            {
                String trimmedLine = line.trim();
                if (  trimmedLine.equals("")
                    ||trimmedLine.startsWith("#") )
                {
                    pw.println(line);
                }
                else 
                {
                    String resultLine = createResultLine(line);
                    pw.println(resultLine);
                }
            }
            pw.close();
            br.close();
        
    }

    /**
     * Creates empty custom properties file.
     */
    private void createEmptyCustomPropertyFile(){
    	
    	if(emptyCustomFile.exists()){
    		emptyCustomFile.delete();
    	}
    	try {
			emptyCustomFile.createNewFile();
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    /**
     * Read the current line and behave according to 
     * following rule set:
     * <ul>
     * <li> rule 1: If the line does not contain a property, copy
     *              it verbatim.</li>
     * 
     * <li> rule 2: Retrieve the current online value of the 
     *      property found in the line parameter and generate an 
     *      appropriate name/value pair in the resultLine.</li>
     * 
     * <li> rule 3: If a property value starts with a "${" in 
     *      the line, keep the value as is. By this we 
     *      can propagate ${variables} to the customFile, which 
     *      will be "resolved late" (during startup of Scarab).</li>
     * </ul>
     * @param line
     * @param props
     * @return
     */
    private String createResultLine(String line)
    {
        String propertyName  = null;
        String templateValue = null;
        String resultLine;
        int index = line.indexOf("=");
        if(index >=0)
        {
            propertyName  = line.substring(0,index).trim();
            templateValue = line.substring(index+1).trim();
            int beginOfValue = line.indexOf(templateValue,index+1);
            resultLine = (beginOfValue == -1) ?
                         line : line.substring(0,beginOfValue);
        }
        else
        {   
            propertyName  = line.trim();
            templateValue = "";
            int endOfLine = line.indexOf(propertyName)+propertyName.length();
            resultLine = ((endOfLine == -1) ? line : line.substring(0,endOfLine)) + " = ";
        }
        
        String newValue = (String) userProperties.get(propertyName);

        if(newValue == null)
        {
            newValue = templateValue;
            if(newValue == null)
            {
                newValue = "";
            }
        }
        else if(newValue.equalsIgnoreCase("**generated**"))
        {
            String dbtype = (String)userProperties.get("scarab.database.type");
            if(dbtype.equals(""))
            {
                dbtype="hypersonic";
            }
            newValue = "${"+propertyName+"."+dbtype+"}";
        }

        if ( newValue.equals(templateValue))
        {
            resultLine = line;
        }
        else
        {
            resultLine += newValue;
        }
        return resultLine;
    }
}
