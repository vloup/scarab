package org.tigris.scarab.util.build.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @goal migrate-1b21-to-1b22
 * @author jhoechstaedter
 *
 */
public class Migrate1b21To1b22 extends AbstractMigrateMojo{

	@Override
	protected List getSqlFiles() throws Exception {
		File scriptDir = getSourceDir();
		
		List basicSqlFiles = new ArrayList();
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-resize_id_prefix.sql"));
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-fill_activity_type.sql"));
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-attributegroup-viewrole.sql"));
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-add-scarab-issues-email-references.sql"));
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-add-new-permissions.sql"));
		basicSqlFiles.add(new File(scriptDir, "/upgrade/upgrade-0.22-global-booleans-to-long-form.sql"));
		
		return basicSqlFiles;
	}

}
