<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:scarab="http://scarab.tigris.org"
                exclude-result-prefixes="xs fn scarab"
>

<xsl:output     method="xml" encoding="UTF-8" indent="yes"
                doctype-system="http://scarab.tigris.org/dtd/scarab-0.21.0.dtd"
/>

<!-- This XSL transform has been created to transform Bugzilla 2.16.1 XML 
     export as returned by xml.cgi to Scarab 1.0-b21-dev import XML.
     
     $Id$
     Copyright (c) 2006 Steve James. All rights reserved ste@cpan.org
     This script is released under the same terms as Scarab itself.
     See http://scarab.tigris.org/
     
     See bugzilla/extensions/README.txt for instructions
-->

<!-- Path to our resources. We look in $resources_path/attachments for
     attachment files. The server instance must be able to see this
     path during the import. -->
<xsl:param name="resources_path" as="xs:string"/>

<!-- The Scarab module code -->
<xsl:param name="module_code" as="xs:string"/>

<!-- ===================================================================== -->
<!-- Parameters for Bugzilla to Scarab mappings -->

<xsl:include href="parameters.xsl" />

<!-- ===================================================================== -->
<!-- A look-up table for Bugzilla to Scarab attribute value mappings -->

<xsl:include href="mappings.xsl" />

<!-- ===================================================================== -->
<!-- A look-up table of attachment mime-types -->
<xsl:include href="attachments/mime_types.xsl" />

<!-- ===================================================================== -->
<!-- The Bugzilla XML export tells us the base URL to the server -->
<xsl:variable name="bz_urlbase" as="xs:string" select="bugzilla/@urlbase" />

<!-- ===================================================================== -->

<!-- This template does a look-up on the given table for a Bugzilla to Scarab
     mapping.
-->
<xsl:template name="scarab:bzlookup" as="xs:string">
  <xsl:param name="table"/>  <!-- The look-up table -->
  <xsl:param name="attr"/>   <!-- The name of the attribute to look for -->
  <xsl:param name="from"/>   <!-- The attribute value to look for -->
  
  <xsl:variable name="result" select="$table[@id=$from]"/>
  <xsl:choose>
    <xsl:when test="count($result) > 1">
      <xsl:value-of select="error(fn:QName('scarab', 'scarab:bzlookup'), fn:concat('There is more than one ', $attr, ' for ', $from))"/>
    </xsl:when>
    <xsl:when test="$result">
      <xsl:value-of select="$result"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="error(fn:QName('scarab', 'scarab:bzlookup'), fn:concat('Do not have a ', $attr,' for ', $from))"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ===================================================================== -->

<!-- Given a Bugzilla account name, returns a Scarab account name -->
<xsl:function name="scarab:account" as="xs:string">
  <xsl:param name="email" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/account"/>
    <xsl:with-param name="attr"  select="'email'"/>
    <xsl:with-param name="from"  select="$email"/>
  </xsl:call-template>
</xsl:function>
    
<!-- Given a Bugzilla status, returns a Scarab status -->
<xsl:function name="scarab:status" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/status"/>
    <xsl:with-param name="attr"  select="'status'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla severity, returns a Scarab severity -->
<xsl:function name="scarab:severity" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>

  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/severity"/>
    <xsl:with-param name="attr"  select="'severity'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla platform, returns a Scarab platform -->
<xsl:function name="scarab:platform" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>

  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/platform"/>
    <xsl:with-param name="attr"  select="'platform'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla OS, returns a Scarab OS -->
<xsl:function name="scarab:os" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>

  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/os"/>
    <xsl:with-param name="attr"  select="'os'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla resolution, returns a Scarab resolution -->
<xsl:function name="scarab:resolution" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/resolution"/>
    <xsl:with-param name="attr"  select="'resolution'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla priority, returns a Scarab priority -->
<xsl:function name="scarab:priority" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/priority"/>
    <xsl:with-param name="attr"  select="'priority'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla component, returns a Scarab functional area -->
<xsl:function name="scarab:functionalarea" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/component"/>
    <xsl:with-param name="attr"  select="'component'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla milestone, returns a Scarab milestone -->
<xsl:function name="scarab:milestone" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/milestone"/>
    <xsl:with-param name="attr"  select="'milestone'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla version, returns a Scarab version -->
<xsl:function name="scarab:version" as="xs:string">
  <xsl:param name="bz" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_to_scarab/version"/>
    <xsl:with-param name="attr"  select="'version'"/>
    <xsl:with-param name="from"  select="$bz"/>
  </xsl:call-template>
