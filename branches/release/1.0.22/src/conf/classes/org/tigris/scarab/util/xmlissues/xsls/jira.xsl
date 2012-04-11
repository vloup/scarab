<?xml version="1.0" encoding="UTF-8" ?>
<!--
    Document   : jira.xsl
    Created on : November 30, 2005, 6:04 AM
    Author     : hair
    Description: Transform a jira xml into scarab xml. The transformed xml can then be imported into scarab.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
                    
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<!--    doctype-system="http://scarab.tigris.org/dtd/scarab-0.21.0.dtd"/-->

    <!-- Path to our resources. We look in $resources_path/attachments for
         attachment files. The server instance must be able to see this
         path during the import. 
         Default is WEB-INF/attachments -->
    <xsl:param name="resources_path" as="xs:string"/>

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
                                        <new-value>(Original JIRA Id: <xsl:value-of select="key"/>) \\
<xsl:call-template name="tidy-html"><xsl:with-param name="input" select="description"/></xsl:call-template>
                                        </new-value>
                                        <description>Description set to  <xsl:call-template name="tidy-html"><xsl:with-param name="input" select="description"/></xsl:call-template>
                                        </description>
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
                                    <xsl:for-each select="component">
                                        <activity>
                                            <attribute>[JIRA] Component</attribute>
                                            <new-option><xsl:value-of select="."/></new-option>
                                            <description>Component set to <xsl:value-of select="."/></description>
                                        </activity>
                                    </xsl:for-each>
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
                                    <xsl:for-each select="customfields/customfield">
                                        <activity>
                                            <attribute>[JIRA] <xsl:value-of select="customfieldname"/></attribute>
                                            <!-- XXX not sure this will work, it should be improved to just use [0] anyway -->
                                            <xsl:for-each select="customfieldvalues/customfieldvalue">
                                                <new-value><xsl:value-of select="."/></new-value>
                                                <description><xsl:value-of select="customfieldname"/> set to <xsl:value-of select="."/></description>
                                            </xsl:for-each>
                                        </activity>
                                    </xsl:for-each>
                                </activities>
                            </activity-set>
                            <activity-set>
                                <type>Edit Issue</type>
                                <xsl:for-each select="assignee"><!-- [XXX] Only ever be one, but we need to get to attribute @username -->
                                    <xsl:element name="created-by">
                                        <xsl:attribute name="username"><xsl:value-of select="@username"/></xsl:attribute>
                                        <xsl:attribute name="fullname"><xsl:value-of select="."/></xsl:attribute>
                                        <xsl:value-of select="@username"/>
                                    </xsl:element>
                                </xsl:for-each>
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
                                    <created-by><xsl:value-of select="@author"/></created-by>
                                    <created-date>
                                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                        <timestamp><xsl:value-of select="../../updated"/></timestamp>
                                    </created-date>
                                    <activities>
                                        <activity>
                                            <attribute>NullAttribute</attribute>
                                            <description>Added comment from '<xsl:value-of select="@author"/>'</description>
                                        </activity>
                                    </activities>
                                            <attachment>
                                                <name>comment</name>
                                                <type>COMMENT</type>
                                                <data><xsl:call-template name="tidy-html"><xsl:with-param name="input" select="."/></xsl:call-template></data>
                                                <mimetype>text/plain</mimetype>
                                                <created-date>
                                                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                                    <timestamp><xsl:value-of select="@created"/></timestamp>
                                                </created-date>
                                                <modified-date>
                                                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                                    <timestamp><xsl:value-of select="@created"/></timestamp>
                                                </modified-date>
                                                <created-by><xsl:value-of select="@author"/></created-by>
                                                <deleted>false</deleted>
                                                <description>Added comment from '<xsl:value-of select="@author"/>'</description>
                                            </attachment>
                                </activity-set>
                            </xsl:for-each>

                            <xsl:for-each select="issuelinks/issuelinktype">
                                <activity-set>
                                    <!-- Note the id uses the id of this dependant bug.
                                    We must use the same activity set id for both the dependant
                                    and prerequisite bugs.
                                    -->
                                    <!--id><xsl:value-of select="@id"/></id-->
                                    <type>Edit Issue</type>
                                    <created-by><xsl:value-of select="../../reporter/@username"/></created-by>
                                    <created-date>
                                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                        <timestamp><xsl:value-of select="../../created"/></timestamp>
                                    </created-date>
                                    <activities>

                                        <xsl:for-each select="outwardlinks/issuelink">
                                            <activity>
                                                <attribute>NullAttribute</attribute>
                                                <xsl:variable name="type">
                                                    <xsl:if test="starts-with(../../name, 'Depends')">blocking</xsl:if>
                                                    <xsl:if test="starts-with(../../name, 'Duplicate')">duplicate</xsl:if>
                                                </xsl:variable>
                                                <new-value><xsl:value-of select="$type"/></new-value>
                                                <dependency>
                                                    <id><xsl:value-of select="issuekey/@id"/></id>
                                                    <parent><xsl:value-of select="translate(../../../../key,'-','')"/></parent>
                                                    <child><xsl:value-of select="translate(issuekey,'-','')"/></child>
                                                    <type><xsl:value-of select="$type"/></type>
                                                    <deleted>false</deleted>
                                                </dependency>
                                                <description><xsl:value-of select="translate(issuekey,'-','')"/> <xsl:value-of select="../@description"/></description>
                                            </activity>
                                        </xsl:for-each>

                                        <xsl:for-each select="inwardlinks/issuelink">
                                            <activity>
                                                <attribute>NullAttribute</attribute>
                                                <xsl:variable name="type">
                                                    <xsl:if test="starts-with(../../name, 'Depends')">blocking</xsl:if>
                                                    <xsl:if test="starts-with(../../name, 'Duplicate')">duplicate</xsl:if>
                                                </xsl:variable>
                                                <new-value><xsl:value-of select="$type"/></new-value>
                                                <dependency>
                                                    <id><xsl:value-of select="issuekey/@id"/></id>
                                                    <parent><xsl:value-of select="translate(issuekey,'-','')"/></parent>
                                                    <child><xsl:value-of select="translate(../../../../key,'-','')"/></child>
                                                    <type><xsl:value-of select="$type"/></type>
                                                    <deleted>false</deleted>
                                                </dependency>
                                                <description><xsl:value-of select="translate(issuekey,'-','')"/> <xsl:value-of select="../@description"/></description>
                                            </activity>
                                        </xsl:for-each>
                                    </activities>

                                </activity-set>
                            </xsl:for-each>
                            
                            <xsl:for-each select="attachments/attachment">
                                <activity-set>
                                    <type>Edit Issue</type>
                                    <created-by><xsl:value-of select="@author"/></created-by>
                                    <created-date>
                                        <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                        <timestamp><xsl:value-of select="@created"/></timestamp>
                                    </created-date>
                                    <activities>
                                        <activity>
                                            <attribute>NullAttribute</attribute>
                                            <description>Added file attachment '<xsl:value-of select="@id"/>_<xsl:value-of select="@name"/>'</description>
                                            <attachment>
                                                <name>attachment</name>
                                                <type>ATTACHMENT</type>
                                                <!-- the following path presumes all attachments have been copied into WEB-INF/attachments/ -->
                                                <filename><xsl:value-of select="$resources_path"/><xsl:value-of select="@id"/>_<xsl:value-of select="@name"/></filename>
                                                <!-- and this will copy it to the correct subdirectory in WEB-INF/attachments (or what scarab.attachents.repository is) -->
                                                <reconcile-path>true</reconcile-path>
                                                <created-date>
                                                    <format>EEE, d MMM yyyy HH:mm:ss Z (z)</format>
                                                    <timestamp><xsl:value-of select="@created"/></timestamp>
                                                </created-date>
                                                <created-by><xsl:value-of select="@author"/></created-by>
                                                <deleted>false</deleted>
                                                <description>Added file attachment '<xsl:value-of select="@id"/>_<xsl:value-of select="@name"/>'</description>
                                            </attachment>
                                        </activity>
                                    </activities>
                                </activity-set>
                            </xsl:for-each>
                            
                        </activity-sets>
                    </issue>

                </xsl:for-each>

            </issues>

        </scarab-issues>

    </xsl:template>

    <!-- Utility templates -->

    <xsl:template name="tidy-html">
       <xsl:param name="input"/>

       <xsl:call-template name="string-replace">
            <xsl:with-param name="string">

                <xsl:call-template name="string-replace">
                    <xsl:with-param name="string" select="$input"/>
                    <xsl:with-param name="from">&amp;nbsp;</xsl:with-param>
                    <xsl:with-param name="to"> </xsl:with-param>
                </xsl:call-template>

            </xsl:with-param>
            <xsl:with-param name="from">&lt;br/&gt;</xsl:with-param>
            <xsl:with-param name="to"> </xsl:with-param>
        </xsl:call-template>

    </xsl:template>

    <xsl:template name="string-replace" >
        <xsl:param name="string"/>
        <xsl:param name="from"/>
        <xsl:param name="to"/>
        <xsl:choose>
            <xsl:when test="contains($string,$from)">
                <xsl:value-of select="substring-before($string,$from)"/>
                <xsl:value-of select="$to"/>
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="string" select="substring-after($string,$from)"/>
                    <xsl:with-param name="from" select="$from"/>
                    <xsl:with-param name="to" select="$to"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string" disable-output-escaping="no"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
