<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    exclude-result-prefixes="xhtml"
    version="2.0">

    <xsl:output method="xml" indent="yes"/>
    
    <xsl:template match="Configurations">
        <xsl:element name="Configurations">
            <xsl:attribute name="xsi:noNamespaceSchemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">YADE_configuration_v2.0.0.xsd</xsl:attribute>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Identity template: copy all elements and attributes unless overridden -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Template for FTPFragment -->
    <xsl:template match="FTPFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- to v1 -->
            <xsl:if test="BasicConnection/ConnectTimeout">
                <ConnectTimeout><xsl:value-of select="BasicConnection/ConnectTimeout"/></ConnectTimeout>
            </xsl:if>
            <xsl:if test="ProxyForFTP">
                <ProxyForFTP><xsl:apply-templates select="ProxyForFTP"/></ProxyForFTP>
            </xsl:if>
            
            <!-- v2 -->
            <xsl:apply-templates select="*[not(self::ProxyForFTP)]"/>
        </xsl:copy>
    </xsl:template>

    <!-- Template for FTPSFragment -->
    <xsl:template match="FTPSFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- v1 -->
            <xsl:if test="BasicConnection/ConnectTimeout">
                <ConnectTimeout><xsl:value-of select="BasicConnection/ConnectTimeout"/></ConnectTimeout>
            </xsl:if>
            <xsl:if test="ProxyForFTPS">
                <ProxyForFTPS><xsl:apply-templates select="ProxyForFTPS"/></ProxyForFTPS>
            </xsl:if>
            <FTPSClientSecurity>
                <SecurityMode><xsl:value-of select="SecurityMode"/></SecurityMode>
                <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreFile">
                    <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreType">
                        <KeyStoreType><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreType"/></KeyStoreType>
                    </xsl:if>
                    <KeyStoreFile><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreFile"/></KeyStoreFile>
                    <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStorePassword">
                        <KeyStorePassword><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStorePassword"/></KeyStorePassword>
                    </xsl:if>
                </xsl:if>
            </FTPSClientSecurity>

            <!-- v2 -->
            <xsl:apply-templates select="*[not(self::ProxyForFTPS)]"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Template for HTTPFragment -->
    <xsl:template match="HTTPFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- v2 -->
            <xsl:apply-templates select="*"/>
        </xsl:copy>  
    </xsl:template>
    
    <!-- Template for HTTPSFragment -->
    <xsl:template match="HTTPSFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- v1 -->
            <xsl:choose>
                <xsl:when test="SSL/UntrustedSSL">
                    <AcceptUntrustedCertificate>true</AcceptUntrustedCertificate>
                    <xsl:copy-of select="SSL/UntrustedSSL/DisableCertificateHostnameVerification"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreFile">
                        <KeyStore>
                            <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreType">
                                <KeyStoreType><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreType"/></KeyStoreType>
                            </xsl:if>
                            <KeyStoreFile><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreFile"/></KeyStoreFile>
                            <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStorePassword">
                                <KeyStorePassword><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStorePassword"/></KeyStorePassword>
                            </xsl:if>
                        </KeyStore>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            
            <!-- v2 -->
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Template for JumpFragment -->
    <xsl:template match="JumpFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- to v1 -->
            <xsl:if test="BasicConnection/ConnectTimeout">
                <ConnectTimeout><xsl:value-of select="BasicConnection/ConnectTimeout"/></ConnectTimeout>
            </xsl:if>
            <xsl:if test="ProxyForSFTP">
                <ProxyForSFTP><xsl:apply-templates select="ProxyForSFTP"/></ProxyForSFTP>
            </xsl:if>
            <xsl:if test="SocketTimeout">
                <ChannelConnectTimeout><xsl:value-of select="SocketTimeout"/></ChannelConnectTimeout>
            </xsl:if>
            <xsl:if test="KeepAlive/KeepAliveInterval">
                <ServerAliveInterval><xsl:value-of select="KeepAlive/KeepAliveInterval"/></ServerAliveInterval>
                <xsl:if test="KeepAlive/MaxAliveCount">
                    <ServerAliveCountMax><xsl:value-of select="KeepAlive/MaxAliveCount"/></ServerAliveCountMax>
                </xsl:if>
            </xsl:if>
            <JumpCommand><xsl:value-of select="YADEClientCommand"/></JumpCommand>
            <xsl:if test="TempDirectoryParent">
                <JumpDirectory><xsl:value-of select="TempDirectoryParent"/></JumpDirectory>
            </xsl:if>
            <xsl:if test="SFTPProcessing">
                <xsl:if test="SFTPProcessing/SFTPPreProcessing/CommandBeforeFile">
                    <JumpCommandBeforeFile>
                        <xsl:if test="SFTPProcessing/SFTPPreProcessing/CommandBeforeFile/@enable_for_skipped_transfer">
                            <xsl:attribute name="enable_for_skipped_transfer"><xsl:value-of select="SFTPProcessing/SFTPPreProcessing/CommandBeforeFile/@enable_for_skipped_transfer"/></xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="SFTPProcessing/SFTPPreProcessing/CommandBeforeFile"/>
                    </JumpCommandBeforeFile>
                </xsl:if>
                <xsl:if test="SFTPProcessing/SFTPPreProcessing/CommandBeforeOperation">
                    <JumpCommandBeforeOperation><xsl:value-of select="SFTPProcessing/SFTPPreProcessing/CommandBeforeOperation"/></JumpCommandBeforeOperation>
                </xsl:if>
                
                <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandAfterFile">
                    <JumpCommandAfterFile>
                        <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandAfterFile/@disable_for_skipped_transfer">
                            <xsl:attribute name="disable_for_skipped_transfer"><xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandAfterFile/@disable_for_skipped_transfer"/></xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandAfterFile"/>
                    </JumpCommandAfterFile>
                </xsl:if>
                <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationOnSuccess">
                    <JumpCommandAfterOperationOnSuccess><xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationOnSuccess"/></JumpCommandAfterOperationOnSuccess>
                </xsl:if>
                <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationOnError">
                    <JumpCommandAfterOperationOnError><xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationOnError"/></JumpCommandAfterOperationOnError>
                </xsl:if>
                <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationFinal">
                    <JumpCommandAfterOperationFinal><xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandAfterOperationFinal"/></JumpCommandAfterOperationFinal>
                </xsl:if>
                <xsl:if test="SFTPProcessing/SFTPPostProcessing/CommandBeforeRename">
                    <JumpCommandBeforeRename><xsl:value-of select="SFTPProcessing/SFTPPostProcessing/CommandBeforeRename"/></JumpCommandBeforeRename>
                </xsl:if> 
            
                <xsl:if test="SFTPProcessing/ProcessingCommandDelimiter">
                    <ProcessingCommandDelimiter><xsl:value-of select="SFTPProcessing/ProcessingCommandDelimiter"/></ProcessingCommandDelimiter>
                </xsl:if>
                <xsl:if test="SFTPProcessing/Platform">
                    <Platform><xsl:value-of select="SFTPProcessing/Platform"/></Platform>
                </xsl:if>
            </xsl:if>

            <!-- v2 -->
            <xsl:apply-templates select="*[not(self::ProxyForSFTP)]"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Template for SFTPFragment -->
    <xsl:template match="SFTPFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- to v1 -->
            <xsl:if test="BasicConnection/ConnectTimeout">
                <ConnectTimeout><xsl:value-of select="BasicConnection/ConnectTimeout"/></ConnectTimeout>
            </xsl:if>
            <xsl:if test="ProxyForSFTP">
                <ProxyForSFTP><xsl:apply-templates select="ProxyForSFTP"/></ProxyForSFTP>
            </xsl:if>
            <xsl:if test="SocketTimeout">
                <ChannelConnectTimeout><xsl:value-of select="SocketTimeout"/></ChannelConnectTimeout>
            </xsl:if>
            <xsl:if test="KeepAlive/KeepAliveInterval">
                <ServerAliveInterval><xsl:value-of select="KeepAlive/KeepAliveInterval"/></ServerAliveInterval>
                <xsl:if test="KeepAlive/MaxAliveCount">
                    <ServerAliveCountMax><xsl:value-of select="KeepAlive/MaxAliveCount"/></ServerAliveCountMax>
                </xsl:if>
            </xsl:if>

            <!-- v2 -->
            <xsl:apply-templates select="*[not(self::ProxyForSFTP)]"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Template for SMBFragment -->
    <xsl:template match="SMBFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- to v1 -->
            <xsl:if test="SMBAuthentication/SMBAuthenticationMethodNTLM and SMBConnection/Hostname">
                <Hostname><xsl:value-of select="SMBConnection/Hostname"/></Hostname>
            </xsl:if>
            
            <!-- v2 -->
            <xsl:copy-of select="CredentialStoreFragmentRef"/>
            <xsl:copy-of select="SMBConnection"/>
            
            <SMBAuthentication>
                <!-- to v1 -->
                <xsl:choose>
                    <xsl:when test="SMBAuthentication/SMBAuthenticationMethodNTLM">
                        <Account><xsl:value-of select="SMBAuthentication/SMBAuthenticationMethodNTLM/Account"/></Account>
                        <Password><xsl:value-of select="SMBAuthentication/SMBAuthenticationMethodNTLM/Password"/></Password>
                        <xsl:if test="SMBAuthentication/SMBAuthenticationMethodNTLM/Domain">
                            <Domain><xsl:value-of select="SMBAuthentication/SMBAuthenticationMethodNTLM/Domain"/></Domain>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="SMBAuthentication/SMBAuthenticationMethodGuest">
                            <xsl:choose>
                                <xsl:when test="SMBAuthentication/SMBAuthenticationMethodGuest/Account">
                                    <Account><xsl:value-of select="SMBAuthentication/SMBAuthenticationMethodGuest/Account"/></Account>
                                </xsl:when>
                                <xsl:otherwise>
                                    <Account>Guest</Account>                                
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            
                <!-- v2 -->
                <xsl:copy-of select="SMBAuthentication/SMBAuthenticationMethodAnonymous"/>
                <xsl:copy-of select="SMBAuthentication/SMBAuthenticationMethodGuest"/>
                <xsl:copy-of select="SMBAuthentication/SMBAuthenticationMethodNTLM"/>
                <xsl:copy-of select="SMBAuthentication/SMBAuthenticationMethodKerberos"/>
                <xsl:copy-of select="SMBAuthentication/SMBAuthenticationMethodSPNEGO"/>
            </SMBAuthentication>
            
            <!-- v2 -->
            <xsl:copy-of select="ConfigurationFiles"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Template for WebDAVFragment -->
    <xsl:template match="WebDAVFragment">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <!-- v1 -->
            <xsl:choose>
                <xsl:when test="SSL/UntrustedSSL">
                    <AcceptUntrustedCertificate>true</AcceptUntrustedCertificate>
                    <xsl:copy-of select="SSL/UntrustedSSL/DisableCertificateHostnameVerification"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreFile">
                        <KeyStore>
                            <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStoreType">
                                <KeyStoreType><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreType"/></KeyStoreType>
                            </xsl:if>
                            <KeyStoreFile><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStoreFile"/></KeyStoreFile>
                            <xsl:if test="SSL/TrustedSSL/TrustStore/TrustStorePassword">
                                <KeyStorePassword><xsl:value-of select="SSL/TrustedSSL/TrustStore/TrustStorePassword"/></KeyStorePassword>
                            </xsl:if>
                        </KeyStore>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            
            <!-- v2 -->
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>
    
    
    
    <!-- ProxyForHTTP and ProxyForWebDAV are not included because they do not need to be transformed -->
    <xsl:template match="ProxyForFTP | ProxyForFTPS | ProxyForSFTP">
        <xsl:if test="HTTPProxy"><!-- v1 and v2 -->
            <xsl:copy-of select="HTTPProxy"/>
        </xsl:if>
        <xsl:if test="SOCKSProxy"> 
            <!-- v2 to v1 -->
            <SOCKS5Proxy><xsl:copy-of select="SOCKSProxy/*"/></SOCKS5Proxy>
            <!-- SOCKSProxy of v2 - not needs because SOCKS5Proxy can be parsed and is the same as SOCKSProxy   -->
        </xsl:if>
    </xsl:template>
        
</xsl:stylesheet>
