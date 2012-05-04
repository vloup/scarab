package org.tigris.scarab.util.build.migration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.tigris.scarab.util.build.DBInitMojo;

public class AbstractMigrateMojo extends DBInitMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.dropSchema = false;
		super.execute();
	}

}
