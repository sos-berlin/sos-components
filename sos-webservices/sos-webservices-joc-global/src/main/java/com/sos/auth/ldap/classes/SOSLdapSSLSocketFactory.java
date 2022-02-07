package com.sos.auth.ldap.classes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicReference;

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
	private static final AtomicReference<SOSLdapSSLSocketFactory> defaultFactory = new AtomicReference<>();
	private String truststorePath;
	private KeystoreType truststoreType;
	private String truststorePass;

	private SSLSocketFactory sf;

	public SOSLdapSSLSocketFactory() {
		KeyStore trustStore = null;
		try {
			setSSLContext();
			trustStore = KeyStoreUtil.readKeyStore(truststorePath, truststoreType, truststorePass);

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
	}

	@Override
	public Socket createSocket(final String s, final int i) throws IOException {
		return sf.createSocket(s, i);
	}

	@Override
	public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1)
			throws IOException {
		return sf.createSocket(s, i, inetAddress, i1);
	}

	@Override
	public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
		return sf.createSocket(inetAddress, i);
	}

	@Override
	public Socket createSocket(final InetAddress inetAddress, final int i, final InetAddress inetAddress1, final int i1)
			throws IOException {
		return sf.createSocket(inetAddress, i, inetAddress1, i1);
	}

	private String getValue(JocCockpitProperties jocCockpitProperties, String property, String defaultValue) {
		String s = jocCockpitProperties.getProperty(property, defaultValue);
		if (s == null || s.isEmpty()) {
			s = defaultValue;
		}
		return s;
	}

	private String getValueOrEmpty(String value, String defaultValue) {
		if (value == null || value.isEmpty()) {
			value = defaultValue;
		}
		return value;
	}

	private void setSSLContext() {
		JocCockpitProperties jocCockpitProperties = Globals.sosCockpitProperties;

		if (jocCockpitProperties == null) {
			jocCockpitProperties = new JocCockpitProperties();
		}

		String truststorePathJocProperties = jocCockpitProperties.getProperty("truststore_path", "");
		String truststorePassJocProperties = jocCockpitProperties.getProperty("truststore_path", "");
		String tTypeJocProperties = jocCockpitProperties.getProperty("truststore_path", "");

		String truststorePathDefault = "";
		String truststoreTypeDefault = "";
		String truststorePassDefault = "";

		if ((truststorePathJocProperties + truststorePassJocProperties + tTypeJocProperties).isEmpty()) {
			truststorePathDefault = jocCockpitProperties.getProperty("truststore_path",
					System.getProperty("javax.net.ssl.trustStore"));
			truststoreTypeDefault = jocCockpitProperties.getProperty("truststore_type",
					System.getProperty("javax.net.ssl.trustStoreType"));
			truststorePassDefault = jocCockpitProperties.getProperty("truststore_password",
					System.getProperty("javax.net.ssl.trustStorePassword"));

			truststorePath = getValue(jocCockpitProperties, "ldap_truststore_path", truststorePathDefault);
			truststorePass = getValue(jocCockpitProperties, "ldap_truststore_password", truststorePassDefault);
			String tType = getValue(jocCockpitProperties, "ldap_truststore_type", truststoreTypeDefault);
			truststoreType = KeystoreType.valueOf(tType);
		} else {
			truststorePathDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStore"), "");
			truststoreTypeDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStoreType"), "");
			truststorePassDefault = getValueOrEmpty(System.getProperty("javax.net.ssl.trustStorePassword"), "");

			truststorePath = jocCockpitProperties.getProperty("truststore_path", truststorePathDefault);
			String tType = jocCockpitProperties.getProperty("truststore_type", truststoreTypeDefault);
			truststoreType = KeystoreType.valueOf(tType);
			truststorePass = jocCockpitProperties.getProperty("truststore_password", truststorePassDefault);
		}

		if (truststorePath != null && !truststorePath.trim().isEmpty()) {
			Path p = jocCockpitProperties.resolvePath(truststorePath.trim());
			truststorePath = p.toString();
		}
	}

	/** The hostname verifier always return true */
	final static class DummyVerifier implements HostnameVerifier {

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
}
