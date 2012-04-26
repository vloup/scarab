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


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;


/**
 * This class is used as ant task backend for the generation
 * of a property file by use of a template file.
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id$
 * 
 * @goal generate-custom-properties
 * 
 */

public class GenerateCustomPropertyFilesMojo extends AbstractMojo
{
 
	/**
	 * Output directory for generated configuration files.
	 * @parameter
	 */
	private String configurationDirectory;
    
    /**
     * The reusable property file generator
     */
    private PropertyFileGenerator generator = new PropertyFileGenerator();
    
    public void setTemplate() throws MojoExecutionException
    {
        boolean status = generator.setTemplate("project.properties");
        if(!status)
        {
            throw new MojoExecutionException("the path ["
                    + configurationDirectory
                    + "] for project.properties is not writeable.");
        }
    }

    public void setEmptyCustom() throws MojoExecutionException
    {
        boolean status = generator.setEmptyCustom(configurationDirectory + "/custom.properties");
        if(!status)
        {
           throw new MojoExecutionException("the path ["
                + configurationDirectory
                + "] for custom.properties is not writeable.");
        }
    }
    
    
    /**
     * Setter: set the path to the final property file.
     * Throws an exception, if the customFile exist, 
     * but can't be overwritten (due to permission settings).
     * @param theCustomPath
     * @throws MojoExecutionException 
     */
    public void setCustom() throws MojoExecutionException
    {
        boolean status = generator.setCustom(configurationDirectory + "/defaultCustom.properties");
        if(!status)
        {
            throw new MojoExecutionException("defaultCustom file for ["
                    + configurationDirectory
                    + "] is not writeable.");

        }
    }

    /**
     * Setter: set the path to the final property file.
     * Throws an exception, if the customFile exist, 
     * but can't be overwritten (due to permission settings).
     * @param theCustomPath
     * @throws MojoExecutionException 
     */
    public void setProperties() throws MojoExecutionException
    {
        boolean status = generator.setProperties("build.properties:project.properties");
        if(!status)
        {
            throw new MojoExecutionException("problem with the propretyFilePathlist["
                    + ""
                    + "] one file is not readable.");

        }
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
    public void execute() {
                   
        try {
        	setEmptyCustom();
            setTemplate();
            setCustom();
            setProperties();
            
            getLog().info("Create custom file: '" + generator.getCustom() + "'.");
            getLog().info("Using  template   : '" + generator.getTemplate() );
            
            generator.execute();
        } catch (Exception e) {
			getLog().error(e.getMessage(), e);
		}

    }

    /**
     * This is the method by which the generator can retrieve
     * property values.
     * @param name
     * @return
     */
    public Object getProperty(String name, Object def)
    {
        String result = null;

        String value = (String) def;
        if (value==null || !value.startsWith("$"))
        {
            result = (String) this.getPluginContext().get(name);
        }
        if(result == null)
        {
            result = value;
        }
        return result;
    }
}
