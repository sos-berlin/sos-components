package com.sos.yade.engine.commons.arguments.loaders.xml;

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
                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
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
                case "ProxyForFTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
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
                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "FTPSClientSecurity":
                    parseFTPSClientSecurity(argsLoader, args, n);
                    break;
                case "FTPSProtocol":
                    args.getSSL().getProtocols().setValue(Arrays.asList(argsLoader.getValue(n)));
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "ProxyForFTPS":
                    parseProxy(argsLoader, args, n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(args.getConnectTimeout(), n);
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
                case "URLConnection":
                    parseURLConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
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
                case "URLConnection":
                    parseURLConnection(argsLoader, args, n);
                    break;
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "ProxyForHTTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsLoader, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    argsLoader.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(argsLoader, args.getSSL(), n);
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
                case "BasicConnection":
                    parseBasicConnection(argsLoader, args, n);
                    break;
                case "SSHAuthentication":
                    parseSFTPSSHAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "JumpFragmentRef":
                    YADEXMLFragmentsProtocolFragmentJumpHelper.parse(argsLoader, n, isSource);
                    break;
                case "ProxyForSFTP":
                    parseProxy(argsLoader, args, n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(args.getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    parseConfigurationFiles(argsLoader, args, n);
                    break;
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
                case "Hostname": // YADE1 - deprecated, SMBConnection should be used
                    argsLoader.setStringArgumentValue(args.getHost(), n);
                    args.tryRedefineHostPort();
                    break;
                case "SMBConnection": // introduced with JS7 YADE-626
                    parseSMBConnection(argsLoader, args, n);
                    break;
                case "SMBAuthentication":
                    parseSMBAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
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
        Node fragment = getProtocolFragment(argsLoader, ref, isSource, "WebDAV");
        String url = argsLoader.getValue(argsLoader.getXPath().selectNode(fragment, "URLConnection/URL"));
        if (url == null) {
            return null;
        }
        String urlLC = url.toLowerCase();
        WebDAVProviderArguments args = urlLC.startsWith("https://") || urlLC.startsWith("webdavs://") ? new WebDAVSProviderArguments()
                : new WebDAVProviderArguments();
        args.applyDefaultIfNullQuietly();
        args.getHost().setValue(url);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicAuthentication":
                    parseBasicAuthentication(argsLoader, args, n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                case "ProxyForWebDAV":
                    parseProxy(argsLoader, args, n);
                    break;
                case "AcceptUntrustedCertificate":
                    argsLoader.setBooleanArgumentValue(args.getSSL().getAcceptUntrustedCertificate(), n);
                    break;
                case "DisableCertificateHostnameVerification":
                    argsLoader.setOppositeBooleanArgumentValue(args.getSSL().getVerifyCertificateHostname(), n);
                    break;
                case "KeyStore":
                    parseKeyStrore(argsLoader, args.getSSL(), n);
                    break;
                case "HTTPHeaders":
                    parseHTTPHeaders(argsLoader, args, n);
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
                    case "HTTPProxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;
                    case "SOCKS4Proxy":
                        proxyArgs.getType().setValue(java.net.Proxy.Type.SOCKS);
                        parseProxy(argsLoader, proxyArgs, n);
                        break;
                    case "SOCKS5Proxy":
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

    private static void parseFTPSClientSecurity(YADEXMLArgumentsLoader argsLoader, FTPSProviderArguments args, Node clientSecurity) {
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
                        args.getSSL().getJavaKeyStore().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getSSL().getJavaKeyStore().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getSSL().getJavaKeyStore().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }

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
                case "Account": // YADE 1 - deprecated
                    argsLoader.setStringArgumentValue(args.getUser(), n);
                    account = true;
                    break;
                case "Domain": // YADE 1 - deprecated
                    argsLoader.setStringArgumentValue(args.getDomain(), n);
                    break;
                case "Password": // YADE 1 - deprecated
                    argsLoader.setStringArgumentValue(args.getPassword(), n);
                    break;
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
            }// else default NTLM
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
                }
            }
        }
        args.getAuthMethod().setValue(SMBAuthMethod.SPNEGO);
    }

    private static void parseKeyStrore(YADEXMLArgumentsLoader argsLoader, SSLArguments args, Node keyStore) {
        NodeList nl = keyStore.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "KeyStoreType":
                    JavaKeyStoreType keyStoreType = JavaKeyStoreType.fromString(argsLoader.getValue(n));
                    if (keyStoreType != null) {
                        args.getJavaKeyStore().getTrustStoreType().setValue(keyStoreType);
                    }
                    break;
                case "KeyStoreFile":
                    args.getJavaKeyStore().getTrustStoreFile().setValue(Path.of(argsLoader.getValue(n)));
                    break;
                case "KeyStorePassword":
                    argsLoader.setStringArgumentValue(args.getJavaKeyStore().getTrustStorePassword(), n);
                    break;
                }
            }
        }
    }
}
