package com.sos.jitl.jobs.common;

import java.net.URI;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.util.SOSString;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class JobApiExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobApiExecutor.class);

    private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
    private static final String WS_API_LOGIN = "/joc/api/authentication/login";
    private static final String WS_API_LOGOUT = "/joc/api/authentication/logout";

    private SOSRestApiClient client = null;
    private URI jocUri = null;
    private String truststoreFile;

    public JobApiExecutor() {
        this(null, null);
    }

    public JobApiExecutor(String truststoreFile, URI jocUri) {
        this.truststoreFile = truststoreFile == null ? DEFAULT_TRUSTSTORE_FILENAME : truststoreFile;
        this.jocUri = jocUri;
    }

    public String login() throws Exception {
        tryCreateClient();

        String response = client.postRestService(jocUri.resolve(WS_API_LOGIN), null);
        LOGGER.trace("HTTP status code: " + client.statusCode());
        LOGGER.trace("response from web server: " + response);
        return client.getResponseHeader("X-Access-Token");
    }

    public void logout(String token) throws Exception {
        if (token != null) {
            tryCreateClient();

            client.addHeader("X-Access-Token", token);
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
        System.setProperty("js7.config-directory", Job.getAgentConfigDir().toFile().getCanonicalPath());
        Config config = readConfig(Job.getAgentPrivateConfFile());
        String jocUri = config.getString("js7.web.joc.url");
        if (!SOSString.isEmpty(jocUri)) {
            this.jocUri = URI.create(jocUri);
        }
        if (this.jocUri == null) {
            throw new Exception("missing jocUri");
        }

        List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(config);
        KeyStore truststore = truststoresCredentials.stream().filter(item -> item.getPath().endsWith(truststoreFile)).map(item -> {
            try {
                return KeyStoreUtil.readTrustStore(item.getPath(), KeyStoreType.PKCS12, item.getStorePwd());
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).findFirst().get();
        KeyStoreCredentials credentials = readKeystoreCredentials(config);
        KeyStore keystore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeyStoreType.PKCS12, credentials.getStorePwd());

        client = new SOSRestApiClient();
        client.setSSLContext(keystore, credentials.getKeyPwd().toCharArray(), truststore);

    }

    private void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }

    private Config readConfig(Path config) {
        return ConfigFactory.parseFile(config.toFile()).withFallback(ConfigFactory.load()).resolve();
    }

    private static KeyStoreCredentials readKeystoreCredentials(Config config) {
        String keystorePath = config.getString("js7.web.https.keystore.file");
        String keyPasswd = config.getString("js7.web.https.keystore.key-password");
        String storePasswd = config.getString("js7.web.https.keystore.store-password");
        return new KeyStoreCredentials(keystorePath, storePasswd, keyPasswd);
    }

    private static List<KeyStoreCredentials> readTruststoreCredentials(Config config) {
        List<? extends Config> list = config.getConfigList("js7.web.https.truststores");
        List<KeyStoreCredentials> credentials = null;
        credentials = list.stream().map(item -> {
            return new KeyStoreCredentials(item.getString("file"), item.getString("store-password"), null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (credentials != null) {
            return credentials;
        } else {
            return Collections.emptyList();
        }
    }

}
