package org.tigris.scarab.util.build.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @goal migrate-1a20-to-1b21
 * @author jhoechstaedter
 *
 */
public class Migrate1a20To1b20 extends AbstractMigrateMojo{

	@Override
	protected List getSqlFiles() throws Exception {
		File scriptDir = getSourceDir();
		
		List basicSqlFiles = new ArrayList();
		basicSqlFiles.add(new File(scriptDir, this.dbType + "/upgrade/" + this.dbType + "-upgrade-1.0a20-1.0b20-1.sql"));
		
		return basicSqlFiles;
	}

}
