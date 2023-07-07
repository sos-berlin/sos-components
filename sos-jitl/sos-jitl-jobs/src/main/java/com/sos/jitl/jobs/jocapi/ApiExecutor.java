package com.sos.jitl.jobs.jocapi;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.vfs.exception.SOSAuthenticationFailedException;
import com.sos.jitl.jobs.common.OrderProcessStepLogger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
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
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS = "js7.web.https.keystore.alias";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY = "js7.web.https.truststores";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH = "file";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD = "store-password";
    private static final String PRIVATE_FOLDER_NAME = "private";
    private static final String PRIVATE_CONF_FILENAME = "private.conf";

    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE = "js7.api-server.cs-file";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE = "js7.api-server.cs-key";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD = "js7.api-server.cs-password";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME = "js7.api-server.username";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD = "js7.api-server.password";
    private static final List<String> DO_NOT_LOG_KEY = Arrays.asList(new String[] { "js7.api-server.password", "js7.api-server.cs-password",
            "js7.web.https.keystore.store-password", "js7.web.https.keystore.key-password", "s7.web.https.keystore.alias",
            "js7.web.https.truststores" });
    private static final String DO_NOT_LOG_VAL = "store-password";

    private final String truststoreFileName;
    private SOSRestApiClient client;
    private URI jocUri;
    private List<String> jocUris;
    private final OrderProcessStepLogger jobLogger;
    private Config config;

    public ApiExecutor(OrderProcessStepLogger jobLogger) {
        this(null, null, jobLogger);
    }

    public ApiExecutor(URI jocUri) {
        this(jocUri, null, null);
    }

    public ApiExecutor(URI jocUri, OrderProcessStepLogger logger) {
        this(jocUri, null, logger);
    }

    public ApiExecutor(URI jocUri, String truststoreFileName) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = null;
    }

    public ApiExecutor(URI jocUri, String truststoreFileName, OrderProcessStepLogger jobLogger) {
        this.jocUri = jocUri;
        this.truststoreFileName = truststoreFileName == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFileName;
        this.jobLogger = jobLogger;
    }

    private String getReasonPhrase() {
        String reasonPhrase = "";
        if (this.getClient() != null && this.getClient().getHttpResponse() != null && this.getClient().getHttpResponse().getStatusLine() != null) {
            reasonPhrase = this.getClient().getHttpResponse().getStatusLine().getReasonPhrase();
        }
        return reasonPhrase;
    }

    public ApiResponse login() throws Exception {
        logDebug("***ApiExecutor***");
        jocUris = getUris();
        String latestError = "";
        String latestResponse = "";
        Integer statusCode = -1;
        URI loginUri = null;
        Exception latestException = null;
        for (String uri : jocUris) {
            try {
                logDebug(String.format("processing Url - %1$s", uri));
                tryCreateClient(uri);
                this.jocUri = URI.create(uri);
                loginUri = jocUri.resolve(WS_API_LOGIN);
                logDebug("send login to: " + loginUri.toString());
                String response = client.postRestService(loginUri, null);
                latestResponse = response;
                statusCode = client.statusCode();
                logDebug("HTTP status code: " + statusCode);
                handleExitCode(client);
                if (client.statusCode() == 401) {
                    // 401 == Unauthorized!
                    JsonReader jsonReader = null;
                    String message = "";
                    try {
                        jsonReader = Json.createReader(new StringReader(response));
                        JsonObject json = jsonReader.readObject();
                        message = json.getString("message", "");
                        if (!message.isEmpty()) {
                            latestError = statusCode + " : " + getReasonPhrase() + " " + message;
                            throw new Exception(latestError);
                        }
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        jsonReader.close();
                    }
                    latestException = new Exception("login failed.");
                    throw latestException;
                } else if (statusCode == 200) {
                    // successful login
                    logDebug(String.format("Connection to URI %1$s established.", loginUri.toString()));
                    return new ApiResponse(statusCode, getReasonPhrase(), response, client.getResponseHeader(ACCESS_TOKEN_HEADER), null);
                } else {
                    String message = statusCode + " : " + getReasonPhrase();
                    throw new Exception(latestError);
                }
            } catch (SOSConnectionRefusedException e) {
                latestError = String.format("connection to URI %1$s failed, trying next Uri.", loginUri.toString());
                logDebug(latestError);
                latestException = e;
                continue;
            } catch (Exception e) {
                if (loginUri != null) {
                    latestError = String.format("connection to URI %1$s: %2$s occurred: %3$s", loginUri, e.getClass(), e.getMessage());
                    logDebug(latestError);
                } else {
                    latestError = String.format("%1$s occurred: %2$s", e.getClass(), e.getMessage());
                    logDebug(latestError);
                }
                latestException = e;
                continue;
            }
        }
        logInfo("No connection attempt was successful. Check agents private.conf.");
        throw latestException;
//        return new ApiResponse(statusCode, getReasonPhrase(), latestResponse, null, new Exception(latestError));
    }

    public ApiResponse post(String token, String apiUrl, String body) throws SOSConnectionRefusedException, SOSBadRequestException {
        if (token == null) {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        } else {
            if (jocUri != null) {
                try {
                    client.addHeader(ACCESS_TOKEN_HEADER, token);
                    client.addHeader(CONTENT_TYPE, APPLICATION_JSON);
                    logDebug("REQUEST: " + apiUrl);
                    logDebug("PARAMS: " + body);
                    if (!apiUrl.toLowerCase().startsWith(WS_API_PREFIX)) {
                        apiUrl = WS_API_PREFIX + apiUrl;
                    }
                    logDebug("resolvedUri: " + jocUri.resolve(apiUrl).toString());
                    String response = client.postRestService(jocUri.resolve(apiUrl), body);
                    logDebug("HTTP status code: " + client.statusCode());
                    handleExitCode(client);
                    return new ApiResponse(client.statusCode(), getReasonPhrase(), response, token, null);
                } catch (SOSException e) {
                    return new ApiResponse(client.statusCode(), getReasonPhrase(), null, token, e);
                }
            } else {
                throw new SOSConnectionRefusedException("No connection established through previous login api call.");
            }
        }
    }

    public ApiResponse logout(String token) throws SOSBadRequestException {
        if (token != null && jocUri != null) {
            try {
                client.addHeader(ACCESS_TOKEN_HEADER, token);
                logDebug("send logout");
                String response = client.postRestService(jocUri.resolve(WS_API_LOGOUT), null);
                logDebug("HTTP status code: " + client.statusCode());
                handleExitCode(client);
                return new ApiResponse(client.statusCode(), getReasonPhrase(), response, token, null);
            } catch (SOSException e) {
                return new ApiResponse(client.statusCode(), getReasonPhrase(), null, token, e);
            }
        } else {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        }
    }

    public void close() {
        closeClient();
    }

    private List<String> getUris() throws SOSMissingDataException {
        if (config == null) {
            readConfig();
        }
        List<String> uris = config.getConfig(PRIVATE_CONF_JS7_PARAM_API_SERVER).getStringList(PRIVATE_CONF_JS7_PARAM_URL);
        Collections.sort(uris);
        return uris;

    }

    public List<String> getJocUris() {
        return jocUris;
    }

    private void applySSLContextCredentials(SOSRestApiClient client) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, SOSSSLException, SOSMissingDataException {
        if (config == null) {
            readConfig();
        }
        List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(config);
        KeyStore truststore = null;
        if (truststoresCredentials != null && !truststoresCredentials.isEmpty()) {
            truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith(truststoreFileName)).map(item -> {
                try {
                    return KeyStoreUtil.readTrustStore(item.getPath(), KeystoreType.PKCS12, item.getStorePwd());
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).findFirst().get();
        }
        KeyStoreCredentials credentials = readKeystoreCredentials(config);
        KeyStore keystore = null;
        if (credentials != null) {
            keystore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
        }
        if (keystore != null && truststore != null) {
            // TODO consider alias JOC-1379
            client.setSSLContext(keystore, credentials.getKeyPwd() != null ? credentials.getKeyPwd().toCharArray() : "".toCharArray(), credentials
                    .getKeyStoreAlias(), truststore);
        }
    }

    private void tryCreateClient(String jocUri) throws SOSMissingDataException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            SOSSSLException, IOException, SOSKeePassDatabaseException {
        if (client != null) {
            client.closeHttpClient();
        }
        if (config == null) {
            readConfig();
        }
        client = new SOSRestApiClient();
        setBasicAuthorizationIfExists(config);
        logDebug("initiate REST api client");
        if (jocUri.toLowerCase().startsWith("https:")) {
            applySSLContextCredentials(client);
        }
    }

    private void setBasicAuthorizationIfExists(Config config) throws SOSKeePassDatabaseException, SOSMissingDataException {
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
            username = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME));
            pwd = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD));
        } else {
            try {
                username = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME);
            } catch (ConfigException e) {
                logDebug("no username found in private.conf.");

            }
            try {
                pwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD);
            } catch (ConfigException e) {
                logDebug("no (user-)password found in private.conf.");

            }
        }
        if (!username.isEmpty() && !pwd.isEmpty()) {
            String basicAuth = Base64.getMimeEncoder().encodeToString((username + ":" + pwd).getBytes());
            client.setBasicAuthorization(basicAuth);
        }
    }

    private void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }

    public void readConfig() throws SOSMissingDataException {
        String agentConfDirPath = System.getenv(AGENT_CONF_DIR_ENV_PARAM);
        if (agentConfDirPath == null) {
            agentConfDirPath = System.getProperty(AGENT_CONF_DIR_ENV_PARAM);
        }
        if (agentConfDirPath == null) {
            throw new SOSMissingDataException(String.format(
                    "Environment variable %1$s not set. Can´t read credentials from agents private.conf file.", AGENT_CONF_DIR_ENV_PARAM));
        }
        logDebug("agentConfDirPath: " + agentConfDirPath);
        Path agentConfDir = Paths.get(agentConfDirPath);
        // set default com.typesafe.config.Config
        Config defaultConfig = ConfigFactory.load();
        // set initial properties - js7.config-directory
        Properties props = new Properties();
        props.setProperty(PRIVATE_CONF_JS7_PARAM_CONFDIR, agentConfDir.toString());
        Path privatFolderPath = agentConfDir.resolve(PRIVATE_FOLDER_NAME);
        logDebug("agents private folder: " + privatFolderPath.toString());
        Config defaultConfigWithAgentConfDir = ConfigFactory.parseProperties(props).withFallback(defaultConfig).resolve();
        logDebug(PRIVATE_CONF_JS7_PARAM_CONFDIR + " (Config): " + defaultConfigWithAgentConfDir.getString(PRIVATE_CONF_JS7_PARAM_CONFDIR));
        if (Files.exists(privatFolderPath.resolve(PRIVATE_CONF_FILENAME))) {
            config = ConfigFactory.parseFile(privatFolderPath.resolve(PRIVATE_CONF_FILENAME).toFile()).withFallback(defaultConfigWithAgentConfDir)
                    .resolve();
            if (config != null) {
                for (Entry<String, ConfigValue> entry : config.entrySet()) {
                    if (entry.getKey().startsWith("js7")) {
                        if (!DO_NOT_LOG_KEY.contains(entry.getKey())) {
                            logDebug(entry.getKey() + ": " + entry.getValue().toString());
                        }
                    }
                }
            }
        } else {
            throw new SOSMissingDataException(String.format("File %1$s does not exists. Can´t read credentials from agents private.conf file.",
                    privatFolderPath.resolve(PRIVATE_CONF_FILENAME).toString()));
        }
    }

    private KeyStoreCredentials readKeystoreCredentials(Config config) {
        String keystorePath = null;
        try {
            keystorePath = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH);
            logDebug("read Keystore from: " + config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
        } catch (ConfigException e) {
            logDebug("no keystore file found in private.conf.");
        }
        String keyPasswd = null;
        try {
            keyPasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD);
        } catch (ConfigException e) {
            logDebug("no keystore key-password found in private.conf.");
        }
        String storePasswd = null;
        try {
            storePasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD);
        } catch (ConfigException e) {
            logDebug("no keystore store-password found in private.conf.");
        }
        String alias = null;
        try {
            alias = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS);
        } catch (ConfigException e) {
            logDebug("no (key-)alias found in private.conf.");
        }
        if (keystorePath != null && !keystorePath.isEmpty()) {
            return new KeyStoreCredentials(keystorePath, storePasswd, keyPasswd, alias);
        } else {
            return null;
        }
    }

    private List<KeyStoreCredentials> readTruststoreCredentials(Config config) {
        List<KeyStoreCredentials> credentials = null;
        try {
            credentials = config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).stream().map(item -> new KeyStoreCredentials(item.getString(
                    PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH), item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD))).filter(
                            Objects::nonNull).collect(Collectors.toList());
            logDebug("read Trustore from: " + config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).get(0).getString(
                    PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
        } catch (ConfigException e) {
            logDebug("no truststore credentials found in private.conf.");
        }
        if (credentials != null) {
            return credentials;
        } else {
            return Collections.emptyList();
        }
    }

    private void logInfo(String log) {
        if (jobLogger != null) {
            jobLogger.info(log);
        } else {
            LOGGER.info(log);
        }
    }

    private void logDebug(String log) {
        if (jobLogger != null) {
            jobLogger.debug(log);
        } else {
            LOGGER.debug(log);
        }
    }

    private void logError(String log) {
        if (jobLogger != null) {
            jobLogger.error(log);
        } else {
            LOGGER.error(log);
        }
    }

    private void handleExitCode(SOSRestApiClient client) throws SOSAuthenticationFailedException, SOSConnectionRefusedException, SOSException {
        if (client.statusCode() >= 500) {
            throw new SOSConnectionRefusedException();
        }
    }

    public SOSRestApiClient getClient() {
        return this.client;
    }

    public Config getConfig() throws SOSMissingDataException {
        if (config == null) {
            readConfig();
        }
        return config;
    }
}
