package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon.JumpHostConfig;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;

/** TODO Not supported for SFTPFragment: <ZlibCompression> <ZlibCompressionLevel>1</ZlibCompressionLevel> </ZlibCompression> */
/** TODO MailServer */
public class YADEXMLJumpHostSettingsWriter {

    private static final String FRAGMENT_NAME = "fragment";
    private static final String CS_FRAMENT_REF = "<CredentialStoreFragmentRef ref=\"cs\"/>";

    // -------- SOURCE_TO_JUMP_HOST XML settings -------------------
    /** COPY/MOVE operations */
    public static String sourceToJumpHostCOPY(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Copy", true);
        return generateConfiguration(fragments, profile).toString();
    }

    /** GETLIST operation */
    public static String sourceToJumpHostGETLIST(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "GetList", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** REMOVE operation */
    public static String sourceToJumpHostREMOVE(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Remove", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** additional configuration for a MOVE operation - removing the source files after successful transfer */
    public static String sourceToJumpHostMOVERemove(AYADEArgumentsLoader argsLoader, JumpHostConfig config, String profileId) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHostMOVERemove(sourceArgs, config, profileId);
        return generateConfiguration(fragments, profile).toString();
    }

    // -------- JUMP_HOST_TO_TARGET -------------------
    /** COPY/MOVE operations<br/>
     * 
     * @apiNote GETLIST and REMOVE operations are ignored because they are performed for the Source(Any Provider) and not require a Jump Host */
    public static String jumpHostToTargetCOPY(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADETargetArguments targetArgs = argsLoader.getTargetArgs();

        StringBuilder fragments = generateFragments(targetArgs.getProvider());
        StringBuilder profile = generateProfileJumpHostToTargetCOPY(argsLoader.getArgs(), targetArgs, config);
        return generateConfiguration(fragments, profile).toString();
    }

    // ------------- Help-Methods -----
    private static StringBuilder generateConfiguration(StringBuilder fragments, StringBuilder profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Configurations>");

        sb.append("<Fragments>").append(fragments).append("</Fragments>");
        sb.append("<Profiles>").append(profile).append("</Profiles>");

        sb.append("</Configurations>");
        return sb;
    }

    private static StringBuilder generateFragments(AProviderArguments providerArgs) {
        boolean generateCS = false;
        if (providerArgs.getCredentialStore() != null && providerArgs.getCredentialStore().getFile().isDirty()) {
            generateCS = true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<ProtocolFragments>");
        switch (providerArgs.getProtocol().getValue()) {
        case SFTP:
            sb.append(generateProtocolFragmentSFTP((SSHProviderArguments) providerArgs, generateCS));
            break;
        case FTP:
            sb.append(generateProtocolFragmentFTP((FTPProviderArguments) providerArgs, generateCS, false));
            break;
        case FTPS:
            sb.append(generateProtocolFragmentFTP((FTPSProviderArguments) providerArgs, generateCS, true));
            break;
        case HTTP:
            sb.append(generateProtocolFragmentHTTP((HTTPProviderArguments) providerArgs, generateCS, false));
            break;
        case HTTPS:
            sb.append(generateProtocolFragmentHTTP((HTTPSProviderArguments) providerArgs, generateCS, true));
            break;
        case WEBDAV:
            sb.append(generateProtocolFragmentWEBDAV((WebDAVProviderArguments) providerArgs, generateCS, false));
            break;
        case WEBDAVS:
            sb.append(generateProtocolFragmentWEBDAV((WebDAVProviderArguments) providerArgs, generateCS, true));
            break;
        case SMB:
            sb.append(generateProtocolFragmentSMB((SMBProviderArguments) providerArgs, generateCS));
            break;
        case LOCAL:
        case SSH:
        case UNKNOWN:
        default:
            sb.append("</ProtocolFragments>");
            return sb;

        }
        sb.append("</ProtocolFragments>");

        if (generateCS) {
            /** CredentialStore Fragment */
            sb.append("<CredentialStoreFragments>");
            sb.append("<CredentialStoreFragment name=\"cs\">");
            sb.append("<CSFile>").append(cdata(providerArgs.getCredentialStore().getFile().getValue())).append("</CSFile>");
            sb.append("<CSAuthentication>");
            if (providerArgs.getCredentialStore().getKeyFile().isDirty()) {
                sb.append("<KeyFileAuthentication>");
                sb.append("<CSKeyFile>").append(cdata(providerArgs.getCredentialStore().getKeyFile().getValue())).append("</CSKeyFile>");
                if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                    sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append("</CSPassword>");
                }
                sb.append("</KeyFileAuthentication>");
            } else if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                sb.append("<PasswordAuthentication>");
                sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append("</CSPassword>");
                sb.append("</PasswordAuthentication>");
            }
            sb.append("</CSAuthentication>");
            sb.append("</CredentialStoreFragment>");
            sb.append("</CredentialStoreFragments>");
        }
        return sb;
    }

