package com.sos.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.keyStore.KeyStoreCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.commons.util.SOSString;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.model.publish.rollout.items.JocConf;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

public class ExecuteRollOut {
    
    public static final String PRIVATE_CONF_JS7_PARAM_CONFDIR = "js7.config-directory";

    private static final String WS_API = "/joc/api/authentication/certificate/create";
    private static final String HELP = "--help";
    private static final String DN_ONLY = "--dn-only";
    private static final String TOKEN = "--token";
    private static final String JOC_URI = "--joc-uri";
    private static final String SAN = "--san";
    private static final String SRC_KEYSTORE = "--source-keystore";
    private static final String SRC_KEYSTORE_TYPE = "--source-keystore-type";
    private static final String SRC_KEYSTORE_PASS = "--source-keystore-pass";
    private static final String SRC_KEYSTORE_ENTRY_PASS = "--source-keystore-entry-pass";
    private static final String SRC_KEYSTORE_ENTRY_ALIAS = "--source-keystore-entry-alias";
    private static final String SRC_TRUSTSTORE = "--source-truststore";
    private static final String SRC_TRUSTSTORE_TYPE = "--source-truststore-type";
    private static final String SRC_TRUSTSTORE_PASS = "--source-truststore-pass";
    private static final String TRG_KEYSTORE = "--target-keystore";
    private static final String TRG_KEYSTORE_TYPE = "--target-keystore-type";
    private static final String TRG_KEYSTORE_PASS = "--target-keystore-pass";
    private static final String TRG_KEYSTORE_ENTRY_PASS = "--target-keystore-entry-pass";
    private static final String TRG_TRUSTSTORE = "--target-truststore";
    private static final String TRG_TRUSTSTORE_TYPE = "--target-truststore-type";
    private static final String TRG_TRUSTSTORE_PASS = "--target-truststore-pass";
    private static final String SUBJECT_DN = "--subject-dn";
    private static final String KS_ALIAS = "--key-alias";
    private static final String TS_ALIAS = "--ca-alias";
    private static final String SRC_PRIVATE_KEY = "--source-private-key";
    private static final String SRC_CERT = "--source-certificate";
    private static final String SRC_CA_CERT = "--source-ca-cert";
    private static final String PRIVATE_FOLDER_NAME = "private";
    private static final String PRIVATE_CONF_FILENAME = "private.conf";
    private static final String PRIVATE_CONF_JS7_PARAM_WEB = "js7.web";
    private static final String PRIVATE_CONF_JS7_PARAM_JOCURL = "js7.web.joc.url";
    private static final String PRIVATE_CONF_JS7_PARAM_API_SERVER = "js7.web.api-server";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH = "js7.web.https.keystore.file";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD = "js7.web.https.keystore.key-password";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD = "js7.web.https.keystore.store-password";
    private static final String PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS = "js7.web.https.keystore.alias";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY = "js7.web.https.truststores";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH = "file";
    private static final String PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD = "store-password";
    private static final String PRIVATE_CONF_JS7_PARAM_DN = "js7.auth.users.controller.distinguished-names"; 
    private static final String PRIVATE_CONF_JS7_PARAM_USERS = "js7.auth.users"; 
    private static final String PRIVATE_CONF_JS7_PARAM_DISTINGUISHED_NAMES = "distinguished-names"; 
    private static final String DEFAULT_KEYSTORE_FILENAME = "https-keystore.p12";
    private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
    private static final String DEFAULT_KEYSTORE_PATH = "${js7.config-directory}\"/private/https-keystore.p12\"";
    private static final String DEFAULT_KEYSTORE_PATH_UNQUOTED = "${js7.config-directory}/private/https-keystore.p12";
    private static final String DEFAULT_TRUSTSTORE_PATH = "${js7.config-directory}\"/private/https-truststore.p12\"";
    private static final String DEFAULT_TRUSTSTORE_PATH_UNQUOTED = "${js7.config-directory}/private/https-truststore.p12";
    private static final String DEFAULT_STORE_PWD = "jobscheduler";
    private static final ConfigParseOptions PARSE_OPTIONS = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF);
    private static final ConfigRenderOptions RENDER_OPTIONS = ConfigRenderOptions.concise().setComments(true).setOriginComments(false)
            .setFormatted(true).setJson(false);
    private static final ConfigResolveOptions RESOLVE_OPTIONS = ConfigResolveOptions.noSystem().setUseSystemEnvironment(false)
            .setAllowUnresolved(true);
