package org.tigris.scarab.util.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.tigris.scarab.util.XSLTransformer;

/**
 * Mojo to create basic property files for Scarab from scarab_properties.xml, e.g. project.properties and minimal.properties.
 * 
 * @goal update-properties
 * @author jhoechstaedter
 *
 */
public class CreateBasicPropertyFileMojo  extends AbstractMojo{

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		XSLTransformer transformer = new XSLTransformer();
		try {
			transformer.transform(new FileInputStream(new File("scarab_properties.xml")), new FileOutputStream(new File("project.properties")), "project.xsl");
			transformer.transform(new FileInputStream(new File("scarab_properties.xml")), new FileOutputStream(new File("minimal.properties")), "minimal.xsl");
			transformer.transform(new FileInputStream(new File("scarab_properties.xml")), new FileOutputStream(new File("wizzard.properties")), "wizzard.xsl");
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		}
		
	}

}
