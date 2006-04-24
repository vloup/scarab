<?xml version="1.0" encoding="UTF-8" ?>
<!--
    Document   : jira.xsl
    Created on : November 30, 2005, 6:04 AM
    Author     : hair
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" encoding="UTF-8" indent="yes"
                doctype-system="http://scarab.tigris.org/dtd/scarab-0.21.0.dtd"/>

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
                  
                  <type>Create Issue</type>
                  <xsl:for-each select="reporter"><!-- [XXX] Only ever be one, but we need to get to attribute @username -->
                    <xsl:element name="created-by">
                        <xsl:attribute name="username"><xsl:value-of select="@username"/></xsl:attribute>
                        <xsl:attribute name="fullname"><xsl:value-of select="."/></xsl:attribute>
                        <xsl:value-of select="@username"/>
                    </xsl:element>
                  </xsl:for-each>
                  <created-date>
                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                    <timestamp><xsl:value-of select="created"/></timestamp>
                  </created-date>
                  <activities>
                    <activity>
                      
                      <attribute>[JIRA] Summary</attribute>
                      <new-value><xsl:value-of select="summary"/></new-value>
                      <description>Issue <xsl:value-of select="key"/> had Summary set to '<xsl:value-of select="summary"/>'</description>
                      <end-date>
                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                        <timestamp><xsl:value-of select="created"/></timestamp>
                      </end-date>
                    </activity>
                    <activity>
                      
                        <attribute>[JIRA] Description</attribute>
                        <!-- JIRA writes its descriptions out in xhtml format -->
                        <new-value>__Original ID: <xsl:value-of select="key"/>__ \\ 
                            <xsl:value-of select="translate(description,'&lt;br/&gt;','')"/>
                        </new-value>
                      <description>Description set to <xsl:value-of select="description" disable-output-escaping="yes"/></description>
                    </activity>
                    <activity>
                      
                        <attribute>[JIRA] Status</attribute>
                      <new-option><xsl:value-of select="status"/></new-option>
                      <description>Status set to <xsl:value-of select="status"/></description>
                    </activity>
                    <activity>
                      
                        <attribute>[JIRA] Environment</attribute>
                      <new-value><xsl:value-of select="environment"/></new-value>
                      <description>Environment set to <xsl:value-of select="environment"/></description>
                    </activity>
                    <activity>
                      
                        <attribute>[JIRA] Priority</attribute>
                      <new-option><xsl:value-of select="priority"/></new-option>
                      <description>Priority set to <xsl:value-of select="priority"/></description>
                    </activity>
                    <xsl:if test="version!=''">
                    <activity>
                      
                        <attribute>[JIRA] Version</attribute>
                      <new-option><xsl:value-of select="version[1]"/></new-option>
                      <description>Version set to <xsl:value-of select="version"/></description>
                    </activity>
                    </xsl:if>
                    <xsl:if test="component!=''">
                        <activity>
                          
                            <attribute>[JIRA] Component</attribute>
                          <new-option><xsl:value-of select="component"/></new-option>
                          <description>Component set to <xsl:value-of select="component"/></description>
                        </activity>
                    </xsl:if>
                    <activity>
                      
                        <attribute>[JIRA] Due</attribute>
                      <new-value><xsl:value-of select="due"/></new-value>
                      <description>Due set to <xsl:value-of select="due"/></description>
                    </activity>
                    <activity>
                      
                        <attribute>[JIRA] Votes</attribute>
                      <new-value><xsl:value-of select="votes"/></new-value>
                      <description>Votes set to <xsl:value-of select="votes"/></description>
                    </activity>
                  </activities>
                </activity-set>
                <activity-set>
                  
                  <type>Edit Issue</type>
                  <created-date>
                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                    <timestamp><xsl:value-of select="updated"/></timestamp>
                  </created-date>
                  <activities>
                    <activity>
                      
                        <attribute>[JIRA] Resolution</attribute>
                      <new-option><xsl:value-of select="resolution"/></new-option>
                      <new-value><xsl:value-of select="resolution"/></new-value>
                      <description>Resolution set to <xsl:value-of select="resolution"/></description>
                    </activity>
                    <xsl:if test="assignee!='Unassigned'">
                        <xsl:for-each select="assignee">
                        <activity>
                            <attribute>[JIRA] Assignee</attribute>
                            <xsl:element name="new-user">
                                <xsl:attribute name="username"><xsl:value-of select="@username"/></xsl:attribute>
                                <xsl:attribute name="fullname"><xsl:value-of select="."/></xsl:attribute>
                                <xsl:value-of select="@username"/>
                            </xsl:element>
                          <new-value><xsl:value-of select="@username"/></new-value>
                          <description>Assignee set to <xsl:value-of select="@username"/> (<xsl:value-of select="."/>)</description>
                        </activity>
                        </xsl:for-each>
                    </xsl:if>
                  </activities>
                </activity-set>
                
                <xsl:for-each select="comments/comment">
                    <activity-set>
                      <type>Edit Issue</type>
                      <created-date>
                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                        <timestamp><xsl:value-of select="../../updated"/></timestamp>
                      </created-date>
                      <attachment>
                        <name>comment</name>
                        <type>COMMENT</type>
                        <data><xsl:value-of select="." disable-output-escaping="yes"/></data>
                        <mimetype>text/plain</mimetype>
                        <created-date>
                          <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                          <timestamp><xsl:value-of select="@created"/></timestamp>
                        </created-date>
                        <created-by><xsl:value-of select="@author"/></created-by>
                        <deleted>false</deleted>
                      </attachment>
                      </activity-set>
                  </xsl:for-each>

              </activity-sets>
            </issue>

        </xsl:for-each>

      </issues>

    </scarab-issues>

  </xsl:template>

</xsl:stylesheet>