</xsl:function>

<!-- Given a Bugzilla bug id, return its mime-type -->
<xsl:function name="scarab:mime-type" as="xs:string">
  <xsl:param name="id" as="xs:string"/>
  
  <xsl:call-template name="scarab:bzlookup">
    <xsl:with-param name="table" select="$bz_mime_types/mime_type"/>
    <xsl:with-param name="attr"  select="'mime_type'"/>
    <xsl:with-param name="from"  select="$id"/>
  </xsl:call-template>
</xsl:function>

<!-- ===================================================================== -->

<!-- A potted reason block for attaching to all activity sets -->
<xsl:variable name="reason_import">
  <attachment>
    <name>reason</name>
    <type>MODIFICATION</type>
    <data>Bugzilla import</data>
    <mimetype>text/plain</mimetype>
    <created-date>
    <format>yyyy-MM-dd HH:mm:ss z</format>
    <timestamp><xsl:value-of select="format-dateTime(current-dateTime(), '[Y0001]-[M01]-[D01] [H01]:[m01]:[s01]'), $bz_timezone" separator=" "/></timestamp>
    </created-date>
    <created-by>Administrator</created-by>
    <deleted>false</deleted>
  </attachment>
</xsl:variable>


<!-- ===================================================================== -->

<!-- Inserts activity for a list attribute -->                    
<xsl:template name="list_attribute_activity">
  <xsl:param name="attribute_name" as="xs:string"/>
  <xsl:param name="attribute_value"/>
  
  <xsl:if test="$attribute_value">
    <activity>
      <attribute>
        <xsl:value-of select="$attribute_name"/></attribute>
      <new-option>
        <xsl:value-of select="$attribute_value"/></new-option>
      <new-value>
        <xsl:value-of select="$attribute_value"/></new-value>
      <description>
        <xsl:value-of select="$attribute_name"/> set to '<xsl:value-of select="$attribute_value"/>'</description>
    </activity>
  </xsl:if>        
  
</xsl:template>

<!-- Inserts activity for a string attribute -->                    
<xsl:template name="string_attribute_activity">
  <xsl:param name="attribute_name" as="xs:string"/>
  <xsl:param name="value_name" as="xs:string"/>
  <xsl:param name="attribute_value"/>

  <xsl:if test="$attribute_value">

    <activity>
      <attribute>
        <xsl:value-of select="$attribute_name"/></attribute>

      <xsl:if test="$value_name">
        <xsl:element name="{$value_name}">
          <xsl:value-of select="$attribute_value"/></xsl:element>
      </xsl:if>
      
      <new-value>
        <xsl:value-of select="$attribute_value"/></new-value>
      
      <!-- When inserting the description attribute, truncate the 
           text if it's long -->
      <xsl:choose>
        <xsl:when test="string-length($attribute_value) > 30">
        <description>
          <xsl:value-of select="$attribute_name"/> set to '<xsl:value-of select="substring($attribute_value,1,30)"/>...'</description>
        </xsl:when>
        <xsl:otherwise>
        <description>
          <xsl:value-of select="$attribute_name"/> set to '<xsl:value-of select="$attribute_value"/>'</description>
        </xsl:otherwise>
      </xsl:choose>
    </activity>
      
  </xsl:if>        
    
</xsl:template>

<!-- ===================================================================== -->

