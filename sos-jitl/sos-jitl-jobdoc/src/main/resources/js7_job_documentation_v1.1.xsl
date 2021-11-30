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
	<xsl:variable name="version" select="'${project.version}'"/>

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
					<div class="section">Class</div>
				</td>
				<td class="td2">
					<div class="label">Name/Title</div>
				</td>
				<td class="td3">
					<div class="label">
					<span class="sourceNameBold">
						<xsl:value-of select="@name" />
					</span>
					<xsl:text>&#160;-&#160;</xsl:text>
					<span class="desc">
						<xsl:value-of select="@title" />
					</span>
					</div>
				</td>
			</tr>
			<xsl:if test="@tasks">
				<tr>
					<td class="td1">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td2"><div class="label">Tasks</div></td>
					<td class="td3">
						<div class="label">
						<span class="desc">
							<xsl:value-of select="@tasks" />
						</span>
						</div>
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
					<div class="section">Documentation</div>
				</td>
				<td class="td2">
					<xsl:text>&#160;</xsl:text>
					<xsl:text />
				</td>
				<td class="td3">
					<div class="label"><xsl:apply-templates mode="copy" select="jobdoc:*|xhtml:*|*"/></div>
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
		<div class="note"><xsl:apply-templates mode="copy"/></div>
	</xsl:template>

	<xsl:template match="jobdoc:explanation">
		<xsl:apply-templates mode="copy" select="*[not(local-name() = 'title')]"/>
	</xsl:template>

	<xsl:template match="jobdoc:note" mode="copy">
		<div class="note"><xsl:apply-templates mode="copy"/></div>
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
			<td class="td2"><div class="label">Script</div></td>
			<td class="td3">
				<div class="label">
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
				</div>
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
			<td class="td2"><div class="label">Environment Variables</div></td>
			<td class="td3">
				<div class="label">
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
				</div>
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
						<div class="section">Configuration</div>
					</td>
					<td class="td2">
						<xsl:text>&#160;</xsl:text>
					</td>
					<td class="td3">
						<div class="label">
						<xsl:choose>
							<xsl:when test="jobdoc:note">
								<xsl:call-template name="call_notes"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>&#160;</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
						</div>
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
		Template for Configuration/ Parameter -->
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
					<xsl:when test="@id='return_parameter'"><div class="label">Return parameters</div></xsl:when>
					<xsl:otherwise><div class="label">Parameters</div></xsl:otherwise>
				</xsl:choose>
			</td>
			<td class="td3">
				<xsl:choose>
					<xsl:when test="jobdoc:note">
						<xsl:call-template name="call_notes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="child::*">
							<table class="section" cellpadding="0" cellspacing="1">
								<xsl:apply-templates select="codeexample|jobdoc:codeexample|jobdoc:param|jobdoc:paramgroup|jobdoc:params/jobdoc:*"/>
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
					<xsl:if test="child::*">
						<table class="section" cellpadding="0" cellspacing="1">
							<xsl:apply-templates select="jobdoc:param|jobdoc:paramgroup"/>
						</table>
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template for Parameter group -->
	<xsl:template match="jobdoc:paramgroup">
		<tr>
			<td class="section1" colspan="2">
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
	</xsl:template>

	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template for single Parameter -->
	<xsl:template match="jobdoc:param">
		<xsl:param name="extdoc" />
		<xsl:if test="@group">
			<tr>
				<td class="sectiongroup" colspan="2">
					<span class="sourceNameBold">
						<xsl:value-of select="@group" />
					</span>
				</td>
			</tr>
		</xsl:if>
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
		<tr>
			<td class="section1">
				<xsl:choose>
					<xsl:when test="@required='false'">
						<span class="labelSmall">[optional]</span>
					</xsl:when>
					<xsl:when test="@required='true'">
						<span class="labelSmall">[required]</span>
					</xsl:when>
					<xsl:otherwise>
						<span class="labelSmall">[required]</span><br/><span class="labelSmall">(<xsl:value-of select="@required" />)</span>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td class="section2">
				<span class="desc">
					<xsl:if test="@DataType and not(@DataType='')">
						<xsl:text>DataType: </xsl:text>
						<xsl:value-of select="@DataType"></xsl:value-of>
						<br />
					</xsl:if>
					<xsl:if test="@data_type and not(@data_type='')">
						<xsl:text>DataType: </xsl:text>
						<xsl:value-of select="@data_type"></xsl:value-of>
						<br />
					</xsl:if>

					<xsl:if test="@Alias and not(@Alias='')">
						<xsl:text>Alias: </xsl:text>
						<xsl:value-of select="@Alias"></xsl:value-of>
						<br />
					</xsl:if>

					<xsl:variable name="defVal">
						<xsl:choose>
							<xsl:when test="@default_display_value and not(@default_display_value='')">
								<xsl:value-of select="@default_display_value" />
							</xsl:when>
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
				<td class="td2"><div class="label">Payload</div></td>
				<td class="td3">
					<div class="label">
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
					</div>
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
		Template for Payload/ Parameter -->
	<xsl:template match="jobdoc:params[parent::jobdoc:payload]">
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
						<div class="label">Settings</div>
					</td>
					<td class="td3">
						<div class="label"><xsl:call-template name="call_notes"/></div>
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
			<td class="td2"><div class="label">Profile</div></td>
			<td class="td3">
				<div class="label">
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
				</div>
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
			<td class="td2"><div class="label">Database</div></td>
			<td class="td3">
				<div class="label">
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
				</div>
			</td>
		</tr>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Application -->
	<xsl:template match="jobdoc:application">
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
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template Section -->
	<xsl:template match="jobdoc:section">
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
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template set_anchor -->
	<xsl:template name="set_anchor">
		<xsl:param name="anchor_name" />
		<a><xsl:attribute name="name"><xsl:value-of select="$anchor_name" /></xsl:attribute></a>
	</xsl:template>
	<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		Template get_css -->
	<xsl:template name="get_css">
		<style type="text/css"><![CDATA[

      * { padding: 0; margin: 0; box-sizing: border-box; }
      html, body { background-color:#eaedf4; margin:4px 10px; font-family:verdana,arial,sans-serif; font-size:10pt; color: #203e8d; }

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
              background-color:#c2cadf;
              font-size:10pt; /* chrome needs this otherwise font-size:medium; in tables */
            }

      td    { padding: 2px;
              vertical-align:top; text-align: left;
            }
      section:first-of-type table.box { border-top-width:2px; margin-top:8px; }
      table.box        { border-width:0 2px 2px; border-style:solid; border-color:#203e8d; }

      td.td1           { width:11%; }
      td.td2           { width:12%; }
      td.td3           { width:77%; }
      td.td1_3         { width:100%; }
      table.explanation    { border:1px solid #203e8d; } 
	  table.explanation td { background-color:#d0d6e6; } 

      table.resource   { background-color:#eaedf4; }
      table.resource th  { background-color:#d0d6e6;
                           text-align: left;
                           font-weight:300;
                         }
      table.resource td  { background-color:#d0d6e6;
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

      table.section   { background-color:#eaedf4; width:100%; }
      table.section td   { background-color:#d0d6e6; color:#8c892c; }

      td.sectiongroup { width:100%; text-align:center; line-height: 40px; }
      td.section1     { width:20%; }
      td.section2     { width:80%; }
                      
      table.description   { background-color:#eaedf4; margin-top:4px; margin-bottom:4px; }
      table.description th, table.description td { background-color:#d0d6e6; }

      div.section     { font-weight:600; color:#203e8d; margin: 4px 0 }          /* blaue Schrift, fett */
      div.label       { font-weight:300; color:#203e8d;  margin: 4px 0 }         /* blaue Schrift */
      .labelSmall     { font-weight:300; font-size:8pt; }          /* schwarze Schrift */
      .sourceName     { color:#8c892c; font-weight:300; }                         /* grüne Schrift */
      .sourceNameBold { color:#8c892c; font-weight:600; }                         /* grüne Schrift */
      .desc           { color:#203e8d; font-weight:300; }                         /* blaue Schrift */

      .note           { margin: 4px 0 }
      .title          { font-weight:600; font-style: italic; }
      .code           { color:#000000; font-weight:300; font-family:"Courier New",sans-serif;font-size:10pt; }
      pre.example     { background-color:#d0d6e6; font-family:"Courier New",sans-serif;font-size:10pt; padding:10px; border:1px solid #eaedf4; }

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

	<xsl:template match="jobdoc:explanation/jobdoc:title | jobdoc:title">
		<p class="title"><xsl:value-of select="." /></p>
	</xsl:template>

	<xsl:template match="jobdoc:title" mode="copy">
		<p class="title"><xsl:value-of select="." /></p>
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