    private static StringBuilder generateProtocolFragmentSFTP(SSHProviderArguments args, boolean generateCSRef) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SFTPFragment name=").append(attrValue(FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartBasicConnection(args.getHost(), args.getPort(), args.getConnectTimeout()));
        // SSHAuthentication
        sb.append("<SSHAuthentication>");
        sb.append("<Account>").append(cdata(args.getUser().getValue())).append("</Account>");
        if (!args.getPreferredAuthentications().isEmpty()) {
            sb.append("<PreferredAuthentications>");
            sb.append(cdata(args.getPreferredAuthenticationsAsString()));
            sb.append("</PreferredAuthentications>");
        }
        if (!args.getRequiredAuthentications().isEmpty()) {
            sb.append("<RequiredAuthentications>");
            sb.append(cdata(args.getRequiredAuthenticationsAsString()));
            sb.append("</RequiredAuthentications>");
        }
        if (!args.getPassword().isEmpty()) {
            sb.append("<AuthenticationMethodPassword>");
            sb.append("<Password>").append(cdata(args.getPassword().getValue())).append("</Password>");
            sb.append("</AuthenticationMethodPassword>");
        }
        if (!args.getAuthFile().isEmpty()) {
            sb.append("<AuthenticationMethodPublickey>");
            sb.append("<AuthenticationFile>").append(cdata(args.getAuthFile().getValue())).append("</AuthenticationFile>");
            if (!args.getPassphrase().isEmpty()) {
                sb.append("<Passphrase>").append(cdata(args.getPassphrase().getValue())).append("</Passphrase>");
            }
            sb.append("</AuthenticationMethodPublickey>");
        }
        sb.append("</SSHAuthentication>");

        if (generateCSRef) {
            sb.append(CS_FRAMENT_REF);
        }

        // ProxyForSFTP
        sb.append(generateProtocolFragmentPartProxy(args.getProxy(), "ProxyForSFTP"));
        // Other
        if (args.getStrictHostkeyChecking().isDirty()) {
            sb.append("<StrictHostkeyChecking>").append(args.getStrictHostkeyChecking().getValue()).append("</StrictHostkeyChecking>");
        }
        if (args.getConfigurationFiles().isDirty()) {
            sb.append("<ConfigurationFiles>");
            for (Path configurationFile : args.getConfigurationFiles().getValue()) {
                sb.append("<ConfigurationFile>").append(cdata(configurationFile.toString())).append("</ConfigurationFile>");
            }
            sb.append("</ConfigurationFiles>");
        }
        // YADE 1 - compatibility
        if (args.getServerAliveInterval().isDirty()) {
            sb.append("<ServerAliveInterval>").append(cdata(args.getServerAliveInterval().getValue())).append("</ServerAliveInterval>");
        }
        if (args.getServerAliveCountMax().isDirty()) {
            sb.append("<ServerAliveCountMax>").append(args.getServerAliveCountMax().getValue()).append("</ServerAliveCountMax>");
        }
        if (args.getConnectTimeout().isDirty()) {
            sb.append("<ConnectTimeout>").append(cdata(args.getConnectTimeout().getValue())).append("</ConnectTimeout>");
        }
        if (args.getSocketTimeout().isDirty()) {
            sb.append("<ChannelConnectTimeout>").append(cdata(args.getSocketTimeout().getValue())).append("</ChannelConnectTimeout>");
        }
        // YADE 1 - compatibility - end
        sb.append("</SFTPFragment>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentFTP(FTPProviderArguments args, boolean generateCSRef, boolean isFTPS) {
        String fragmentElementName = isFTPS ? "FTPSFragment" : "FTPFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartBasicConnection(args.getHost(), args.getPort(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAMENT_REF);
        }
        if (isFTPS) {
            FTPSProviderArguments ftps = (FTPSProviderArguments) args;
            SslArguments ssl = ftps.getSsl();
            // YADE 1 - compatibility
            sb.append("<FTPSClientSecurity>");
            sb.append("<SecurityMode>").append(cdata(ftps.getSecurityModeValue())).append("</SecurityMode>");
            if (ftps.getSsl().getTrustedSsl().isCustomTrustStoreEnabled()) {
                sb.append(generateProtocolFragmentPartYADE1KeyStore(ssl));
            }
            sb.append("<FTPSClientSecurity>");
            // YADE JS7
            sb.append(generateProtocolFragmentPartSsl(ssl));
        } else {
            sb.append("<PassiveMode>").append(args.getPassiveMode().getValue()).append("</PassiveMode>");
            if (args.getTransferMode().getValue() != null) {
                sb.append("<TransferMode>").append(cdata(args.getTransferModeValue())).append("</TransferMode>");
            }
        }
        sb.append(generateProtocolFragmentPartProxy(args.getProxy(), isFTPS ? "ProxyForFTPS" : "ProxyForFTP"));
        // Other - YADE 1 - compatibility
        if (args.getConnectTimeout().isDirty()) {
            sb.append("<ConnectTimeout>").append(cdata(args.getConnectTimeout().getValue())).append("</ConnectTimeout>");
        }
        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentHTTP(HTTPProviderArguments args, boolean generateCSRef, boolean isHTTPS) {
        String fragmentElementName = isHTTPS ? "HTTPSFragment" : "HTTPFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartURLConnection(args.getHost(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAMENT_REF);
        }
        if (isHTTPS) {
            SslArguments ssl = ((HTTPSProviderArguments) args).getSsl();
            // YADE 1 - compatibility
            if (ssl.getUntrustedSsl().isTrue()) {
                sb.append("<AcceptUntrustedCertificate>true</AcceptUntrustedCertificate>");
                sb.append("<DisableCertificateHostnameVerification>");
                sb.append(getOppositeValue(ssl.getUntrustedSslVerifyCertificateHostname()));
                sb.append("</DisableCertificateHostnameVerification>");
            }
            if (ssl.getTrustedSsl().isCustomTrustStoreEnabled()) {
                sb.append(generateProtocolFragmentPartYADE1KeyStore(ssl));
            }
            // YADE JS7
            sb.append(generateProtocolFragmentPartSsl(ssl));
        }