<!-- Emits a Scarab issue for a given Bugzilla bug -->
<xsl:template name="issue" match="bug">
  <xsl:param name="artifact_type" as="xs:string"/>

  <issue>
    <id><xsl:value-of select="bug_id"/></id>
    <artifact-type><xsl:value-of select="$artifact_type"/></artifact-type>
    <activity-sets>
      <!-- We set an alphabetic prefix on activity set IDs so that Scarab auto-
           allocates new ID numbers. Note that each activity set name must be
           unique, so we use a prefix according to the purpose of the set, and
           the Bugzilla bug ID as a unique suffix.
      -->
    
      <!-- =============================================================== -->
      <!-- First activity set creates the Scarab issue -->
        
      <activity-set>
        <id>bug<xsl:value-of select="bug_id"/></id>
        <type>Create Issue</type>
        <created-by>
            <xsl:value-of select="scarab:account(reporter)"/>
        </created-by>
        <created-date>
            <format>yyyy-MM-dd HH:mm z</format>
            <timestamp><xsl:value-of select="creation_ts, $bz_timezone" separator=" "/></timestamp>
        </created-date>

        <activities>
            
          <!-- =========================================================== -->
          <!-- Start of import of Bugzilla attributes -->

          <activity>
            <attribute>NullAttribute</attribute>
            <description>Issue created by import from Bugzilla bug <xsl:value-of select="bug_id"/></description>
          </activity>

          <!-- =========================================================== -->
          <!-- String attributes -->
          
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Summary'"/>
            <xsl:with-param name="value_name"
                select="''"/>
            <xsl:with-param name="attribute_value"
                select="short_desc"/>
          </xsl:call-template>
          
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Description'"/>
            <xsl:with-param name="value_name"
                select="''"/>
            <!-- The description is in the first long_desc -->
            <xsl:with-param name="attribute_value"
                select="long_desc[1]/thetext"/>
          </xsl:call-template>
          
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'AssignedTo'"/>
            <xsl:with-param name="value_name"
                select="'new-user'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:account(assigned_to)"/>
          </xsl:call-template>
          
          <xsl:for-each select="cc">
          <xsl:call-template name="string_attribute_activity">
             <xsl:with-param name="attribute_name"
                select="'AssignedCC'"/>
            <xsl:with-param name="value_name"
                select="'new-user'"/>
             <xsl:with-param name="attribute_value"
                select="scarab:account(.)"/>
          </xsl:call-template>
          </xsl:for-each>
          
          <!-- =========================================================== -->
          <!-- Enumerated attributes -->
          
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Status'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:status(current()/bug_status)"/>
          </xsl:call-template>
          
          <xsl:if test="resolution">
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Resolution'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:resolution(current()/resolution)"/>
          </xsl:call-template>
          </xsl:if>

          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Priority'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:priority(current()/priority)"/>
          </xsl:call-template>
          
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'FunctionalArea'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:functionalarea(current()/component)"/>
          </xsl:call-template>

          <!-- =========================================================== -->
          <!-- These standard Bugzilla attributes do not appear in the
          'standard' Scarab defect and enhancement artifacts. They
          must be created first. -->
          
          <!--Note, to transform this Bugzilla attribute the 
              user attribute 'AssignedQA' must be created in 
              Scarab first.
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'AssignedQA'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:account(current()/qa_contact)"/>
          </xsl:call-template>
          -->
          
          <!--Note, to transform this Bugzilla attribute the 
              global string attribute 'Keywords' must be created in 
              Scarab first.
          -->
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Keywords'"/>
            <xsl:with-param name="value_name"
                select="''"/>
            <xsl:with-param name="attribute_value"
                select="keywords"/>
          </xsl:call-template>
          
          <!--Note, to transform this Bugzilla attribute the 
              global string attribute 'Whiteboard' must be created in 
              Scarab first.
          <xsl:call-template name="string_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Whiteboard'"/>
            <xsl:with-param name="attribute_value"
                select="status_whiteboard"/>
          </xsl:call-template>
          -->
                                                          
          <!--Note, to transform this Bugzilla attribute the 
              global list attribute 'Milestone' must be created in 
              Scarab first.
          -->
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'TargetMilestone'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:milestone(current()/target_milestone)"/>
          </xsl:call-template>
          
          <!-- =========================================================== -->
          <!-- Attributes that are applicable to defect artifacts only -->
                                                            
          <xsl:choose> 
          <xsl:when test="$artifact_type = 'Enhancement'" />
              <!--- Enhancements don't have the following attributes -->
          <xsl:otherwise>
          
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Severity'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:severity(current()/bug_severity)"/>
          </xsl:call-template>
          
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Platform'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:platform(current()/rep_platform)"/>
          </xsl:call-template>
          
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'OperatingSystem'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:os(current()/op_sys)"/>
          </xsl:call-template>
          
          <!--Note, to transform this Bugzilla attribute the 
              global list attribute 'Version' must be created in 
              Scarab first.
          -->
          <xsl:call-template name="list_attribute_activity">
            <xsl:with-param name="attribute_name"
                select="'Version'"/>
            <xsl:with-param name="attribute_value"
                select="scarab:version(current()/version)"/>
          </xsl:call-template>
          
          </xsl:otherwise>
          </xsl:choose>
          
          <!-- =========================================================== -->
          <!-- Bugzilla URL attribute -->
          
          <xsl:if test="bug_file_loc">
          <activity>
            <attribute>NullAttribute</attribute>
            <description>Added Bugzilla URL</description>
            <attachment>
              <name>Bugzilla URL</name>
              <type>URL</type>
              <data><xsl:value-of select="bug_file_loc"/></data>
              <mimetype>text/plain</mimetype>
              <created-date>
                <format>yyyy-MM-dd HH:mm z</format>
                <timestamp><xsl:value-of select="creation_ts, $bz_timezone" separator=" "/></timestamp>
              </created-date>
              <!-- Bugzilla doesn't store who created the URL -->
              <created-by>Administrator</created-by>
              <deleted>false</deleted>
            </attachment>
          </activity>
          </xsl:if>
          
          <!-- End of artifact creation activities -->
          <!-- =========================================================== -->
                                          
        </activities>

        <!-- Add the Bugzilla import reason -->
        <xsl:copy-of select="$reason_import"/>
                            
      </activity-set>

      
      <!-- =============================================================== -->
      <!-- These activity sets transform dependency relationships -->
     
      <xsl:for-each select="dependson">
      <activity-set>
        <!-- Note the activity id uses the Bugzilla id of this dependant bug.
             We must use the same activity set id for both the dependant
             and prerequisite bugs.
        -->
        <id>depend<xsl:value-of select="../bug_id"/></id>
        <type>Edit Issue</type>
        <created-by><xsl:value-of select="scarab:account(current()/../reporter)"/></created-by>
        <created-date>
          <format>yyyy-MM-dd HH:mm z</format>
          <timestamp><xsl:value-of select="../creation_ts, $bz_timezone" separator=" "/></timestamp>
        </created-date>
        <activities>
                              
          <activity>
            <attribute>NullAttribute</attribute>
            <new-value>blocking</new-value>
            <dependency>
              <parent><xsl:value-of select="$module_code"/><xsl:value-of select="."/></parent>
              <child><xsl:value-of select="$module_code"/><xsl:value-of select="../bug_id"/></child>
              <type>blocking</type>
              <deleted>false</deleted>
            </dependency>
            
            <description>Issue <xsl:value-of select="$module_code"/><xsl:value-of select="../bug_id"/> depends on issue <xsl:value-of select="$module_code"/><xsl:value-of select="."/></description>
          </activity>
        
        </activities>
            
        <xsl:copy-of select="$reason_import"/>
              
      </activity-set>
      </xsl:for-each>

      
      <xsl:for-each select="blocks">
      <activity-set>
        <!-- Note the activity id uses the Bugzilla id of the dependant bug 
             We must use the same activity set id for both the dependant
             and prerequisite bugs.
        -->
        <id>depend<xsl:value-of select="."/></id>
        <type>Edit Issue</type>
        <created-by>Administrator</created-by>
        <created-date>
          <format>yyyy-MM-dd HH:mm z</format>
          <timestamp><xsl:value-of select="../creation_ts, $bz_timezone" separator=" "/></timestamp>
        </created-date>
        <activities>
        
          <activity>
            <attribute>NullAttribute</attribute>
            <new-value>blocking</new-value>
            <dependency>
              <parent><xsl:value-of select="$module_code"/><xsl:value-of select="../bug_id"/></parent>
              <child><xsl:value-of select="$module_code"/><xsl:value-of select="."/></child>
              <type>blocking</type>
              <deleted>false</deleted>
            </dependency>
            
            <description>Issue <xsl:value-of select="$module_code"/><xsl:value-of select="."/> depends on issue <xsl:value-of select="$module_code"/><xsl:value-of select="../bug_id"/></description>
          </activity>
        </activities>
            
        <xsl:copy-of select="$reason_import"/>
              
      </activity-set>
      </xsl:for-each>
          
      <!-- =============================================================== -->
      <!-- This activity set transforms comments -->
    
      <xsl:for-each select="long_desc[position() != 1]">
        <!-- The Description is in the first long_desc so we skip it here -->
          
      <activity-set>
        <!-- Each comment will go into a unique activity set -->
        <id>comments<xsl:value-of select="../bug_id"/>_<xsl:number/></id>
        <type>Edit Issue</type>
        <created-by>Administrator</created-by>
        <created-date>
          <format>yyyy-MM-dd HH:mm:ss z</format>
          <timestamp><xsl:value-of select="bug_when, $bz_timezone" separator=" "/></timestamp>
        </created-date>
        <activities>
          <activity>
            <attribute>NullAttribute</attribute>
            <description><xsl:value-of select="scarab:account(current()/who)"/> added a comment</description>
            <attachment>
              <id>comment<xsl:value-of select="../bug_id"/>_<xsl:number/></id>
              <name>comment</name>
              <type>COMMENT</type>
              <data><xsl:value-of select="thetext"/></data>
              <mimetype>text/plain</mimetype>
              <created-date>
                <format>yyyy-MM-dd HH:mm:ss z</format>
                <timestamp><xsl:value-of select="bug_when, $bz_timezone" separator=" "/></timestamp>
              </created-date>
              <created-by><xsl:value-of select="scarab:account(current()/who)"/></created-by>
              <deleted>false</deleted>
            </attachment>
          </activity>
        </activities>
            
        <xsl:copy-of select="$reason_import"/>
              
      </activity-set>
      </xsl:for-each>

      <!-- =============================================================== -->
      <!-- This activity set transforms attachments -->
      
      <!-- We add attachments here as extra activites. A more correct solution
           would be to detect the "Created an attachment (id=[0-9]+)" messages
           inserted into the comments by Bugzilla. The attachment could then
           be added as activity in that comment's activity set.
      -->
      
      <xsl:for-each select="attachment">
      <!-- The Bugzilla XML export does not contain the attachment data, only
           their IDs, description and date.
      -->
      <activity-set>
        <!-- Each attachment will go into a unique activity set -->
        <id>attachments<xsl:value-of select="../bug_id"/>_<xsl:value-of select="attachid"/></id>
        <type>Edit Issue</type>
        <!-- Bugzilla doesn't store who created the attachment -->
        <created-by>Administrator</created-by>
        <created-date>
            <format>MM/dd/yy HH:mm z</format>
            <timestamp><xsl:value-of select="date, $bz_timezone" separator=" "/></timestamp>
        </created-date>
        <activities>
          <activity>
            <attribute>NullAttribute</attribute>
            <description>Added attachment (Bugzilla id=<xsl:value-of select="attachid"/>)</description>
            <attachment>
              <id>attachment<xsl:value-of select="../bug_id"/>_<xsl:value-of select="attachid"/></id>
              <name><xsl:value-of select="desc"/></name>
              <type>ATTACHMENT</type>
  
              <filename><xsl:value-of select="$resources_path"/>/attachments/<xsl:value-of select="attachid"/></filename>
              <reconcile-path>true</reconcile-path>
              <mimetype><xsl:value-of select="scarab:mime-type(attachid)"/></mimetype>
  
              <created-date>
                  <format>MM/dd/yy HH:mm z</format>
                  <timestamp><xsl:value-of select="date, $bz_timezone" separator=" "/></timestamp>
              </created-date>
              <!-- Bugzilla doesn't store who created the attachment -->
              <created-by>Administrator</created-by>
              <deleted>false</deleted>
            </attachment>
          </activity>
        </activities>
        
        <xsl:copy-of select="$reason_import"/>
          
      </activity-set>
      </xsl:for-each>
    
    </activity-sets>
  </issue>