//    private static final List<String> CONFIG_DIRECTORY_KEYS = Arrays.asList("js7.configuration.trusted-signature-keys", 
//            "js7.web.https.keystore", "js7.web.https.truststores");
    private static SOSRestApiClient client;
    private static String token;
    private static String subjectDN;
    private static String san;
    private static URI jocUri;
    private static List<URI> jocUris = new ArrayList<URI>();;
    private static String srcKeystore;
    private static String srcKeystoreType = "PKCS12";
    private static String srcKeystorePasswd;
    private static String srcKeystoreEntryPasswd;
    private static String srcKeystoreEntryAlias;
    private static String srcTruststore;
    private static String srcTruststoreType = "PKCS12";
    private static String srcTruststorePasswd;
    private static String targetKeystore;
    private static String targetKeystoreType = "PKCS12";
    private static String targetKeystorePasswd;
    private static String targetKeystoreEntryPasswd;
    private static String targetTruststore;
    private static String targetTruststoreType = "PKCS12";
    private static String targetTruststorePasswd;
    private static String keyAlias;
    private static String caAlias;
    private static String srcPrivateKeyPath;
    private static String srcCertPath;
    private static String srcCaCertPath;
    private static String confDir;
    private static Config resolved;
    private static Config toUpdate;
    private static boolean dnOnly = false;
    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);


    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("cert")) {
            args = Arrays.stream(args).skip(1).toArray(String[]::new);
        }
        if (args == null || args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("--help"))) {
            printUsage();
        } else {
            for(int i = 0; i < args.length; i++) {
                String[] split = args[i].split("=", 2);
                if (args[i].startsWith(TOKEN + "=")) {
                    token = split[1];
                } else if (args[i].startsWith(JOC_URI + "=")) {
                    jocUri = URI.create(split[1]);
                } else if (args[i].startsWith(SRC_KEYSTORE + "=")) {
                    srcKeystore = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_TYPE + "=")) {
                    srcKeystoreType = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_PASS + "=")) {
                    srcKeystorePasswd = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_ENTRY_PASS + "=")) {
                    srcKeystoreEntryPasswd = split[1];
                } else if (args[i].startsWith(SRC_KEYSTORE_ENTRY_ALIAS + "=")) {
                    srcKeystoreEntryAlias = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE + "=")) {
                    srcTruststore = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE_TYPE + "=")) {
                    srcTruststoreType = split[1];
                } else if (args[i].startsWith(SRC_TRUSTSTORE_PASS + "=")) {
                    srcTruststorePasswd = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE + "=")) {
                    targetKeystore = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE_TYPE + "=")) {
                    targetKeystoreType = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE_PASS + "=")) {
                    targetKeystorePasswd = split[1];
                } else if (args[i].startsWith(TRG_KEYSTORE_ENTRY_PASS + "=")) {
                    targetKeystoreEntryPasswd = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE + "=")) {
                    targetTruststore = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE_TYPE + "=")) {
                    targetTruststoreType = split[1];
                } else if (args[i].startsWith(TRG_TRUSTSTORE_PASS + "=")) {
                    targetTruststorePasswd = split[1];
                } else if (args[i].startsWith(SUBJECT_DN + "=")) {
                    subjectDN = split[1];
                } else if (args[i].startsWith(SAN + "=")) {
                    san = split[1];
                } else if (args[i].startsWith(KS_ALIAS + "=")) {
                    keyAlias = split[1];
                } else if (args[i].startsWith(TS_ALIAS + "=")) {
                    caAlias = split[1];
                } else if (args[i].startsWith(SRC_PRIVATE_KEY + "=")) {
                    srcPrivateKeyPath = split[1];
                } else if (args[i].startsWith(SRC_CERT + "=")) {
                    srcCertPath = split[1];
                } else if (args[i].startsWith(SRC_CA_CERT + "=")) {
                    srcCaCertPath = split[1];
                } else if (args[i].startsWith(DN_ONLY)) {
                    dnOnly = true;
                }
            }
            readConfig();
