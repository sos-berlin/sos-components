package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobStorageClientAuthMethod;
import com.sos.commons.util.keystore.KeyStoreType;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.vfs.azure.commons.AzureBlobStorageProviderArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments.TransferMode;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSSecurityMode;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.smb.commons.SMBAuthMethod;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVSProviderArguments;
import com.sos.commons.xml.SOSXML;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;

public class YADEXMLFragmentsProtocolFragmentHelper {

    // Source/Target?
    private static Set<String> VISITED_ALTERNATIVES = new HashSet<>();

    protected static AzureBlobStorageProviderArguments parseAzureBlobStorage(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref,
            boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "AzureBlobStorage");

        AzureBlobStorageProviderArguments args = new AzureBlobStorageProviderArguments();
        args.applyDefaultIfNullQuietly();
        args.getAuthMethod().setValue(AzureBlobStorageClientAuthMethod.PUBLIC);
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;
                case "AzureBlobStorageConnection":
                    parseAzureBlobStorageConnection(logger, argsLoader, args, n);
                    break;
                case "AzureBlobStorageAuthentication":
                    parseAzureBlobStorageAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForAzure":
                    parseProxy(logger, argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(logger, argsLoader, args, n);
                    break;
                case "SSL": // JS7 - YADE-626
                    parseSSL(logger, argsLoader, args.getSsl(), n);
                    break;
                case "AzureBlobStorageFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static FTPProviderArguments parseFTP(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "FTP");

        FTPProviderArguments args = new FTPProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;
                case "BasicConnection":
                    parseBasicConnection(logger, argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForFTP":
                    parseProxy(logger, argsLoader, args, n);
                    break;
                case "KeepAlive": // YADE JS7 - YADE 626
                    parseFTPKeepAlive(logger, argsLoader, args, n);
                    break;
                case "PassiveMode":
                    argsLoader.setBooleanArgumentValue(args.getPassiveMode(), n);
                    break;
                case "TransferMode":
                    TransferMode transferMode = TransferMode.fromString(argsLoader.getValue(n));
                    if (transferMode != null) {
                        args.getTransferMode().setValue(transferMode);
                    }
                    break;
                case "FTPFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static FTPSProviderArguments parseFTPS(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "FTPS");

        FTPSProviderArguments args = new FTPSProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "FTPSClientSecurity":
                    parseYADE1FTPSClientSecurity(logger, argsLoader, args, n);
                    break;
                case "FTPSProtocol":
                    args.getSsl().getEnabledProtocols().setValue(argsLoader.getValue(n));
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;
                case "BasicConnection":
                    parseBasicConnection(logger, argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForFTPS":
                    parseProxy(logger, argsLoader, args, n);
                    break;
                case "KeepAlive": // YADE JS7 - YADE-626
                    parseFTPKeepAlive(logger, argsLoader, args, n);
                    break;
                case "SecurityMode":// YADE JS7 - YADE-626
                    FTPSSecurityMode securityMode = FTPSSecurityMode.fromString(argsLoader.getValue(n));
                    if (securityMode != null) {
                        args.getSecurityMode().setValue(securityMode);
                    }
                    break;
                case "SSL":// YADE JS7 - YADE-626
                    parseSSL(logger, argsLoader, args.getSsl(), n);
                    break;
                case "FTPFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPProviderArguments parseHTTP(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "HTTP");

        HTTPProviderArguments args = new HTTPProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;
                case "URLConnection":
                    parseURLConnection(logger, argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForHTTP":
                    parseProxy(logger, argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(logger, argsLoader, args, n);
                    break;
                case "HTTPFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPSProviderArguments parseHTTPS(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "HTTPS");

        HTTPSProviderArguments args = new HTTPSProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "AcceptUntrustedCertificate":
                    argsLoader.setBooleanArgumentValue(args.getSsl().getUntrustedSsl(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSsl().getUntrustedSslVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseYADE1KeyStore(logger, argsLoader, args.getSsl(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;
                case "URLConnection":
                    parseURLConnection(logger, argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForHTTP":
                    parseProxy(logger, argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(logger, argsLoader, args, n);
                    break;
                case "SSL": // JS7 - YADE-626
                    parseSSL(logger, argsLoader, args.getSsl(), n);
                    break;
                case "HTTPFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static SSHProviderArguments parseSFTP(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        return parseSFTP(logger, argsLoader, ref, isSource, false, VISITED_ALTERNATIVES);
    }

    protected static SSHProviderArguments parseSFTP(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource, boolean isJump,
            Set<String> visitedAlternatives) throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "SFTP");

        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, visitedAlternatives);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        if (logger.isDebugEnabled()) {
            logger.debug("[parseSFTP][" + args.getKey().getValue() + "][isJump=" + isJump + "][isSource=" + isSource + "]visitedAlternatives="
                    + visitedAlternatives);
        }

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "ServerAliveInterval":
                    argsLoader.setStringArgumentValue(args.getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsLoader.setIntegerArgumentValue(args.getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getSocketTimeout(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;

                case "BasicConnection":
                    parseBasicConnection(logger, argsLoader, args, n);
                    break;
                case "SSHAuthentication":
                    parseSFTPSSHAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForSFTP":
                    parseProxy(logger, argsLoader, args, n);
                    break;

                case "SocketTimeout": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(args.getSocketTimeout(), n);
                    break;
                case "KeepAlive": // JS7 - YADE-626
                    parseSFTPKeepAlive(logger, argsLoader, args, n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(args.getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(logger, argsLoader, args, n);
                    break;
                case "DisableAutoDetectShell": // JS7 - YADE-632
                    argsLoader.setBooleanArgumentValue(args.getDisableAutoDetectShell(), n);
                    break;
                case "SFTPFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, isJump, visitedAlternatives);
                    break;
                }
            }
        }
        return args;
    }

    protected static SMBProviderArguments parseSMB(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "SMB");

        SMBProviderArguments args = new SMBProviderArguments();
        args.applyDefaultIfNullQuietly();
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "Hostname":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    args.tryRedefineHostPort();
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "SMBConnection": // JS7 - YADE-626
                    parseSMBConnection(logger, argsLoader, args, n);
                    break;
                case "SMBAuthentication":
                    parseSMBAuthentication(logger, argsLoader, args, n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(logger, argsLoader, args, n);
                    break;
                case "SMBFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static WebDAVProviderArguments parseWebDAV(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource)
            throws Exception {
        // throws on exception if not found
        Node fragment = getProtocolFragment(logger, argsLoader, ref, isSource, "WebDAV");
        // Node refRef = argsLoader.getXPath().selectNode(ref, "*[1]"); // first child
        Node urlConnectionURL = argsLoader.getXPath().selectNode(fragment, "URLConnection/URL");
        if (urlConnectionURL == null) {
            throw new SOSMissingDataException("[WebDAV]URLConnection/URL");
        }

        String url = argsLoader.getValue(urlConnectionURL);
        String urlLC = url.toLowerCase();
        WebDAVProviderArguments args = urlLC.startsWith("https://") || urlLC.startsWith("webdavs://") ? new WebDAVSProviderArguments()
                : new WebDAVProviderArguments();
        args.applyDefaultIfNullQuietly();
        args.getHost().setValue(url);
        setFragmentKeyFromFragment(fragment, args, VISITED_ALTERNATIVES);
        handleJumpHostProtocolFragments(logger, argsLoader, fragment, args);

        Node urlConnectionConnectTimeout = argsLoader.getXPath().selectNode(fragment, "URLConnection/ConnectTimeout");
        if (urlConnectionConnectTimeout != null) {
            args.getConnectTimeout().setValue(argsLoader.getValue(urlConnectionConnectTimeout));
        }

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "AcceptUntrustedCertificate":
                    argsLoader.setBooleanArgumentValue(args.getSsl().getUntrustedSsl(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSsl().getUntrustedSslVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseYADE1KeyStore(logger, argsLoader, args.getSsl(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(logger, argsLoader, n, isSource);
                    break;

                // URLConnection already set
                case "BasicAuthentication":
                    parseBasicAuthentication(logger, argsLoader, args, n);
                    break;
                case "ProxyForWebDAV":
                    parseProxy(logger, argsLoader, args, n);
                    break;

                case "HTTPHeaders":
                    parseHTTPHeaders(logger, argsLoader, args, n);
                    break;
                case "SSL": // JS7 - YADE-626
                    parseSSL(logger, argsLoader, args.getSsl(), n);
                    break;
                case "WebDAVFragmentAlternatives":
                    parseAlternativeFragments(logger, argsLoader, args, fragment, n, isSource, false, VISITED_ALTERNATIVES);
                    break;
                }
            }
        }
        return args;
    }

    protected static void parseHTTPHeaders(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AzureBlobStorageProviderArguments args, Node headers)
            throws Exception {
        NodeList nl = headers.getChildNodes();
        if (args.getHttpHeaders().getValue() == null) {
            args.getHttpHeaders().setValue(new ArrayList<>());
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HTTPHeader":
                    args.getHttpHeaders().getValue().add(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    protected static void parseHTTPHeaders(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, HTTPProviderArguments args, Node headers)
            throws Exception {
        NodeList nl = headers.getChildNodes();
        if (args.getHttpHeaders().getValue() == null) {
            args.getHttpHeaders().setValue(new ArrayList<>());
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HTTPHeader":
                    args.getHttpHeaders().getValue().add(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    protected static Node getProtocolFragment(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource, String fragmentPrefix)
            throws Exception {
        String exp = "Fragments/ProtocolFragments/" + fragmentPrefix + "Fragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node node = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (node == null) {
            throw new SOSMissingDataException("[profile=" + argsLoader.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target")
                    + "][" + exp + "]referenced Protocol fragment not found");
        }
        return node;
    }

    protected static void parseBasicConnection(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node basicConnection)
            throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    argsLoader.setIntegerArgumentValue(args.getPort(), n);
                    break;
                case "ConnectTimeout":// YADE JS7
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                }
            }
        }
    }

    protected static void parseConfigurationFiles(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args,
            Node configurationFiles) {
        List<String> files = new ArrayList<>();

        NodeList nl = configurationFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "ConfigurationFile".equals(n.getNodeName())) {
                files.add(argsLoader.getValue(n));
            }
        }

        if (files.size() > 0) {
            args.getConfigurationFiles().setValue(files);
        }
    }

    protected static void parseProxy(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            ProxyConfigArguments proxyArgs = new ProxyConfigArguments();
            proxyArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    // YADE 1 -compatibility
                    case "SOCKS4Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(logger, argsLoader, proxyArgs, n);
                        break;
                    case "SOCKS5Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(logger, argsLoader, proxyArgs, n);
                        break;

                    // YADE JS7
                    case "HTTPProxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
                        parseProxy(logger, argsLoader, proxyArgs, n);
                        break;
                    case "SOCKSProxy": // YADE JS7 - YADE-626
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(logger, argsLoader, proxyArgs, n);
                        break;

                    }
                }
            }
            args.setProxy(proxyArgs);
        }
    }

    protected static void parseSFTPSSHAuthentication(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args,
            Node sshAuthentication) {
        NodeList nl = sshAuthentication.getChildNodes();

        SSHAuthMethod authMethod = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "AuthenticationMethodPassword":
                    parseSFTPSSHAuthenticationMethodPassword(logger, argsLoader, args, n);
                    authMethod = SSHAuthMethod.PASSWORD;
                    break;
                case "AuthenticationMethodPublickey":
                    parseSFTPSSHAuthenticationMethodPublickey(logger, argsLoader, args, n);
                    authMethod = SSHAuthMethod.PUBLICKEY;
                    break;
                case "AuthenticationMethodKeyboardInteractive":
                    // ignore - not argsLoaderemented yet
                    authMethod = SSHAuthMethod.KEYBOARD_INTERACTIVE;
                    break;
                case "PreferredAuthentications":
                    args.getPreferredAuthentications().setValue(SSHAuthMethod.fromString(argsLoader.getValue(n)));
                    break;
                case "RequiredAuthentications":
                    args.getRequiredAuthentications().setValue(SSHAuthMethod.fromString(argsLoader.getValue(n)));
                    break;
                }
            }
        }
        args.getAuthMethod().setValue(authMethod);
    }

    private static void parseAlternativeFragments(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node fragment,
            Node alternativeFragmentRef, boolean isSource, boolean isJump, Set<String> visitedAlternatives) throws Exception {

        NodeList nl = alternativeFragmentRef.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node ref = nl.item(i);
            if (ref.getNodeType() == Node.ELEMENT_NODE) {
                String refNodeName = ref.getNodeName();
                String refId = getFragmentKeyFromRef(refNodeName, ref);

                if (args.keyEquals(refId)) {
                    continue;
                }
                if (!visitedAlternatives.add(refId)) {
                    continue;
                }

                AProviderArguments alternative = null;
                switch (refNodeName) {
                case "AzureBlobStorageFragmentRef":
                    alternative = parseAzureBlobStorage(logger, argsLoader, ref, isSource);
                    break;
                case "FTPFragmentRef":
                    alternative = parseFTP(logger, argsLoader, ref, isSource);
                    break;
                case "FTPSFragmentRef":
                    alternative = parseFTPS(logger, argsLoader, ref, isSource);
                    break;
                case "HTTPFragmentRef":
                    alternative = parseHTTP(logger, argsLoader, ref, isSource);
                    break;
                case "HTTPSFragmentRef":
                    alternative = parseHTTPS(logger, argsLoader, ref, isSource);
                    break;
                case "SFTPFragmentRef":
                    alternative = parseSFTP(logger, argsLoader, ref, isSource, isJump, visitedAlternatives);
                    break;
                case "SMBFragmentRef":
                    alternative = parseSMB(logger, argsLoader, ref, isSource);
                    break;
                case "WebDAVFragmentRef":
                    alternative = parseWebDAV(logger, argsLoader, ref, isSource);
                    break;
                }

                if (alternative != null) {
                    alternative.getKey().setValue(refId);
                    args.mergeNestedAlternatives(alternative);
                }
            }
        }
    }

    private static void parseBasicAuthentication(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args,
            Node basicAuthentication) throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseAzureBlobStorageConnection(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AzureBlobStorageProviderArguments args,
            Node urlConnection) throws Exception {
        NodeList nl = urlConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "ServiceEndpoint":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    args.getServiceEndpoint().setValue(args.getHost().getValue());
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
    }

    private static void parseAzureBlobStorageAuthentication(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader,
            AzureBlobStorageProviderArguments args, Node basicAuthentication) throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AccountName":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "ContainerName":
                    argsLoader.setStringArgumentValue(args.getContainerName(), n);
                    break;
                case "ApiVersion":
                    argsLoader.setStringArgumentValue(args.getApiVersion(), n);
                    break;
                case "SharedKey":
                    parseAzureSharedKey(logger, argsLoader, args, n);
                    break;
                case "SASToken":
                    parseAzureSASToken(logger, argsLoader, args, n);
                    break;
                }
            }
        }
    }

    private static void parseAzureSharedKey(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AzureBlobStorageProviderArguments args,
            Node sharedKey) throws Exception {
        args.getAuthMethod().setValue(AzureBlobStorageClientAuthMethod.SHARED_KEY);
        NodeList nl = sharedKey.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AccountKey":
                    argsLoader.setStringArgumentValue(args.getAccountKey(), n);
                    break;
                }
            }
        }
    }

    private static void parseAzureSASToken(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AzureBlobStorageProviderArguments args,
            Node sasToken) throws Exception {
        args.getAuthMethod().setValue(AzureBlobStorageClientAuthMethod.SAS_TOKEN);
        NodeList nl = sasToken.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AccountKey":
                    argsLoader.setStringArgumentValue(args.getAccountKey(), n);
                    break;
                case "Token":
                    argsLoader.setStringArgumentValue(args.getSASToken(), n);
                    break;
                }
            }
        }
    }

    private static void parseURLConnection(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node urlConnection)
            throws Exception {
        NodeList nl = urlConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URL":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    break;
                case "ConnectTimeout":// YADE JS7
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
    }

    // JS7 - YADE-626
    private static void parseSSL(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node ssl) throws Exception {
        if (args == null) {
            return;
        }
        NodeList nl = ssl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustedSSL":
                    parseTrustedSSL(logger, argsLoader, args, n);
                    break;
                case "UntrustedSSL":
                    parseUntrustedSSL(logger, argsLoader, args, n);
                    break;
                case "EnabledProtocols":
                    argsLoader.setStringArgumentValue(args.getEnabledProtocols(), n);
                    break;
                }
            }
        }
    }

    // JS7 - YADE-626
    private static void parseTrustedSSL(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node trustedSSL) throws Exception {
        NodeList nl = trustedSSL.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustStore":
                    parseTrustedSSLTrustStore(logger, argsLoader, args, n);
                    break;
                case "KeyStore":
                    parseTrustedSSLKeyStore(logger, argsLoader, args, n);
                    break;
                }
            }
        }
    }

    // JS7 - YADE-626
    private static void parseTrustedSSLTrustStore(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node trustStore) {
        NodeList nl = trustStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustStoreType":
                    KeyStoreType keyStoreType = KeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSsl().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "TrustStoreFile":
                    args.getTrustedSsl().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "TrustStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSsl().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

    // sets KeyStore arguments
    private static void parseTrustedSSLKeyStore(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    KeyStoreType keyStoreType = KeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSsl().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getTrustedSsl().getKeyStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSsl().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }

    // YADE 1 - compatibility - !!! sets TrustStore arguments
    private static void parseYADE1KeyStore(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    KeyStoreType keyStoreType = KeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSsl().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getTrustedSsl().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSsl().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseUntrustedSSL(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SslArguments args, Node untrustedSSL)
            throws Exception {
        args.getUntrustedSsl().setValue(true);

        NodeList nl = untrustedSSL.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getUntrustedSslVerifyCertificateHostname(), n);
                    break;
                }
            }
        }
    }

    private static void parseProxy(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, ProxyConfigArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseProxyBasicConnection(logger, argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseProxyBasicAuthentication(logger, argsLoader, args, n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicConnection(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, ProxyConfigArguments args,
            Node basicConnection) throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    argsLoader.setIntegerArgumentValue(args.getPort(), n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicAuthentication(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, ProxyConfigArguments args,
            Node basicAuthentication) throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    /** YADE JS7 */
    private static void parseFTPKeepAlive(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, FTPProviderArguments args, Node keepAlive) {
        NodeList nl = keepAlive.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeepAliveTimeout":
                    argsLoader.setStringArgumentValue(args.getKeepAliveTimeout(), n);
                    break;
                }
            }
        }
    }

    /** YADE JS7 */
    public static void parseSFTPKeepAlive(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args, Node keepAlive) {
        NodeList nl = keepAlive.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeepAliveInterval":
                    argsLoader.setStringArgumentValue(args.getServerAliveInterval(), n);
                    break;
                case "MaxAliveCount":
                    argsLoader.setIntegerArgumentValue(args.getServerAliveCountMax(), n);
                    break;
                }
            }
        }
    }

    public static void clear() {
        VISITED_ALTERNATIVES.clear();
        YADEXMLFragmentsProtocolFragmentJumpHelper.clear();
    }

    private static void parseSFTPSSHAuthenticationMethodPassword(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args,
            Node methodPassword) {
        NodeList nl = methodPassword.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseSFTPSSHAuthenticationMethodPublickey(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args,
            Node methodPublickey) {
        NodeList nl = methodPublickey.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AuthenticationFile":
                    argsLoader.setStringArgumentValue(args.getAuthFile(), n);
                    break;
                case "Passphrase":
                    argsLoader.setStringArgumentValue(args.getPassphrase(), n);
                    break;
                }
            }
        }
    }

    private static void parseYADE1FTPSClientSecurity(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, FTPSProviderArguments args,
            Node clientSecurity) {
        NodeList nl = clientSecurity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SecurityMode":
                    FTPSSecurityMode securityMode = FTPSSecurityMode.fromString(argsLoader.getValue(n));
                    if (securityMode != null) {
                        args.getSecurityMode().setValue(securityMode);
                    }
                    break;
                case "KeyStoreType":
                    KeyStoreType keyStoreType = KeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getSsl().getTrustedSsl().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getSsl().getTrustedSsl().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getSsl().getTrustedSsl().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

    // YADE JS7
    private static void parseSMBConnection(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node smbAuthentication) {
        NodeList nl = smbAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    argsLoader.setIntegerArgumentValue(args.getPort(), n);
                    break;
                case "Sharename":
                    argsLoader.setStringArgumentValue(args.getShareName(), n);
                    break;
                }
            }
        }
    }

    private static void parseSMBAuthentication(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args,
            Node smbAuthentication) {
        NodeList nl = smbAuthentication.getChildNodes();
        boolean account = false;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    account = true;
                    break;
                case "Domain":
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;

                // YADE JS7
                case "SMBAuthenticationMethodAnonymous":
                    args.getAuthMethod().setValue(SMBAuthMethod.ANONYMOUS);
                    break;
                case "SMBAuthenticationMethodGuest":
                    parseSMBAuthenticationMethodGuest(logger, argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodNTLM":
                    parseSMBAuthenticationMethodNTLM(logger, argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodKerberos":
                    parseSMBAuthenticationMethodKerberos(logger, argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodSPNEGO":
                    parseSMBAuthenticationMethodSPNEGO(logger, argsLoader, args, n);
                    break;
                }
            }
        }
        if (account) {// YADE1 -> JS7
            if (args.getPassword().isEmpty() && args.getDomain().isEmpty()) {
                if (SMBAuthMethod.ANONYMOUS.name().equalsIgnoreCase(args.getUser().getValue())) {
                    args.getAuthMethod().setValue(SMBAuthMethod.ANONYMOUS);
                } else {
                    args.getAuthMethod().setValue(SMBAuthMethod.GUEST);
                }
            } else {
                args.getAuthMethod().setValue(SMBAuthMethod.NTLM); // default
            }
        }
    }

    private static void parseSMBAuthenticationMethodGuest(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args,
            Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Domain":
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                }
            }
        }
        if (args.getUser().isEmpty()) {
            args.getUser().setValue("guest");
        }
        args.getAuthMethod().setValue(SMBAuthMethod.GUEST);
    }

    private static void parseSMBAuthenticationMethodNTLM(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                case "Domain":
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                }
            }
        }
        args.getAuthMethod().setValue(SMBAuthMethod.NTLM);
    }

    private static void parseSMBAuthenticationMethodKerberos(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args,
            Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                case "Domain":
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "LoginContextName":
                    argsLoader.setStringArgumentValue(args.getLoginContextName(), n);
                    break;
                }
            }
        }
        args.getAuthMethod().setValue(SMBAuthMethod.KERBEROS);
    }

    private static void parseSMBAuthenticationMethodSPNEGO(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args,
            Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
                case "Domain":
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "LoginContextName":
                    argsLoader.setStringArgumentValue(args.getLoginContextName(), n);
                    break;
                }
            }
        }
        args.getAuthMethod().setValue(SMBAuthMethod.SPNEGO);
    }

    protected static String getLocalFragmentKey() {
        return "Local";
    }

    private static void setFragmentKeyFromFragment(Node fragment, AProviderArguments args, Set<String> visitedAlternatives) {
        YADEArgumentsHelper.setFragmentKey(args, fragment.getNodeName(), SOSXML.getAttributeValue(fragment, "name"));
        visitedAlternatives.add(args.getKey().getValue());
    }

    protected static String getFragmentKeyFromRef(String refNodeName, Node ref) {
        if (refNodeName.endsWith("Ref")) {
            return YADEArgumentsHelper.getFragmentKey(refNodeName.substring(0, refNodeName.length() - 3), SOSXML.getAttributeValue(ref, "ref"));
        }
        return getLocalFragmentKey();
    }

    private static void handleJumpHostProtocolFragments(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node fragment,
            AProviderArguments args) {
        if (argsLoader.getJumpHostArgs() == null) {
            return;
        }
        argsLoader.getJumpHostArgs().addConfiguredProtocolFragment(fragment, args);
    }

}
