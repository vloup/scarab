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
      <import-type>create-same-db</import-type>
      
     <!-- ImportIssues.insertModuleNode(..)  -->
      
      <issues>

        <xsl:for-each select="item">  

            <issue>
              <id><xsl:value-of select="translate(key,'-','')"/></id> <!-- remove hyphons, not scarab style. -->
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
                      <attribute>[JIRA] Summary</attribute>                                                                                    
                      <new-value><xsl:value-of select="summary"/></new-value>
                      <description>Issue <xsl:value-of select="key"/> had Summary set to '<xsl:value-of select="summary"/>'</description>              
                      <end-date>
                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                        <timestamp><xsl:value-of select="created"/></timestamp>
                      </end-date>
                    </activity> 
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Description</attribute>                                
                        <new-value><xsl:value-of select="description"/></new-value>
                      <description>Description set to <xsl:value-of select="description"/></description>                
                    </activity>      
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Status</attribute>                                
                      <new-option><xsl:value-of select="status"/></new-option>
                      <description>Status set to <xsl:value-of select="status"/></description>                
                    </activity>
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Environment</attribute>                                
                      <new-value><xsl:value-of select="environment"/></new-value>
                      <description>Environment set to <xsl:value-of select="environment"/></description>                
                    </activity>   
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Priority</attribute>                                
                      <new-option><xsl:value-of select="priority"/></new-option>
                      <description>Priority set to <xsl:value-of select="priority"/></description>                
                    </activity>
                    <xsl:if test="version!=''">
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Version</attribute>                                
                      <new-option><xsl:value-of select="version"/></new-option>
                      <description>Version set to <xsl:value-of select="version"/></description>                
                    </activity>   
                    </xsl:if>
                    <xsl:if test="component!=''">
                        <activity>
                          <!--id>4</id-->
                            <attribute>[JIRA] Component</attribute>                                
                          <new-option><xsl:value-of select="component"/></new-option>
                          <description>Component set to <xsl:value-of select="component"/></description>                
                        </activity> 
                    </xsl:if>
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Due</attribute>                                
                      <new-value><xsl:value-of select="due"/></new-value>
                      <description>Due set to <xsl:value-of select="due"/></description>                
                    </activity>    
                    <activity>
                      <!--id>4</id-->
                        <attribute>[JIRA] Votes</attribute>                                
                      <new-value><xsl:value-of select="votes"/></new-value>
                      <description>Votes set to <xsl:value-of select="votes"/></description>                
                    </activity>         
                  </activities>
                </activity-set>
                <activity-set>
                  <!--id>410</id-->
                  <type>Edit Issue</type>
                  <created-date>
                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                    <timestamp><xsl:value-of select="updated"/></timestamp>
                  </created-date>
                  <activities>
                    <activity>
                      <!--id>720</id-->
                        <attribute>[JIRA] Resolution</attribute>                
                      <new-option><xsl:value-of select="resolution"/></new-option>
                      <new-value><xsl:value-of select="resolution"/></new-value>
                      <description>Resolution set to <xsl:value-of select="resolution"/></description>              
                    </activity>
                    <xsl:if test="assignee!='Unassigned'">
                        <xsl:for-each select="assignee">
                        <activity>
                              <!--id>720</id-->
                                <attribute>[JIRA] Assignee</attribute>                
                              <new-user><xsl:value-of select="@username"/></new-user>
                              <new-value><xsl:value-of select="@username"/></new-value>
                              <description>Assignee set to <xsl:value-of select="@username"/> (<xsl:value-of select="."/>)</description>              
                            </activity>
                        </xsl:for-each>
                    </xsl:if>
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