        sb.append(generateProtocolFragmentPartProxy(args.getProxy(), "ProxyForHTTP"));
        sb.append(generateProtocolFragmentPartHTTPHeaders(args.getHttpHeaders()));

        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    // TODO WebDAVSFragment ???
    private static StringBuilder generateProtocolFragmentWEBDAV(HTTPProviderArguments args, boolean generateCSRef, boolean isWEBDAVS) {
        String fragmentElementName = isWEBDAVS ? "WebDAVFragment" : "WebDAVFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartURLConnection(args.getHost(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAMENT_REF);
        }
        if (isWEBDAVS) {
            SslArguments ssl = ((HTTPSProviderArguments) args).getSsl();
            // YADE 1 - compatibility
            if (ssl.getUntrustedSsl().isTrue()) {
                sb.append("<AcceptUntrustedCertificate>true</AcceptUntrustedCertificate>");
                sb.append("<DisableCertificateHostnameVerification>");
                sb.append(getOppositeValue(ssl.getUntrustedSslVerifyCertificateHostname()));
                sb.append("</DisableCertificateHostnameVerification>");
            }
            if (ssl.getTrustedSsl().isCustomTrustStoreEnabled()) {
                sb.append(generateProtocolFragmentPartYADE1KeyStore(ssl));
            }
            // YADE JS7
            sb.append(generateProtocolFragmentPartSsl(ssl));
        }

        sb.append(generateProtocolFragmentPartProxy(args.getProxy(), "ProxyForWebDAV"));
        sb.append(generateProtocolFragmentPartHTTPHeaders(args.getHttpHeaders()));

        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentSMB(SMBProviderArguments args, boolean generateCSRef) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SMBFragment name=").append(attrValue(FRAGMENT_NAME)).append(">");
        // YADE 1 - compatibility
        sb.append("<Hostname>").append(cdata(args.getHost().getValue())).append("</Hostname>");

        if (args.getPort().isDirty() || !args.getShareName().isEmpty()) {
            // YADE JS7
            sb.append("<SMBConnection>");
            sb.append("<Hostname>").append(cdata(args.getHost().getValue())).append("</Hostname>");
            if (args.getPort().isDirty()) {
                sb.append("<Port>").append(args.getPort().getValue()).append("</Port>");
            }
            sb.append("<Sharename>").append(cdata(args.getShareName().getValue())).append("</Sharename>");
            sb.append("</SMBConnection>");
        }

        sb.append("<SMBAuthentication>");
        // YADE 1 - compatibility at this level
        sb.append(generateProtocolFragmentPartSMBAuthChildren(args));
        // YADE JS7
        switch (args.getAuthMethod().getValue()) {
        case ANONYMOUS:
            sb.append("<SMBAuthenticationMethodAnonymous />");
            break;
        case GUEST:
            sb.append("<SMBAuthenticationMethodGuest />");
            break;
        case NTLM:
            sb.append("<SMBAuthenticationMethodNTLM>");
            sb.append(generateProtocolFragmentPartSMBAuthChildren(args));
            sb.append("</SMBAuthenticationMethodNTLM>");
            break;
        case KERBEROS:
            sb.append("<SMBAuthenticationMethodKerberos>");
            sb.append(generateProtocolFragmentPartSMBAuthChildren(args));
            sb.append("</SMBAuthenticationMethodKerberos>");
            break;
        case SPNEGO:
            sb.append("<SMBAuthenticationMethodSPNEGO>");
            sb.append(generateProtocolFragmentPartSMBAuthChildren(args));
            sb.append("</SMBAuthenticationMethodSPNEGO>");
            break;
        default:
            break;
        }
        sb.append("</SMBAuthentication>");

        if (generateCSRef) {
            sb.append(CS_FRAMENT_REF);
        }

