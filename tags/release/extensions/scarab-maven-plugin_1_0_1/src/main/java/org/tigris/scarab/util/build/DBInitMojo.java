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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal for building Scarab's database.
 * @goal create-db
 * @author jhoechstaedter
 *
 */
public class DBInitMojo  extends PrepareDBScriptsMojo{

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
	 * True if Scarab's database should not be dropped during build.
	 * @parameter default-value="true" expression="${scarab.database.build.drop}"
	 */
	protected boolean dropSchema;
	
	/**
	 * General execution.
	 */
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
	 * Creates database by script.
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
	 * Execute list of given sql files.
	 * @param stmt
	 * @param files
	 */
	protected void executeScripts(Statement stmt, List files) throws Exception{
		Iterator iterFiels = files.iterator();
		while(iterFiels.hasNext()){
			FileReader fr = new FileReader((File)iterFiels.next());  
			
			//read in all lines of sql except comment lines, which start with "--"
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
  
            //prepare and execute sql, (separated by ";")
            String[] inst = sb.toString().split(";");      
            for(int stmtIndex = 0; stmtIndex<inst.length; stmtIndex++)  
            {  
	            try{          
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

}
