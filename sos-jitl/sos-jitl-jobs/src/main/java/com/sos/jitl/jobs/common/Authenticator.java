package com.sos.jitl.jobs.common;

import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Authenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);
	private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
	
	public static String login (SOSRestApiClient httpsRestApiClient, URI jocLoginURI) throws SOSException {
		String response = httpsRestApiClient.postRestService(jocLoginURI, null);
		LOGGER.trace("HTTP status code: " + httpsRestApiClient.statusCode());
		LOGGER.trace("response from web server: " + response);
    	return httpsRestApiClient.getResponseHeader("X-Access-Token");
    }

    public static String logout (SOSRestApiClient httpsRestApiClient, String accessToken, URI jocLogoutURI) throws SOSException {
		try {
			if (accessToken != null) {
				httpsRestApiClient.addHeader("X-Access-Token", accessToken);
				return httpsRestApiClient.postRestService(jocLogoutURI, null);
			}
		} finally {
			httpsRestApiClient.closeHttpClient();
		}
		return null;
    }
    
    public static SOSRestApiClient createHttpsRestApiClient (String privateConfPath) throws Exception {
    	return createHttpsRestApiClient(privateConfPath, null);
    }
    
    public static SOSRestApiClient createHttpsRestApiClient (String privateConfPath, String truststoreFilename) throws Exception {
    	Config privateConf = readPrivateConf(privateConfPath);
    	KeyStoreCredentials keystoreCredentials = readKeystoreCredentials(privateConf);
    	KeyStore keystore = KeyStoreUtil.readKeyStore(keystoreCredentials.getPath(), KeyStoreType.PKCS12, keystoreCredentials.getStorePwd());
    	List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(privateConf);
    	final String usedTruststoreFilename;
    	if (truststoreFilename == null) {
    		usedTruststoreFilename = DEFAULT_TRUSTSTORE_FILENAME;
    	} else {
    		usedTruststoreFilename = truststoreFilename;
    	}
    	KeyStore truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith(usedTruststoreFilename)).map(item -> {
			try {
				return KeyStoreUtil.readTrustStore(item.getPath(), KeyStoreType.PKCS12, item.getStorePwd());
			} catch (Exception e) {
				return null;
			}
		}).filter(Objects::nonNull).findFirst().get();
		SOSRestApiClient restApiClient = new SOSRestApiClient();
		restApiClient.setSSLContext(keystore, keystoreCredentials.getKeyPwd().toCharArray(), truststore);
    	return restApiClient;
    }

    public static Config readPrivateConf(String privateConfPath) {
    	return readConfig(privateConfPath);
    }
    
    public static Config readAgentConf(String agentConfPath) {
    	return readConfig(agentConfPath);
    }
    
    private static Config readConfig(String configPath) {
		Config defaultConfig = ConfigFactory.load();
    	Config config = ConfigFactory.parseFile(new File(configPath)).withFallback(defaultConfig).resolve();
    	return config;
    }
    
    private static KeyStoreCredentials readKeystoreCredentials (Config config) {
    	String keystorePath = config.getString("js7.web.https.keystore.file");
    	String keyPasswd = config.getString("js7.web.https.keystore.key-password");
    	String storePasswd = config.getString("js7.web.https.keystore.store-password");
    	KeyStoreCredentials credentials = new KeyStoreCredentials(keystorePath, storePasswd, keyPasswd);
    	return credentials;
    }
    
    private static List<KeyStoreCredentials> readTruststoreCredentials (Config config) {
		List<? extends Config> truststores = config.getConfigList("js7.web.https.truststores");
		List<KeyStoreCredentials> truststoreListCredentials = null;
		truststoreListCredentials = truststores.stream().map(item -> {
			return new KeyStoreCredentials(item.getString("file"), item.getString("store-password"), null);
		}).filter(Objects::nonNull).collect(Collectors.toList());
		if (truststoreListCredentials != null) {
			return truststoreListCredentials;
		} else {
			return Collections.emptyList();
		}
    }

}
