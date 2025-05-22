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
        <FTPFragment>
            <xsl:copy-of select="@name"/>
        
            <xsl:copy-of select="CredentialStoreFragmentRef"/>
            <xsl:copy-of select="JumpFragmentRef"/>
            
            <xsl:apply-templates select="BasicConnection"/>
            <xsl:copy-of select="BasicAuthentication"/>
            <xsl:if test="ProxyForFTP">
                <ProxyForFTP><xsl:apply-templates select="ProxyForFTP"/></ProxyForFTP>
            </xsl:if>            
            
            <xsl:copy-of select="KeepAlive"/>
            <xsl:copy-of select="PassiveMode"/>
            <xsl:copy-of select="TransferMode"/>
             
        </FTPFragment>        
    </xsl:template>

    <!-- Template for FTPSFragment: check if SSL exists to determine if it's v2 -->
    <xsl:template match="FTPSFragment">
        <xsl:choose>
            <!-- If SSL element exists, it's v2, so copy the entire FTPSFragment as is -->
            <xsl:when test="SSL">
                <xsl:copy-of select="."/>
            </xsl:when>

            <!-- If SSL element does not exist, it must be v1, so transform it to v2 -->
            <xsl:otherwise>
                <FTPSFragment>
                    <xsl:copy-of select="@name"/>
          
                    <xsl:copy-of select="CredentialStoreFragmentRef"/>
                    <xsl:copy-of select="JumpFragmentRef"/>                    
          
                    <xsl:apply-templates select="BasicConnection"/>
                    <xsl:copy-of select="BasicAuthentication"/>
                    <xsl:if test="ProxyForFTPS">
                        <ProxyForFTPS><xsl:apply-templates select="ProxyForFTPS"/></ProxyForFTPS>
                    </xsl:if>

                    <!-- Handle FTPSClientSecurity (e.g., SecurityMode) -->
                    <xsl:if test="FTPSClientSecurity/SecurityMode">
                        <SecurityMode>
                            <xsl:value-of select="FTPSClientSecurity/SecurityMode"/>
                        </SecurityMode>
                    </xsl:if>

                    <!-- Transfer SSL information if it exists in v1 -->
                    <SSL>
                        <xsl:if test="FTPSClientSecurity/KeyStoreFile">
                            <TrustedSSL>
                                <TrustStore>
                                    <xsl:if test="FTPSClientSecurity/KeyStoreType">
                                        <TrustStoreType><xsl:value-of select="FTPSClientSecurity/KeyStoreType"/></TrustStoreType>
                                    </xsl:if>
                                    <TrustStoreFile><xsl:value-of select="FTPSClientSecurity/KeyStoreFile"/></TrustStoreFile>
                                    <xsl:if test="FTPSClientSecurity/KeyStorePassword">
                                        <TrustStorePassword><xsl:value-of select="FTPSClientSecurity/KeyStorePassword"/></TrustStorePassword>
                                    </xsl:if>
                                </TrustStore>
                            </TrustedSSL>
                        </xsl:if>
                    </SSL>                
                </FTPSFragment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Template for HTTPFragment -->
    <xsl:template match="HTTPFragment">
        <HTTPFragment>
            <xsl:copy-of select="@name"/>
        
            <xsl:copy-of select="CredentialStoreFragmentRef"/>
            <xsl:copy-of select="JumpFragmentRef"/>

            <xsl:apply-templates select="URLConnection"/>
            <xsl:copy-of select="BasicAuthentication"/>
            <xsl:if test="ProxyForHTTP">
                <ProxyForHTTP><xsl:apply-templates select="ProxyForHTTP"/></ProxyForHTTP>
            </xsl:if> 
            
            <xsl:copy-of select="HTTPHeaders"/>
        </HTTPFragment>        
    </xsl:template>
    
    <!-- Template for HTTPSFragment: check if SSL exists to determine if it's v2 -->
    <xsl:template match="HTTPSFragment">
        <xsl:choose>
            <!-- If SSL element exists, it's v2, so copy the entire HTTPSFragment as is -->
            <xsl:when test="SSL">
                <xsl:copy-of select="."/>
            </xsl:when>

            <!-- If SSL element does not exist, it must be v1, so transform it to v2 -->
            <xsl:otherwise>
                <HTTPSFragment>
                    <xsl:copy-of select="@name"/>
        
                    <xsl:copy-of select="CredentialStoreFragmentRef"/>
                    <xsl:copy-of select="JumpFragmentRef"/>

                    <xsl:apply-templates select="URLConnection"/>
                    <xsl:copy-of select="BasicAuthentication"/>
                    <xsl:if test="ProxyForHTTP">
                        <ProxyForHTTP><xsl:apply-templates select="ProxyForHTTP"/></ProxyForHTTP>
                    </xsl:if> 
            
                    <xsl:copy-of select="HTTPHeaders"/>
                    <!-- Transfer SSL information if it exists in v1 -->
                    <SSL>
                        <!-- Transfer AcceptUntrustedCertificate for v1 if it exists, otherwise use TrustedSSL -->
                        <xsl:choose>
                            <!-- If AcceptUntrustedCertificate exists in v1 (HTTPSFragment) -->
                            <xsl:when test="AcceptUntrustedCertificate">
                                <UntrustedSSL>
                                    <xsl:copy-of select="DisableCertificateHostnameVerification"/>
                                </UntrustedSSL>
                            </xsl:when>        
                            <xsl:otherwise>
                                <xsl:if test="KeyStore/KeyStoreFile">
                                    <TrustedSSL>
                                        <TrustStore>
                                            <xsl:if test="KeyStore/KeyStoreType">
                                                <TrustStoreType><xsl:value-of select="KeyStore/KeyStoreType"/></TrustStoreType>
                                            </xsl:if>
                                            <TrustStoreFile><xsl:value-of select="KeyStore/KeyStoreFile"/></TrustStoreFile>
                                            <xsl:if test="KeyStore/KeyStorePassword">
                                                <TrustStorePassword><xsl:value-of select="KeyStore/KeyStorePassword"/></TrustStorePassword>
                                            </xsl:if>
                                        </TrustStore>
                                    </TrustedSSL>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>    
                    </SSL> 
                </HTTPSFragment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Template for SFTPFragment -->
    <xsl:template match="SFTPFragment">
        <SFTPFragment>
            <xsl:copy-of select="@name"/>
        
            <xsl:copy-of select="CredentialStoreFragmentRef"/>
            <xsl:copy-of select="JumpFragmentRef"/>
            
            <xsl:apply-templates select="BasicConnection"/>
            <xsl:copy-of select="SSHAuthentication"/>
            <xsl:if test="ProxyForSFTP">
                <ProxyForSFTP><xsl:apply-templates select="ProxyForSFTP"/></ProxyForSFTP>
            </xsl:if> 
            
            <!-- SocketTimeout -->
            <xsl:choose>
                <!-- If ChannelConnectTimeout exists in v1, transform it to SocketTimeout for v2 -->
                <xsl:when test="ChannelConnectTimeout">
                   <SocketTimeout><xsl:value-of select="ChannelConnectTimeout"/></SocketTimeout>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="SocketTimeout"/>
                </xsl:otherwise>
            </xsl:choose>
            
            <!-- KeepAlive -->
            <xsl:choose>
                <!-- If ServerAliveInterval/ServerAliveCountMax exists in v1, transform it to KeepAlive for v2 -->
                <xsl:when test="ServerAliveInterval">
                    <KeepAlive>
                        <KeepAliveInterval><xsl:value-of select="ServerAliveInterval"/></KeepAliveInterval>
                        <xsl:if test="ServerAliveCountMax">
                            <MaxAliveCount><xsl:value-of select="ServerAliveCountMax"/></MaxAliveCount>
                        </xsl:if> 
                    </KeepAlive>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="KeepAlive"/>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:copy-of select="StrictHostkeyChecking"/>
            <xsl:copy-of select="ConfigurationFiles"/>
             
        </SFTPFragment>        
    </xsl:template>
    
    <!-- Template for SMBFragment: check if SMBConnection exists to determine if it's v2 -->
    <xsl:template match="SMBFragment">
        <xsl:choose>
            <!-- If SMBAuthentication/Account element exists, it's v1, so transform it to v2 -->
            <xsl:when test="SMBAuthentication/Account">
                <SMBFragment>
                    <xsl:copy-of select="@name"/>
          
                    <xsl:copy-of select="CredentialStoreFragmentRef"/>
                    <xsl:copy-of select="JumpFragmentRef"/>
                    
                    <!-- Workaround: SMBConnection is v2, but was introduced before transformation, so it should be copied as is, or it is v1 -->
                    <xsl:choose>
                        <xsl:when test="SMBConnection">
                            <xsl:copy-of select="SMBConnection"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- Handle Hostname in v2 format -->
                            <SMBConnection>
                                <xsl:copy-of select="Hostname"/>
                            </SMBConnection>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <SMBAuthentication>
                        <xsl:choose>
                            <!-- If SMBAuthentication/Password exists in v1, transform it to SMBAuthenticationMethodNTLM for v2 -->
                            <xsl:when test="SMBAuthentication/Password">
                                <SMBAuthenticationMethodNTLM>
                                    <xsl:copy-of select="SMBAuthentication/Account"/>
                                    <xsl:copy-of select="SMBAuthentication/Password"/>
                                    <xsl:copy-of select="SMBAuthentication/Domain"/>
                                </SMBAuthenticationMethodNTLM>
                            </xsl:when>
                            <xsl:otherwise>
                                <SMBAuthenticationMethodGuest>
                                    <Account><xsl:value-of select="SMBAuthentication/Account"/></Account>
                                </SMBAuthenticationMethodGuest>
                            </xsl:otherwise>
                        </xsl:choose>
                    </SMBAuthentication>
          
                    <xsl:copy-of select="ConfigurationFiles"/>
                </SMBFragment>
            </xsl:when>
            
            <!-- If SMBAuthentication/Account element does not exist, it must be v1, so copy the entire SMBFragment as is -->
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
            
        </xsl:choose>
    </xsl:template>
    
    <!-- Template for WebDAVFragment -->
    <xsl:template match="WebDAVFragment">        
        <WebDAVFragment>
            <xsl:copy-of select="@name"/>
        
            <xsl:copy-of select="CredentialStoreFragmentRef"/>
            <xsl:copy-of select="JumpFragmentRef"/>

            <xsl:apply-templates select="URLConnection"/>
            <xsl:copy-of select="BasicAuthentication"/>
            <xsl:if test="ProxyForWebDAV">
                <ProxyForWebDAV><xsl:apply-templates select="ProxyForWebDAV"/></ProxyForWebDAV>
            </xsl:if>
        
            <xsl:copy-of select="HTTPHeaders"/>
            <xsl:choose>
                <xsl:when test="SSL"><!-- v2 -->                    
                    <xsl:copy-of select="SSL"/>
                </xsl:when>        
                <xsl:otherwise><!-- v1 -->  
                    <xsl:choose>                
                        <!-- If AcceptUntrustedCertificate exists in v1 (WebDAVFragment) -->
                        <xsl:when test="AcceptUntrustedCertificate">
                            <SSL>
                                <UntrustedSSL><xsl:copy-of select="DisableCertificateHostnameVerification"/></UntrustedSSL>
                            </SSL>
                        </xsl:when>        
                        <xsl:otherwise>
                            <xsl:if test="KeyStore/KeyStoreFile">
                                <SSL>
                                    <TrustedSSL>
                                        <TrustStore>
                                            <xsl:if test="KeyStore/KeyStoreType">
                                                <TrustStoreType><xsl:value-of select="KeyStore/KeyStoreType"/></TrustStoreType>
                                            </xsl:if>
                                            <TrustStoreFile><xsl:value-of select="KeyStore/KeyStoreFile"/></TrustStoreFile>
                                            <xsl:if test="KeyStore/KeyStorePassword">
                                                <TrustStorePassword><xsl:value-of select="KeyStore/KeyStorePassword"/></TrustStorePassword>
                                            </xsl:if>
                                        </TrustStore>
                                    </TrustedSSL>
                                </SSL>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>    
        </WebDAVFragment>            
    </xsl:template> 

    <!-- Template for JumpFragment: check if YADEClientCommand exists to determine if it's v2 -->
    <xsl:template match="JumpFragment">
        <xsl:choose>
            <!-- If YADEClientCommand element exists, it's v2, so copy the entire JumpFragment as is -->
            <xsl:when test="YADEClientCommand">
                <xsl:copy-of select="."/>
            </xsl:when>

            <!-- If YADEClientCommand element does not exist, it must be v1, so transform it to v2 -->
            <xsl:otherwise>
                <JumpFragment>
                    <xsl:copy-of select="@name"/>
          
                    <xsl:copy-of select="CredentialStoreFragmentRef"/>
                    
                    <xsl:copy-of select="BasicConnection"/>
                    <xsl:copy-of select="SSHAuthentication"/>
                    
                    <xsl:if test="JumpCommand">
                        <YADEClientCommand><xsl:value-of select="JumpCommand"/></YADEClientCommand>
                    </xsl:if>
                    <xsl:if test="JumpDirectory">
                        <TempDirectoryParent><xsl:value-of select="JumpDirectory"/></TempDirectoryParent>
                    </xsl:if>                       
                    
                    <xsl:if test="ProxyForSFTP">
                        <ProxyForSFTP><xsl:apply-templates select="ProxyForSFTP"/></ProxyForSFTP>
                    </xsl:if> 
                    <!-- SocketTimeout -->
                    <xsl:if test="ChannelConnectTimeout">
                        <SocketTimeout><xsl:value-of select="ChannelConnectTimeout"/></SocketTimeout>
                    </xsl:if>                    
                    <!-- KeepAlive -->
                    <xsl:if test="ServerAliveInterval">
                        <KeepAlive>
                            <KeepAliveInterval><xsl:value-of select="ServerAliveInterval"/></KeepAliveInterval>
                            <xsl:if test="ServerAliveCountMax">
                                <MaxAliveCount><xsl:value-of select="ServerAliveCountMax"/></MaxAliveCount>
                            </xsl:if> 
                        </KeepAlive>
                    </xsl:if>                    
                    <xsl:copy-of select="StrictHostkeyChecking"/>
                    <xsl:copy-of select="ConfigurationFiles"/>
                    
                    <xsl:if test="JumpCommandBeforeFile or JumpCommandBeforeOperation">
                        <SFTPPreProcessing>
                            <xsl:if test="JumpCommandBeforeFile">
                                <CommandBeforeFile><xsl:value-of select="JumpCommandBeforeFile"/></CommandBeforeFile>
                            </xsl:if>
                            <xsl:if test="JumpCommandBeforeOperation">
                                <CommandBeforeOperation><xsl:value-of select="JumpCommandBeforeOperation"/></CommandBeforeOperation>
                            </xsl:if> 
                        </SFTPPreProcessing>
                    </xsl:if>
                    <xsl:if test="JumpCommandAfterFile or JumpCommandAfterOperationOnSuccess or JumpCommandAfterOperationOnError or JumpCommandAfterOperationFinal or JumpCommandBeforeRename">
                        <SFTPPostProcessing>
                            <xsl:if test="JumpCommandAfterFile">
                                <CommandAfterFile><xsl:value-of select="JumpCommandAfterFile"/></CommandAfterFile>
                            </xsl:if>
                            <xsl:if test="JumpCommandAfterOperationOnSuccess">
                                <CommandAfterOperationOnSuccess><xsl:value-of select="JumpCommandAfterOperationOnSuccess"/></CommandAfterOperationOnSuccess>
                            </xsl:if>
                            <xsl:if test="JumpCommandAfterOperationOnError">
                                <CommandAfterOperationOnError><xsl:value-of select="JumpCommandAfterOperationOnError"/></CommandAfterOperationOnError>
                            </xsl:if> 
                            <xsl:if test="JumpCommandAfterOperationFinal">
                                <CommandAfterOperationFinal><xsl:value-of select="JumpCommandAfterOperationFinal"/></CommandAfterOperationFinal>
                            </xsl:if> 
                            <xsl:if test="JumpCommandBeforeRename">
                                <CommandBeforeRename><xsl:value-of select="JumpCommandBeforeRename"/></CommandBeforeRename>
                            </xsl:if> 
                        </SFTPPostProcessing>
                    </xsl:if>
                    <xsl:if test="JumpCommandDelimiter">
                        <ProcessingCommandDelimiter><xsl:value-of select="JumpCommandDelimiter"/></ProcessingCommandDelimiter>
                    </xsl:if> 
                    <xsl:copy-of select="Platform"/>                    
                </JumpFragment>
            </xsl:otherwise>
        </xsl:choose>     
    </xsl:template>
       
    <!-- Handle BasicConnection in v2 format -->
    <xsl:template match="BasicConnection">
        <BasicConnection>
            <xsl:copy-of select="Hostname"/>
            <xsl:copy-of select="Port"/>
        
            <xsl:choose>
                 <xsl:when test="ConnectTimeout">
                    <xsl:copy-of select="ConnectTimeout"/>
                </xsl:when>        
                <xsl:otherwise>
                    <xsl:copy-of select="../ConnectTimeout"/>
                </xsl:otherwise>
            </xsl:choose>
        </BasicConnection>
    </xsl:template>
    
     <!-- Handle URLConnection in v2 format -->
    <xsl:template match="URLConnection">   
        <URLConnection>
            <xsl:copy-of select="URL"/>
           
            <xsl:choose>
                 <xsl:when test="ConnectTimeout">
                    <xsl:copy-of select="ConnectTimeout"/>
                </xsl:when>        
                <xsl:otherwise>
                    <xsl:copy-of select="../ConnectTimeout"/>
                </xsl:otherwise>
            </xsl:choose>
        </URLConnection>
    </xsl:template>
    
    <xsl:template match="ProxyForFTP | ProxyForFTPS | ProxyForHTTP | ProxyForSFTP | ProxyForWebDAV">
        <xsl:if test="HTTPProxy"><!-- v1 and v2 -->
            <xsl:copy-of select="HTTPProxy"/>
        </xsl:if>
        <xsl:if test="SOCKS4Proxy"> <!-- v1 to v2 -->
            <SOCKSProxy><xsl:copy-of select="SOCKS4Proxy/*"/></SOCKSProxy>
        </xsl:if>
        <xsl:if test="SOCKS5Proxy"> <!-- v1 to v2 -->
            <SOCKSProxy><xsl:copy-of select="SOCKS5Proxy/*"/></SOCKSProxy>
        </xsl:if>
        <xsl:if test="SOCKSProxy"> <!-- v2 -->
            <xsl:copy-of select="SOCKSProxy"/>
        </xsl:if>
    </xsl:template>
    
    <!-- TODO MailServer ??? -->    
    
    <!-- Remove deprecated elements from v1 that are no longer part of v2 -->
    <xsl:template match="FTPSClientSecurity | FTPSProtocol | AcceptUntrustedCertificate | ServerAliveInterval | ServerAliveCountMax | SOCKS4Proxy | SOCKS5Proxy | JumpDirectory | JumpCommand | JumpCommandBeforeFile | JumpCommandAfterFileOnSuccess | JumpCommandBeforeOperation | JumpCommandAfterOperationOnSuccess | JumpCommandAfterOperationOnError | JumpCommandAfterOperationFinal | JumpCommandDelimiter"/>

</xsl:stylesheet>
