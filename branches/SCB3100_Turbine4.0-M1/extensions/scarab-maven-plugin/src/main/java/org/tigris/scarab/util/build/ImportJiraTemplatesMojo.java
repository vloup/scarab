package org.tigris.scarab.util.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Imports jira templates into Scarab's database.
 * @goal import-jira-templates
 * @author jhoechstaedter
 *
 */
public class ImportJiraTemplatesMojo extends DBInitMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.dropSchema = false;
		super.execute();
	}

	@Override
	protected List getSqlFiles() throws Exception {
		File scriptDir = getSourceDir();
		
		List basicSqlFiles = new ArrayList();
		basicSqlFiles.add(new File(scriptDir, "scarab-jira-templates-data.sql"));
		
		return basicSqlFiles;
	}
	
}