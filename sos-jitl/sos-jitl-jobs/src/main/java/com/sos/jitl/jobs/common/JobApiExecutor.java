package com.sos.jitl.jobs.common;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.util.SOSString;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class JobApiExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApiExecutor.class);

    private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
    private static final String WS_API_LOGIN = "/joc/api/authentication/login";
    private static final String WS_API_LOGOUT = "/joc/api/authentication/logout";
    private static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
    private static final String AGENT_CONF_DIR_ENV_PARAM = "JS7_AGENT_CONFIG_DIR";
    private static final String PRIVATE_CONF_JS7_PARAM_CONFDIR = "js7.config-directory";
    private static final String PRIVATE_CONF_JS7_PARAM_JOCURL = "js7.web.joc.url";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH = "js7.web.https.keystore.file";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD = "js7.web.https.keystore.key-password";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD = "js7.web.https.keystore.store-password";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY = "js7.web.https.truststores";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH = "file";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD = "store-password";
    private static final String PRIVATE_FOLDER_NAME = "private";
    private static final String PRIVATE_CONF_FILENAME = "private.conf";
    
    
    private final String truststoreFileName;

    private SOSRestApiClient client;
    private URI jocUri;
    private final JobLogger jobLogger;

    public JobApiExecutor() {
        this(null, null, null);
    }

    public JobApiExecutor(JobLogger jobLogger) {
        this(null, null, jobLogger);
    }

    public JobApiExecutor(URI jocUri) {
        this(jocUri, null, null);
    }

    public JobApiExecutor(URI jocUri, JobLogger logger) {
        this(jocUri, null, logger);
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = null;
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName, JobLogger jobLogger) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = jobLogger;
    }

    public String login() throws Exception {
    	logInfo("***JobApiExecutor***");

    	tryCreateClient();
    	logInfo("send login to: " + jocUri.resolve(WS_API_LOGIN).toString());
        String response = client.postRestService(jocUri.resolve(WS_API_LOGIN), null);
        logInfo("HTTP status code: " + client.statusCode());
        logInfo("response from web server: " + response);
        logInfo("access token: " + client.getResponseHeader(ACCESS_TOKEN_HEADER));
        return client.getResponseHeader(ACCESS_TOKEN_HEADER);
    }
    
    

    public void logout(String token) throws Exception {
        if (token != null) {
            tryCreateClient();

            client.addHeader(ACCESS_TOKEN_HEADER, token);
        	logInfo("send logout");
            client.postRestService(jocUri.resolve(WS_API_LOGOUT), null);
        }
    }

    public void close() {
        closeClient();
    }

    private void tryCreateClient() throws Exception {
        if (client != null) {
            return;
        }
        Path agentConfDir = Paths.get(System.getenv(AGENT_CONF_DIR_ENV_PARAM));
        Config config = readConfig(agentConfDir);
        logDebug("agent private folder: " + agentConfDir.resolve(PRIVATE_FOLDER_NAME).toString());
        for (Entry<String, ConfigValue> entry : config.entrySet()) {
        	if(entry.getKey().startsWith("js7")) {
        		logDebug(entry.getKey() + ": " + entry.getValue().toString());
        	}
        }
        String jocUri = config.getString(PRIVATE_CONF_JS7_PARAM_JOCURL);
        logDebug("JOC Url (private.conf): " + jocUri);
        if (!SOSString.isEmpty(jocUri)) {
            this.jocUri = URI.create(jocUri);
        }
        if (this.jocUri == null) {
            throw new Exception("missing jocUri");
        }

        List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(config);
    	logInfo("read Trustore from: " + config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).get(0).getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
    	KeyStore truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith(truststoreFileName)).map(item -> {
            try {
                return KeyStoreUtil.readTrustStore(item.getPath(), KeystoreType.PKCS12, item.getStorePwd());
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).findFirst().get();
        KeyStoreCredentials credentials = readKeystoreCredentials(config);
    	logInfo("read Keystore from: " + config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
        KeyStore keystore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());

        client = new SOSRestApiClient();
    	logInfo("initiate REST api client");
        client.setSSLContext(keystore, credentials.getKeyPwd().toCharArray(), truststore);
    }

    private void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }

    public Config readConfig(Path agentConfigDirectory) {
    	// set default com.typesafe.config.Config
        Config defaultConfig = ConfigFactory.load();
        /*
         * set initial properties 
         * - js7.config-directory
         * 
         * */
        Properties props = new Properties();
    	props.setProperty(PRIVATE_CONF_JS7_PARAM_CONFDIR, agentConfigDirectory.toString());
    	Path privatFolderPath = agentConfigDirectory.resolve(PRIVATE_FOLDER_NAME);
    	logInfo("agents private folder: " + privatFolderPath.toString());
    	Config defaultConfigWithAgentConfDir = ConfigFactory.parseProperties(props).withFallback(defaultConfig).resolve();
    	logInfo(PRIVATE_CONF_JS7_PARAM_CONFDIR + " (Config): " + defaultConfigWithAgentConfDir.getString(PRIVATE_CONF_JS7_PARAM_CONFDIR));
        return ConfigFactory.parseFile(privatFolderPath.resolve(PRIVATE_CONF_FILENAME).toFile()).withFallback(defaultConfigWithAgentConfDir).resolve();
    }

    private static KeyStoreCredentials readKeystoreCredentials(Config config) {
        String keystorePath = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH);
        String keyPasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD);
        String storePasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD);
        return new KeyStoreCredentials(keystorePath, storePasswd, keyPasswd);
    }

    private static List<KeyStoreCredentials> readTruststoreCredentials(Config config) {
        List<KeyStoreCredentials> credentials = config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).stream().map(item -> {
            return new KeyStoreCredentials(item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH),
            		item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD), null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (credentials != null) {
            return credentials;
        } else {
            return Collections.emptyList();
        }
    }

    private void logInfo (String log) {
    	if (jobLogger != null) {
    		jobLogger.info(log);
    	} else {
    		LOGGER.info(log);
    	}
    }
    
    private void logDebug (String log) {
    	if (jobLogger != null) {
    		jobLogger.debug(log);
    	} else {
    		LOGGER.debug(log);
    	}
    }
    
    private void logTrace (String log) {
    	if (jobLogger != null) {
    		jobLogger.trace(log);
    	} else {
    		LOGGER.trace(log);
    	}
    }
    
    private void logError (String log) {
    	if (jobLogger != null) {
    		jobLogger.error(log);
    	} else {
    		LOGGER.error(log);
    	}
    }
    
}
