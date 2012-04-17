package org.tigris.scarab.util.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal for building Scarab's database.
 * @goal create-db
 * @execute lifecycle="sql-init" phase="process-resources"
 * @author jhoechstaedter
 *
 */
public class DBInitMojo  extends AbstractMojo{

	/**
	 * Type for DB, e.g, mysql, or hypersonic.
	 * @parameter
	 */
	protected String dbType;
	
	/**
	 * Full database URL, including all extensions.
	 * @parameter
	 */
	protected String dbFullUrl;
	
	/**
	 * Database URL for server without extensions and schema name.
	 * @parameter
	 */
	protected String dbServerUrl;
	
	/**
	 * DB Username
	 * @parameter
	 */
	protected String dbUser;
	
	/**
	 * DB Password
	 * @parameter
	 */
	protected String dbPassword;
	
	/**
	 * Driver name for jdbc connection.
	 * @parameter
	 */
	protected String dbDriver;
	
	/**
	 * Input directory for Scarab's content SQL files.
	 * @parameter expression="${project.build.directory}/sql-resources/source"
	 * @readonly
	 */
	protected String sqlSrcDir;
	
	/**
	 * Input directory for schema SQL files. 
	 * @parameter expression="${project.build.directory}/sql-resources/schema"
	 * @readonly
	 */
	protected String sqlSchemaDir;
	
	/**
	 * Mode for db, e.g. "sample", "default" or "basic"
	 * @parameter default-value="basic" expression="${scarab.database.build.mode}"
	 */
	protected String dbMode;
	