        // Other
        if (args.getConfigurationFiles().isDirty()) {
            sb.append("<ConfigurationFiles>");
            for (Path configurationFile : args.getConfigurationFiles().getValue()) {
                sb.append("<ConfigurationFile>").append(cdata(configurationFile.toString())).append("</ConfigurationFile>");
            }
            sb.append("</ConfigurationFiles>");
        }
        sb.append("</SMBFragment>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartSMBAuthChildren(SMBProviderArguments args) {
        StringBuilder sb = new StringBuilder();
        // YADE 1/YADE JS7
        if (!args.getUser().isEmpty()) {
            sb.append("<Account>");
            sb.append(cdata(args.getUser().getValue()));
            sb.append("</Account>");
        }
        if (!args.getDomain().isEmpty()) {
            sb.append("<Domain>");
            sb.append(cdata(args.getDomain().getValue()));
            sb.append("</Domain>");
        }
        if (!args.getPassword().isEmpty()) {
            sb.append("<Password>");
            sb.append(cdata(args.getPassword().getValue()));
            sb.append("</Password>");
        }
        // JS7
        if (!args.getLoginContextName().isEmpty()) {
            sb.append("<LoginContextName>");
            sb.append(cdata(args.getLoginContextName().getValue()));
            sb.append("</LoginContextName>");
        }
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartHTTPHeaders(SOSArgument<List<String>> headers) {
        StringBuilder sb = new StringBuilder();
        if (headers.getValue() == null || headers.getValue().size() == 0) {
            return sb;
        }
        sb.append("<HTTPHeaders>");
        for (String header : headers.getValue()) {
            sb.append("<HTTPHeader >").append(cdata(header)).append("</HTTPHeader>");
        }
        sb.append("</HTTPHeaders>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartProxy(ProxyConfigArguments args, String elementName) {
        StringBuilder sb = new StringBuilder();
        if (args == null) {
            return sb;
        }

        sb.append("<").append(elementName).append(">");
        if (args.isHTTP()) {
            sb.append("<HTTPProxy>");
            sb.append(generateProtocolFragmentPartBasicConnection(args.getHost(), args.getPort(), args.getConnectTimeout()));
            sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
            sb.append("</HTTPProxy>");
        } else {
            // YADE 1 - compatibility (OK for YADE JS7)
            sb.append("<SOCKS5Proxy>");
            sb.append(generateProtocolFragmentPartBasicConnection(args.getHost(), args.getPort(), args.getConnectTimeout()));
            sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
            sb.append("</SOCKS5Proxy>");
        }
        sb.append("</").append(elementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartYADE1KeyStore(SslArguments args) {
        StringBuilder sb = new StringBuilder();
        sb.append("<KeyStoreType>");
        sb.append(cdata(args.getTrustedSsl().getTrustStoreType().getValue().name()));
        sb.append("</KeyStoreType>");
        sb.append("<KeyStoreFile>");
        sb.append(cdata(args.getTrustedSsl().getTrustStoreFile().getValue().toString()));
        sb.append("</KeyStoreFile>");
        if (!args.getTrustedSsl().getTrustStorePassword().isEmpty()) {
            sb.append("<KeyStorePassword >");
            sb.append(cdata(args.getTrustedSsl().getTrustStorePassword().getValue()));
            sb.append("</KeyStorePassword>");
        }
        return sb;
    }

    // YADE JS7
    private static StringBuilder generateProtocolFragmentPartSsl(SslArguments args) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SSL>");

        if (args.getUntrustedSsl().isTrue()) {
            sb.append("<UntrustedSSL>");
            sb.append("<DisableCertificateHostnameVerification>");
            sb.append(getOppositeValue(args.getUntrustedSslVerifyCertificateHostname()));
            sb.append("</DisableCertificateHostnameVerification>");
            sb.append("</UntrustedSSL>");
        } else {
            sb.append("<TrustedSSL>");
            if (args.getTrustedSsl().isCustomTrustStoreEnabled()) {
                sb.append("<TrustStore>");
                sb.append("<TrustStoreType>");
                sb.append(cdata(args.getTrustedSsl().getTrustStoreType().getValue().name()));
                sb.append("</TrustStoreType>");
                sb.append("<TrustStoreFile>");
                sb.append(cdata(args.getTrustedSsl().getTrustStoreFile().getValue().toString()));
                sb.append("</TrustStoreFile>");
                if (!args.getTrustedSsl().getTrustStorePassword().isEmpty()) {
                    sb.append("<TrustStorePassword >");
                    sb.append(cdata(args.getTrustedSsl().getTrustStorePassword().getValue()));
                    sb.append("</TrustStorePassword>");
                }
                sb.append("</TrustStore>");
            }
            if (args.getTrustedSsl().isCustomKeyStoreEnabled()) {
                sb.append("<KeyStore>");
                sb.append("<KeyStoreType>");
                sb.append(cdata(args.getTrustedSsl().getTrustStoreType().getValue().name()));
                sb.append("</KeyStoreType>");
                sb.append("<KeyStoreFile>");
                sb.append(cdata(args.getTrustedSsl().getTrustStoreFile().getValue().toString()));
                sb.append("</KeyStoreFile>");
                if (!args.getTrustedSsl().getTrustStorePassword().isEmpty()) {
                    sb.append("<KeyStorePassword >");
                    sb.append(cdata(args.getTrustedSsl().getTrustStorePassword().getValue()));
                    sb.append("</KeyStorePassword>");
                }
                sb.append("</KeyStore>");
            }
            sb.append("<TrustedSSL>");
        }
        if (!args.getEnabledProtocols().isEmpty()) {
            sb.append("<EnabledProtocols>").append(cdata(args.getEnabledProtocols().getValue())).append("</EnabledProtocols>");
        }

        sb.append("</SSL>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartURLConnection(SOSArgument<String> host, SOSArgument<String> connectTimeout) {
        StringBuilder sb = new StringBuilder();
        sb.append("<URLConnection>");

        sb.append("<URL>").append(cdata(host.getValue())).append("</URL>");
        if (connectTimeout.isDirty()) { // YADE JS7
            sb.append("<ConnectTimeout>").append(cdata(connectTimeout.getValue())).append("</ConnectTimeout>");
        }

        sb.append("</URLConnection>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartBasicConnection(SOSArgument<String> host, SOSArgument<Integer> port,
            SOSArgument<String> connectTimeout) {
        StringBuilder sb = new StringBuilder();
        sb.append("<BasicConnection>");

        sb.append("<Hostname>").append(cdata(host.getValue())).append("</Hostname>");
        if (port.isDirty()) {
            sb.append("<Port>").append(cdata(String.valueOf(port.getValue()))).append("</Port>");
        }
        if (connectTimeout.isDirty()) { // YADE JS7
            sb.append("<ConnectTimeout>").append(cdata(connectTimeout.getValue())).append("</ConnectTimeout>");
        }
        sb.append("</BasicConnection>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentPartBasicAuthentication(SOSArgument<String> user, SOSArgument<String> password) {
        StringBuilder sb = new StringBuilder();
        if (user.isEmpty()) {
            return sb;
        }
        sb.append("<BasicAuthentication>");

        sb.append("<Account>").append(cdata(user.getValue())).append("</Account>");
        if (password.isDirty()) {
            sb.append("<Password>").append(cdata(password.getValue())).append("</Password>");
        }

        sb.append("</BasicAuthentication>");
        return sb;
    }

    private static StringBuilder generateProfileSourceToJumpHost(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, JumpHostConfig config, String operation, boolean useTarget) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId())).append(">");
        sb.append("<Operation>");
        sb.append("<").append(operation).append(">");

        String sourcePrefix = operation + "Source";
        sb.append("<").append(sourcePrefix).append(">");
        sb.append("<").append(sourcePrefix).append("FragmentRef>");
        sb.append(generateProfilePartTargetFragmentRef(sourceArgs));
        sb.append("</").append(sourcePrefix).append("FragmentRef>");

        sb.append("<SourceFileOptions>");
        // Source - Selection
        sb.append("<Selection>");
        if (config.getSourceToJumpHost().getFileList() != null) {
            sb.append("<FileListSelection>");
            sb.append("<FileList>").append(cdata(config.getSourceToJumpHost().getFileList().getJumpHostFile())).append("</FileList>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FileListSelection>");
        } else if (sourceArgs.isFilePathEnabled()) {
            sb.append("<FilePathSelection>");
            sb.append("<FilePath>").append(cdata(sourceArgs.getFilePathAsString())).append("</FilePath>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FilePathSelection>");
        } else {
            sb.append("<FileSpecSelection>");
            sb.append("<FileSpec>").append(cdata(sourceArgs.getFileSpec().getValue())).append("</FileSpec>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (!sourceArgs.getExcludedDirectories().isEmpty()) {
                sb.append("<ExcludedDirectories>").append(cdata(sourceArgs.getExcludedDirectories().getValue())).append("</ExcludedDirectories>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FileSpecSelection>");
        }
        sb.append("</Selection>");

        // Source - CheckSteadyState
        if (sourceArgs.isCheckSteadyStateEnabled()) {
            sb.append("<CheckSteadyState>");
            sb.append("<CheckSteadyStateInterval>");
            sb.append(cdata(sourceArgs.getCheckSteadyStateInterval().getValue()));
            sb.append("</CheckSteadyStateInterval>");
            if (!sourceArgs.getCheckSteadyCount().isEmpty()) {
                sb.append("<CheckSteadyStateCount>");
                sb.append(sourceArgs.getCheckSteadyCount().getValue());
                sb.append("</CheckSteadyStateCount>");
            }
            sb.append("</CheckSteadyState>");
        }
        // Source - Directives
        if (sourceArgs.isDirectivesEnabled()) {
            sb.append("<Directives>");
            if (sourceArgs.getErrorOnNoFilesFound().isDirty()) {
                sb.append("<DisableErrorOnNoFilesFound>");
                sb.append(getOppositeValue(sourceArgs.getErrorOnNoFilesFound()));
                sb.append("</DisableErrorOnNoFilesFound>");
            }
            if (sourceArgs.getZeroByteTransfer().isDirty()) {
                sb.append("<TransferZeroByteFiles>");
                sb.append(cdata(sourceArgs.getZeroByteTransfer().getValue().name()));
                sb.append("</TransferZeroByteFiles>");
            }
            sb.append("</Directives>");
        }
        // Source - Polling
        if (sourceArgs.isPollingEnabled()) {
            sb.append("<Polling>");
            if (sourceArgs.getPolling().getPollInterval().isDirty()) {
                sb.append("<PollInterval>");
                sb.append(cdata(sourceArgs.getPolling().getPollInterval().getValue()));
                sb.append("</PollInterval>");
            }
            if (sourceArgs.getPolling().getPollTimeout().isDirty()) {
                sb.append("<PollTimeout>");
                sb.append(sourceArgs.getPolling().getPollTimeout().getValue());
                sb.append("</PollTimeout>");
            }
            if (sourceArgs.getPolling().getPollMinFiles().isDirty()) {
                sb.append("<MinFiles>");
                sb.append(sourceArgs.getPolling().getPollMinFiles().getValue());
                sb.append("</MinFiles>");
            }
            if (sourceArgs.getPolling().getWaitingForLateComers().isDirty()) {
                sb.append("<WaitForSourceFolder>");
                sb.append(sourceArgs.getPolling().getWaitingForLateComers().getValue());
                sb.append("</WaitForSourceFolder>");
            }
            if (sourceArgs.getPolling().getPollingServer().isDirty()) {
                sb.append("<PollingServer>");
                sb.append(sourceArgs.getPolling().getPollingServer().getValue());
                sb.append("</PollingServer>");
            }
            if (sourceArgs.getPolling().getPollingServerDuration().isDirty()) {
                sb.append("<PollingServerDuration>");
                sb.append(cdata(sourceArgs.getPolling().getPollingServerDuration().getValue()));
                sb.append("</PollingServerDuration>");
            }
            if (sourceArgs.getPolling().getPollingServerPollForever().isDirty()) {
                sb.append("<PollForever>");
                sb.append(sourceArgs.getPolling().getPollingServerPollForever().getValue());
                sb.append("</PollForever>");
            }
            sb.append("</Polling>");
        }
        if (config.getSourceToJumpHost().getResultSetFile() != null) {
            sb.append("<ResultSet>");
            sb.append("<ResultSetFile>").append(cdata(config.getSourceToJumpHost().getResultSetFile().getJumpHostFile())).append("</ResultSetFile>");
            if (clientArgs.isCheckResultSetCountEnabled()) {
                sb.append("<CheckResultSetCount>");
                if (clientArgs.getExpectedResultSetCount().isDirty()) {
                    sb.append("<ExpectedResultSetCount>");
                    sb.append(clientArgs.getExpectedResultSetCount().getValue());
                    sb.append("</ExpectedResultSetCount>");
                }
                if (clientArgs.getRaiseErrorIfResultSetIs().isDirty()) {
                    sb.append("<RaiseErrorIfResultSetIs>");
                    sb.append(cdata(clientArgs.getRaiseErrorIfResultSetIs().getValue().getFirstAlias()));
                    sb.append("</RaiseErrorIfResultSetIs>");
                }
                sb.append("</CheckResultSetCount>");
            }
            sb.append("</ResultSet>");
        }
        if (sourceArgs.getMaxFiles().isDirty()) {
            sb.append("<MaxFiles>").append(sourceArgs.getMaxFiles().getValue()).append("</MaxFiles>");
        }
        if (sourceArgs.getCheckIntegrityHash().isTrue()) {
            sb.append("<CheckIntegrityHash>");
            sb.append("<HashAlgorithm>").append(cdata(sourceArgs.getIntegrityHashAlgorithm().getValue())).append("</HashAlgorithm>");
            sb.append("</CheckIntegrityHash>");
        }
        sb.append("</SourceFileOptions>");
        sb.append("</").append(sourcePrefix).append(">");

        if (useTarget) {
            // Target (Jump) ----------------------------------
            sb.append(generateProfilePartJumpHostLocalCopyTarget(targetArgs, config));
            // TransferOptions
            sb.append(generateProfilePartTransferOptions(args, sourceArgs, config));
        }

        sb.append("</").append(operation).append(">");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileSourceToJumpHostMOVERemove(YADESourceArguments sourceArgs, JumpHostConfig config, String profileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(profileId)).append(">");
        sb.append("<Operation>");
        sb.append("<Remove>");
        sb.append("<RemoveSource>");

        sb.append("<RemoveSourceFragmentRef>");
        sb.append("<").append(getFragmentNamePrefix(sourceArgs.getProvider().getProtocol())).append("FragmentRef ref=");
        sb.append(attrValue(FRAGMENT_NAME)).append(" />");
        sb.append("</RemoveSourceFragmentRef>");

        sb.append("<SourceFileOptions>");
        // Source - Selection
        sb.append("<Selection>");
        sb.append("<FileListSelection>");
        sb.append("<FileList>").append(cdata(config.getSourceToJumpHost().getResultSetFile().getJumpHostFile())).append("</FileList>");
        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("</FileListSelection>");
        sb.append("</Selection>");
        sb.append("</SourceFileOptions>");

        sb.append("</RemoveSource>");
        sb.append("</Remove>");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileJumpHostToTargetCOPY(YADEArguments args, YADETargetArguments targetArgs, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId())).append(">");
        sb.append("<Operation>");
        sb.append("<Copy>");

        // Source (Jump)
        sb.append(generateProfilePartJumpHostLocalCopySource(config));
        // Target
        sb.append("<CopyTarget>");
        sb.append("<CopyTargetFragmentRef>");
        sb.append(generateProfilePartTargetFragmentRef(targetArgs));
        sb.append("</CopyTargetFragmentRef>");
        if (targetArgs.getDirectory().isDirty()) {
            sb.append("<Directory>").append(cdata(targetArgs.getDirectory().getValue())).append("</Directory>");
        }
        sb.append(generateProfilePartTargetFileOptions(targetArgs, config));
        sb.append("</CopyTarget>");
        // TransferOptions
        sb.append(generateProfilePartTransferOptions(args, targetArgs, config));

        sb.append("</Copy>");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfilePartJumpHostLocalCopySource(JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<CopySource>");

        sb.append("<CopySourceFragmentRef>");
        sb.append("<LocalSource ").append(generateJumpAttribute()).append("/>");
        sb.append("</CopySourceFragmentRef>");

        sb.append("<SourceFileOptions>");
        sb.append("<Selection>");
        sb.append("<FileSpecSelection>");
        sb.append("<FileSpec><![CDATA[.*]]></FileSpec>");
        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("<Recursive>true</Recursive>");
        sb.append("</FileSpecSelection>");
        sb.append("</Selection>");
        sb.append("</SourceFileOptions>");

        sb.append("</CopySource>");
        return sb;
    }

    private static StringBuilder generateProfilePartJumpHostLocalCopyTarget(YADETargetArguments targetArgs, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<CopyTarget>");

        sb.append("<CopyTargetFragmentRef>");
        sb.append("<LocalTarget ").append(generateJumpAttribute()).append("/>");
        sb.append("</CopyTargetFragmentRef>");

        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");

        // only KeepModificationDate - see comments YADEEngineJumpHostAddon.init()
        sb.append("<TargetFileOptions>");
        sb.append("<KeepModificationDate>").append(targetArgs.getKeepModificationDate().getValue()).append("</KeepModificationDate>");
        sb.append("</TargetFileOptions>");

        sb.append("</CopyTarget>");
        return sb;
    }

    private static StringBuilder generateProfilePartTargetFragmentRef(YADESourceTargetArguments args) {
        String fragmentPrefix = getFragmentNamePrefix(args.getProvider().getProtocol());
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentPrefix).append("FragmentRef ref=").append(attrValue(FRAGMENT_NAME)).append(">");
        // Pre-Processing
        if (args.getCommands().isPreProcessingEnabled()) {
            sb.append("<").append(fragmentPrefix).append("PreProcessing>");
            if (args.getCommands().getCommandsBeforeFile().isDirty()) {
                sb.append("<CommandBeforeFile enable_for_skipped_transfer=");
                sb.append(attrValue(args.getCommands().getCommandsBeforeFileEnableForSkipped().getValue() + ""));
                sb.append(">");
                sb.append(cdata(args.getCommands().getCommandsBeforeFileAsString()));
                sb.append("</CommandBeforeFile>");
            }
            if (args.getCommands().getCommandsBeforeOperation().isDirty()) {
                sb.append("<CommandBeforeOperation>");
                sb.append(cdata(args.getCommands().getCommandsBeforeOperationAsString()));
                sb.append("</CommandBeforeOperation>");
            }
            sb.append("</").append(fragmentPrefix).append("PreProcessing>");
        }
        // Post-Processing
        if (args.getCommands().isPostProcessingEnabled()) {
            sb.append("<").append(fragmentPrefix).append("PostProcessing>");
            if (args.getCommands().getCommandsAfterFile().isDirty()) {
                sb.append("<CommandAfterFile disable_for_skipped_transfer=");
                sb.append(attrValue(args.getCommands().getCommandsAfterFileDisableForSkipped().getValue() + ""));
                sb.append(">");
                sb.append(cdata(args.getCommands().getCommandsAfterFileAsString()));
                sb.append("</CommandAfterFile>");
            }
            if (args.getCommands().getCommandsAfterOperationOnSuccess().isDirty()) {
                sb.append("<CommandAfterOperationOnSuccess>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationOnSuccessAsString()));
                sb.append("</CommandAfterOperationOnSuccess>");
            }
            if (args.getCommands().getCommandsAfterOperationOnError().isDirty()) {
                sb.append("<CommandAfterOperationOnError>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationOnErrorAsString()));
                sb.append("</CommandAfterOperationOnError>");
            }
            if (args.getCommands().getCommandsAfterOperationFinal().isDirty()) {
                sb.append("<CommandAfterOperationFinal>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationFinalAsString()));
                sb.append("</CommandAfterOperationFinal>");
            }
            if (args.getCommands().getCommandsBeforeRename().isDirty()) {
                sb.append("<CommandBeforeRename>");
                sb.append(cdata(args.getCommands().getCommandsBeforeRenameAsString()));
                sb.append("</CommandBeforeRename>");
            }
            sb.append("</").append(fragmentPrefix).append("PostProcessing>");
        }
        if (args.getCommands().getCommandDelimiter().isDirty()) {
            sb.append("<ProcessingCommandDelimiter>");
            sb.append(cdata(args.getCommands().getCommandDelimiter().getValue()));
            sb.append("</ProcessingCommandDelimiter>");
        }
        // Rename
        if (args.isReplacementEnabled()) {
            sb.append("<Rename>");
            sb.append("<ReplaceWhat>").append(cdata(args.getReplacing().getValue())).append("</ReplaceWhat>");
            sb.append("<ReplaceWith>").append(cdata(args.getReplacement().getValue())).append("</ReplaceWith>");
            sb.append("</Rename>");
        }
        // Schema: to remove because not used <ZlibCompression><ZlibCompressionLevel>1</ZlibCompressionLevel></ZlibCompression>
        sb.append("</").append(fragmentPrefix).append("FragmentRef>");
        return sb;
    }

    private static StringBuilder generateProfilePartTargetFileOptions(YADETargetArguments args, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TargetFileOptions>");

        if (args.getAppendFiles().isDirty()) {
            sb.append("<AppendFiles>").append(args.getAppendFiles().getValue()).append("</AppendFiles>");
        }
        if (config.isAtomicEnabled()) {
            sb.append("<Atomicity>");
            if (config.getAtomicPrefix() != null) {
                sb.append("<AtomicPrefix>").append(cdata(config.getAtomicPrefix())).append("</AtomicPrefix>");
            }
            if (config.getAtomicSuffix() != null) {
                sb.append("<AtomicSuffix>").append(cdata(config.getAtomicSuffix())).append("</AtomicSuffix>");
            }
            sb.append("</Atomicity>");
        }
        if (args.getCheckSize().isDirty()) {
            sb.append("<CheckSize>").append(args.getCheckSize().getValue()).append("</CheckSize>");
        }
        if (args.isCumulateFilesEnabled()) {
            sb.append("<CumulateFiles>");
            sb.append("<CumulativeFileSeparator>").append(cdata(args.getCumulativeFileSeparator().getValue())).append("</CumulativeFileSeparator>");
            sb.append("<CumulativeFilename>").append(cdata(args.getCumulativeFileName().getValue())).append("</CumulativeFilename>");
            sb.append("<CumulativeFileDelete>").append(args.getCumulativeFileDelete().getValue()).append("</CumulativeFileDelete>");
            sb.append("</CumulateFiles>");
        }
        if (args.isCompressFilesEnabled()) {
            sb.append("<CompressFiles>");
            sb.append("<CompressedFileExtension>").append(cdata(args.getCompressedFileExtension().getValue())).append("</CompressedFileExtension>");
            sb.append("</CompressFiles>");
        }
        if (args.getCreateIntegrityHashFile().isTrue()) {
            sb.append("<CreateIntegrityHashFile>");
            sb.append("<HashAlgorithm>").append(cdata(args.getIntegrityHashAlgorithm().getValue())).append("</HashAlgorithm>");
            sb.append("</CreateIntegrityHashFile>");
        }
        sb.append("<KeepModificationDate>").append(args.getKeepModificationDate().getValue()).append("</KeepModificationDate>");
        sb.append("<DisableMakeDirectories>").append(getOppositeValue(args.getCreateDirectories())).append("</DisableMakeDirectories>");
        sb.append("<DisableOverwriteFiles>").append(getOppositeValue(args.getOverwriteFiles())).append("</DisableOverwriteFiles>");

        sb.append("</TargetFileOptions>");
        return sb;
    }

    private static StringBuilder generateProfilePartTransferOptions(YADEArguments args, YADESourceTargetArguments sourceTargetArgs,
            JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TransferOptions>");

        sb.append("<Transactional>").append(config.isTransactional()).append("</Transactional>");
        if (args.getBufferSize().isDirty()) {
            sb.append("<BufferSize>").append(args.getBufferSize().getValue()).append("</BufferSize>");
        }
        if (sourceTargetArgs.isRetryOnConnectionErrorEnabled()) {
            sb.append("<RetryOnConnectionError>");
            sb.append("<RetryCountMax>").append(sourceTargetArgs.getConnectionErrorRetryCountMax().getValue()).append("</RetryCountMax>");
            sb.append("<RetryInterval>").append(cdata(sourceTargetArgs.getConnectionErrorRetryInterval().getValue())).append("</RetryInterval>");
            sb.append("</RetryOnConnectionError>");
        }

        sb.append("</TransferOptions>");
        return sb;
    }

    private static String getFragmentNamePrefix(SOSArgument<Protocol> protocol) {
        switch (protocol.getValue()) {
        case LOCAL:
            return "Local";
        case WEBDAV:
        case WEBDAVS:
            return "WebDAV";
        default:
            return protocol.getValue().name();
        }
    }

    private static String generateJumpAttribute() {
        return YADEXMLArgumentsLoader.INTERNAL_ATTRIBUTE_LABEL + "=" + attrValue(YADEJumpHostArguments.LABEL);
    }

    private static String attrValue(String val) {
        return "\"" + val + "\"";
    }

    private static String cdata(String val) {
        return "<![CDATA[" + val + "]]>";
    }

    private static boolean getOppositeValue(SOSArgument<Boolean> arg) {
        return !arg.isTrue();
    }

}
