package com.sos.jitl.jobs.common;

import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Authenticator {

	public static String login (SOSRestApiClient httpsRestApiClient, URI jocLoginURI) throws SOSException {
		String response = httpsRestApiClient.postRestService(jocLoginURI, null);
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
    	Config privateConf = readPrivateConf(privateConfPath);
    	KeyStoreCredentials keystoreCredentials = readKeystoreCredentials(privateConf);
    	KeyStore keystore = KeyStoreUtil.readKeyStore(keystoreCredentials.getPath(), KeyStoreType.PKCS12, keystoreCredentials.getStorePwd());
    	List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(privateConf); 
    	KeyStore truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith("https-truststore.p12")).map(item -> {
			try {
				return KeyStoreUtil.readTrustStore(item.getPath(), KeyStoreType.PKCS12, item.getStorePwd());
			} catch (Exception e) {
				return null;
			}
		}).filter(Objects::nonNull).findFirst().get();
		SOSRestApiClient restApiClient = new SOSRestApiClient();
		restApiClient.setSSLContext(keystore, keystoreCredentials.getKeyPwd().toCharArray(), truststore);
		// the need for Basic Authentication has to be removed from ./login web service first
		restApiClient.setBasicAuthorization("cm9vdDpyb290");
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
