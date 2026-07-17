package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
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
import com.sos.commons.xml.transform.SOSXmlTransformer;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon.JumpHostConfig;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;

/** TODO Not supported for SFTPFragment: <ZlibCompression> <ZlibCompressionLevel>1</ZlibCompressionLevel> </ZlibCompression> */
/** TODO MailServer */
public class YADEXMLJumpHostSettingsWriter {

    private static final String PROTOCOL_FRAGMENT_NAME = "yade_internal_fragment";
    private static final String CS_FRAGMENT_NAME = "yade_internal_cs";
    private static final String DECRYPTION_FRAGMENT_NAME = "yade_internal_decryption";

    private static final String CS_FRAGMENT_REF = "<CredentialStoreFragmentRef ref=\"" + CS_FRAGMENT_NAME + "\"/>";
    private static final String DECRYPTION_FRAGMENT_REF = "<DecryptionFragmentRef ref=\"" + DECRYPTION_FRAGMENT_NAME + "\"/>";

    // -------- SOURCE_TO_JUMP_HOST XML settings -------------------
    /** COPY/MOVE operations */
    public static String sourceToJumpHostCOPY(ISOSLogger logger, AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(logger, argsLoader.getJumpHostArgs(), sourceArgs);
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Copy", true);
        return generateConfiguration(fragments, profile).toString();
    }

