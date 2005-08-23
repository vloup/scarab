<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="username"/>
	<xsl:param name="moduleid"/>
	<xsl:param name="modulename"/>
	<xsl:param name="scarabdomain"/>
	<xsl:param name="modulecode"/>
	<xsl:param name="ImportDatetime"/>
	<xsl:template match="/">
		<scarab-issues>
			<import-type>create-different-db</import-type>
			<module>
				<id><xsl:value-of select="$moduleid"/></id>
				<parent-id/>
				<name><xsl:value-of select="$modulename"/></name>
				<owner>1</owner>
				<description/>
				<url></url>
				<code><xsl:value-of select="$modulecode"/></code>
			</module>
			<issues>
				<xsl:apply-templates select="issues/issueList"/>
			</issues>
		</scarab-issues>
	</xsl:template>
	<xsl:template match="issue">
			<issue>
				<id><xsl:value-of select="Issue_ID"/></id>
				<artifact-type><xsl:value-of select="Issue_Type"/></artifact-type>
				<activity-sets>
					<activity-set>
						<id><xsl:value-of select="count(preceding-sibling::issue) + 1"/></id>
						<type>Create Issue</type>
						<created-by><xsl:value-of select="$username"/></created-by>
							<created-date>
							<format>yyyy-MM-dd HH:mm:ss</format>
							<timestamp><xsl:value-of select="$ImportDatetime"/></timestamp>
						</created-date>
						<activities>
							<xsl:for-each select="child::*[name(.)!='Issue_ID' and name(.)!='Issue_Type']">
							<xsl:if test="normalize-space(.)">
							<activity>
								<id><xsl:value-of select="position()"/></id>
								<attribute><xsl:value-of select="name(.)"/></attribute>
								<new-value><xsl:value-of select="."/></new-value>
								<description>Value of '<xsl:value-of select="name(.)"/>' imported to '<xsl:value-of select="."/>'</description>
							</activity>
							</xsl:if>
							</xsl:for-each>
						</activities>
					</activity-set>
				</activity-sets>
			</issue>
	</xsl:template>
</xsl:stylesheet>