</xsl:template>

<!-- ===================================================================== -->

<xsl:template name="bugzilla-to-scarab" match="bugzilla">

  <scarab-issues>
    <import-type>create-same-db</import-type>
    
    <!-- ImportIssues.insertModuleNode(..)  -->

    <issues>
      <!-- We iterate over all the bugs for the given product 
            Selecting only those for the current Product
            We also skip any bug that has a Status of CLOSED
            We also skip any bug that has a Resolution of INVALID
            We also skip any bug that has a Resolution of DUPLICATE
            We also skip any bug that has a Resolution of MOVED
      -->
      <xsl:for-each select="bug[
        product=$bz_product
        and bug_status!='CLOSED'
        and ( not(resolution)
              or resolution!='INVALID'
              or resolution!='DUPLICATE'
              or resolution!='MOVED' )
        ]">

        <!-- Report what we're processing -->
        <xsl:message>
          <xsl:value-of select="bug_id" />
          : <xsl:value-of select="assigned_to" />
          : <xsl:value-of select="bug_status" />
          : <xsl:value-of select="resolution" />
          : <xsl:value-of select="substring(short_desc, 1, 67)"/>
        </xsl:message>
      
        <xsl:choose>
          <xsl:when test="@error">
            <!-- don't output bugs which are in error -->
          </xsl:when>
          
          <xsl:when test="bug_severity = 'enhancement'">
            <xsl:call-template name="issue">
                <xsl:with-param name="artifact_type" select="'Enhancement'"/>
            </xsl:call-template>
          </xsl:when>
          
          <xsl:otherwise>
            <xsl:call-template name="issue">
                <xsl:with-param name="artifact_type" select="'Defect'"/>
            </xsl:call-template>
          </xsl:otherwise>
        
        </xsl:choose>
      </xsl:for-each>
    </issues>
    
  </scarab-issues>
</xsl:template>

<!-- ===================================================================== -->
</xsl:stylesheet>
