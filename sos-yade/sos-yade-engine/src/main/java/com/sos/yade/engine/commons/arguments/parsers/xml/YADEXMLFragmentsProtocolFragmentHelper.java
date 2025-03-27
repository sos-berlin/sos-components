package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.arguments.impl.JavaKeyStoreType;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSSecurityMode;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVSProviderArguments;
import com.sos.commons.xml.SOSXML;

public class YADEXMLFragmentsProtocolFragmentHelper {

    protected static FTPProviderArguments parseFTP(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "FTP");

        FTPProviderArguments args = new FTPProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(impl, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ProxyForFTP":
                    parseProxy(impl, args, n);
                    break;
                case "ConnectTimeout":
                    impl.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static FTPSProviderArguments parseFTPS(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "FTPS");

        FTPSProviderArguments args = new FTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(impl, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(impl, args, n);
                    break;
                case "FTPSClientSecurity":
                    parseFTPSClientSecurity(impl, args, n);
                    break;
                case "FTPSProtocol":
                    args.getSSL().getProtocols().setValue(Arrays.asList(impl.getValue(n)));
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ProxyForFTPS":
                    parseProxy(impl, args, n);
                    break;
                case "ConnectTimeout":
                    impl.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPProviderArguments parseHTTP(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "HTTP");

        HTTPProviderArguments args = new HTTPProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URLConnection":
                    parseURLConnection(impl, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ProxyForHTTP":
                    parseProxy(impl, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(impl, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPSProviderArguments parseHTTPS(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "HTTPS");

        HTTPSProviderArguments args = new HTTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URLConnection":
                    parseURLConnection(impl, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ProxyForHTTP":
                    parseProxy(impl, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(impl, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    impl.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    impl.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(impl, args.getSSL(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SSHProviderArguments parseSFTP(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "SFTP");

        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(impl, args, n);
                    break;
                case "SSHAuthentication":
                    parseSFTPSSHAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(impl, ref, isSource);
                    break;
                case "ProxyForSFTP":
                    parseProxy(impl, args, n);
                    break;
                case "StrictHostkeyChecking":
                    impl.setBooleanArgumentValue(args.getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(impl, args, n);
                    break;
                case "ServerAliveInterval":
                    impl.setStringArgumentValue(args.getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    impl.setIntegerArgumentValue(args.getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    impl.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    impl.setStringArgumentValue(args.getSocketTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SMBProviderArguments parseSMB(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "SMB");

        SMBProviderArguments args = new SMBProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    impl.setStringArgumentValue(args.getHost(), n);
                    break;
                case "SMBAuthentication":
                    parseSMBAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(impl, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static WebDAVProviderArguments parseWebDAV(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(impl, ref, isSource, "WebDAV");
        String url = impl.getValue(impl.getXPath().selectNode(fragment, "URLConnection/URL"));
        if (url == null) {
            return null;
        }

        WebDAVProviderArguments args = url.toLowerCase().startsWith("webdavs://") ? new WebDAVSProviderArguments() : new WebDAVProviderArguments();
        args.applyDefaultIfNullQuietly();
        args.getHost().setValue(url);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicAuthentication":
                    parseBasicAuthentication(impl, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                case "ProxyForWebDAV":
                    parseProxy(impl, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    impl.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    impl.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(impl, args.getSSL(), n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(impl, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static void parseHTTPHeaders(YADEXMLParser impl, HTTPProviderArguments args, Node headers) throws Exception {
        NodeList nl = headers.getChildNodes();
        args.getHTTPHeaders().setValue(new ArrayList<>());
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HTTPHeader":
                    args.getHTTPHeaders().getValue().add(impl.getValue(n));
                    break;
                }
            }
        }
    }

    protected static Node getProtocolFragment(YADEXMLParser impl, Node ref, boolean isSource, String fragmentPrefix) throws Exception {
        String exp = "Fragments/ProtocolFragments/" + fragmentPrefix + "Fragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node node = impl.getXPath().selectNode(impl.getRoot(), exp);
        if (node == null) {
            throw new SOSMissingDataException("[profile=" + impl.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target") + "]["
                    + exp + "]referenced Protocol fragment not found");
        }
        return node;
    }

    protected static void parseBasicConnection(YADEXMLParser impl, AProviderArguments args, Node basicConnection) throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    impl.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    impl.setIntegerArgumentValue(args.getPort(), n);
                    break;
                }
            }
        }
    }

    private static void parseBasicAuthentication(YADEXMLParser impl, AProviderArguments args, Node basicAuthentication) throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    impl.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    impl.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseURLConnection(YADEXMLParser impl, AProviderArguments args, Node urlConnection) throws Exception {
        NodeList nl = urlConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URL":
                    impl.setStringArgumentValue(args.getHost(), n);
                    break;
                }
            }
        }
    }

    protected static void parseConfigurationFiles(YADEXMLParser impl, AProviderArguments args, Node configurationFiles) {
        List<Path> files = new ArrayList<>();

        NodeList nl = configurationFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "ConfigurationFile".equals(n.getNodeName())) {
                files.add(Path.of(impl.getValue(n)));
            }
        }

        if (files.size() > 0) {
            args.getConfigurationFiles().setValue(files);
        }
    }

    protected static void parseProxy(YADEXMLParser impl, AProviderArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            ProxyArguments proxyArgs = new ProxyArguments();
            proxyArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "HTTPProxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
                        parseProxy(impl, proxyArgs, n);
                        break;
                    case "SOCKS4Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(impl, proxyArgs, n);
                        break;
                    case "SOCKS5Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(impl, proxyArgs, n);
                        break;
                    }
                }
            }
            args.setProxy(proxyArgs);
        }
    }

    private static void parseProxy(YADEXMLParser impl, ProxyArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseProxyBasicConnection(impl, args, n);
                    break;
                case "BasicAuthentication":
                    parseProxyBasicAuthentication(impl, args, n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicConnection(YADEXMLParser impl, ProxyArguments args, Node basicConnection) throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    impl.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    impl.setIntegerArgumentValue(args.getPort(), n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicAuthentication(YADEXMLParser impl, ProxyArguments args, Node basicAuthentication) throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    impl.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    impl.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    protected static void parseSFTPSSHAuthentication(YADEXMLParser impl, SSHProviderArguments args, Node sshAuthentication) {
        NodeList nl = sshAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    impl.setStringArgumentValue(args.getUser(), n);
                    break;
                case "AuthenticationMethodPassword":
                    parseSFTPSSHAuthenticationMethodPassword(impl, args, n);
                    break;
                case "AuthenticationMethodPublickey":
                    parseSFTPSSHAuthenticationMethodPublickey(impl, args, n);
                    break;
                case "AuthenticationMethodKeyboardInteractive":
                    // ignore - not implemented yet
                    break;
                case "PreferredAuthentications":
                    args.getPreferredAuthentications().setValue(SSHAuthMethod.fromString(impl.getValue(n)));
                    break;
                case "RequiredAuthentications":
                    args.getRequiredAuthentications().setValue(SSHAuthMethod.fromString(impl.getValue(n)));
                    break;
                }
            }
        }
    }

    private static void parseSFTPSSHAuthenticationMethodPassword(YADEXMLParser impl, SSHProviderArguments args, Node methodPassword) {
        NodeList nl = methodPassword.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Password":
                    impl.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseSFTPSSHAuthenticationMethodPublickey(YADEXMLParser impl, SSHProviderArguments args, Node methodPublickey) {
        NodeList nl = methodPublickey.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AuthenticationFile":
                    impl.setStringArgumentValue(args.getAuthFile(), n);
                    break;
                case "Passphrase":
                    impl.setStringArgumentValue(args.getPassphrase(), n);
                    break;
                }
            }
        }
    }

    private static void parseFTPSClientSecurity(YADEXMLParser impl, FTPSProviderArguments args, Node clientSecurity) {
        NodeList nl = clientSecurity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SecurityMode":
                    FTPSSecurityMode securityMode = FTPSSecurityMode.fromString(impl.getValue(n));
                    if (securityMode != null) {
                        args.getSecurityMode().setValue(securityMode);
                    }
                    break;
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(impl.getValue(n));
                    if (keyStoreType != null) {
                        args.getSSL().getJavaKeyStore().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getSSL().getJavaKeyStore().getKeyStoreFile().setValue(Path.of(impl.getValue(n)));
                    break;
                case "KeyStorePassword":
                    impl.setStringArgumentValue(args.getSSL().getJavaKeyStore().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseSMBAuthentication(YADEXMLParser impl, SMBProviderArguments args, Node smbAuthentication) {
        NodeList nl = smbAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    impl.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Domain":
                    impl.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "Password":
                    impl.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseKeyStrore(YADEXMLParser impl, SSLArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(impl.getValue(n));
                    if (keyStoreType != null) {
                        args.getJavaKeyStore().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getJavaKeyStore().getKeyStoreFile().setValue(Path.of(impl.getValue(n)));
                    break;
                case "KeyStorePassword":
                    impl.setStringArgumentValue(args.getJavaKeyStore().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }
}
