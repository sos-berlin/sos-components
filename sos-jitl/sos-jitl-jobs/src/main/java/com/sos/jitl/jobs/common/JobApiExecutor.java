package com.sos.jitl.jobs.common;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
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
    
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE = "js7.web.http.cs_file";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE = "js7.web.http.cs_keyFile";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD = "js7.web.http.cs_password";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME = "js7.web.http.username";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD = "js7.web.http.password";
    
    private final String truststoreFileName;

    private SOSRestApiClient client;
    private URI jocUri;
    private final JobLogger jobLogger;
    private boolean useHttps = false;

    public JobApiExecutor() {
        this(null, null, null);
    }

    public JobApiExecutor(JobLogger jobLogger) {
        this(null, null, jobLogger);
    }

    public JobApiExecutor(JobLogger jobLogger, boolean useHttps) {
        this(null, null, jobLogger, useHttps);
    }

    public JobApiExecutor(URI jocUri) {
        this(jocUri, null, null);
    }

    public JobApiExecutor(URI jocUri, boolean useHttps) {
        this(jocUri, null, null, useHttps);
    }

    public JobApiExecutor(URI jocUri, JobLogger logger) {
        this(jocUri, null, logger);
    }

    public JobApiExecutor(URI jocUri, JobLogger logger, boolean useHttps) {
        this(jocUri, null, logger, useHttps);
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = null;
        // default
        this.useHttps = true;
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName, boolean useHttps) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = null;
        this.useHttps = useHttps;
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName, JobLogger jobLogger) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = jobLogger;
        // default
        this.useHttps = true;
    }

    public JobApiExecutor(URI jocUri, String truststoreFileName, JobLogger jobLogger, boolean useHttps) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = jobLogger;
        this.useHttps = useHttps;
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
        String agentConfDirPath = System.getenv(AGENT_CONF_DIR_ENV_PARAM);
        logInfo("agentConfDirPath: " + agentConfDirPath);
        Path agentConfDir = Paths.get(System.getenv(AGENT_CONF_DIR_ENV_PARAM));
        Config config = readConfig(agentConfDir);
        
        logInfo("agent private folder: " + agentConfDir.resolve(PRIVATE_FOLDER_NAME).toString());
        for (Entry<String, ConfigValue> entry : config.entrySet()) {
            if(entry.getKey().startsWith("js7")) {
                logDebug(entry.getKey() + ": " + entry.getValue().toString());
            }
        }
        String jocUri = config.getString(PRIVATE_CONF_JS7_PARAM_JOCURL);
        logInfo("JOC Url (private.conf): " + jocUri);
        if (!SOSString.isEmpty(jocUri)) {
            this.jocUri = URI.create(jocUri);
        }
        if (this.jocUri == null) {
            throw new Exception("missing jocUri");
        }
        
        client = new SOSRestApiClient();
        logInfo("initiate REST api client");
        logInfo("use https: " + useHttps);
        if (useHttps) {
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
            if (keystore != null && truststore != null) {
                client.setSSLContext(keystore, credentials.getKeyPwd().toCharArray(), truststore);
            }
        } else {
            String csFile = null;
            String csKeyFile = null;
            String csPwd = null;
            try {
                csFile = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE);
            } catch (Exception e) {
                logDebug(e.getMessage());
            }
            try {
                csKeyFile = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE);
            } catch (Exception e) {
                logDebug(e.getMessage());
            }
            try {
                csPwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD);
            } catch (Exception e) {
                logDebug(e.getMessage());
            }
            String username = "";
            String pwd = "";
            if(csFile != null && !csFile.isEmpty()) {
                SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
                username = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME));
                pwd = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD));
            } else {
                username = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME);
                pwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD);
            }
            String basicAuth = Base64.getMimeEncoder().encodeToString((username + ":" + pwd).getBytes());
            client.setBasicAuthorization(basicAuth);
        }


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
