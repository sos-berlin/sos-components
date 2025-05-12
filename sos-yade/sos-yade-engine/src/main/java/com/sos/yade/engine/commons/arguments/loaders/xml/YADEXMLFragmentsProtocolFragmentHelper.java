package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.arguments.impl.JavaKeyStoreType;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.util.arguments.impl.SSLArguments;
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

public class YADEXMLFragmentsProtocolFragmentHelper {

    protected static FTPProviderArguments parseFTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "FTP");

        FTPProviderArguments args = new FTPProviderArguments();
        args.applyDefaultIfNullQuietly();

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
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;
                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForFTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "KeepAlive": // YADE JS7 - YADE 626
                    parseFTPKeepAlive(argsLoader, args, n);
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
                }
            }
        }
        return args;
    }

    protected static FTPSProviderArguments parseFTPS(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "FTPS");

        FTPSProviderArguments args = new FTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "FTPSClientSecurity":
                    parseYADE1FTPSClientSecurity(argsLoader, args, n);
                    break;
                case "FTPSProtocol":
                    args.getSSL().getEnabledProtocols().setValue(argsLoader.getValue(n));
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;
                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForFTPS":
                    parseProxy(argsLoader, args, n);
                    break;
                case "KeepAlive": // YADE JS7 - YADE-626
                    parseFTPKeepAlive(argsLoader, args, n);
                    break;
                case "SecurityMode":// YADE JS7 - YADE-626
                    FTPSSecurityMode securityMode = FTPSSecurityMode.fromString(argsLoader.getValue(n));
                    if (securityMode != null) {
                        args.getSecurityMode().setValue(securityMode);
                    }
                    break;
                case "SSL":// YADE JS7 - YADE-626
                    parseSSL(argsLoader, args.getSSL(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPProviderArguments parseHTTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "HTTP");

        HTTPProviderArguments args = new HTTPProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;
                case "URLConnection":
                    parseURLConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForHTTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsLoader, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPSProviderArguments parseHTTPS(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "HTTPS");

        HTTPSProviderArguments args = new HTTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE 1 - compatibility
                case "AcceptUntrustedCertificate":
                    argsLoader.setBooleanArgumentValue(args.getSSL().getUntrustedSSL(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSSL().getUntrustedSSLVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseYADE1KeyStore(argsLoader, args.getSSL(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;
                case "URLConnection":
                    parseURLConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForHTTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsLoader, args, n);
                    break;
                case "SSL": // JS7 - YADE-626
                    parseSSL(argsLoader, args.getSSL(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SSHProviderArguments parseSFTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "SFTP");

        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultIfNullQuietly();

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
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;

                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "SSHAuthentication":
                    parseSFTPSSHAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForSFTP":
                    parseProxy(argsLoader, args, n);
                    break;

                case "SocketTimeout": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(args.getSocketTimeout(), n);
                    break;
                case "KeepAlive": // JS7 - YADE-626
                    parseSFTPKeepAlive(argsLoader, args, n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(args.getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(argsLoader, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SMBProviderArguments parseSMB(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "SMB");

        SMBProviderArguments args = new SMBProviderArguments();
        args.applyDefaultIfNullQuietly();

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
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "SMBConnection": // JS7 - YADE-626
                    parseSMBConnection(argsLoader, args, n);
                    break;
                case "SMBAuthentication":
                    parseSMBAuthentication(argsLoader, args, n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(argsLoader, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static WebDAVProviderArguments parseWebDAV(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        // throws on exception if not found
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "WebDAV");
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
                    argsLoader.setBooleanArgumentValue(args.getSSL().getUntrustedSSL(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSSL().getUntrustedSSLVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseYADE1KeyStore(argsLoader, args.getSSL(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;

                // URLConnection already set
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "ProxyForWebDAV":
                    parseProxy(argsLoader, args, n);
                    break;

                case "HTTPHeaders":
                    parseHTTPHeaders(argsLoader, args, n);
                    break;
                case "SSL": // JS7 - YADE-626
                    parseSSL(argsLoader, args.getSSL(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static void parseHTTPHeaders(YADEXMLArgumentsLoader argsLoader, HTTPProviderArguments args, Node headers) throws Exception {
        NodeList nl = headers.getChildNodes();
        args.getHTTPHeaders().setValue(new ArrayList<>());
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HTTPHeader":
                    args.getHTTPHeaders().getValue().add(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    protected static Node getProtocolFragment(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource, String fragmentPrefix) throws Exception {
        String exp = "Fragments/ProtocolFragments/" + fragmentPrefix + "Fragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node node = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (node == null) {
            throw new SOSMissingDataException("[profile=" + argsLoader.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target")
                    + "][" + exp + "]referenced Protocol fragment not found");
        }
        return node;
    }

    protected static void parseBasicConnection(YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node basicConnection) throws Exception {
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

    protected static void parseConfigurationFiles(YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node configurationFiles) {
        List<Path> files = new ArrayList<>();

        NodeList nl = configurationFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "ConfigurationFile".equals(n.getNodeName())) {
                files.add(Path.of(argsLoader.getValue(n)));
            }
        }

        if (files.size() > 0) {
            args.getConfigurationFiles().setValue(files);
        }
    }

    protected static void parseProxy(YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            ProxyArguments proxyArgs = new ProxyArguments();
            proxyArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    // YADE 1 -compatibility
                    case "SOCKS4Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;
                    case "SOCKS5Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;

                    // YADE JS7
                    case "HTTPProxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;
                    case "SOCKSProxy": // YADE JS7 - YADE-626
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;

                    }
                }
            }
            args.setProxy(proxyArgs);
        }
    }

    protected static void parseSFTPSSHAuthentication(YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args, Node sshAuthentication) {
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
                    parseSFTPSSHAuthenticationMethodPassword(argsLoader, args, n);
                    authMethod = SSHAuthMethod.PASSWORD;
                    break;
                case "AuthenticationMethodPublickey":
                    parseSFTPSSHAuthenticationMethodPublickey(argsLoader, args, n);
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

    private static void parseBasicAuthentication(YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node basicAuthentication)
            throws Exception {
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

    private static void parseURLConnection(YADEXMLArgumentsLoader argsLoader, AProviderArguments args, Node urlConnection) throws Exception {
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
    private static void parseSSL(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node ssl) throws Exception {
        if (args == null) {
            return;
        }
        NodeList nl = ssl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustedSSL":
                    parseTrustedSSL(argsLoader, args, n);
                    break;
                case "UntrustedSSL":
                    parseUntrustedSSL(argsLoader, args, n);
                    break;
                case "EnabledProtocols":
                    argsLoader.setStringArgumentValue(args.getEnabledProtocols(), n);
                    break;
                }
            }
        }
    }

    // JS7 - YADE-626
    private static void parseTrustedSSL(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node trustedSSL) throws Exception {
        NodeList nl = trustedSSL.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustStore":
                    parseTrustedSSLTrustStore(argsLoader, args, n);
                    break;
                case "KeyStore":
                    parseTrustedSSLKeyStore(argsLoader, args, n);
                    break;
                }
            }
        }
    }

    // JS7 - YADE-626
    private static void parseTrustedSSLTrustStore(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node trustStore) {
        NodeList nl = trustStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "TrustStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSSL().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "TrustStoreFile":
                    args.getTrustedSSL().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "TrusStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSSL().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseTrustedSSLKeyStore(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSSL().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getTrustedSSL().getKeyStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSSL().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseUntrustedSSL(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node untrustedSSL) throws Exception {
        args.getUntrustedSSL().setValue(true);

        NodeList nl = untrustedSSL.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getUntrustedSSLVerifyCertificateHostname(), n);
                    break;
                }
            }
        }
    }

    private static void parseProxy(YADEXMLArgumentsLoader argsLoader, ProxyArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseProxyBasicConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseProxyBasicAuthentication(argsLoader, args, n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicConnection(YADEXMLArgumentsLoader argsLoader, ProxyArguments args, Node basicConnection) throws Exception {
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

    private static void parseProxyBasicAuthentication(YADEXMLArgumentsLoader argsLoader, ProxyArguments args, Node basicAuthentication)
            throws Exception {
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
    private static void parseFTPKeepAlive(YADEXMLArgumentsLoader argsLoader, FTPProviderArguments args, Node keepAlive) {
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
    public static void parseSFTPKeepAlive(YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args, Node keepAlive) {
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

    private static void parseSFTPSSHAuthenticationMethodPassword(YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args, Node methodPassword) {
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

    private static void parseSFTPSSHAuthenticationMethodPublickey(YADEXMLArgumentsLoader argsLoader, SSHProviderArguments args,
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

    private static void parseYADE1FTPSClientSecurity(YADEXMLArgumentsLoader argsLoader, FTPSProviderArguments args, Node clientSecurity) {
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
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getSSL().getTrustedSSL().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getSSL().getTrustedSSL().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getSSL().getTrustedSSL().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

    // YADE JS7
    private static void parseSMBConnection(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node smbAuthentication) {
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

    private static void parseSMBAuthentication(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node smbAuthentication) {
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
                    parseSMBAuthenticationMethodGuest(argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodNTLM":
                    parseSMBAuthenticationMethodNTLM(argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodKerberos":
                    parseSMBAuthenticationMethodKerberos(argsLoader, args, n);
                    break;
                case "SMBAuthenticationMethodSPNEGO":
                    parseSMBAuthenticationMethodSPNEGO(argsLoader, args, n);
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

    private static void parseSMBAuthenticationMethodGuest(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node node) {
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

    private static void parseSMBAuthenticationMethodNTLM(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node node) {
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

    private static void parseSMBAuthenticationMethodKerberos(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node node) {
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

    private static void parseSMBAuthenticationMethodSPNEGO(YADEXMLArgumentsLoader argsLoader, SMBProviderArguments args, Node node) {
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

    // YADE 1 - compatibility
    private static void parseYADE1KeyStore(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getTrustedSSL().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getTrustedSSL().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getTrustedSSL().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }
}
