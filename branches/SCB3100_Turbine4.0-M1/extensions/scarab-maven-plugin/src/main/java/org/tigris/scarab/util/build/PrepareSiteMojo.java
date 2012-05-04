package org.tigris.scarab.util.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.tigris.scarab.util.XSLTransformer;

/**
 * Generates site xdocs as there are l10n checks and properties guide.
 * @goal prepare-site-docs
 * @execute lifecycle="prepare-xdocs" phase="pre-site"
 * @author jhoechstaedter
 *
 */
public class PrepareSiteMojo extends AbstractMojo{
	
	/**
	 * Output directory for xdoc files.
	 * @parameter
	 */
	private String xdocDirectory;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		XSLTransformer transformer = new XSLTransformer();
		try {
			File targetDir = new File(xdocDirectory);
			targetDir.mkdirs();
			transformer.transform(new FileInputStream(new File("scarab_properties.xml")), new FileOutputStream(new File(targetDir, "scarab_properties_final.xml")), "properties.xsl");
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		}
		
	}

}