    /** GETLIST operation */
    public static String sourceToJumpHostGETLIST(ISOSLogger logger, AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(logger, argsLoader.getJumpHostArgs(), sourceArgs);
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "GetList", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** REMOVE operation */
    public static String sourceToJumpHostREMOVE(ISOSLogger logger, AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(logger, argsLoader.getJumpHostArgs(), sourceArgs);
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Remove", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** additional configuration for a MOVE operation - removing the source files after successful transfer */
    public static String sourceToJumpHostMOVERemove(ISOSLogger logger, AYADEArgumentsLoader argsLoader, JumpHostConfig config, String profileId) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments(logger, argsLoader.getJumpHostArgs(), sourceArgs);
        StringBuilder profile = generateProfileSourceToJumpHostMOVERemove(argsLoader.getArgs(), sourceArgs, config, profileId);
        return generateConfiguration(fragments, profile).toString();
    }

    // -------- JUMP_HOST_TO_TARGET -------------------
    /** COPY/MOVE operations<br/>
     * 
     * @apiNote GETLIST and REMOVE operations are ignored because they are performed for the Source(Any Provider) and not require a Jump Host */
    public static String jumpHostToTargetCOPY(ISOSLogger logger, AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADETargetArguments targetArgs = argsLoader.getTargetArgs();

        StringBuilder fragments = generateFragments(logger, argsLoader.getJumpHostArgs(), targetArgs);
        StringBuilder profile = generateProfileJumpHostToTargetCOPY(argsLoader.getArgs(), argsLoader.getSourceArgs(), targetArgs, config);
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

    private static StringBuilder generateFragments(ISOSLogger logger, YADEJumpHostArguments jumpHostArgs, YADESourceTargetArguments args) {
        StringBuilder sb = new StringBuilder();

        try {
            AProviderArguments providerArgs = args.getProvider();

            boolean generateCSRef = false;
            if (providerArgs.getCredentialStore() != null && providerArgs.getCredentialStore().getFile().isDirty()) {
                generateCSRef = true;
            }
            boolean generateDecryptionRef = false;
            if (providerArgs.getEncryptionDecrypt() != null && providerArgs.getEncryptionDecrypt().getPrivateKeyPath().isDirty()) {
                generateDecryptionRef = true;
            }

            sb.append("<ProtocolFragments>");

            List<String[]> protocolFragmentAlternatives = null;
            if (providerArgs.hasAlternatives()) {
                protocolFragmentAlternatives = new ArrayList<>();
                for (AProviderArguments a : providerArgs.getAlternatives()) {
                    String[] key = YADEArgumentsHelper.parseFragmentKey(a);
                    if (key == null) {
                        continue;
                    }
                    Node fragment = jumpHostArgs.getConfiguredProtocolFragment(a.getKey().getValue());
                    if (fragment == null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[generateFragments][getConfiguredProtocolFragment=%s]not found", a.getKey().getValue());
                        }
                        continue;
                    }
                    try {
                        sb.append(SOSXmlTransformer.nodeToString(fragment));
                        protocolFragmentAlternatives.add(key);
                    } catch (Exception e) {
                        logger.warn("[generateFragments][configure protocol fragment alternative=%s failed]%s", a.getKey().getValue(), e.toString());
                    }
                }
            }

            switch (providerArgs.getProtocol().getValue()) {
            case SFTP:
                sb.append(generateProtocolFragmentSFTP((SSHProviderArguments) providerArgs, generateCSRef, generateDecryptionRef,
                        protocolFragmentAlternatives));
                break;
            case FTP:
                sb.append(generateProtocolFragmentFTP((FTPProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, false,
                        protocolFragmentAlternatives));
                break;
            case FTPS:
                sb.append(generateProtocolFragmentFTP((FTPSProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, true,
                        protocolFragmentAlternatives));
                break;
            case HTTP:
                sb.append(generateProtocolFragmentHTTP((HTTPProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, false,
                        protocolFragmentAlternatives));
                break;
            case HTTPS:
                sb.append(generateProtocolFragmentHTTP((HTTPSProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, true,
                        protocolFragmentAlternatives));
                break;
            case WEBDAV:
                sb.append(generateProtocolFragmentWEBDAV((WebDAVProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, false,
                        protocolFragmentAlternatives));
                break;
            case WEBDAVS:
                sb.append(generateProtocolFragmentWEBDAV((WebDAVProviderArguments) providerArgs, generateCSRef, generateDecryptionRef, true,
                        protocolFragmentAlternatives));
                break;
            case SMB:
                sb.append(generateProtocolFragmentSMB((SMBProviderArguments) providerArgs, generateCSRef, generateDecryptionRef,
                        protocolFragmentAlternatives));
                break;
            case LOCAL:
            case SSH:
            case UNKNOWN:
            default:
                sb.append("</ProtocolFragments>");
                return sb;
            }
            sb.append("</ProtocolFragments>");

            Map<String, Node> configuredCsFragments = jumpHostArgs.getConfiguredCsFragments();
            if (generateCSRef || configuredCsFragments != null) {
                /** CredentialStore Fragment */
                sb.append("<CredentialStoreFragments>");

                if (configuredCsFragments != null) {
                    for (Map.Entry<String, Node> entry : configuredCsFragments.entrySet()) {
                        try {
                            sb.append(SOSXmlTransformer.nodeToString(entry.getValue()));
                        } catch (Exception e) {
                            logger.warn("[generateFragments][configure credential store fragment=%s failed]%s", entry.getKey(), e.toString());
                        }
                    }
                }

                if (generateCSRef) {
                    sb.append("<CredentialStoreFragment name=\"" + CS_FRAGMENT_NAME + "\">");
                    sb.append("<CSFile>").append(cdata(providerArgs.getCredentialStore().getFile().getValue())).append("</CSFile>");
                    sb.append("<CSAuthentication>");
                    if (providerArgs.getCredentialStore().getKeyFile().isDirty()) {
                        sb.append("<KeyFileAuthentication>");
                        sb.append("<CSKeyFile>").append(cdata(providerArgs.getCredentialStore().getKeyFile().getValue())).append("</CSKeyFile>");
                        if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                            sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append(
                                    "</CSPassword>");
                        }
                        sb.append("</KeyFileAuthentication>");
                    } else if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                        sb.append("<PasswordAuthentication>");
                        sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append("</CSPassword>");
                        sb.append("</PasswordAuthentication>");
                    }
                    sb.append("</CSAuthentication>");
                    if (providerArgs.getCredentialStore().getKeePassModule().isDirty()) {
                        sb.append("<CSKeePass>");
                        sb.append("<CSKeePassModule>");
                        sb.append(cdata(providerArgs.getCredentialStore().getKeePassModule().getValue()));
                        sb.append("</CSKeePassModule>");
                        sb.append("</CSKeePass>");
                    }
                    sb.append("</CredentialStoreFragment>");
                }
                sb.append("</CredentialStoreFragments>");
            }
            Map<String, Node> configuredDecryptionFragments = jumpHostArgs.getConfiguredDecryptionFragments();
            if (generateDecryptionRef || configuredDecryptionFragments != null) {
                /** Decryption Fragment */
                sb.append("<DecryptionFragments>");

                if (configuredDecryptionFragments != null) {
                    for (Map.Entry<String, Node> entry : configuredDecryptionFragments.entrySet()) {
                        try {
                            sb.append(SOSXmlTransformer.nodeToString(entry.getValue()));
                        } catch (Exception e) {
                            logger.warn("[generateFragments][configure decryption fragment=%s failed]%s", entry.getKey(), e.toString());
                        }
                    }
                }

                if (generateDecryptionRef) {
                    sb.append("<DecryptionFragment name=\"" + DECRYPTION_FRAGMENT_NAME + "\">");
                    sb.append("<EnciphermentPrivateKey>").append(cdata(providerArgs.getEncryptionDecrypt().getPrivateKeyPath().getValue())).append(
                            "</EnciphermentPrivateKey>");
                    sb.append("</DecryptionFragment>");
                }
                sb.append("</DecryptionFragments>");
            }

        } finally {
            jumpHostArgs.clear();
        }

        return sb;
    }

    private static StringBuilder generateProtocolFragmentSFTP(SSHProviderArguments args, boolean generateCSRef, boolean generateDecryptionRef,
            List<String[]> protocolFragmentAlternatives) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SFTPFragment name=").append(attrValue(PROTOCOL_FRAGMENT_NAME)).append(">");
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
            sb.append(CS_FRAGMENT_REF);
        }
        if (generateDecryptionRef) {
            sb.append(DECRYPTION_FRAGMENT_REF);
        }
        // ProxyForSFTP
        sb.append(generateProtocolFragmentPartProxy(args.getProxy(), "ProxyForSFTP"));
        // Other
        if (args.getStrictHostkeyChecking().isDirty()) {
            sb.append("<StrictHostkeyChecking>").append(args.getStrictHostkeyChecking().getValue()).append("</StrictHostkeyChecking>");
        }
        if (args.getConfigurationFiles().isDirty()) {
            sb.append("<ConfigurationFiles>");
            for (String configurationFile : args.getConfigurationFiles().getValue()) {
                sb.append("<ConfigurationFile>").append(cdata(configurationFile.toString())).append("</ConfigurationFile>");
            }
            sb.append("</ConfigurationFiles>");
        }
        if (args.getDisableAutoDetectShell().isDirty()) { // JS7 - YADE-632
            sb.append("<DisableAutoDetectShell>").append(args.getDisableAutoDetectShell().getValue()).append("</DisableAutoDetectShell>");
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
        sb.append(generateProtocolFragmentAlternatives("SFTPFragmentAlternatives", protocolFragmentAlternatives));

        // YADE 1 - compatibility - end
        sb.append("</SFTPFragment>");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentFTP(FTPProviderArguments args, boolean generateCSRef, boolean generateDecryptionRef,
            boolean isFTPS, List<String[]> protocolFragmentAlternatives) {
        String fragmentElementName = isFTPS ? "FTPSFragment" : "FTPFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(PROTOCOL_FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartBasicConnection(args.getHost(), args.getPort(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAGMENT_REF);
        }
        if (generateDecryptionRef) {
            sb.append(DECRYPTION_FRAGMENT_REF);
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

        sb.append(generateProtocolFragmentAlternatives("FTPFragmentAlternatives", protocolFragmentAlternatives));

        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentHTTP(HTTPProviderArguments args, boolean generateCSRef, boolean generateDecryptionRef,
            boolean isHTTPS, List<String[]> protocolFragmentAlternatives) {
        String fragmentElementName = isHTTPS ? "HTTPSFragment" : "HTTPFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(PROTOCOL_FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartURLConnection(args.getHost(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAGMENT_REF);
        }
        if (generateDecryptionRef) {
            sb.append(DECRYPTION_FRAGMENT_REF);
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

        sb.append(generateProtocolFragmentAlternatives("HTTPFragmentAlternatives", protocolFragmentAlternatives));

        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentWEBDAV(HTTPProviderArguments args, boolean generateCSRef, boolean generateDecryptionRef,
            boolean isWEBDAVS, List<String[]> protocolFragmentAlternatives) {
        String fragmentElementName = isWEBDAVS ? "WebDAVFragment" : "WebDAVFragment";
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(fragmentElementName).append(" name=").append(attrValue(PROTOCOL_FRAGMENT_NAME)).append(">");
        sb.append(generateProtocolFragmentPartURLConnection(args.getHost(), args.getConnectTimeout()));
        sb.append(generateProtocolFragmentPartBasicAuthentication(args.getUser(), args.getPassword()));
        if (generateCSRef) {
            sb.append(CS_FRAGMENT_REF);
        }
        if (generateDecryptionRef) {
            sb.append(DECRYPTION_FRAGMENT_REF);
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

        sb.append(generateProtocolFragmentAlternatives("WebDAVFragmentAlternatives", protocolFragmentAlternatives));

        sb.append("</").append(fragmentElementName).append(">");
        return sb;
    }

    private static StringBuilder generateProtocolFragmentSMB(SMBProviderArguments args, boolean generateCSRef, boolean generateDecryptionRef,
            List<String[]> protocolFragmentAlternatives) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SMBFragment name=").append(attrValue(PROTOCOL_FRAGMENT_NAME)).append(">");
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
            sb.append(CS_FRAGMENT_REF);
        }
        if (generateDecryptionRef) {
            sb.append(DECRYPTION_FRAGMENT_REF);
        }

        // Other
        if (args.getConfigurationFiles().isDirty()) {
            sb.append("<ConfigurationFiles>");
            for (String configurationFile : args.getConfigurationFiles().getValue()) {
                sb.append("<ConfigurationFile>").append(cdata(configurationFile.toString())).append("</ConfigurationFile>");
            }
            sb.append("</ConfigurationFiles>");
        }

        sb.append(generateProtocolFragmentAlternatives("SMBFragmentAlternatives", protocolFragmentAlternatives));

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
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId()));
        sb.append(generateUseJumpInitialSourceTargetConnectionErrorCode(args));
        sb.append(">");
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
            if (!SOSString.isEmpty(sourceArgs.getPolling().getPollTimeoutValue())) {
                sb.append("<PollTimeout>");
                sb.append(sourceArgs.getPolling().getPollTimeoutValue());
                sb.append("</PollTimeout>");
            }
            if (sourceArgs.getPolling().getPollMinFiles().isDirty()) {
                sb.append("<MinFiles>");
                sb.append(sourceArgs.getPolling().getPollMinFiles().getValue());
                sb.append("</MinFiles>");
            }
            if (sourceArgs.getPolling().getWaitForSourceFolder().isTrue()) {
                sb.append("<WaitForSourceFolder>");
                sb.append(sourceArgs.getPolling().getWaitForSourceFolder().getValue());
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

    private static StringBuilder generateProfileSourceToJumpHostMOVERemove(YADEArguments args, YADESourceArguments sourceArgs, JumpHostConfig config,
            String profileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(profileId));
        sb.append(generateUseJumpInitialSourceTargetConnectionErrorCode(args));
        sb.append(">");
        sb.append("<Operation>");
        sb.append("<Remove>");
        sb.append("<RemoveSource>");

        sb.append("<RemoveSourceFragmentRef>");
        sb.append("<");
        sb.append(getFragmentNamePrefix(sourceArgs.getProvider().getProtocol())).append("FragmentRef ref=");
        sb.append(attrValue(PROTOCOL_FRAGMENT_NAME));
        sb.append(" ").append(generateFragmentRefAttribute(sourceArgs.getLabel().getValue()));
        sb.append(" />");
        sb.append("</RemoveSourceFragmentRef>");

        sb.append("<SourceFileOptions>");
        // Source - Selection
        sb.append("<Selection>");
        sb.append("<FileListSelection>");
        sb.append("<FileList>").append(cdata(config.getSourceToJumpHost().getResultSetFile().getJumpHostFile())).append("</FileList>");
        // sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
        sb.append("</FileListSelection>");
        sb.append("</Selection>");
        sb.append("<Directives>");
        sb.append("<DisableErrorOnNoFilesFound>");
        sb.append(getOppositeValue(sourceArgs.getErrorOnNoFilesFound()));
        sb.append("</DisableErrorOnNoFilesFound>");
        sb.append("</Directives>");
        sb.append("</SourceFileOptions>");

        sb.append("</RemoveSource>");
        sb.append("</Remove>");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileJumpHostToTargetCOPY(YADEArguments args, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId()));
        sb.append(generateUseJumpInitialSourceTargetConnectionErrorCode(args));
        sb.append(">");
        sb.append("<Operation>");
        sb.append("<Copy>");

        // Source (Jump)
        sb.append(generateProfilePartJumpHostLocalCopySource(sourceArgs, config));
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

    private static StringBuilder generateProfilePartJumpHostLocalCopySource(YADESourceArguments sourceArgs, JumpHostConfig config) {
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
        sb.append("<Directives>");
        sb.append("<DisableErrorOnNoFilesFound>");
        sb.append(sourceArgs.getErrorOnNoFilesFound().isTrue() ? "false" : "true"); // opposite value
        sb.append("</DisableErrorOnNoFilesFound>");
        sb.append("</Directives>");
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
        sb.append("<").append(fragmentPrefix).append("FragmentRef ref=").append(attrValue(PROTOCOL_FRAGMENT_NAME));
        sb.append(" ").append(generateFragmentRefAttribute(args.getLabel().getValue())).append(" >");
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
        sb.append("<ResumeFiles>").append(args.getResumeFiles().getValue()).append("</ResumeFiles>");
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
        if (args.getRetryOnConnectionError().isEnabled()) {
            sb.append("<RetryOnConnectionError>");
            sb.append("<RetryCountMax>").append(args.getConnectionErrorRetryCountMax().getValue()).append("</RetryCountMax>");
            sb.append("<RetryInterval>").append(cdata(args.getConnectionErrorRetryInterval().getValue())).append("</RetryInterval>");
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

    private static String generateFragmentRefAttribute(String label) {
        return YADEXMLArgumentsLoader.INTERNAL_ATTRIBUTE_LABEL + "=" + attrValue(label);
    }

    private static String generateProtocolFragmentAlternatives(String name, List<String[]> protocolFragmentAlternatives) {
        if (SOSCollection.isEmpty(protocolFragmentAlternatives)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(name).append(">");
        for (String[] key : protocolFragmentAlternatives) {
            sb.append("<").append(key[0]).append("Ref ref=").append(attrValue(key[1])).append(" />");
        }
        sb.append("</").append(name).append(">");
        return sb.toString();
    }

    private static String generateUseJumpInitialSourceTargetConnectionErrorCode(YADEArguments args) {
        if (args.getAlternativeProfile().isEmpty()) {
            return "";
        }
        return " " + YADEXMLProfileHelper.ATTR_NAME_USE_JUMP_INITIAL_SOURCE_TARGET_CONNECTION_ERROR_CODE + "=" + attrValue("true");
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
