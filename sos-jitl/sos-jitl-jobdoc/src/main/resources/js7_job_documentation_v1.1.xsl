<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xi="http://www.w3.org/2001/XInclude"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/js7_job_documentation_v1.1"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="jobdoc xhtml xi fn">
	<xsl:output method="html" encoding="utf-8" indent="yes" />


	<xsl:template match="/jobdoc:description">
		<html>
			<head>
				<meta charset="utf-8" />
				<meta http-equiv="X-UA-Compatible" content="IE=edge" />
				<meta name="description" content="opensource, JobScheduler, sos-berlin, js7, workload, automation, JOC Cockpit"/>
				<meta name="viewport" content="width=device-width, initial-scale=1" />
				<link rel="icon" type="image/x-icon" href="favicon.ico" />
				<meta name="author" content="SOS GmbH" />
				<meta name="publisher" content="Software- und Organisations- Service GmbH (SOS), Berlin" />
				<meta name="copyright" content="Copyright Software- und Organisations- Service GmbH (SOS), Berlin. All rights reserved." />
				<title>
					<xsl:value-of select="concat(//jobdoc:job/@name, ' - JITL - JS7 Integrated Template Library')" />
				</title>
				<xsl:call-template name="get_css" />
			</head>
			<body>
				<xsl:apply-templates select="/jobdoc:description" mode="main"/>
			</body>
		</html>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template MAIN -->
	<xsl:template match="/jobdoc:description" mode="main">
		<main>
			<header>
					<div><img src="JS7_blue_blue_orange_on_transparent100x100.png" alt="JS7 logo"/></div>
					<h1><span><xsl:value-of select="//jobdoc:job/@name"/></span> - JITL (JS7 Integrated Template Library)</h1>
					<div> </div>
			</header>
			<xsl:if test="jobdoc:documentation">
				<section><xsl:apply-templates select="jobdoc:documentation"/></section>
			</xsl:if>
			<section><xsl:apply-templates select="jobdoc:job"/></section>
			<section><xsl:apply-templates select="jobdoc:configuration"/></section>
		</main>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template Job -->
	<xsl:template match="jobdoc:job">
		<table class="box">
			<tr>
				<td class="td1">
					<span class="section">Class</span>
				</td>
				<td class="td2">
					<span class="label">Name/Title</span>
				</td>
				<td class="td3">
					<span class="sourceNameBold">
						<xsl:value-of select="@name" />
					</span>
					<xsl:text>&#160;-&#160;</xsl:text>
					<span class="desc">
						<xsl:value-of select="@title" />
					</span>
				</td>
			</tr>
			<xsl:if test="@tasks">
				<tr>
					<td class="td1">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td2"><span class="label">Tasks</span></td>
					<td class="td3">
						<span class="desc">
							<xsl:value-of select="@tasks" />
						</span>
					</td>
				</tr>
			</xsl:if>
			<xsl:apply-templates select="jobdoc:script"/>
		</table>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template jobdoc:documentation -->
	<xsl:template match="/jobdoc:description/jobdoc:documentation">
		<table class="box">
			<tr>
				<td class="td1">
					<span class="section">Documentation</span>
				</td>
				<td class="td2">
					<xsl:text>&#160;</xsl:text>
					<xsl:text />
				</td>
				<td class="td3">
					<xsl:apply-templates mode="copy" select="jobdoc:*|xhtml:*|*"/>
				</td>
			</tr>
		</table>
	</xsl:template>

	<xsl:template match="xhtml:br" mode="copy">
		<br />
	</xsl:template>

	<xsl:template match="jobdoc:paramref | xhtml:paramref" mode="copy">
		<i>
			<code>
				<xsl:value-of select="." />
			</code>
		</i>
	</xsl:template>

	<xsl:template match="xhtml:paramval" mode="copy">
		<i>
			<code>
				<xsl:value-of select="." />
			</code>
		</i>
	</xsl:template>

	<xsl:template match="xhtml:shell" mode="copy">
		<code>
			<xsl:value-of select="." />
		</code>
	</xsl:template>

	<xsl:template match="*" mode="copy">
		<xsl:element name="{name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="copy"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="text()" mode="copy">
		<xsl:value-of select="." />
	</xsl:template>

	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template note -->
	<xsl:template match="jobdoc:note">
		<xsl:if test="@since">
			<p>
				<xsl:value-of select="concat(@since, ' is the version with the following bevaviour added ')" />
			</p>
		</xsl:if>
		<xsl:apply-templates mode="copy"/>
	</xsl:template>

	<xsl:template match="jobdoc:explanation">
		<xsl:apply-templates mode="copy" select="*[not(local-name() = 'title')]"/>
	</xsl:template>

	<xsl:template match="jobdoc:note" mode="copy">
		<xsl:if test="@since">
			<p>
				<xsl:value-of select="concat(@since, ' is the version with the following bevaviour added ')" />
			</p>
		</xsl:if>
		<xsl:apply-templates mode="copy"/>
	</xsl:template>

	<xsl:template match="*[local-name() = 'features']" mode="copy">
		<ul>
			<xsl:apply-templates/>
		</ul>
	</xsl:template>

	<xsl:template match="*[local-name() = 'feature']">
		<li>
			<xsl:apply-templates mode="copy"/>
		</li>
	</xsl:template>

	<xsl:template name="call_notes">
		<xsl:apply-templates select="jobdoc:note|jobdoc:examples|jobdoc:codeexample"/>
	</xsl:template>

	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template Script/ Monitor Script -->
	<xsl:template match="jobdoc:script">
		<tr>
			<td class="td1">
				<xsl:text>&#160;</xsl:text>
			</td>
			<td class="td2"><span class="label">Script</span></td>
			<td class="td3">
				<xsl:if test="@language">
					<ul>
						<xsl:if test="@language">
							<li><xsl:text>Language: </xsl:text>
								<span class="sourceName">
									<xsl:value-of select="@language" />
								</span>
							</li>
						</xsl:if>
						<xsl:if test="@java_class">
							<li><xsl:text>Name of Java Class: </xsl:text>
								<span class="sourceName">
									<xsl:value-of select="@java_class" />
								</span>
							</li>
						</xsl:if>
					</ul>
					<xsl:apply-templates select="jobdoc:include" />
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template Include -->
	<xsl:template match="jobdoc:include">
		<xsl:if test="not(@file='')">
			<ul>
				<li><xsl:text>Include: </xsl:text>
					<span class="sourceName">
						<xsl:value-of select="@file" />
					</span>
				</li>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
	Template Environment -->
	<xsl:template match="jobdoc:environment">
		<tr>
			<td class="td1">
				<xsl:text>&#160;</xsl:text>
			</td>
			<td class="td2"><span class="label">Environment Variables</span></td>
			<td class="td3">
				<table class="resource" cellpadding="0" cellspacing="1">
					<tbody>
						<tr>
							<th class="resource1">
								<span class="desc">Name</span>
							</th>
							<th class="resource2">
								<span class="desc">Value</span>
							</th>
							<th class="resource3"></th>
							<th class="resource4"></th>
						</tr>
						<xsl:apply-templates select="jobdoc:variable" />
					</tbody>
				</table>
			</td>
		</tr>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für Environment/ Variable als Tabellenzeile -->
	<xsl:template match="jobdoc:variable">
		<tr>
			<td class="resource1">
				<span class="desc">
					<xsl:value-of select="@name" />
				</span>
			</td>
			<td class="resource2">
				<span class="desc">
					<xsl:value-of select="@value" />
				</span>
			</td>
			<td class="resource3" />
			<td class="resource4" />
		</tr>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für Konfiguration -->
	<xsl:template match="jobdoc:configuration">
		<!-- Nur wenn es Elemente unterhalb von configuration gibt -->
		<xsl:if test="child::*">
			<table class="box">
				<tr>
					<td class="td1">
						<span class="section">Configuration</span>
					</td>
					<td class="td2">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td3">
						<xsl:choose>
							<xsl:when test="jobdoc:note">
								<xsl:call-template name="call_notes"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>&#160;</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
				<xsl:apply-templates select="jobdoc:params">
					<xsl:sort select="@order" data-type="number" />
				</xsl:apply-templates>
				<xsl:apply-templates select="jobdoc:payload"/>
				<xsl:apply-templates select="jobdoc:settings"/>
			</table>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für Configuration/ Parameter -->
	<xsl:template match="jobdoc:params[parent::jobdoc:configuration] | jobdoc:params[parent::jobdoc:params]">
		<tr>
			<td class="td1">
				<xsl:call-template name="set_anchor">
					<xsl:with-param name="anchor_name" select="@id" />
				</xsl:call-template>
				<xsl:text>&#160;</xsl:text>
			</td>
			<td class="td2">
				<xsl:choose>
					<xsl:when test="@id='return_parameter'"><span class="label">Return parameters</span></xsl:when>
					<xsl:otherwise><span class="label">Parameters</span></xsl:otherwise>
				</xsl:choose>
			</td>
			<td class="td3">
				<xsl:choose>
					<xsl:when test="jobdoc:note">
						<xsl:call-template name="call_notes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="@reference and not(@reference='')">
							<xsl:if test="jobdoc:note">
								<br />
							</xsl:if>
							<xsl:call-template name="process_reference"/>
						</xsl:if>
						<xsl:if test="child::*">
							<table class="section" cellpadding="0" cellspacing="1">
								<xsl:apply-templates select="codeexample|jobdoc:codeexample|jobdoc:param|jobdoc:params/jobdoc:*"/>
							</table>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
		<xsl:if test="jobdoc:note">
			<tr>
				<td class="td1">
					<xsl:text>&#160;</xsl:text>
				</td>
				<td class="td1">
					<xsl:text>&#160;</xsl:text>
				</td>
				<td class="td3">
					<xsl:if test="@reference and not(@reference='')">
						<xsl:call-template name="process_reference"/>
					</xsl:if>
					<xsl:if test="child::*">
						<table class="section" cellpadding="0" cellspacing="1">
							<xsl:apply-templates select="jobdoc:param"/>
						</table>
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für Configuration/ Parameter als Referenz! -->
	<xsl:template match="jobdoc:params" mode="reference">
		<xsl:variable name="reftext">
			<xsl:choose>
				<xsl:when test="ancestor::jobdoc:payload"><span class="label">Payload Parameters</span></xsl:when>
				<xsl:otherwise><span class="label">Job Parameters</span></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a class="doc">
			<xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
			<span style="font-family:Arial;font-size:12px;">&#8594;</span>
			<xsl:text>&#160;</xsl:text>
			<xsl:value-of select="$reftext" />
		</a>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für einzelnen Parameter -->
	<xsl:template match="jobdoc:param">
		<xsl:param name="extdoc" />
		<tr>
			<td class="section1" colspan="2">
				<xsl:call-template name="set_anchor">
					<xsl:with-param name="anchor_name" select="@id" />
				</xsl:call-template>
				<xsl:call-template name="set_anchor">
					<xsl:with-param name="anchor_name" select="@name" />
				</xsl:call-template>
				<span class="sourceNameBold">
					<xsl:choose>
						<xsl:when test="@display">
							<xsl:value-of select="@display" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="@name" />
						</xsl:otherwise>
					</xsl:choose>
				</span>
			</td>
		</tr>
		<xsl:if test="not(@reference)">
			<tr>
				<td class="section1">
					<xsl:if test="@required='false'">
						<span class="labelSmall">[optional]</span>
					</xsl:if>
					<xsl:if test="@required='true'">
						<span class="labelSmall">[required]</span>
					</xsl:if>
				</td>
				<td class="section2">
					<span class="desc">
						<xsl:if test="@DataType and not(@DataType='')">
							<xsl:text>DataType: </xsl:text>
							<xsl:value-of select="@DataType"></xsl:value-of>
							<br />
						</xsl:if>

						<xsl:if test="@Alias and not(@Alias='')">
							<xsl:text>Alias: </xsl:text>
							<xsl:value-of select="@Alias"></xsl:value-of>
							<br />
						</xsl:if>

						<xsl:choose>
							<xsl:when test="@reference and not(@reference='')">
								<xsl:call-template name="process_reference"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="defVal">
									<xsl:choose>
										<xsl:when test="@default_value and not(@default_value='')">
											<xsl:value-of select="@default_value" />
										</xsl:when>
										<xsl:when test="@DefaultValue and not(@DefaultValue='')">
											<xsl:value-of select="@DefaultValue" />
										</xsl:when>
										<xsl:otherwise>
											---
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<xsl:text>Default: </xsl:text>
								<xsl:value-of select="$defVal" />
							</xsl:otherwise>
						</xsl:choose>

						<xsl:if test="@since and @since != ''">
							<p>
								<xsl:value-of select="concat('This parameter is introduced with version ', @since)" />
							</p>
						</xsl:if>
						<p> </p>
						<xsl:choose>
							<xsl:when test="$extdoc and $extdoc//jobdoc:note">
								<xsl:apply-templates select="$extdoc//jobdoc:note|$extdoc//note|$extdoc//jobdoc:codeexample"/>
							</xsl:when>
							<xsl:when test="jobdoc:note">
								<xsl:call-template name="call_notes"/>
							</xsl:when>
						</xsl:choose>
					</span>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für einzelnen Parameter als Referenz! -->
	<xsl:template match="jobdoc:param" mode="reference">
		<xsl:variable name="reftext">
			<xsl:choose>
				<xsl:when test="ancestor::jobdoc:payload"><span class="label">Payload Parameters</span></xsl:when>
				<xsl:otherwise><span class="label">Job Parameters</span></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a class="doc">
			<xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
			<!-- xsl:attribute name="href">#<xsl:value-of select="@name" /></xsl:attribute -->
			<span style="font-family:Arial;font-size:12px;">&#8594;</span>
			<xsl:text>&#160;</xsl:text>
			<xsl:value-of select="$reftext" />
			<xsl:text>&#160;</xsl:text>
		</a>
		<span class="sourceNameBold">
			<xsl:value-of select="@name" />
		</span>
	</xsl:template>

	<xsl:template match="jobdoc:p | xhtml:p">
		<xsl:apply-templates />
	</xsl:template>

	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Payload -->
	<xsl:template match="jobdoc:payload">
		<xsl:if test="child::*">
			<xsl:call-template name="set_anchor">
				<xsl:with-param name="anchor_name" select="jobdoc:params/@id" />
			</xsl:call-template>
			<tr>
				<td class="td1">
					<xsl:text>&#160;</xsl:text>
				</td>
				<td class="td2"><span class="label">Payload</span></td>
				<td class="td3">
					<xsl:choose>
						<xsl:when test="jobdoc:note">
							<xsl:call-template name="call_notes"/>
						</xsl:when>
						<xsl:otherwise>
							<table class="section" cellpadding="0" cellspacing="1">
								<xsl:apply-templates select="jobdoc:params"/>
							</table>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
			<xsl:if test="jobdoc:note">
				<tr>
					<td class="td1">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td1">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td3">
						<table class="section" cellpadding="0" cellspacing="1">
							<xsl:apply-templates select="jobdoc:params"/>
						</table>
					</td>
				</tr>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template für Payload/ Parameter -->
	<xsl:template match="jobdoc:params[parent::jobdoc:payload]">
		<xsl:if test="@reference and not(@reference='')">
			<xsl:call-template name="process_reference"/>
		</xsl:if>
		<xsl:if test="child::*">
			<xsl:apply-templates select="jobdoc:param"/>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Settings -->
	<xsl:template match="jobdoc:settings">
		<xsl:if test="child::*">
			<xsl:if test="jobdoc:note">
				<tr>
					<td class="td1">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td2">
						<span class="label">Settings</span>
					</td>
					<td class="td3">
						<xsl:call-template name="call_notes"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:apply-templates select="jobdoc:profile"/>
			<xsl:apply-templates select="jobdoc:connection"/>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Settings/ Profile -->
	<xsl:template match="jobdoc:profile">
		<tr>
			<td class="td1">
				<xsl:text>&#160;</xsl:text>
			</td>
			<td class="td2"><span class="label">Profile</span></td>
			<td class="td3">
				<xsl:if test="jobdoc:note">
					<xsl:call-template name="call_notes"/>
					<br />
				</xsl:if>
				<xsl:text>Name of Configuration File: </xsl:text>
				<xsl:if test="@name">
					<span class="sourceName">
						<xsl:value-of select="@name" />
					</span>
				</xsl:if>
				<xsl:apply-templates select="jobdoc:section"/>
			</td>
		</tr>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Settings/ Connection -->
	<xsl:template match="jobdoc:connection">
		<tr>
			<td class="td1">
				<xsl:text>&#160;</xsl:text>
			</td>
			<td class="td2"><span class="label">Database</span></td>
			<td class="td3">
				<xsl:if test="jobdoc:note">
					<xsl:call-template name="call_notes"/>
					<p></p>
				</xsl:if>
				<xsl:text>Database Name: </xsl:text>
				<xsl:if test="@name">
					<span class="sourceName">
						<xsl:value-of select="@name" />
					</span>
				</xsl:if>
				<table cellpadding="0" cellspacing="0">
					<xsl:apply-templates select="jobdoc:application"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Application -->
	<xsl:template match="jobdoc:application">
		<xsl:if test="@reference and not(@reference='')">
			<xsl:call-template name="process_reference"/>
		</xsl:if>
		<xsl:if test="child::*">
			<tr>
				<td class="td1_3">
					<xsl:call-template name="set_anchor">
						<xsl:with-param name="anchor_name" select="@id" />
					</xsl:call-template>
					<span >Application: </span>
					<span class="sourceName">
						<xsl:value-of select="@name" />
					</span>
					<xsl:text>&#160;</xsl:text>
					<xsl:apply-templates select="jobdoc:section"/>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Application als Referenz! -->
	<xsl:template match="jobdoc:application" mode="reference">
		<a class="doc">
			<xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
			<span style="font-family:Arial;font-size:12px;">&#8594;</span>
			<xsl:text>&#160;</xsl:text><!--TODO -->
			Database Settings: Application
		</a>
		<xsl:text>&#160;</xsl:text>
		<span class="sourceName">
			<xsl:value-of select="@name" />
		</span>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Section -->
	<xsl:template match="jobdoc:section">
		<xsl:if test="@reference and not(@reference='')">
			<br />
			<xsl:call-template name="process_reference"/>
		</xsl:if>
		<xsl:if test="child::*">
			<table class="section" cellpadding="0" cellspacing="1">
				<tr>
					<td class="td1_3">
						<xsl:call-template name="set_anchor">
							<xsl:with-param name="anchor_name" select="@id" />
						</xsl:call-template>
						<span >Section: </span>
						<span class="sourceName">
							<xsl:value-of select="@name" />
						</span>
					</td>
				</tr>
			</table>
			<table class="section" cellpadding="0" cellspacing="1">
				<xsl:apply-templates select="jobdoc:setting"/>
			</table>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Section als Referenz! -->
	<xsl:template match="jobdoc:section" mode="reference">
		<a class="doc">
			<xsl:attribute name="href">#<xsl:value-of select="@name" /></xsl:attribute>
			<span style="font-family:Arial;font-size:12px;">&#8594;</span>
			<xsl:text>&#160;</xsl:text>
			Profile Settings: Section
			<xsl:text>&#160;</xsl:text>
		</a>
		<span class="sourceName">
			<xsl:value-of select="@name" />
		</span>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Setting -->
	<xsl:template match="jobdoc:setting">
		<tr>
			<td class="section1" colspan="2">
				<xsl:call-template name="set_anchor">
					<xsl:with-param name="anchor_name" select="@id" />
				</xsl:call-template>
				<span class="sourceNameBold">
					<xsl:value-of select="@name" />
				</span>
			</td>
		</tr>
		<xsl:if test="not(@reference)">
			<tr>
				<td class="section1">
					<xsl:if test="@required='false'">
						<span class="labelSmall">[optional]</span>
					</xsl:if>
					<xsl:if test="@required='true'">
						<xsl:text>&#160;</xsl:text>
					</xsl:if>
				</td>
				<td class="section2">
					<span class="desc">
						<xsl:if test="jobdoc:note">
							<xsl:call-template name="call_notes"/>
						</xsl:if>
					</span>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Setting als Referenz! -->
	<xsl:template match="jobdoc:setting" mode="reference">
		<xsl:variable name="reftext">
			<xsl:choose>
				<xsl:when test="ancestor::jobdoc:profile">
					Profile-Setting
				</xsl:when>
				<xsl:otherwise>
					Connection Setting
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a class="doc">
			<xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
			<span style="font-family:Arial;font-size:12px;">&#8594;</span>
			<xsl:text>&#160;</xsl:text>
			<xsl:value-of select="$reftext" />
			<xsl:text>&#160;</xsl:text>
		</a>
		<span class="sourceName">
			<xsl:value-of select="@name" />
		</span>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Process Reference -->
	<xsl:template name="process_reference">
		<xsl:variable name="reference" select="@reference" />
		<xsl:apply-templates
			select="//*[@id=$reference or @name=$reference]" mode="reference"/>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template set_anchor -->
	<xsl:template name="set_anchor">
		<xsl:param name="anchor_name" />
		<a><xsl:attribute name="name"><xsl:value-of select="$anchor_name" /></xsl:attribute></a>
	</xsl:template>
	
	<xsl:template match="jobdoc:note | jobdoc:documentation" mode="reference">
		<xsl:apply-templates select="." />
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template get_css -->
	<xsl:template name="get_css">
		<style type="text/css"><![CDATA[

      body { background-color:#d2d8e8; margin:6px 20px; padding: 0; font-family:verdana,arial,sans-serif; font-size:10pt; color: #203e8d; }

      header { display: grid;
               grid-template-columns: 100px auto 100px;
               gap: 10px;
               place-items: center;
               justify-content: space-between;
             }

      ul    { margin-top: 0px; margin-bottom: 10px; margin-left: 0; padding-left: 1em;
              font-weight:300; }
      li    { margin-bottom: 10px; }

      h1    { font-weight:600; color:#203e8d; }
      h1 span { font-weight:600; color:#de5906; }

      table { width:100%;
              background-color:#a5b1d1;
            }

      td    { padding: 2px;
              vertical-align:top; text-align: left;
            }
      section:first-of-type table.box { border-top-width:2px; }
      table.box        { border-width:0 2px 2px; border-style:solid; border-color:#203e8d; }

      td.td1           {width:11%; }
      td.td2           {width:12%; }
      td.td3           {width:77%; }
      td.td1_3         {width:100%; }

      table.resource   { background-color:#d2d8e8; }
      table.resource th  { background-color:#bcc5dc;
                           text-align: left;
                           font-weight:300;
                         }
      table.resource td  { background-color:#bcc5dc;
                           color:#203e8d;
                           text-align: left;
                           font-weight:300;
                         }

      th.resource1     { width:20%; }
      th.resource2     { width:10%; }
      th.resource3     { width:10%; }
      th.resource2_3   { width:20%; }
      th.resource4     { width:60%; }
      th.resource5     { width:10%; }

      td.resource1     { color:#8c892c; width:25%; }
      td.resource2     { width:10%; }
      td.resource3     { width:10%; }
      td.resource2_3   { width:20%; }
      td.resource4     { width:60%; }

      table.section   { background-color:#d2d8e8; width:100%; }
      table.section td   { background-color:#bcc5dc; color:#8c892c; }

      td.section1     { width:20%; }
      td.section2     { width:80%; }
                      
      table.description   { background-color:#d2d8e8; margin-top:4px; margin-bottom:4px; }
      table.description th, table.description td { background-color:#bcc5dc; }

      .section        {font-weight:600; color:#203e8d; }          /* blaue Schrift, fett */
      .label          {font-weight:300; color:#203e8d; }          /* blaue Schrift */
      .labelSmall     {font-weight:300; font-size:8pt; }          /* schwarze Schrift */
      .sourceName     {color:#8c892c; font-weight:300; }                         /* grüne Schrift */
      .sourceNameBold {color:#8c892c; font-weight:600; }                         /* grüne Schrift */
      .desc           {color:#203e8d; font-weight:300; }                         /* blaue Schrift */

      .code           {color:#000000; font-weight:300; font-family:"Courier New",sans-serif;font-size:10pt; }      /* Schrift für XML-Code */
      pre.example     {background-color:#bcc5dc; padding-left:10px;}

      /*** LINK Formatierungen ***/
      a                   { font-weight:600; text-decoration:none; font-size:10pt; color:#de5906; font-weight:300;}
      /* Mail-Verweis */
      a.mail              { color:#de5906; font-weight:300;}

    ]]></style>
	</xsl:template>

	<xsl:template match="code">
		<code>
			<xsl:apply-templates select="." />
		</code>
	</xsl:template>

	<xsl:template match="jobdoc:examples">
		<xsl:text>---- examples ----</xsl:text>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="jobdoc:examples" mode="copy">
		<xsl:apply-templates select="." />
	</xsl:template>

	<xsl:template match="jobdoc:title">
		<xsl:if test="parent::jobdoc:note">
			<i>
				<b>
					<xsl:value-of select="." />
				</b>
			</i>
		</xsl:if>
		<xsl:if test="not (parent::jobdoc:note)">
			<xsl:value-of select="." />
		</xsl:if>
		<br />
	</xsl:template>

	<xsl:template match="jobdoc:title" mode="copy">
		<xsl:if test="parent::jobdoc:note">
			<i>
				<b>
					<xsl:value-of select="." />
				</b>
			</i>
		</xsl:if>
		<xsl:if test="not (parent::jobdoc:note)">
			<xsl:value-of select="." />
		</xsl:if>
		<br />
	</xsl:template>
	
	<xsl:template match="jobdoc:codeexample | jobdoc:codeExample">
		<xsl:apply-templates select="jobdoc:title|title|jobdoc:explanation/jobdoc:title"/>
		<xsl:apply-templates select="jobdoc:embeddedexample|jobdoc:embeddedExample|embeddedExample|embeddedexample|jobdoc:explanation"/>
	</xsl:template>

	<xsl:template match="jobdoc:codeexample | jobdoc:codeExample" mode="copy">
		<xsl:apply-templates select="jobdoc:title|title|jobdoc:explanation/jobdoc:title"/>
		<xsl:apply-templates select="jobdoc:embeddedexample|jobdoc:embeddedExample|embeddedExample|jobdoc:explanation"/>
	</xsl:template>

	<xsl:template match="jobdoc:embeddedexample|jobdoc:embeddedExample">
		<pre class="example">
			<xsl:value-of select="." />
		</pre>
	</xsl:template>

	<xsl:template match="jobdoc:paramref">
		<code>
			<i>
				<xsl:apply-templates select="." />
			</i>
		</code>
	</xsl:template>

	<xsl:template match="jobdoc:items | xhtml:items | items" mode="copy">
		<xsl:apply-templates select="./*"/>
	</xsl:template>

	<xsl:template match="jobdoc:items | xhtml:items | items">
		<xsl:apply-templates select="./*"/>
	</xsl:template>


</xsl:stylesheet>
