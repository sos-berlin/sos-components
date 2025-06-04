package com.sos.js7.job.jocapi;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.encryption.exception.SOSEncryptionException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.exception.SOSKeyException;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class ApiExecutor {

    private static final String X_ID_TOKEN = "X-ID-TOKEN";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExecutor.class);

    // private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
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
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY = "js7.web.https.truststores";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH = "file";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_STOREPWD = "store-password";
    private static final String PRIVATE_FOLDER_NAME = "private";
    private static final String PRIVATE_CONF_FILENAME = "private.conf";

    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE = "js7.api-server.cs-file";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE = "js7.api-server.cs-key";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD = "js7.api-server.cs-password";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME = "js7.api-server.username";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_TOKEN = "js7.api-server.token";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD = "js7.api-server.password";
    private static final String PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PK_PATH = "js7.api-server.privatekey.path";
    private static final List<String> DO_NOT_LOG_KEY = Arrays.asList(new String[] { PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD,
            PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD, PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD, PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD,
            PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS, PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY, PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_STOREPWD,
            PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PK_PATH });
    // private static final String DO_NOT_LOG_VAL = PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_STOREPWD;
    private static final String JOB_ARGUMENT_DELIMITER_REGEX = "\\|";
    private static final String JOB_ARGUMENT_KEYSTORE_FILE = "js7.web.https.keystore.file";
    private static final String JOB_ARGUMENT_KEYSTORE_KEY_PASSWD = "js7.web.https.keystore.key-password";
    private static final String JOB_ARGUMENT_KEYSTORE_STORE_PASSWD = "js7.web.https.keystore.store-password";
    private static final String JOB_ARGUMENT_KEYSTORE_ALIAS = "js7.web.https.keystore.alias";
    private static final String JOB_ARGUMENT_TRUSTSTORE_FILE = "js7.web.https.truststore.file";
    private static final String JOB_ARGUMENT_TRUSTSTORE_PWD = "js7.web.https.truststore.store-password";
    private static final String JOB_ARGUMENT_APISERVER_URL = "js7.api-server.url";
    private static final String JOB_ARGUMENT_APISERVER_CSFILE = "js7.api-server.cs-file";
    private static final String JOB_ARGUMENT_APISERVER_CSKEY = "js7.api-server.cs-key";
    private static final String JOB_ARGUMENT_APISERVER_CSPASSWD = "js7.api-server.cs-password";
    private static final String JOB_ARGUMENT_APISERVER_BASIC_AUTH_USERNAME = "js7.api-server.username";
    private static final String JOB_ARGUMENT_APISERVER_BASIC_AUTH_TOKEN = "js7.api-server.token";
    private static final String JOB_ARGUMENT_APISERVER_BASIC_AUTH_PWD = "js7.api-server.password";
    private static final String JOB_ARGUMENT_APISERVER_PRIVATEKEYPATH = "js7.api-server.privatekey.path";

    private final OrderProcessStep<?> step;

    private SOSRestApiClient client;
    private URI jocUri;
    private List<String> jocUris;
    private Config config;
    private String truststorePath;
    private String keystorePath;
    private String truststorePasswd;
    private String keystorePasswd;
    private String keystoreKeyPasswd;
    private String truststoreType;
    private String keystoreType;
    private Map<String, DetailValue> jobResources;

    public ApiExecutor(OrderProcessStep<?> step) {
        this.step = step;
    }

    public void setJobResources(Map<String, DetailValue> jobResources) {
        this.jobResources = jobResources;
    }

    private String getReasonPhrase() {
        String reasonPhrase = "";
        if (this.getClient() != null && this.getClient().getHttpResponse() != null && this.getClient().getHttpResponse().getStatusLine() != null) {
            reasonPhrase = this.getClient().getHttpResponse().getStatusLine().getReasonPhrase();
        }
        return reasonPhrase;
    }

    public void setKeystoreCredentials(String path, String type, String storePwd, String keyPwd) {
        this.keystorePath = path;
        this.keystoreType = type;
        this.keystorePasswd = storePwd;
        this.keystoreKeyPasswd = keyPwd;
    }

    public void setTruststoreCredentials(String path, String type, String storePwd) {
        this.truststorePath = path;
        this.truststoreType = type;
        this.truststorePasswd = storePwd;
    }

    public ApiResponse login() throws Exception {
        /*
         * TODO: first check variables from OrderProcessStep if required values are available if available use this configuration if not available use
         * configuration from private.conf
         */
        boolean isDebugEnabled = step.getLogger().isDebugEnabled();
        step.getLogger().debug("***ApiExecutor***");
        jocUris = getUris();
        String latestError = "";
        // String latestResponse = "";
        Integer statusCode = -1;
        URI loginUri = null;
        Exception latestException = null;
        for (String uri : jocUris) {
            try {
                if (isDebugEnabled) {
                    step.getLogger().debug("processing Url - %s", uri);
                }
                tryCreateClient(uri);
                this.jocUri = URI.create(uri);
                loginUri = jocUri.resolve(WS_API_LOGIN);
                if (isDebugEnabled) {
                    step.getLogger().debug("send login to: %s", loginUri.toString());
                }
                String response = client.postRestService(loginUri, null);
                // latestResponse = response;
                statusCode = client.statusCode();
                if (isDebugEnabled) {
                    step.getLogger().debug("HTTP status code: %s", statusCode);
                }
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
                    if (isDebugEnabled) {
                        step.getLogger().debug("Connection to URI %s established.", loginUri.toString());
                    }
                    return new ApiResponse(statusCode, getReasonPhrase(), response, client.getResponseHeader(ACCESS_TOKEN_HEADER), null);
                } else {
                    // String message = statusCode + " : " + getReasonPhrase();
                    throw new Exception(latestError);
                }
            } catch (SOSConnectionRefusedException e) {
                latestError = String.format("connection to URI %1$s failed, trying next Uri.", loginUri.toString());
                if (isDebugEnabled) {
                    step.getLogger().debug(latestError);
                }
                latestException = e;
                continue;
            } catch (Exception e) {
                if (loginUri != null) {
                    latestError = String.format("connection to URI %1$s: %2$s occurred: %3$s", loginUri, e.getClass(), e.getMessage());
                } else {
                    latestError = String.format("%1$s occurred: %2$s", e.getClass(), e.getMessage());
                }
                if (isDebugEnabled) {
                    step.getLogger().debug(latestError);
                }

                latestException = e;
                continue;
            }
        }
        step.getLogger().info("No connection attempt was successful. Check agents private.conf.");
        throw latestException;
    }

    public ApiResponse post(String token, String apiUrl, String body) throws SOSConnectionRefusedException, SOSBadRequestException {
        if (token == null) {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        } else {
            if (jocUri != null) {
                boolean isDebugEnabled = step.getLogger().isDebugEnabled();
                try {
                    client.addHeader(ACCESS_TOKEN_HEADER, token);
                    client.addHeader(CONTENT_TYPE, APPLICATION_JSON);
                    if (isDebugEnabled) {
                        step.getLogger().debug("REQUEST: %s", apiUrl);
                        step.getLogger().debug("PARAMS: %s", body);
                    }
                    if (!apiUrl.toLowerCase().startsWith(WS_API_PREFIX)) {
                        apiUrl = WS_API_PREFIX + apiUrl;
                    }
                    if (isDebugEnabled) {
                        step.getLogger().debug("resolvedUri: %s", jocUri.resolve(apiUrl).toString());
                    }
                    String response = client.postRestService(jocUri.resolve(apiUrl), body);
                    if (isDebugEnabled) {
                        step.getLogger().debug("HTTP status code: %s", client.statusCode());
                    }
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
                step.getLogger().debug("send logout");
                String response = client.postRestService(jocUri.resolve(WS_API_LOGOUT), null);
                if (step.getLogger().isDebugEnabled()) {
                    step.getLogger().debug("HTTP status code: %s", client.statusCode());
                }
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
        List<String> uris = new ArrayList<String>();
        String apiServers = getDecrytedValueOfArgument(JOB_ARGUMENT_APISERVER_URL);
        if (apiServers != null) {
            String[] apiServersSplitted = apiServers.split(JOB_ARGUMENT_DELIMITER_REGEX);
            uris = Arrays.asList(apiServersSplitted).stream().peek(item -> item.trim()).toList();
        } else {
            if (config == null) {
                readConfig();
            }
            uris = config.getConfig(PRIVATE_CONF_JS7_PARAM_API_SERVER).getStringList(PRIVATE_CONF_JS7_PARAM_URL);
        }
        //Collections.sort(uris);
        return uris;

    }

    public List<String> getJocUris() {
        return jocUris;
    }

    private KeyStore readTrustStore(String path, KeystoreType type, String passwd) throws FileNotFoundException {
        if (!Files.exists(Paths.get(path))) {
            throw new FileNotFoundException(String.format("Cannot read from truststore %1$s. File does not exist!", path));
        }
        try {
            return KeyStoreUtil.readTrustStore(path, type, passwd);
        } catch (Exception e) {
            return null;
        }
    }

    private KeyStore readKeyStore(String path, KeystoreType type, String passwd) throws FileNotFoundException {
        if (!Files.exists(Paths.get(path))) {
            throw new FileNotFoundException(String.format("Cannot read from keystore %1$s. File does not exist!", path));
        }
        try {
            return KeyStoreUtil.readKeyStore(path, type, passwd);
        } catch (Exception e) {
            return null;
        }
    }

    private void applySSLContextCredentials(SOSRestApiClient client) throws KeyManagementException, SOSMissingDataException, NoSuchAlgorithmException,
            FileNotFoundException {
        List<KeyStoreCredentials> truststoresCredentials = new ArrayList<KeyStoreCredentials>();
        String truststores = getDecrytedValueOfArgument(JOB_ARGUMENT_TRUSTSTORE_FILE);
        
        if (truststores != null) {
            String[] truststoresSplitted = truststores.split(JOB_ARGUMENT_DELIMITER_REGEX);
            String truststorePwd = getDecrytedValueOfArgument(JOB_ARGUMENT_TRUSTSTORE_PWD, "");
            truststoresCredentials = Arrays.asList(truststoresSplitted).stream().peek(item -> item.trim()).map(path -> new KeyStoreCredentials(path,
                    truststorePwd)).toList();
        } else {
            if (config == null) {
                readConfig();
            }
            truststoresCredentials = readTruststoreCredentials(config);
        }
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        sslContextBuilder.setKeyManagerFactoryAlgorithm(KeyManagerFactory.getDefaultAlgorithm());
        sslContextBuilder.setTrustManagerFactoryAlgorithm(TrustManagerFactory.getDefaultAlgorithm());
        if (truststorePath != null && truststoreType != null && truststorePasswd != null) {
            KeyStore truststore = readTrustStore(truststorePath, KeystoreType.fromValue(truststoreType), truststorePasswd);
            if (truststore != null) {
                try {
                    sslContextBuilder.loadTrustMaterial(truststore, null);
                } catch (Exception e) {
                }
            }
        } else if (truststoresCredentials != null && !truststoresCredentials.isEmpty()) {
            truststoresCredentials.stream().forEach(item -> {
                KeyStore truststore;
                try {
                    truststore = readTrustStore(item.getPath(), KeystoreType.PKCS12, item.getStorePwd());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (truststore != null) {
                    try {
                        sslContextBuilder.loadTrustMaterial(truststore, null);
                    } catch (Exception e) {
                    }
                }
            });
        }
        KeyStoreCredentials credentials = null;
        String keyStoreFromOrder = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_FILE);
        
        if (keyStoreFromOrder != null) {
            String keyStoreKeyPwdFromOrder = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_KEY_PASSWD, "");
            String keyStoreStorePwdFromOrder = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_STORE_PASSWD, "");
            String keyStoreAliasFromOrder = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_ALIAS, "");

            credentials = new KeyStoreCredentials(keyStoreFromOrder, keyStoreStorePwdFromOrder, keyStoreKeyPwdFromOrder, keyStoreAliasFromOrder);
        } else {
            credentials = readKeystoreCredentials(config);
        }
        if (keystorePath != null && keystoreType != null && keystorePasswd != null) {
            KeyStore keystore = readKeyStore(keystorePath, KeystoreType.fromValue(keystoreType), keystorePasswd);
            if (keystore != null) {
                try {
                    if (keystoreKeyPasswd != null) {
                        sslContextBuilder.loadKeyMaterial(keystore, keystoreKeyPasswd.toCharArray());
                    } else {
                        sslContextBuilder.loadKeyMaterial(keystore, "".toCharArray());
                    }
                } catch (Exception e) {
                }
            }
        } else if (credentials != null) {
            KeyStore keystore = readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
            try {
                if (keystoreKeyPasswd != null) {
                    sslContextBuilder.loadKeyMaterial(keystore, keystoreKeyPasswd.toCharArray());
                } else {
                    sslContextBuilder.loadKeyMaterial(keystore, "".toCharArray());
                }
            } catch (Exception e) {
            }
        }
        client.setSSLContext(sslContextBuilder.build());
    }

    private void tryCreateClient(String jocUri) throws SOSMissingDataException, KeyManagementException, SOSKeePassDatabaseException,
            NoSuchAlgorithmException, FileNotFoundException {
        if (client != null) {
            client.closeHttpClient();
        }
        if (config == null) {
            readConfig();
        }
        client = new SOSRestApiClient();
        
        String csFile = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSFILE);
        String pwd = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_PWD);
        String username = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_USERNAME);
        
        if (csFile != null || (username != null && pwd != null)) {
            
            String token = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_TOKEN, "");
            String csPwd = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSPASSWD);
            String csKey = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSKEY);
            
            setBasicAuthorizationIfExists(username, pwd, token, csFile, csKey, csPwd);
        } else {
            setBasicAuthorizationIfExists(config);
        }
        step.getLogger().debug("initiate REST api client");
        if (jocUri.toLowerCase().startsWith("https:")) {
            applySSLContextCredentials(client);
        }
    }
    
    private String getDecrytedValueOfArgument(String key) {
        return getDecrytedValueOfArgument(key, null);
    }
    
    private String getDecrytedValueOfArgument(String key, String _default) {
        return getDecryptedValue(getValueOfArgument(key, _default), key);
    }
    
    private String getValueOfArgument(String key) {
        return getValueOfArgument(key, null);
    }
    
    private String getValueOfArgument(String key, String _default) {
        return getValueOfArgument(step.getAllArguments().get(key), _default);
    }
    
    private String getValueOfArgument(JobArgument<?> arg, String _default) {
        return Optional.ofNullable(arg).map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse(_default);
    }

    private String getPrivateKeyPath() {
        try {
            String privateKeyPath = getValueOfArgument(JOB_ARGUMENT_APISERVER_PRIVATEKEYPATH);
            if (privateKeyPath != null) {
                return privateKeyPath;
            } else {
                return config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PK_PATH);
            }
        } catch (ConfigException e) {
            step.getLogger().error("no private key path found in private.conf.");
        }
        return "";
    }

    private String getDecryptedValue(String in, String propertyName) {
        return getDecryptedValue(in, null, propertyName);
    }

    private String getDecryptedValue(String in, String privateKeyPath, String propertyName) {
        if (in != null && in.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER)) {
            String encryptedIn = in;
            if (encryptedIn.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER + "//")) {
                encryptedIn = encryptedIn.substring((EncryptionUtils.ENCRYPTION_IDENTIFIER + "//").length());
            } else {
                encryptedIn = encryptedIn.substring((EncryptionUtils.ENCRYPTION_IDENTIFIER).length());
            }

            if (privateKeyPath == null) {
                privateKeyPath = getPrivateKeyPath();
            }

            try {
                in = decryptValue(encryptedIn, propertyName, privateKeyPath);
            } catch (SOSKeyException | SOSMissingDataException | SOSEncryptionException e) {
                step.getLogger().error("error occurred decrypting ".concat(propertyName).concat(" !"), e);
            }
        }
        return in;
    }

    private void setBasicAuthorizationIfExists(String username, String pwd, String token, String csFile, String csKeyFile, String csPwd)
            throws SOSKeePassDatabaseException {
        if (csFile != null && !csFile.isEmpty()) {
            csFile = getDecryptedValue(csFile, JOB_ARGUMENT_APISERVER_CSFILE);

            SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
            username = resolver.resolve(username);
            pwd = resolver.resolve(pwd);
            token = resolver.resolve(token);
        }
        if (token.isEmpty()) {
            username = getDecryptedValue(username, "username");
            pwd = getDecryptedValue(pwd, "password");
        }
        if (!token.isEmpty() && jobResources != null) {
            LOGGER.debug("get jobresource and variable from token:" + token);
            String[] tokenJobResource = token.split(":");
            if (tokenJobResource.length == 2) {
                String variableName = tokenJobResource[1];
                String jobResourceName = tokenJobResource[0];
                DetailValue detailValue = jobResources.get(variableName);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(SOSString.toString(detailValue));
                    LOGGER.debug("variableName:" + variableName);
                    LOGGER.debug("jobResourceName:" + jobResourceName);
                }
                if (detailValue != null) {
                    if (detailValue.getSource().equals(jobResourceName)) {
                        token = (String) detailValue.getValue();
                    } else {
                        LOGGER.info("Name of JobResource: " + detailValue.getSource() + " does not match the " + tokenJobResource[0]);
                    }
                }
            }
            client.addHeader(X_ID_TOKEN, token);
        }
        if (!SOSString.isEmpty(username) && !SOSString.isEmpty(pwd)) {
            String basicAuth = Base64.getMimeEncoder().encodeToString((username + ":" + pwd).getBytes());
            client.setBasicAuthorization(basicAuth);
        }
    }

    private void setBasicAuthorizationIfExists(Config config) throws SOSKeePassDatabaseException, SOSMissingDataException {
        String csFile = null;
        String csKeyFile = null;
        String csPwd = null;
        try {
            csFile = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_FILE);
        } catch (Exception e) {
            step.getLogger().debug(e.getMessage());
        }
        try {
            csKeyFile = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_KEYFILE);
        } catch (Exception e) {
            step.getLogger().debug(e.getMessage());
        }
        try {
            csPwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_CS_PWD);
        } catch (Exception e) {
            step.getLogger().debug(e.getMessage());
        }
        String username = "";
        String pwd = "";
        String token = "";
        if (csFile != null && !csFile.isEmpty()) {
            SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
            username = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME));
            pwd = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD));
            token = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_TOKEN));
        } else {
            try {
                token = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_TOKEN);
            } catch (ConfigException e) {
                step.getLogger().debug("no token found in private.conf.");
            }

            if (token.isEmpty()) {
                try {
                    username = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME);
                } catch (ConfigException e) {
                    step.getLogger().debug("no username found in private.conf.");

                }
                try {
                    pwd = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD);
                } catch (ConfigException e) {
                    step.getLogger().debug("no (user-)password found in private.conf.");
                }

                String privateKeyPath = null;
                username = getDecryptedValue(username, privateKeyPath, "username");
                pwd = getDecryptedValue(pwd, privateKeyPath, "password");
            }

        }
        if (!token.isEmpty() && jobResources != null) {

            LOGGER.debug("get jobresource and variable from token:" + token);

            String[] tokenJobResource = token.split(":");
            if (tokenJobResource.length == 2) {
                String variableName = tokenJobResource[1];
                String jobResourceName = tokenJobResource[0];
                DetailValue detailValue = jobResources.get(variableName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(SOSString.toString(detailValue));
                    LOGGER.debug("variableName:" + variableName);
                    LOGGER.debug("jobResourceName:" + jobResourceName);
                }
                if (detailValue != null) {
                    if (detailValue.getSource().equals(jobResourceName)) {
                        token = (String) detailValue.getValue();
                    } else {
                        LOGGER.info("Name of JobResource: " + detailValue.getSource() + " does not match the " + tokenJobResource[0]);
                    }
                }
            }
            client.addHeader(X_ID_TOKEN, token);
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
        boolean isDebugEnabled = step.getLogger().isDebugEnabled();

        String agentConfDirPath = System.getenv(AGENT_CONF_DIR_ENV_PARAM);
        if (agentConfDirPath == null) {
            agentConfDirPath = System.getProperty(AGENT_CONF_DIR_ENV_PARAM);
        }
        if (agentConfDirPath == null) {
            throw new SOSMissingDataException(String.format(
                    "Environment variable %1$s not set. Can´t read credentials from agents private.conf file.", AGENT_CONF_DIR_ENV_PARAM));
        }
        if (isDebugEnabled) {
            step.getLogger().debug("agentConfDirPath: %s", agentConfDirPath);
        }
        Path agentConfDir = Paths.get(agentConfDirPath);
        // set default com.typesafe.config.Config
        Config defaultConfig = ConfigFactory.load();
        // set initial properties - js7.config-directory
        Properties props = new Properties();
        props.setProperty(PRIVATE_CONF_JS7_PARAM_CONFDIR, agentConfDir.toString());
        Path privatFolderPath = agentConfDir.resolve(PRIVATE_FOLDER_NAME);
        if (isDebugEnabled) {
            step.getLogger().debug("agents private folder: %s", privatFolderPath.toString());
        }
        Config defaultConfigWithAgentConfDir = ConfigFactory.parseProperties(props).withFallback(defaultConfig).resolve();
        if (isDebugEnabled) {
            step.getLogger().debug(PRIVATE_CONF_JS7_PARAM_CONFDIR + " (Config): " + defaultConfigWithAgentConfDir.getString(
                    PRIVATE_CONF_JS7_PARAM_CONFDIR));
        }
        if (Files.exists(privatFolderPath.resolve(PRIVATE_CONF_FILENAME))) {
            config = ConfigFactory.parseFile(privatFolderPath.resolve(PRIVATE_CONF_FILENAME).toFile()).withFallback(defaultConfigWithAgentConfDir)
                    .resolve();
            if (config != null) {
                for (Entry<String, ConfigValue> entry : config.entrySet()) {
                    if (entry.getKey().startsWith("js7")) {
                        if (!DO_NOT_LOG_KEY.contains(entry.getKey())) {
                            if (isDebugEnabled) {
                                step.getLogger().debug(entry.getKey() + ": " + entry.getValue().toString());
                            }
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
            if (step.getLogger().isDebugEnabled()) {
                step.getLogger().debug("read Keystore from: %s", config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
            }
        } catch (ConfigException e) {
            step.getLogger().debug("no keystore file found in private.conf.");
        }
        String keyPasswd = null;
        try {
            keyPasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD);
        } catch (ConfigException e) {
            step.getLogger().debug("no keystore key-password found in private.conf.");
        }
        String storePasswd = null;
        try {
            storePasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD);
        } catch (ConfigException e) {
            step.getLogger().debug("no keystore store-password found in private.conf.");
        }
        String alias = null;
        try {
            alias = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS);
        } catch (ConfigException e) {
            step.getLogger().debug("no (key-)alias found in private.conf.");
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
            credentials = config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY).stream().map(item -> new KeyStoreCredentials(item.getString(
                    PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH), item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_STOREPWD))).filter(
                            Objects::nonNull).collect(Collectors.toList());
            if (step.getLogger().isDebugEnabled()) {
                step.getLogger().debug("read Truststore from: %s", config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY).get(0).getString(
                        PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
            }
        } catch (ConfigException e) {
            step.getLogger().debug("no truststore credentials found in private.conf.");
        }
        if (credentials != null) {
            return credentials;
        } else {
            return Collections.emptyList();
        }
    }

    private void handleExitCode(SOSRestApiClient client) throws SOSConnectionRefusedException {
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

    public ISOSLogger getLogger() {
        return step.getLogger();
    }

    private String decryptValue(String encryptedValue, String propertyName, String privateKeyPath) throws SOSKeyException, SOSMissingDataException,
            SOSEncryptionException {
        return decryptValue(encryptedValue, propertyName, Paths.get(privateKeyPath));
    }

    private String decryptValue(String encryptedValue, String propertyName, Path privateKeyPath) throws SOSKeyException, SOSMissingDataException,
            SOSEncryptionException {
        PrivateKey pk = KeyUtil.getPrivateKey(privateKeyPath);
        if (pk == null) {
            throw new SOSMissingDataException("encrypted values found, but no private key provided or path wrong for decryption!");
        }
        EncryptedValue encrypted = EncryptedValue.getInstance(propertyName, encryptedValue);
        return Decrypt.decrypt(encrypted, pk);
    }
}