	/**
	 * True if Scarab's database should not be dropped during build.
	 * @parameter default-value="true" expression="${scarab.database.build.drop}"
	 */
	protected boolean dropSchema;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Connection con = null;
		 Statement stmt = null;
		 try  
	        {
			 
			 getLog().info("Build database for type '" + dbType + "'");
			 if("hypersonic".equals(dbType)){
				//configurations special for hypersonic's database build 
				dropSchema = false;
				dbServerUrl = dbFullUrl;
			 }
			 
			 getLog().info("scarab.database.build.drop=" + dropSchema);
			 
			 if(dropSchema && !createDB()){
				 throw new MojoExecutionException("Failed to create database schema.");
			 }
			 
			 con = getFullDatabaseConnection();
			 stmt = con.createStatement();
			 executeScripts(stmt, getSqlFiles());
			 
	        }  
	        catch(Exception e)  
	        {  
	        	getLog().error(e.getMessage(), e);
	        }
	        finally{
	        	if(stmt != null){
	        		try {
	        			stmt.close();
					} catch (SQLException e) {
						getLog().error(e.getMessage(), e);
					}
	        	}
	        	if(con != null){
	        		try {
						con.close();
					} catch (SQLException e) {
						getLog().error(e.getMessage(), e);
					}
	        	}
	        }
		
	}
	
	/**
	 * Creats database by script.
	 * @return
	 * @throws Exception
	 */
	protected boolean createDB(){
		Connection con = null;
		Statement stmt = null;
        boolean success = true;
		try{
        	con = getServerConnection();
    		stmt = con.createStatement();
			File scriptDir = new File(sqlSrcDir + "/" + dbType);
			File[] schemaSqlFiles = {
	        		 new File(scriptDir, "create-db.sql")
	        };
			
			executeScripts(stmt, new ArrayList(Arrays.asList(schemaSqlFiles)));
        }
        catch(Exception e){
        	success = false;
        	getLog().error(e.getMessage(), e);
        }
        finally{
        	if(stmt != null){
        		try {
        			stmt.close();
				} catch (SQLException e) {
		        	success = false;
					getLog().error(e.getMessage(), e);
				}
        	}
        	if(con != null){
        		try {
					con.close();
				} catch (SQLException e) {
		        	success = false;
					getLog().error(e.getMessage(), e);
				}
        	}
        }
        return success;
	}
	
	/**
	 * 
	 * @param con
	 * @param files
	 */
	protected void executeScripts(Statement stmt, List files) throws Exception{
		Iterator iterFiels = files.iterator();
		while(iterFiels.hasNext()){
			FileReader fr = new FileReader((File)iterFiels.next());  
			
            BufferedReader br = new BufferedReader(fr);  
            String s = new String();
            StringBuffer sb = new StringBuffer();
            while((s = br.readLine()) != null)  
            {  
            	if(!s.trim().startsWith("--")){
            		s = s.endsWith(" ") ? s : s + " ";
                    sb.append(s);  
            	}
            }  
            br.close();  
  
            // here is our splitter ! We use ";" as a delimiter for each request  
            // then we are sure to have well formed statements  
            String[] inst = sb.toString().split(";");      
            for(int stmtIndex = 0; stmtIndex<inst.length; stmtIndex++)  
            {  
	            try{          
		                // we ensure that there is no spaces before or after the request string  
		                // in order to not execute empty statements  
		                if(!inst[stmtIndex].trim().equals(""))  
		                {  
		                	stmt.executeUpdate(inst[stmtIndex]);   
		                }  
	
	            }catch(Exception e){
	            	
	            	getLog().error("Could not execute line: " + inst[stmtIndex] + ", due to the following reason: " + e.getMessage());
	            }	            
            }
		}
	}
	
	/**
	 * Gets connection to server.
	 * @return
	 * @throws Exception
	 */
	protected Connection getServerConnection() throws Exception{
		 Class.forName(dbDriver).newInstance();  
         getLog().info("Driver: " + dbDriver);
         getLog().info("Url: " + dbServerUrl);
         Connection con = DriverManager.getConnection(dbServerUrl, dbUser, dbPassword);
         return con;
	}
	
	/**
	 * Gets fill database connection including suffixes.
	 * @return
	 * @throws Exception
	 */
	protected Connection getFullDatabaseConnection() throws Exception{
		 Class.forName(dbDriver).newInstance();  
         getLog().info("Driver: " + dbDriver);
         getLog().info("Url: " + dbFullUrl);
         Connection con = DriverManager.getConnection(dbFullUrl, dbUser, dbPassword);
         return con;
	}
	
	protected File getSourceDir() throws Exception{
		 File srcDir = new File(sqlSrcDir);
		 if(!srcDir.exists()){
	          	throw new MojoExecutionException("Files for data in '" + sqlSrcDir + "' not found.");
	     } 
		 return srcDir;
	}
	
	protected File getSchemDir() throws Exception{
		 File scriptSchemaDir = new File(sqlSchemaDir);
		 if(!scriptSchemaDir.exists()){
	          	throw new MojoExecutionException("Files for schema in '" + sqlSchemaDir + "' not found.");
	     } 
		 return scriptSchemaDir;
	}
	
	/**
	 * Get SQL file for schema and content.
	 * @return
	 * @throws Exception
	 */
	protected List getSqlFiles() throws Exception{
         File scriptDir = getSourceDir();
         File scriptSchemaDir = getSchemDir();   
         
		 getLog().info("scarab.database.build.mode=" + dbMode);
         
         File[] schemaSqlFiles = {// sql schema files, generated by torque:sql
        		 new File(scriptSchemaDir, "turbine-schema.sql"),
        		 new File(scriptSchemaDir, "scarab-schema.sql"), 
        		 new File(scriptSchemaDir, "id_table-schema.sql")
         };
         File[] basicSqlFiles = {// content sql files from scr/sql
        		 new File(scriptDir, "turbine-id-table-init.sql"),
        		 new File(scriptDir, "scarab-id-table-init.sql"), 
        		 new File(scriptDir, "scarab-security.sql"),
        		 new File(scriptDir, "scarab-required-data.sql"),
        		 new File(scriptDir, "scarab-anonymous.sql")};
         
         File[] preconfiguredSqlFiles = {
        		 new File(scriptDir, "scarab-default-data.sql")
         };
         File[] sampleSqlFiles = {
        		 new File(scriptDir, "scarab-sample-data.sql"),
        		};
         
         List sqlFiles = null;    
         sqlFiles = new ArrayList(Arrays.asList(schemaSqlFiles));
         sqlFiles.addAll(new ArrayList(Arrays.asList(basicSqlFiles)));
         if(dbMode.equalsIgnoreCase("default")){
        	 sqlFiles.addAll(new ArrayList(Arrays.asList(preconfiguredSqlFiles)));
         }
         else if(dbMode.equalsIgnoreCase("sample")){
        	 sqlFiles.addAll(new ArrayList(Arrays.asList(preconfiguredSqlFiles)));
        	 sqlFiles.addAll(new ArrayList(Arrays.asList(sampleSqlFiles)));
         }
         
         return sqlFiles;
	}

}
