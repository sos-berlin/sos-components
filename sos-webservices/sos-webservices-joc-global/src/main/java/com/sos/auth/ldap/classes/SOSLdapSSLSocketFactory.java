package com.sos.auth.ldap.classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;

public class SOSLdapSSLSocketFactory extends SocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapSSLSocketFactory.class);
    private String truststorePath;
    private KeystoreType truststoreType = KeystoreType.PKCS12;
    private String truststorePass;

    private SSLSocketFactory sf;

    public SOSLdapSSLSocketFactory() {
        KeyStore trustStore = null;
        try {
            getTruststoreSettings();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (truststorePath != null && !truststorePath.isEmpty()) {
                Path p = Globals.sosCockpitProperties.resolvePath(truststorePath);

                if (Files.exists(p) && Files.isRegularFile(p)) {
                    trustStore = KeyStoreUtil.readTrustStore(p, truststoreType, truststorePass);
                } else {
                    LOGGER.debug("trustorePath: " + truststorePass + " not found");
                }
            } else {
                LOGGER.debug("trustorePath is empty");
            }
            if (trustStore == null) {
                LOGGER.debug("Using default TrustManager");
            }
            tmf.init(trustStore);
            SSLContext ctx = SSLContext.getInstance("TLS");

            ctx.init(null, tmf.getTrustManagers(), null);
            sf = ctx.getSocketFactory();

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static SocketFactory getDefault() {
        return new SOSLdapSSLSocketFactory();
    }

    @Override
    public Socket createSocket(final String s, final int i) throws IOException {
        return sf.createSocket(s, i);
    }

    @Override
    public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1) throws IOException {
        return sf.createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
        return sf.createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i, final InetAddress inetAddress1, final int i1) throws IOException {
        return sf.createSocket(inetAddress, i, inetAddress1, i1);
    }

    private String getValue(JocCockpitProperties jocCockpitProperties, String property, String defaultValue) {
        String s = jocCockpitProperties.getProperty(property, defaultValue);
        if (s == null || s.isEmpty()) {
            s = defaultValue;
        }
        return s;
    }

    private String getValueOrEmpty(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

    private void getTruststoreSettings() {

        String truststorePathDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStore"));
        String truststoreTypeDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStoreType"));
        String truststorePassDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStorePassword"));
        
        LOGGER.debug("javax.net.ssl.trustStore" + truststorePathDefault);
        LOGGER.debug("javax.net.ssl.trustStoreType" + truststoreTypeDefault);
        LOGGER.debug("javax.net.ssl.trustStorePassword" + truststorePassDefault);

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        JocCockpitProperties jocCockpitProperties = Globals.sosCockpitProperties;
        truststorePath = jocCockpitProperties.getProperty("ldap_truststore_path", "");
        if ((truststorePath).isEmpty()) {
            truststorePath = getValue(jocCockpitProperties, "truststore_path", truststorePathDefault);
            LOGGER.debug("truststore_path from joc.properties: " + truststorePath);
        }else {
            LOGGER.debug("truststore_path from identity service: " + truststorePath);
        }

        if (truststorePath != null && !truststorePath.trim().isEmpty()) {
            Path p = jocCockpitProperties.resolvePath(truststorePath.trim());
            truststorePath = p.toString();
            LOGGER.debug("resolved truststorePath: " + truststorePath);
        }

        truststorePass = jocCockpitProperties.getProperty("ldap_truststore_password", "");
        if ((truststorePass).isEmpty()) {
            truststorePass = getValue(jocCockpitProperties, "truststore_password", truststoreTypeDefault);
            LOGGER.debug("truststore_password from joc.properties: " + truststorePass);
        }else {
            LOGGER.debug("truststore_password from identity service: " + truststorePass);
        }

        String tType = jocCockpitProperties.getProperty("ldap_truststore_type", "");
        if ((tType).isEmpty()) {
            tType = getValue(jocCockpitProperties, "truststore_type", truststorePassDefault);
            LOGGER.debug("truststore_type from joc.properties: " + tType);
        }else {
            LOGGER.debug("truststore_type from identity service: " + tType);
        }
        if (tType == null || tType.isEmpty()) {
            tType = "PKCS12";
            LOGGER.debug("truststore_type set to default: " + tType);
        }
        truststoreType = KeystoreType.fromValue(tType);
 
    }

    /** The hostname verifier always return true */
    final static class DummyVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
