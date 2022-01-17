package com.sos.auth.ldap.classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
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
    private static final AtomicReference<SOSLdapSSLSocketFactory> defaultFactory = new AtomicReference<>();
    private String trustStorePath;
    private KeystoreType trustStoreType;
    private String trustStorePass;

    private SSLSocketFactory sf;

    public SOSLdapSSLSocketFactory() {
        KeyStore trustStore = null;
        LOGGER.info("===> SOSLdapSSLSocketFactory");
        try {
            setSSLContext();
            trustStore = KeyStoreUtil.readKeyStore(trustStorePath, trustStoreType, trustStorePass);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
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
      /*  final SOSLdapSSLSocketFactory value = defaultFactory.get();
        if (value == null) {
            defaultFactory.compareAndSet(null, new SOSLdapSSLSocketFactory());
            return defaultFactory.get();
        }
        return value;
        */
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
    
    private String getValue(JocCockpitProperties  jocCockpitProperties,String property, String defaultValue) { 
        String s = jocCockpitProperties.getProperty(property,defaultValue);
        if (s == null || s.isEmpty()){
            s = defaultValue;
        }
        return s; 
    }

    private void setSSLContext() {
        JocCockpitProperties jocCockpitProperties = Globals.sosCockpitProperties;

        if (jocCockpitProperties == null) {
            jocCockpitProperties = new JocCockpitProperties();
        }

        String trustStorePathDefault = jocCockpitProperties.getProperty("truststore_path", System.getProperty("javax.net.ssl.trustStore"));
        String trustStoreTypeDefault = jocCockpitProperties.getProperty("truststore_type", System.getProperty("javax.net.ssl.trustStoreType"));
        String trustStorePassDefault = jocCockpitProperties.getProperty("truststore_password", System.getProperty("javax.net.ssl.trustStorePassword"));
       
        trustStorePath = getValue(jocCockpitProperties,"ldap_truststore_path",trustStorePathDefault);
        trustStorePass = getValue(jocCockpitProperties,"ldap_truststore_password",trustStorePassDefault);
        String tType = getValue(jocCockpitProperties,"ldap_truststore_type",trustStoreTypeDefault);
        trustStoreType = KeystoreType.valueOf(tType);
        
        if (trustStorePath != null && !trustStorePath.trim().isEmpty()) {
            Path p = jocCockpitProperties.resolvePath(trustStorePath.trim());
            trustStorePath = p.toString();
        }
    }
}