//            setKeyStoreCredentials();
            try {
                createClient();
                String response = callWebService();
                if(client.getHttpResponse() == null) {
                    throw new Exception("no connection to any api-server url could be established, latest error: " + response);
                }
                if (client.statusCode() != 200) {
                    String message = "";
                    try {
                        message = parseErrorResponse(response);
                    } catch (Exception e) {
                        throw new Exception("API return code was " + client.statusCode());
                    } 
                    throw new Exception(message);
                } else {
                    RolloutResponse rollout = mapper.readValue(response, RolloutResponse.class);
                    if (!dnOnly) {
                        addKeyAndCertToStore(rollout);
                    }
                    if(confDir != null) {
                        updatePrivateConf(toUpdate, rollout);
                    } else {
                        System.out.println("no entries stored to private.conf file. Environment variable <" + PRIVATE_CONF_JS7_PARAM_CONFDIR + "> not properly set!");
                    }
                }
            } catch (Throwable e) {
                System.out.println(e.toString());
            } finally {
                closeClient();
            }             
        }
    }
    
    private static KeyStore createKeyStore (boolean isKeystore)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keystore = null;
        Path path = null;
        String passwd = null;
        if(isKeystore) {
            if(targetKeystore != null && !targetKeystore.isEmpty()) {
                path = Paths.get(targetKeystore);
            } else {
                path = Paths.get(confDir).resolve(PRIVATE_FOLDER_NAME).resolve(DEFAULT_KEYSTORE_FILENAME);
            }
            if(targetKeystorePasswd != null) {
                passwd = targetKeystorePasswd;
            } else {
                passwd = DEFAULT_STORE_PWD;
            }
        } else {
            if(targetTruststore != null && !targetTruststore.isEmpty()) {
                path = Paths.get(targetTruststore);
            } else {
                path = Paths.get(confDir).resolve(PRIVATE_FOLDER_NAME).resolve(DEFAULT_TRUSTSTORE_FILENAME);
            }
            if(targetTruststorePasswd != null) {
                passwd = targetTruststorePasswd;
            } else {
                passwd = DEFAULT_STORE_PWD;
            }
        }
        Files.createDirectories(path.getParent());
        keystore = KeyStore.getInstance(KeystoreType.PKCS12.value().toLowerCase());
        try (OutputStream os = Files.newOutputStream(path)){
            keystore.load(null, passwd.toCharArray());
            keystore.store(os, passwd.toCharArray());
        }
        return keystore;
    }
    
    private static String parseErrorResponse(String response) throws IOException {
//      ^.*code\":\"(.*)\",\"message\":\"([.[^\"]]*)(.*)$
        try {
            Pattern pattern = Pattern.compile("^.*code\\\":\\\"(.*)\\\",\\\"message\\\":\\\"([.[^\\\"]]*).*$");
            String code = "";
            String message = "";
            Reader reader = new StringReader(response);
            BufferedReader buff = new BufferedReader(reader);
            String line = null;
            while ((line = buff.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                if(matcher.matches()) {
                    code = matcher.group(1);
                    message = matcher.group(2);
                    continue;
                }
            }
            return code + " : " + message;
        } catch (Exception e) {
            return response;
        }
    }
    
    private static void updatePrivateConf (Config config, RolloutResponse response) throws Exception {
        String dnPath = PRIVATE_CONF_JS7_PARAM_DN;
        if(response.getControllerId() != null) {
            dnPath = PRIVATE_CONF_JS7_PARAM_USERS + "." + response.getControllerId() + "." + PRIVATE_CONF_JS7_PARAM_DISTINGUISHED_NAMES;
        }
        if (config.hasPath(dnPath)) {
            config = addToExistingList(config, dnPath, response.getDNs());
        } else {
            config = addToNewList(config, dnPath, response.getDNs());
        }
        if(response.getJocConfs() != null) {
            for (JocConf joc : response.getJocConfs()) {
                if(response.getAgentId() == null) {
                    dnPath = PRIVATE_CONF_JS7_PARAM_USERS + "." + joc.getJocId().toUpperCase() + "." + PRIVATE_CONF_JS7_PARAM_DISTINGUISHED_NAMES;
                    if(config.hasPath(dnPath)) {
                        config = addToExistingList(config, dnPath, joc.getDN());
                    } else {
                        config = addToNewList(config, dnPath, joc.getDN());
                    }
                } else {
                    String webPath = PRIVATE_CONF_JS7_PARAM_WEB + "." + joc.getJocId() + ".url";
                    if (config.hasPath(webPath)) {
                        try {
                            config = addToExistingList(config, webPath, joc.getUrl());
                        } catch (Exception e) {
                            if (config.getString(webPath).equals(joc.getUrl())) {
                                continue;
                            } else {
                                List<String> newValues = new ArrayList<String>();
                                newValues.add(config.getString(webPath));
                                newValues.add(joc.getUrl());
                                config = addToNewList(config, webPath, newValues);
                            }
                        }
                    } else {
                        config = addToNewList(config, webPath, joc.getUrl());
                    }
                }
            }
        }
        saveConfigToFile(config);
    }
    
    private static Config addToExistingList (Config config, String configPath, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        return addToExistingList(config, configPath, values);
    }
    
    private static Config addToExistingList (Config config, String configPath, List<String> newValues) {
        List<String> values = config.getStringList(configPath);
        for (String value : newValues) {
            if(!values.contains(value)) {
                values.add(value);
            }
        }
        ConfigValue configValue = ConfigValueFactory.fromAnyRef(values);
        return config.withValue(configPath, configValue);
    }
    
    private static Config addToNewList (Config config, String configPath, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        return addToNewList(config, configPath, values);
    }
    
    private static Config addToNewList (Config config, String configPath, List<String> values) {
        List<String> newValues = new ArrayList<String>();
        newValues.addAll(values);
        ConfigValue configValue = ConfigValueFactory.fromIterable(newValues);
        return config.withValue(configPath, configValue);
    }
    
    private static void saveConfigToFile(final Config config) throws IOException {
        String configAsHoconString = config.root().render(RENDER_OPTIONS);
        configAsHoconString = configAsHoconString.replace(confDir, "${" + PRIVATE_CONF_JS7_PARAM_CONFDIR + "}");
        Files.write(Paths.get(confDir).resolve(PRIVATE_FOLDER_NAME).resolve(PRIVATE_CONF_FILENAME), configAsHoconString.getBytes());
    }
    
    private static void readConfig() {
        // set default com.typesafe.config.Config
        /*
         * set initial properties 
         * - js7.config-directory
         * 
         * */
        confDir = System.getProperty(PRIVATE_CONF_JS7_PARAM_CONFDIR);
        Properties props = new Properties();
        if (confDir != null && !confDir.isEmpty()) {
            props.put(PRIVATE_CONF_JS7_PARAM_CONFDIR, confDir);
            // original file without substitution
            toUpdate = ConfigFactory.parseFile(Paths.get(confDir).resolve(PRIVATE_FOLDER_NAME).resolve(PRIVATE_CONF_FILENAME).toFile(), PARSE_OPTIONS).resolve(RESOLVE_OPTIONS);
            // Config to substitute
            Config defaultConfigWithConfDir = ConfigFactory.parseProperties(props, PARSE_OPTIONS).resolve();
            // file with substituted values
            resolved = ConfigFactory.parseFile(Paths.get(confDir).resolve(PRIVATE_FOLDER_NAME).resolve(PRIVATE_CONF_FILENAME).toFile(), PARSE_OPTIONS)
                    .withFallback(defaultConfigWithConfDir).resolve();
        }
    }

    private static void setKeystoreCredentials() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;
        String alias = "";
        if (targetKeystore != null && !targetKeystore.isEmpty()) {
            // args
            keyStore = KeyStoreUtil.readKeyStore(targetKeystore, KeystoreType.fromValue(targetKeystoreType), targetKeystorePasswd);
            alias = keyAlias;
        } else if (resolved != null) {
            // private.conf
            KeyStoreCredentials credentials = readKeystoreCredentials(resolved);
            keyStore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
        } else {
            //default
            keyStore = KeyStoreUtil.readKeyStore("", KeystoreType.PKCS12, null);
        }
    }
    
    private static void addKeyAndCertToStore(RolloutResponse rolloutResponse) throws Exception {
        KeyStore targetKeyStore = null;
        KeyStore targetTrustStore = null;
        Boolean keyStoreAlreadyExists = false;
        Boolean trustStoreAlreadyExists = false;
        try {
            X509Certificate certificate = KeyUtil.getX509Certificate(rolloutResponse.getJocKeyPair().getCertificate());
            PrivateKey privKey = KeyUtil.getPrivateECDSAKeyFromString(rolloutResponse.getJocKeyPair().getPrivateKey());
            X509Certificate rootCaCertificate = KeyUtil.getX509Certificate(rolloutResponse.getCaCert());
            Certificate[] chain = new Certificate[] {certificate, rootCaCertificate}; 
            if (targetKeystore != null && !targetKeystore.isEmpty()) {
                Path targetKeystorePath = Paths.get(targetKeystore);
                keyStoreAlreadyExists = Files.exists(targetKeystorePath);
                if (!keyStoreAlreadyExists) {
                    targetKeyStore = createKeyStore(true);
                }
                targetKeyStore = KeyStoreUtil.readKeyStore(targetKeystore, KeystoreType.fromValue(targetKeystoreType), targetKeystorePasswd);
                if (targetKeyStore != null) {
                    if (keyAlias != null && !keyAlias.isEmpty()) {
                        targetKeyStore.setKeyEntry(keyAlias, privKey, targetKeystoreEntryPasswd.toCharArray(), chain);
                        targetKeyStore.store(new FileOutputStream(new File(targetKeystore)), targetKeystorePasswd.toCharArray());
                    } else {
                        System.err.println(String.format("no alias provided for the certificate and its private key. Parameter <%1$s> is required.", KS_ALIAS));
                    }
                }
            } else if (resolved != null) {
                KeyStoreCredentials credentials = readKeystoreCredentials(resolved);
                targetKeyStore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
                if (targetKeyStore != null) {
                    String defaultAlias = CertificateUtils.extractDistinguishedNameQualifier(certificate);
                    if (defaultAlias != null) {
                        targetKeyStore.setKeyEntry(defaultAlias, privKey, resolved.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD).toCharArray(), chain);
                        // targetKeystoreEntryPasswd.toCharArray(), 
                        targetKeyStore.store(new FileOutputStream(new File(credentials.getPath())), credentials.getStorePwd().toCharArray());
                    } else {
                        System.err.println(String.format("no alias provided for the certificate and its private key. Parameter <%1$s> is required.", KS_ALIAS));
                    }
                }
            } else {
                System.err.println(String.format("no keystore found. Parameter <%1$s> is required.", TRG_KEYSTORE));
            }
            if (targetTruststore != null && !targetTruststore.isEmpty()) {
                trustStoreAlreadyExists = Files.exists(Paths.get(targetTruststore));
                if (!trustStoreAlreadyExists) {
                    targetTrustStore = createKeyStore(false);
                }
                targetTrustStore = KeyStoreUtil.readTrustStore(targetTruststore, KeystoreType.fromValue(targetTruststoreType), targetTruststorePasswd);
                if (targetTrustStore != null) {
                    if (caAlias != null && !caAlias.isEmpty()) {
                        targetTrustStore.setCertificateEntry(caAlias, rootCaCertificate);
                        targetTrustStore.store(new FileOutputStream(new File(targetTruststore)), targetTruststorePasswd.toCharArray());
                    } else {
                        System.err.println(String.format("no alias provided for the CA certificate. Parameter <%1$s> is required.", TS_ALIAS));
                    }
                }
            } else if (resolved != null) {
                List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(resolved);
                Optional<KeyStoreCredentials> defaultTruststoreCredentials = truststoresCredentials.stream()
                        .filter(item -> item.getPath().endsWith(DEFAULT_TRUSTSTORE_FILENAME)).filter(Objects::nonNull).findFirst();
                if (defaultTruststoreCredentials.isPresent()) {
                    KeyStoreCredentials credentials = defaultTruststoreCredentials.get();
                    targetTrustStore = KeyStoreUtil.readTrustStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
                    if (targetTrustStore != null) {
                        String defaultAlias = CertificateUtils.extractDistinguishedNameQualifier(rootCaCertificate);
                        if (defaultAlias == null) {
                            defaultAlias = CertificateUtils.extractFirstCommonName(rootCaCertificate);
                        }
                        if (defaultAlias != null) {
                            targetTrustStore.setCertificateEntry(defaultAlias, rootCaCertificate);
                            targetTrustStore.store(new FileOutputStream(new File(credentials.getPath())), credentials.getStorePwd().toCharArray());
                        } else {
                            System.err.println(String.format("no alias provided for the certificate and its private key. Parameter <%1$s> is required.", KS_ALIAS));
                        }
                    }
                }
            } else {
                System.err.println(String.format("no truststore found. Parameter <%1$s> is required.", TRG_TRUSTSTORE));
            }
        } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println(e.toString());
        }
        if(targetKeyStore != null) {
            ConfigValue keystorePathValue = null;
            if(!keyStoreAlreadyExists) {
                keystorePathValue = ConfigValueFactory.fromAnyRef(DEFAULT_KEYSTORE_PATH_UNQUOTED);
            } else {
                keystorePathValue = ConfigValueFactory.fromAnyRef(targetKeystore);
            }
            toUpdate = toUpdate.withValue(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH, keystorePathValue);
            ConfigValue keystorePasswdValue = ConfigValueFactory.fromAnyRef(targetKeystorePasswd);
            toUpdate = toUpdate.withValue(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD, keystorePasswdValue);
            ConfigValue keystoreEntryPasswdValue = ConfigValueFactory.fromAnyRef(targetKeystoreEntryPasswd);
            toUpdate = toUpdate.withValue(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD, keystoreEntryPasswdValue);
            ConfigValue keystoreAliasValue = ConfigValueFactory.fromAnyRef(keyAlias);
            toUpdate = toUpdate.withValue(PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS, keystoreAliasValue);
        }
        if(targetTrustStore != null) {
            List<ConfigObject> truststores = new ArrayList<ConfigObject>();
            Map<String, Object> values = new HashMap<String, Object>();
            if(!trustStoreAlreadyExists) {
                values.put(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH, DEFAULT_TRUSTSTORE_PATH_UNQUOTED);
            } else {
                values.put(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH, targetTruststore);
            }
            values.put(PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD, targetTruststorePasswd);
            ConfigObject truststoreObject = ConfigValueFactory.fromMap(values);
            truststores.add(truststoreObject);
            ConfigList truststoreList = ConfigValueFactory.fromIterable(truststores);
            ConfigValue truststorePathsValue = ConfigValueFactory.fromIterable(truststoreList);
            toUpdate = toUpdate.withValue(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY, truststorePathsValue);
        }
    }
    
    private static KeyStoreCredentials readKeystoreCredentials(Config config) {
        String keystorePath = "";
        String keyPasswd = "";
        String storePasswd = "";
        String alias = "";
        try { keystorePath = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH); } catch (Exception e) {}
        try { keyPasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD); } catch (Exception e) {}
        try { storePasswd = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD); } catch (Exception e) {}
        try { alias = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS); } catch (Exception e) {}
        return new KeyStoreCredentials(keystorePath, storePasswd, keyPasswd, alias);
    }

    private static List<KeyStoreCredentials> readTruststoreCredentials(Config config) {
        List<KeyStoreCredentials> credentials = Collections.emptyList();
        try {
            credentials = config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).stream().map(item -> {
                    return new KeyStoreCredentials(item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH), 
                        item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTORES_SUB_STOREPWD));
                }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {}
        return credentials;
    }

    private static void createClient() throws Exception {
        if (client != null) {
            return;
        }
        client = new SOSRestApiClient();
        if (resolved != null) {
            for (Entry<String, ConfigValue> entry : resolved.entrySet()) {
                if(entry.getKey().startsWith("js7")) {
                    if(entry.getKey().contains("password")) {
                        System.out.println(entry.getKey() + ": " + "********");
                    } else {
                        System.out.println(entry.getKey() + ": " + entry.getValue().toString());
                    }
                }
            }
            String jocUriFromConfig = "";
            try {
                // old state of agents private.conf path=js7.web.joc.url - type=String
                // e.g. js7.web.joc{"url"="https://hostname:port"}
                jocUriFromConfig = resolved.getString(PRIVATE_CONF_JS7_PARAM_JOCURL);
                System.out.println("JOC Url (private.conf): " + jocUriFromConfig);
            } catch (Exception e) {}
            if (!SOSString.isEmpty(jocUriFromConfig) && jocUri == null) {
                jocUri = URI.create(jocUriFromConfig);
            } else {
                if(resolved.hasPath(PRIVATE_CONF_JS7_PARAM_API_SERVER)) {
                    try {
                        // current state of agents private.conf path js7.web.api-server.url - type=String
                        // e.g. js7.web.api-server {"url"="https://hostname:port"}
                        String uri = resolved.getString(PRIVATE_CONF_JS7_PARAM_API_SERVER + ".url");
                        jocUri = URI.create(uri);
                    } catch (Exception e) {
                        try {
                            // possible future state of agents private.conf path js7.web.api-server.url - type StringList
                            // e.g. js7.web.api-server.url= ["https://hostname:port","https://hostname2:port2"]
                            // or js7.web.api-server { url= ["https://hostname:port","https://hostname2:port2"]}
                            List<String> uris = resolved.getStringList(PRIVATE_CONF_JS7_PARAM_API_SERVER + ".url");
                            for(String uri : uris) {
                                URI url = URI.create(uri);
                                jocUris.add(url);
                            }
                        } catch (Exception e1) {
                            // possible future state of agents private.conf path js7.web.api-server - type ConfigList
                            // e.g. js7.web.api-server= [{"url" = "https://hostname:port"},{"url" = "https://hostname2:port2"}]
                            List<Config> objects = (List<Config>) resolved.getConfigList(PRIVATE_CONF_JS7_PARAM_API_SERVER);
                            for(Config cfg : objects) {
                                if(cfg.hasPath("url")) {
                                    URI url = URI.create(cfg.getString("url"));
                                    jocUris.add(url);
                                }
                            }
                        }
                    }
                }
            }
            if (jocUri == null && jocUris.isEmpty()) {
                throw new Exception("missing api-server url");
            }
            
            List<KeyStoreCredentials> truststoresCredentials = readTruststoreCredentials(resolved);
            Optional<KeyStore> truststoreOptional = null;
            try {
                System.out.println("read Trustore from: " + resolved.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTORES_ARRAY).get(0).getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
                truststoreOptional = truststoresCredentials.stream()
                        .filter(item -> item.getPath().endsWith(DEFAULT_TRUSTSTORE_FILENAME)).map(item -> {
                            try {
                                return KeyStoreUtil.readTrustStore(item.getPath(), KeystoreType.PKCS12, item.getStorePwd());
                            } catch (Exception e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).findAny();
            } catch (Exception e) {
                System.out.println("create new Truststore at default location.");
                truststoreOptional = Optional.empty();
            }
            KeyStore truststore = null; 
            if (truststoreOptional.isPresent()) {
                truststore = truststoreOptional.get();
            } else {
                truststore = createKeyStore(false);
            }
            KeyStoreCredentials credentials = readKeystoreCredentials(resolved);
            KeyStore keystore = null;
            try {
                System.out.println("read Keystore from: " + resolved.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
                keystore = KeyStoreUtil.readKeyStore(credentials.getPath(), KeystoreType.PKCS12, credentials.getStorePwd());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | ConfigException | IOException e) {
                System.out.println("create new Keystore at default location.");
                keystore = null;
            }
            if(keystore == null) {
                keystore = createKeyStore(true);
            }
            if (keystore != null && truststore != null) {
                client.setSSLContext(keystore, credentials.getKeyPwd().toCharArray(), credentials.getKeyStoreAlias(), truststore);
            }
        } else {
            KeyStore srcKeyStore = null;
            KeyStore srcTrustStore = null;
            if (srcKeystore != null && !srcKeystore.isEmpty() && srcTruststore != null && !srcTruststore.isEmpty()) {
                srcKeyStore = KeyStoreUtil.readKeyStore(srcKeystore, KeystoreType.fromValue(srcKeystoreType), srcKeystorePasswd);
                srcTrustStore = KeyStoreUtil.readTrustStore(srcTruststore, KeystoreType.fromValue(srcTruststoreType), srcTruststorePasswd);
            } else if (srcPrivateKeyPath != null && !srcPrivateKeyPath.isEmpty()
                    && srcCertPath != null && !srcCertPath.isEmpty()
                    && srcCaCertPath != null && !srcCaCertPath.isEmpty()) {
                PrivateKey privKey = null;
                X509Certificate cert = null;
                X509Certificate caCert = null;
                String pk = new String (Files.readAllBytes(Paths.get(srcPrivateKeyPath)), StandardCharsets.UTF_8);
                if (pk != null && !pk.isEmpty()) {
                    if (pk.contains(SOSKeyConstants.RSA_ALGORITHM_NAME)) {
                        privKey = KeyUtil.getPrivateRSAKeyFromString(pk);
                    } else {
                        privKey = KeyUtil.getPrivateECDSAKeyFromString(pk);
                    }
                }
                cert = (X509Certificate)KeyUtil.getCertificate(Paths.get(srcCertPath));
                Certificate[] chain = null;
                Certificate[] caChain = null;
                if (srcCaCertPath.contains(",")) {
                    String[] caCertPaths = srcCaCertPath.split(",");
                    caChain = new Certificate [caCertPaths.length];
                    chain = new Certificate[caChain.length + 1];
                    chain[0] = cert;
                    for (int i=0; i < caCertPaths.length; i++) {
                        X509Certificate caCertficate = (X509Certificate)KeyUtil.getCertificate(Paths.get(caCertPaths[i].trim()));
                        caChain[i] = caCertficate;
                        chain[i+1] = caCertficate;
                    }
                } else {
                    caCert = (X509Certificate)KeyUtil.getCertificate(srcCaCertPath);
                    chain = new Certificate[] {cert, caCert};
                }
                srcKeyStore = KeyStore.getInstance("PKCS12");
                srcKeyStore.load(null, null);
                srcKeyStore.setKeyEntry(keyAlias, privKey, "".toCharArray(), chain);
                srcTrustStore = KeyStore.getInstance("PKCS12");
                srcTrustStore.load(null, null);
                if (caChain.length != 0) {
                    for (int i=0; i < caChain.length; i++) {
                        srcTrustStore.setCertificateEntry(caAlias + (i+1), caChain[i]);
                    }
                } else {
                    srcTrustStore.setCertificateEntry(caAlias, caCert);
                }
            }
            if (srcKeyStore != null && srcTrustStore != null) {
                if (srcKeystoreEntryPasswd != null) {
                    client.setSSLContext(srcKeyStore, srcKeystoreEntryPasswd.toCharArray(), srcKeystoreEntryAlias, srcTrustStore);
                } else {
                    client.setSSLContext(srcKeyStore, "".toCharArray(), srcKeystoreEntryAlias, srcTrustStore);
                }
            }
        }
    }

    private static void closeClient() {
        if (client != null) {
            client.closeHttpClient();
        }
    }
    
    private static String createRequestBody (String dn, String hostname) throws InvalidNameException, JsonProcessingException {
        // --subject-dn="CN=sp, OU=IT, O=SOS GmbH, ST=Berlin, L=Berlin, C=DE"
        CreateCSRFilter filter = new CreateCSRFilter();
        filter.setDn(dn);
        filter.setHostname(hostname);
        filter.setHostname("sp");
        filter.setSan(san);
        filter.setDnOnly(dnOnly);
        return mapper.writeValueAsString(filter);
    }
    
    private static String callWebService() throws Exception {
        client.addHeader("X-Onetime-Token", token);
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Accept", "application/json");
        String hostname = InetAddress.getLocalHost().getCanonicalHostName();
        String response = "";
        if(jocUri != null) {
            return client.postRestService(jocUri.resolve(WS_API), createRequestBody(subjectDN, hostname));
        } else if (!jocUris.isEmpty()) {
            for (URI uri : jocUris) {
                try {
                    response = client.postRestService(uri.resolve(WS_API), createRequestBody(subjectDN, hostname));
                } catch(SOSConnectionRefusedException e) {
                    response = e.getMessage();
                    continue;
                } catch (JsonProcessingException | InvalidNameException | SOSException e) {
                    response = e.getMessage();
                    continue;
                }
                if (client.statusCode() != 200) {
                    continue;
                } else {
                    return response;
                }
            }
        }
        return response;
    }
    
    private static String getPriorizedKeystoreKey() {
        // priorities: args -> private.conf -> default
        return null;
    }

    private static String getPriorizedKeystoreEntryKey() {
        // priorities: args -> private.conf -> default
        return null;
    }

    private static String getPriorizedTruststoreKey() {
        // priorities: args -> private.conf -> default
        return null;
    }

    private static void printUsage(){
        System.out.println();
        System.out.println("Executes a roll out of ssl certificates on a controller or an agent instance.");
        System.out.println();
        System.out.println(" [ExecuteRollOut] [Options]");
        System.out.println();
        System.out.printf("  %-29s | %s%n", HELP, "Shows this help page, this option is exclusive and has no value");
        System.out.printf("  %-29s | %s%n", DN_ONLY, "Flag to receive relevant DNs to update the private.conf file, without certficate generation.");
        System.out.printf("  %-29s | %s%n", TOKEN + "=", "UUID of the token for a onetime authentication to JS7 JOC to receive the generated certificates.");
        System.out.printf("  %-29s | %s%n", SUBJECT_DN + "=", "The SubjectDN to be used consisting of [CN, OU, O, C, L, S] where the current hostname has to be set as CN.");
        System.out.printf("  %-29s | %s%n", SAN + "=", "The subject alternative names(SAN) should be set with variation of the hostname e.g. including the domain part. The alternatives are separated by comma.");
        System.out.printf("  %-29s | %s%n", JOC_URI + "=", "URI of the JS7 JOC to receive the generated certificates from.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE + "=", "Path to the Keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_TYPE + "=", "Type of the keystore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_PASS + "=", "Password for the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_ENTRY_PASS + "=", "Password for the private key entry of the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_KEYSTORE_ENTRY_ALIAS + "=", "Alias of the private key entry of the keystore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE + "=", "Path to the Truststore holding the trusted certificates to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_TYPE + "=", "Type of the truststore to connect to JS7 JOC over https. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", SRC_TRUSTSTORE_PASS + "=", "Password for the truststore holding the keys to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_PRIVATE_KEY + "=", "Path to the private Key file used to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_CERT + "=", "Path to the certificate file used to connect to JS7 JOC over https.");
        System.out.printf("  %-29s | %s%n", SRC_CA_CERT + "=", "Path to the CA certificate file(s) used to connect to JS7 JOC over https. (Comma separated)");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE + "=", "Path to the Keystore where the generated SSL certificates and keys should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_TYPE + "=", "Type of the keystore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_PASS + "=", "Password for the keystore to store to.");
        System.out.printf("  %-29s | %s%n", TRG_KEYSTORE_ENTRY_PASS + "=", "Password for the private key/certificate entry of the keystore holding the keys for mutual https.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE + "=", "Path to the Truststore where the trusted ca certificate should be stored.");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_TYPE + "=", "Type of the truststore to store to. (PKCS12[default] and JKS are supported only)");
        System.out.printf("  %-29s | %s%n", TRG_TRUSTSTORE_PASS + "=", "Password for the truststore to store to.");
        System.out.printf("  %-29s | %s%n", KS_ALIAS + "=", "Alias used to store the certificate and its private key in the target keystore.");
        System.out.printf("  %-29s | %s%n", TS_ALIAS + "=", "Alias used to store the ca certificate in both, the target keystore and truststore.");
        System.out.println();
    }
    
    private class PriorizedKeyStoreCredentials {
        
        private KeyStore keystore;
        private KeyStore truststore;
        
        public PriorizedKeyStoreCredentials (KeyStore keystore, KeyStore truststore) {
            this.keystore = keystore;
            this.truststore = truststore;
        }
        
        public KeyStore getKeystore() {
            return this.keystore;
        }
        
        public KeyStore getTruststore() {
            return this.truststore;
        }
    }
}
