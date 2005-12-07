<?xml version="1.0" encoding="UTF-8" ?>
<!--
    Document   : jira.xsl
    Created on : November 30, 2005, 6:04 AM
    Author     : hair
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    
    <xsl:template match="rss/channel">

    <scarab-issues>
      <import-type>create-different-db</import-type>
      
     <!-- ImportIssues.insertModuleNode(..) w -->
      
      <issues>

        <xsl:for-each select="item">  

            <issue>
              <id><xsl:value-of select="key"/></id>
              <artifact-type>[JIRA] <xsl:value-of select="type"/></artifact-type>
              <activity-sets>
                <activity-set>
                  <!--id>2</id-->
                  <type>Create Issue</type>
                  <xsl:for-each select="reporter"><!-- [XXX] Only ever be one, but we need to get to attribute @username -->
                    <created-by><xsl:value-of select="@username"/></created-by>
                  </xsl:for-each>
                  <created-date>
                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                    <timestamp><xsl:value-of select="created"/></timestamp>
                  </created-date>
                  <activities>         
                    <activity>
                      <!--id>3</id-->              
                      <attribute>Summary</attribute>                                                                                    
                      <new-value><xsl:value-of select="summary"/></new-value>
                      <description>Issue <xsl:value-of select="key"/> had Summary set to '<xsl:value-of select="summary"/>'</description>              
                      <end-date>
                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                        <timestamp><xsl:value-of select="created"/></timestamp>
                      </end-date>
                    </activity> 
                    <activity>
                      <!--id>4</id-->
                        <attribute>Description</attribute>                                
                      <new-option><xsl:value-of select="description"/></new-option>
                      <description>Description set to <xsl:value-of select="description"/></description>                
                    </activity>      
                    <activity>
                      <!--id>4</id-->
                        <attribute>Status</attribute>                                
                      <new-option><xsl:value-of select="status"/></new-option>
                      <description>Status set to <xsl:value-of select="status"/></description>                
                    </activity>
                    <activity>
                      <!--id>4</id-->
                        <attribute>Environment</attribute>                                
                      <new-option><xsl:value-of select="environment"/></new-option>
                      <description>Environment set to <xsl:value-of select="environment"/></description>                
                    </activity>   
                    <activity>
                      <!--id>4</id-->
                        <attribute>Priority</attribute>                                
                      <new-option><xsl:value-of select="priority"/></new-option>
                      <description>Priority set to <xsl:value-of select="priority"/></description>                
                    </activity>
                    <activity>
                      <!--id>4</id-->
                        <attribute>Version</attribute>                                
                      <new-option><xsl:value-of select="version"/></new-option>
                      <description>Version set to <xsl:value-of select="version"/></description>                
                    </activity>   
                    <activity>
                      <!--id>4</id-->
                        <attribute>Component</attribute>                                
                      <new-option><xsl:value-of select="component"/></new-option>
                      <description>Component set to <xsl:value-of select="component"/></description>                
                    </activity> 
                    <activity>
                      <!--id>4</id-->
                        <attribute>Due</attribute>                                
                      <new-option><xsl:value-of select="due"/></new-option>
                      <description>Due set to <xsl:value-of select="due"/></description>                
                    </activity>    
                    <activity>
                      <!--id>4</id-->
                        <attribute>Votes</attribute>                                
                      <new-option><xsl:value-of select="votes"/></new-option>
                      <description>Votes set to <xsl:value-of select="votes"/></description>                
                    </activity>         
                  </activities>
                </activity-set>
                <activity-set>
                  <!--id>410</id-->
                  <type>Edit Issue</type>
                  <created-by><xsl:value-of select="created"/></created-by>
                  <created-date>
                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                    <timestamp><xsl:value-of select="updated"/></timestamp>
                  </created-date>
                  <activities>
                    <activity>
                      <!--id>720</id-->
                        <attribute>Resolution</attribute>                
                      <new-option><xsl:value-of select="resolution"/></new-option>
                      <new-value><xsl:value-of select="resolution"/></new-value>
                      <description>Resolution set to <xsl:value-of select="resolution"/></description>              
                    </activity>
                    <activity>
                      <!--id>720</id-->
                        <attribute>Assignee</attribute>                
                      <new-option><xsl:value-of select="assignee"/></new-option>
                      <new-value><xsl:value-of select="assignee"/></new-value>
                      <description>Assignee set to <xsl:value-of select="assignee"/></description>              
                    </activity>            
                  </activities>

                  <xsl:for-each select="comments/comment">

                      <attachment>
                        <!--id>410</id-->
                        <name>comment</name>
                        <type>MODIFICATION</type>             
                        <data><xsl:value-of select="."/></data>     
                        <mimetype>text/plain</mimetype>
                        <created-date>
                          <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                          <timestamp><xsl:value-of select="@created"/></timestamp>
                        </created-date>
                        <created-by><xsl:value-of select="@author"/></created-by>
                        <deleted>false</deleted>
                      </attachment>

                  </xsl:for-each>

                </activity-set>

              </activity-sets>
            </issue>

        </xsl:for-each>

      </issues>

    </scarab-issues>        
        
  </xsl:template>

</xsl:stylesheet>