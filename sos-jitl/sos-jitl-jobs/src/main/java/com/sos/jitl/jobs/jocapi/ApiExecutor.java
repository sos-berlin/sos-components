package com.sos.jitl.jobs.jocapi;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
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
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.exception.SOSAuthenticationFailedException;
import com.sos.jitl.jobs.common.JobLogger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class ApiExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExecutor.class);

    private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
    private static final String WS_API_LOGIN = "/joc/api/authentication/login";
    private static final String WS_API_LOGOUT = "/joc/api/authentication/logout";
    private static final String WS_API_PREFIX = "/joc/api";
    private static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AGENT_CONF_DIR_ENV_PARAM = "JS7_AGENT_CONFIG_DIR";
    private static final String PRIVATE_CONF_JS7_PARAM_CONFDIR = "js7.config-directory";
    private static final String PRIVATE_CONF_JS7_PARAM_API_SERVER = "js7.api-server";
    private static final String PRIVATE_CONF_JS7_PARAM_URL = "url";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH = "js7.web.https.keystore.file";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD = "js7.web.https.keystore.key-password";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD = "js7.web.https.keystore.store-password";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY = "js7.web.https.truststores";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH = "file";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD = "store-password";
    private static final String PRIVATE_FOLDER_NAME = "private";
    private static final String PRIVATE_CONF_FILENAME = "private.conf";
    
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE = "js7.api-server.cs-file";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE = "js7.api-server.cs-keyFile";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD = "js7.api-server.cs-password";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME = "js7.api-server.username";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD = "js7.api-server.password";
    private static final List<String> DO_NOT_LOG_KEY = Arrays.asList(new String []{"js7.api-server.password", 
            "js7.api-server.cs-password", "js7.web.https.keystore.store-password", "js7.web.https.keystore.key-password",
            "js7.web.https.truststores"});
    private static final String DO_NOT_LOG_VAL = "store-password";
    
    private final String truststoreFileName;
    private SOSRestApiClient client;
    private URI jocUri;
    private List<String> jocUris;
    private final JobLogger jobLogger;

    public ApiExecutor(JobLogger jobLogger) {
        this(null, null, jobLogger);
    }

    public ApiExecutor(URI jocUri) {
        this(jocUri, null, null);
    }

    public ApiExecutor(URI jocUri, JobLogger logger) {
        this(jocUri, null, logger);
    }

    public ApiExecutor(URI jocUri, String truststoreFileName) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = null;
    }

    public ApiExecutor(URI jocUri, String truststoreFileName, JobLogger jobLogger) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = jobLogger;
    }

    public ApiResponse login() throws SOSAuthenticationFailedException, Exception {
    	logDebug("***ApiExecutor***");
    	jocUris = getUris();
    	for(String uri : jocUris) {
    	    try{
    	        tryCreateClient(uri);
    	        this.jocUri = URI.create(uri);
    	        logDebug("send login to: " + jocUri.resolve(WS_API_LOGIN).toString());
    	        String response = client.postRestService(jocUri.resolve(WS_API_LOGIN), null);
    	        logDebug("HTTP status code: " + client.statusCode());
    	        try {
                    handleExitCode(client);
                    logDebug(String.format("Connection to URI %1$s established.", jocUri.resolve(WS_API_LOGIN).toString()));
                    return new ApiResponse(client.statusCode(), response, client.getResponseHeader(ACCESS_TOKEN_HEADER), null);
                } catch (SOSConnectionRefusedException e) {
                    logDebug(String.format("connection to URI %1$s failed, trying next Uri.", jocUri.resolve(WS_API_LOGIN).toString()));
                    continue;
                }
    	    }catch (Exception e) {
    	        logDebug(String.format("connection to URI %1$s failed, trying next Uri.", jocUri.resolve(WS_API_LOGIN).toString()));
            }
    	}
        return new ApiResponse(null, null, null, null);
    }
    
    public ApiResponse post(String token, String apiUrl, String body) throws Exception {
        if (token == null) {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        } else {
            if (jocUri != null) {
                try {
                    client.addHeader(ACCESS_TOKEN_HEADER, token);
                    client.addHeader(CONTENT_TYPE, APPLICATION_JSON);
                    logInfo("REQUEST: " + apiUrl);
                    logInfo("PARAMS: " + body);
                    if(!apiUrl.toLowerCase().startsWith(WS_API_PREFIX)) {
                        apiUrl = WS_API_PREFIX + apiUrl;
                    }
                    logDebug("resolvedUri: " + jocUri.resolve(apiUrl).toString());
                    String response = client.postRestService(jocUri.resolve(apiUrl), body);
                    logDebug("HTTP status code: " + client.statusCode());
                    handleExitCode(client);
                    return new ApiResponse(client.statusCode(), response, token, null);
                } catch (SOSException e) {
                    return new ApiResponse(client.statusCode(), null, token, e);
                }
            } else {
                throw new SOSConnectionRefusedException("No connection established through previous login api call.");
            }
        }
    }
    
    public ApiResponse logout(String token) throws Exception {
        if (token != null && jocUri != null) {
            try {
                client.addHeader(ACCESS_TOKEN_HEADER, token);
                logDebug("send logout");
                String response = client.postRestService(jocUri.resolve(WS_API_LOGOUT), null);
                logDebug("HTTP status code: " + client.statusCode());
                handleExitCode(client);
                return new ApiResponse(client.statusCode(), response, token, null);
            } catch (SOSException e) {
                return new ApiResponse(client.statusCode(), null, token, e);
            }
        } else {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        }
    }

    public void close() {
        closeClient();
    }

    private List<String> getUris() throws SOSMissingDataException {
        Config config = readConfig();
        List<String> uris =  config.getConfig(PRIVATE_CONF_JS7_PARAM_API_SERVER).getStringList(PRIVATE_CONF_JS7_PARAM_URL);
        Collections.sort(uris);
        return uris;
        
    }
    
    private void applySSLContextCredentials(SOSRestApiClient client) throws Exception {
        Config config = readConfig();
        List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(config);
        logDebug("read Trustore from: " + config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).get(0).getString(
                PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
        KeyStore truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith(truststoreFileName)).map(item -> {
            try {
                return KeyStoreUtil.readTrustStore(item.getPath(), KeystoreType.PKCS12, item.getStorePwd());
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).findFirst().get();
        KeyStoreCredentials credentials = readKeystoreCredentials(config);
        logDebug("read Keystore from: " + config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
        KeyStore keystore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
        if (keystore != null && truststore != null) {
            client.setSSLContext(keystore, credentials.getKeyPwd().toCharArray(), truststore);
        }
    }
    
    private void tryCreateClient(String jocUri) throws Exception {
        if (client != null) {
            client.closeHttpClient();
        }
        Config config = readConfig();
        
        for (Entry<String, ConfigValue> entry : config.entrySet()) {
            if(entry.getKey().startsWith("js7")) {
                if (!DO_NOT_LOG_KEY.contains(entry.getKey())) {
                    logDebug(entry.getKey() + ": " + entry.getValue().toString());
                }
            }
        }
        List<String> jocUris = getUris();
        while(jocUris.iterator().hasNext()) {
            String uri = jocUris.iterator().next();
            if (!SOSString.isEmpty(uri)) {
                this.jocUri = URI.create(uri);
            } else {
                throw new Exception("no uri provided for api server.");
            }
            
        }
        if (!SOSString.isEmpty(jocUri)) {
            this.jocUri = URI.create(jocUri);
        }
        if (this.jocUri == null) {
            throw new Exception("missing JOC Uri");
        }

        client = new SOSRestApiClient();
        logDebug("initiate REST api client");
        if (jocUri.toLowerCase().startsWith("https:")) {
            applySSLContextCredentials(client);
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
            if (csFile != null && !csFile.isEmpty()) {
                SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
                try {
                    username = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME));
                    pwd = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD));
                } catch (Exception e) {
                    logDebug(e.getMessage());
                }
            } else {
                try {
                    username = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME);
                    pwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD);
                } catch (Exception e) {
                    logDebug(e.getMessage());
                }
            }
            if ((username == null || username.isEmpty()) && (pwd == null || pwd.isEmpty())) {
                logError("no username and password configured in private.conf");
            } else {
                String basicAuth = Base64.getMimeEncoder().encodeToString((username + ":" + pwd).getBytes());
                client.setBasicAuthorization(basicAuth);
            }
        }

    }

    private void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }

    public Config readConfig() throws SOSMissingDataException {
        String agentConfDirPath = System.getenv(AGENT_CONF_DIR_ENV_PARAM);
        if(agentConfDirPath == null) {
            throw new SOSMissingDataException(
                    String.format("Environment variable %1$s not set. Can´t read credetials from agents private.conf file.",
                            AGENT_CONF_DIR_ENV_PARAM));
        }
        logDebug("agentConfDirPath: " + agentConfDirPath);
        Path agentConfDir = Paths.get(agentConfDirPath);
    	// set default com.typesafe.config.Config
        Config defaultConfig = ConfigFactory.load();
        /*
         * set initial properties 
         * - js7.config-directory
         * 
         * */
        Properties props = new Properties();
    	props.setProperty(PRIVATE_CONF_JS7_PARAM_CONFDIR, agentConfDir.toString());
    	Path privatFolderPath = agentConfDir.resolve(PRIVATE_FOLDER_NAME);
    	logDebug("agents private folder: " + privatFolderPath.toString());
    	Config defaultConfigWithAgentConfDir = ConfigFactory.parseProperties(props).withFallback(defaultConfig).resolve();
    	logDebug(PRIVATE_CONF_JS7_PARAM_CONFDIR + " (Config): " + defaultConfigWithAgentConfDir.getString(PRIVATE_CONF_JS7_PARAM_CONFDIR));
        return ConfigFactory.parseFile(privatFolderPath.resolve(PRIVATE_CONF_FILENAME).toFile()).withFallback(defaultConfigWithAgentConfDir)
                .resolve();
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
    
    private void logError (String log) {
    	if (jobLogger != null) {
    		jobLogger.error(log);
    	} else {
    		LOGGER.error(log);
    	}
    }
    
    private void handleExitCode (SOSRestApiClient client)
            throws SOSAuthenticationFailedException, SOSConnectionRefusedException, SOSException {
        if(client.statusCode() >= 500) {
            throw new SOSConnectionRefusedException();
        }
    }
    
}
