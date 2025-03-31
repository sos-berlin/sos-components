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

    protected static FTPProviderArguments parseFTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "FTP");

        FTPProviderArguments args = new FTPProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(argsSetter, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ProxyForFTP":
                    parseProxy(argsSetter, args, n);
                    break;
                case "ConnectTimeout":
                    argsSetter.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static FTPSProviderArguments parseFTPS(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "FTPS");

        FTPSProviderArguments args = new FTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(argsSetter, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsSetter, args, n);
                    break;
                case "FTPSClientSecurity":
                    parseFTPSClientSecurity(argsSetter, args, n);
                    break;
                case "FTPSProtocol":
                    args.getSSL().getProtocols().setValue(Arrays.asList(argsSetter.getValue(n)));
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ProxyForFTPS":
                    parseProxy(argsSetter, args, n);
                    break;
                case "ConnectTimeout":
                    argsSetter.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPProviderArguments parseHTTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "HTTP");

        HTTPProviderArguments args = new HTTPProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URLConnection":
                    parseURLConnection(argsSetter, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ProxyForHTTP":
                    parseProxy(argsSetter, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsSetter, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static HTTPSProviderArguments parseHTTPS(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "HTTPS");

        HTTPSProviderArguments args = new HTTPSProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URLConnection":
                    parseURLConnection(argsSetter, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ProxyForHTTP":
                    parseProxy(argsSetter, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsSetter, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    argsSetter.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsSetter.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(argsSetter, args.getSSL(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SSHProviderArguments parseSFTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "SFTP");

        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseBasicConnection(argsSetter, args, n);
                    break;
                case "SSHAuthentication":
                    parseSFTPSSHAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsSetter, ref, isSource);
                    break;
                case "ProxyForSFTP":
                    parseProxy(argsSetter, args, n);
                    break;
                case "StrictHostkeyChecking":
                    argsSetter.setBooleanArgumentValue(args.getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(argsSetter, args, n);
                    break;
                case "ServerAliveInterval":
                    argsSetter.setStringArgumentValue(args.getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsSetter.setIntegerArgumentValue(args.getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsSetter.setStringArgumentValue(args.getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsSetter.setStringArgumentValue(args.getSocketTimeout(), n);
                    break;
                }
            }
        }
        return args;
    }

    protected static SMBProviderArguments parseSMB(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "SMB");

        SMBProviderArguments args = new SMBProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsSetter.setStringArgumentValue(args.getHost(), n);
                    break;
                case "SMBAuthentication":
                    parseSMBAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(argsSetter, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static WebDAVProviderArguments parseWebDAV(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = getProtocolFragment(argsSetter, ref, isSource, "WebDAV");
        String url = argsSetter.getValue(argsSetter.getXPath().selectNode(fragment, "URLConnection/URL"));
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
                    parseBasicAuthentication(argsSetter, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                case "ProxyForWebDAV":
                    parseProxy(argsSetter, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    argsSetter.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsSetter.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(argsSetter, args.getSSL(), n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsSetter, args, n);
                    break;
                }
            }
        }
        return args;
    }

    protected static void parseHTTPHeaders(YADEXMLArgumentsSetter argsSetter, HTTPProviderArguments args, Node headers) throws Exception {
        NodeList nl = headers.getChildNodes();
        args.getHTTPHeaders().setValue(new ArrayList<>());
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HTTPHeader":
                    args.getHTTPHeaders().getValue().add(argsSetter.getValue(n));
                    break;
                }
            }
        }
    }

    protected static Node getProtocolFragment(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource, String fragmentPrefix) throws Exception {
        String exp = "Fragments/ProtocolFragments/" + fragmentPrefix + "Fragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node node = argsSetter.getXPath().selectNode(argsSetter.getRoot(), exp);
        if (node == null) {
            throw new SOSMissingDataException("[profile=" + argsSetter.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target")
                    + "][" + exp + "]referenced Protocol fragment not found");
        }
        return node;
    }

    protected static void parseBasicConnection(YADEXMLArgumentsSetter argsSetter, AProviderArguments args, Node basicConnection) throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsSetter.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    argsSetter.setIntegerArgumentValue(args.getPort(), n);
                    break;
                }
            }
        }
    }

    private static void parseBasicAuthentication(YADEXMLArgumentsSetter argsSetter, AProviderArguments args, Node basicAuthentication)
            throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsSetter.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsSetter.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseURLConnection(YADEXMLArgumentsSetter argsSetter, AProviderArguments args, Node urlConnection) throws Exception {
        NodeList nl = urlConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "URL":
                    argsSetter.setStringArgumentValue(args.getHost(), n);
                    break;
                }
            }
        }
    }

    protected static void parseConfigurationFiles(YADEXMLArgumentsSetter argsSetter, AProviderArguments args, Node configurationFiles) {
        List<Path> files = new ArrayList<>();

        NodeList nl = configurationFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "ConfigurationFile".equals(n.getNodeName())) {
                files.add(Path.of(argsSetter.getValue(n)));
            }
        }

        if (files.size() > 0) {
            args.getConfigurationFiles().setValue(files);
        }
    }

    protected static void parseProxy(YADEXMLArgumentsSetter argsSetter, AProviderArguments args, Node proxy) throws Exception {
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
                        parseProxy(argsSetter, proxyArgs, n);
                        break;
                    case "SOCKS4Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsSetter, proxyArgs, n);
                        break;
                    case "SOCKS5Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsSetter, proxyArgs, n);
                        break;
                    }
                }
            }
            args.setProxy(proxyArgs);
        }
    }

    private static void parseProxy(YADEXMLArgumentsSetter argsSetter, ProxyArguments args, Node proxy) throws Exception {
        NodeList nl = proxy.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    parseProxyBasicConnection(argsSetter, args, n);
                    break;
                case "BasicAuthentication":
                    parseProxyBasicAuthentication(argsSetter, args, n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicConnection(YADEXMLArgumentsSetter argsSetter, ProxyArguments args, Node basicConnection) throws Exception {
        NodeList nl = basicConnection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Hostname":
                    argsSetter.setStringArgumentValue(args.getHost(), n);
                    break;
                case "Port":
                    argsSetter.setIntegerArgumentValue(args.getPort(), n);
                    break;
                }
            }
        }
    }

    private static void parseProxyBasicAuthentication(YADEXMLArgumentsSetter argsSetter, ProxyArguments args, Node basicAuthentication)
            throws Exception {
        NodeList nl = basicAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsSetter.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Password":
                    argsSetter.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    protected static void parseSFTPSSHAuthentication(YADEXMLArgumentsSetter argsSetter, SSHProviderArguments args, Node sshAuthentication) {
        NodeList nl = sshAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsSetter.setStringArgumentValue(args.getUser(), n);
                    break;
                case "AuthenticationMethodPassword":
                    parseSFTPSSHAuthenticationMethodPassword(argsSetter, args, n);
                    break;
                case "AuthenticationMethodPublickey":
                    parseSFTPSSHAuthenticationMethodPublickey(argsSetter, args, n);
                    break;
                case "AuthenticationMethodKeyboardInteractive":
                    // ignore - not argsSetteremented yet
                    break;
                case "PreferredAuthentications":
                    args.getPreferredAuthentications().setValue(SSHAuthMethod.fromString(argsSetter.getValue(n)));
                    break;
                case "RequiredAuthentications":
                    args.getRequiredAuthentications().setValue(SSHAuthMethod.fromString(argsSetter.getValue(n)));
                    break;
                }
            }
        }
    }

    private static void parseSFTPSSHAuthenticationMethodPassword(YADEXMLArgumentsSetter argsSetter, SSHProviderArguments args, Node methodPassword) {
        NodeList nl = methodPassword.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Password":
                    argsSetter.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseSFTPSSHAuthenticationMethodPublickey(YADEXMLArgumentsSetter argsSetter, SSHProviderArguments args,
            Node methodPublickey) {
        NodeList nl = methodPublickey.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AuthenticationFile":
                    argsSetter.setStringArgumentValue(args.getAuthFile(), n);
                    break;
                case "Passphrase":
                    argsSetter.setStringArgumentValue(args.getPassphrase(), n);
                    break;
                }
            }
        }
    }

    private static void parseFTPSClientSecurity(YADEXMLArgumentsSetter argsSetter, FTPSProviderArguments args, Node clientSecurity) {
        NodeList nl = clientSecurity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SecurityMode":
                    FTPSSecurityMode securityMode = FTPSSecurityMode.fromString(argsSetter.getValue(n));
                    if (securityMode != null) {
                        args.getSecurityMode().setValue(securityMode);
                    }
                    break;
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsSetter.getValue(n));
                    if (keyStoreType != null) {
                        args.getSSL().getJavaKeyStore().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getSSL().getJavaKeyStore().getKeyStoreFile().setValue(Path.of(argsSetter.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsSetter.setStringArgumentValue(args.getSSL().getJavaKeyStore().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseSMBAuthentication(YADEXMLArgumentsSetter argsSetter, SMBProviderArguments args, Node smbAuthentication) {
        NodeList nl = smbAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Account":
                    argsSetter.setStringArgumentValue(args.getUser(), n);
                    break;
                case "Domain":
                    argsSetter.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "Password":
                    argsSetter.setStringArgumentValue(args.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseKeyStrore(YADEXMLArgumentsSetter argsSetter, SSLArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsSetter.getValue(n));
                    if (keyStoreType != null) {
                        args.getJavaKeyStore().getKeyStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getJavaKeyStore().getKeyStoreFile().setValue(Path.of(argsSetter.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsSetter.setStringArgumentValue(args.getJavaKeyStore().getKeyStorePassword(), n);
                    break;
                }
            }
        }
    }
}
