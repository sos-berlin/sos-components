package com.sos.js7.job.jocapi;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.encryption.exception.SOSEncryptionException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.BaseHttpClient.Builder;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.httpclient.commons.mulitpart.HttpFormData;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.keystore.KeyStoreFile;
import com.sos.commons.util.keystore.KeyStoreType;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.exception.SOSKeyException;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public class ApiExecutor implements AutoCloseable {

    private static final String X_ID_TOKEN = "X-ID-TOKEN";

    // private static final String DEFAULT_TRUSTSTORE_FILENAME = "https-truststore.p12";
    private static final String WS_API_LOGIN = "/joc/api/authentication/login";
    private static final String WS_API_LOGOUT = "/joc/api/authentication/logout";
    private static final String WS_API_PREFIX = "/joc/api";

    private static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
    private static final String SOS_EXPORT_DIR_HEADER = "X-Export-Directory";
    private static final String SOS_IMPORT_FILEPATH_HEADER = "X-Import-File";

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

    private BaseHttpClient client;
    private URI jocUri;
    private List<String> jocUris;
    private Config config;
    
    private Map<String, DetailValue> jobResources;
    private Map<String, String> additionalHeaders;
    
    private Map<String, String> responseHeaders;
    
    public ApiExecutor(OrderProcessStep<?> step) {
        this.step = step;
    }
    
    public ApiExecutor(OrderProcessStep<?> step, Map<String, String> additionalHeaders) {
        this(step);
        this.additionalHeaders = additionalHeaders;
    }
    
    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }
    
    public void setAdditionalHeaders(Map<String, String> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public void setJobResources(Map<String, DetailValue> jobResources) {
        this.jobResources = jobResources;
    }

    public ApiResponse login() throws Exception {
        return login(Duration.ofSeconds(30));
    }

    public ApiResponse login(Duration connectTimeout) throws Exception {
        /*
         * TODO: first check variables from OrderProcessStep if required values are available 
         *    if available use this configuration 
         *    if not available use configuration from private.conf
         */
        boolean isDebugEnabled = step.getLogger().isDebugEnabled();
        step.getLogger().debug("***ApiExecutor***");
        jocUris = getUris();
        String latestError = "";
        // String latestResponse = "";
        URI loginUri = null;
        Exception latestException = null;
        for (String uri : jocUris) {
            try {
                createClient(uri, connectTimeout);
                this.jocUri = URI.create(uri);
                loginUri = jocUri.resolve(WS_API_LOGIN);
                HttpExecutionResult<String> result = client.executePOST(loginUri);
                // result.formatWithResponseBody(true);
                if (isDebugEnabled) {
                    step.getLogger().debug("[login]" + BaseHttpClient.formatExecutionResult(result));
                }
                int code = result.response().statusCode();
                if (!HttpUtils.isSuccessful(code)) {
                    if (HttpUtils.isServerError(code)) {
                        throw new SOSConnectionRefusedException(BaseHttpClient.formatExecutionResult(result));
                    }
                    if (HttpUtils.isUnauthorized(code)) {
                        String message = (String) client.getJsonProperty(result.response(), "message");
                        if (SOSString.isEmpty(message)) {
                            latestException = new Exception("login failed.");
                            throw latestException;
                        } else {
                            latestError = code + " : " + HttpUtils.getReasonPhrase(code) + " " + message;
                            throw new Exception(latestError);
                        }
                    }
                }
                setResponseHeaders(result.response());
                return new ApiResponse(code, HttpUtils.getReasonPhrase(code), result.response().body(), client.getResponseHeader(result.response(),
                        ACCESS_TOKEN_HEADER).orElse(null), null);
            } catch (ConnectException | SOSConnectionRefusedException e) {
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
        Map<String, String> requestHeaders = Map.of(ACCESS_TOKEN_HEADER, token, HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_JSON);
        return post(token, apiUrl, HttpRequest.BodyPublishers.ofString(body), requestHeaders);
    }

    public ApiResponse post(String token, String apiUrl, HttpFormData formData) throws SOSConnectionRefusedException, SOSBadRequestException {
        Map<String, String> requestHeaders = Map.of(ACCESS_TOKEN_HEADER, token, HttpUtils.HEADER_CONTENT_TYPE, formData.getContentType());
        return post(token, apiUrl, HttpRequest.BodyPublishers.ofByteArrays(formData), requestHeaders);
    }
    
    private ApiResponse post(String token, String apiUrl, HttpRequest.BodyPublisher publisher, Map<String, String> requestHeaders) throws SOSConnectionRefusedException, SOSBadRequestException {
        if (token == null) {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        } else {
            if (jocUri != null) {
                boolean isDebugEnabled = step.getLogger().isDebugEnabled();
                try {
                    if (isDebugEnabled) {
                        step.getLogger().debug("REQUEST: %s", apiUrl);
                        step.getLogger().debug("PARAMS:\n%s", publisher.toString());
                    }
                    if (!apiUrl.toLowerCase().startsWith(WS_API_PREFIX)) {
                        apiUrl = WS_API_PREFIX + apiUrl;
                    }
                    if (isDebugEnabled) {
                        step.getLogger().debug("resolvedUri: %s", jocUri.resolve(apiUrl).toString());
                    }
                    HttpExecutionResult<InputStream> result = client.executePOST(
                            jocUri.resolve(apiUrl), 
                            client.mergeWithDefaultHeaders(requestHeaders),
                            publisher, 
                            HttpResponse.BodyHandlers.ofInputStream());
                    String responseBody = readPostResponseBody(result);
                    // result.formatWithResponseBody(true);
                    if (step.getLogger().isDebugEnabled()) {
                        step.getLogger().debug("[post]" + BaseHttpClient.formatExecutionResult(result));
                    }
                    int code = result.response().statusCode();
                    setResponseHeaders(result.response());

                    if (responseBody.startsWith("outfile:")) {
                        step.getLogger().debug("set outcome variable: js7ApiExecutorOutfile=" + responseBody.substring("outfile:".length()));
                        step.getOutcome().putVariable("js7ApiExecutorOutfile", responseBody.substring("outfile:".length()));
                    }
                    return new ApiResponse(code, HttpUtils.getReasonPhrase(code), responseBody, token, null);
                } catch (Exception e) {
                    return new ApiResponse(-1, e.getClass().getSimpleName(), (String) null, token, e);
                }
            } else {
                throw new SOSConnectionRefusedException("No connection established through previous login api call.");
            }
        }
    }

    public ApiResponse logout(String token) throws SOSBadRequestException {
        if (token != null && jocUri != null) {
            try {
                HttpExecutionResult<String> result = client.executePOST(jocUri.resolve(WS_API_LOGOUT), client.mergeWithDefaultHeaders(Map.of(
                        ACCESS_TOKEN_HEADER, token)));
                // result.formatWithResponseBody(true);
                if (step.getLogger().isDebugEnabled()) {
                    step.getLogger().debug("[logout]" + BaseHttpClient.formatExecutionResult(result));
                }
                int code = result.response().statusCode();
                setResponseHeaders(result.response());
                return new ApiResponse(code, HttpUtils.getReasonPhrase(code), result.response().body(), token, null);
            } catch (Exception e) {
                return new ApiResponse(-1, e.getClass().getSimpleName(), (String) null, token, e);
            }
        } else {
            throw new SOSBadRequestException("no access token provided. permission denied.");
        }
    }

    @Override
    public void close() {
        SOSClassUtil.closeQuietly(client);
        client = null;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    private SslArguments getSslArguments() throws KeyManagementException, SOSMissingDataException, NoSuchAlgorithmException, FileNotFoundException {
        SslArguments args = new SslArguments();

        // TrustStore(s): 0->n
        List<KeyStoreFile> trustStoreFiles = getTrustStoreFilesFromOrder();
        if (trustStoreFiles == null) {
            trustStoreFiles = getTrustStoreFilesFromConfig(config);
        }
        args.getTrustedSsl().setTrustStoreFiles(trustStoreFiles);

        // KeyStore: 0->1
        KeyStoreFile keyStoreFile = getKeyStoreFileFromOrder();
        if (keyStoreFile == null) {
            keyStoreFile = getKeyStoreFileFromConfig(config);
        }
        args.getTrustedSsl().setKeyStoreFile(keyStoreFile);
        return args;
    }

    private void createClient(String jocUri, Duration connectTimeout) throws Exception {
        close();

        if (config == null) {
            readConfig();
        }

        step.getLogger().debug("initiate REST api client");

        Builder builder = BaseHttpClient.withBuilder();
        builder = builder.withLogger(step.getLogger());
        builder = builder.withConnectTimeout(connectTimeout);
        builder = builder.withDefaultHeaders(additionalHeaders);
        builder = builder.withAuth(getAuthConfig());
        builder = builder.withProxyConfig(getProxyConfig());

        if (jocUri.toLowerCase().startsWith("https:")) {
            builder.withSSL(getSslArguments());
        }

        client = builder.build();
        client.addSensitiveHeaders(Set.of(X_ID_TOKEN, ACCESS_TOKEN_HEADER));
    }

    // TODO
    private ProxyConfig getProxyConfig() {
        return null;
    }

    private HttpClientAuthConfig getAuthConfig() throws Exception {
        String csFile = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSFILE);
        String password = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_PWD);
        String username = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_USERNAME);

        if (csFile != null || (username != null && password != null)) {
            String token = getValueOfArgument(JOB_ARGUMENT_APISERVER_BASIC_AUTH_TOKEN, "");
            String csPwd = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSPASSWD);
            String csKeyFile = getValueOfArgument(JOB_ARGUMENT_APISERVER_CSKEY);

            if (!SOSString.isEmpty(csFile)) {
                csFile = getDecryptedValue(csFile, JOB_ARGUMENT_APISERVER_CSFILE);

                SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
                username = resolver.resolve(username);
                password = resolver.resolve(password);
                token = resolver.resolve(token);
            }
            if (token.isEmpty()) {
                username = getDecryptedValue(username, "username");
                password = getDecryptedValue(password, "password");
            }
            if (!token.isEmpty() && jobResources != null) {
                step.getLogger().debug("get jobresource and variable from token:" + token);
                String[] tokenJobResource = token.split(":");
                if (tokenJobResource.length == 2) {
                    String variableName = tokenJobResource[1];
                    String jobResourceName = tokenJobResource[0];
                    DetailValue detailValue = jobResources.get(variableName);
                    if (step.getLogger().isDebugEnabled()) {
                        step.getLogger().debug(SOSString.toString(detailValue));
                        step.getLogger().debug("variableName:" + variableName);
                        step.getLogger().debug("jobResourceName:" + jobResourceName);
                    }
                    if (detailValue != null) {
                        if (detailValue.getSource().equals(jobResourceName)) {
                            token = (String) detailValue.getValue();
                        } else {
                            step.getLogger().info("Name of JobResource: " + detailValue.getSource() + " does not match the " + tokenJobResource[0]);
                        }
                    }
                }
                if (additionalHeaders == null) {
                    additionalHeaders = new LinkedHashMap<>();
                }
                additionalHeaders.put(X_ID_TOKEN, token);
            }
        } else {
            csFile = null;
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
            username = "";
            password = "";
            String token = "";
            if (csFile != null && !csFile.isEmpty()) {
                SOSKeePassResolver resolver = new SOSKeePassResolver(csFile, csKeyFile, csPwd);
                username = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_USERNAME));
                password = resolver.resolve(config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD));
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
                        password = config.getString(PRIVATE_CONF_JS7_PARAM_HTTP_BASIC_AUTH_PWD);
                    } catch (ConfigException e) {
                        step.getLogger().debug("no (user-)password found in private.conf.");
                    }

                    String privateKeyPath = null;
                    username = getDecryptedValue(username, privateKeyPath, "username");
                    password = getDecryptedValue(password, privateKeyPath, "password");
                }

            }
            if (!token.isEmpty() && jobResources != null) {
                step.getLogger().debug("get jobresource and variable from token:" + token);

                String[] tokenJobResource = token.split(":");
                if (tokenJobResource.length == 2) {
                    String variableName = tokenJobResource[1];
                    String jobResourceName = tokenJobResource[0];
                    DetailValue detailValue = jobResources.get(variableName);

                    if (step.getLogger().isDebugEnabled()) {
                        step.getLogger().debug(SOSString.toString(detailValue));
                        step.getLogger().debug("variableName:" + variableName);
                        step.getLogger().debug("jobResourceName:" + jobResourceName);
                    }
                    if (detailValue != null) {
                        if (detailValue.getSource().equals(jobResourceName)) {
                            token = (String) detailValue.getValue();
                        } else {
                            step.getLogger().info("Name of JobResource: " + detailValue.getSource() + " does not match the " + tokenJobResource[0]);
                        }
                    }
                }
                if (additionalHeaders == null) {
                    additionalHeaders = new LinkedHashMap<>();
                }
                additionalHeaders.put(X_ID_TOKEN, token);
            }
        }

        if (SOSString.isEmpty(username) && SOSString.isEmpty(password)) {
            return null;
        }
        // BASIC
        return new HttpClientAuthConfig(username, password);
    }

    private List<String> getUris() throws SOSMissingDataException {
        List<String> uris = new ArrayList<String>();
        String apiServers = getDecrytedValueOfArgument(JOB_ARGUMENT_APISERVER_URL);
        if (apiServers != null) {
            String[] apiServersSplitted = apiServers.split(JOB_ARGUMENT_DELIMITER_REGEX);
            uris = Arrays.asList(apiServersSplitted).stream().peek(String::trim).toList();
        } else {
            if (config == null) {
                readConfig();
            }
            uris = config.getConfig(PRIVATE_CONF_JS7_PARAM_API_SERVER).getStringList(PRIVATE_CONF_JS7_PARAM_URL);
        }
        return uris;

    }

    private String readPostResponseBody(HttpExecutionResult<InputStream> result) throws Exception {
        String responseBody = null;
        String contentEncoding = client.getResponseHeader(result.response(), 
                HttpUtils.HEADER_CONTENT_ENCODING).orElse("identity").toLowerCase(Locale.ROOT);
        Path responseBodyFile = getResponseBodyFile(result);
        try (InputStream in = decodeInputStream(result.response().body(), contentEncoding); 
                OutputStream out = responseBodyFile == null ? new ByteArrayOutputStream() : 
                    Files.newOutputStream(responseBodyFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
            byte[] buffer = new byte[4_096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            if (responseBodyFile == null) {
                responseBody = ((ByteArrayOutputStream) out).toString(client.extractCharsetFromResponseContentType(result.response()));
            } else {
                responseBody = "outfile:" + responseBodyFile.toString();
            }
        }
        return responseBody;
    }

    private Path getResponseBodyFile(HttpExecutionResult<InputStream> result) throws Exception {
        String contentDisposition = client.getResponseHeader(result.response(), HttpUtils.HEADER_CONTENT_DISPOSITION).orElse(null);
        if (contentDisposition == null || !contentDisposition.contains("filename")) {
            return null;
        }
        Optional<String> targetPath = client.getRequestHeader(result.request(), SOS_EXPORT_DIR_HEADER);
        if (!targetPath.isPresent()) {
            targetPath = client.getRequestHeader(result.request(), SOS_EXPORT_DIR_HEADER.toLowerCase());
        }
        Path target = Paths.get(System.getProperty("user.dir"));
        if (targetPath.isPresent()) {
            target = target.resolve(targetPath.get());
        }
        Files.createDirectories(target);
        String filename = decodeDisposition(contentDisposition);
        return target.resolve(filename);
    }

    // Supports gzip, deflate, identity
    private static InputStream decodeInputStream(InputStream in, String encoding) throws IOException {
        switch (encoding) {
        case "gzip":
            return new GZIPInputStream(in);
        case "deflate":
            return new InflaterInputStream(in);
        case "identity":
        case "":
        default:
            return in;
        // default: TODO check if better to throw
        // throw new UnsupportedEncodingException("Unsupported Content-Encoding: " + encoding);
        }
    }

    private static String decodeDisposition(String disposition) throws UnsupportedEncodingException {
        String dispositionFilenameValue = disposition.replaceFirst("(?i)^.*filename(?:=\"?([^\"]+)\"?|\\*=([^;,]+)).*$", "$1$2");
        return decodeFromUriFormat(dispositionFilenameValue);
    }

    private static String decodeFromUriFormat(String parameter) throws UnsupportedEncodingException {
        final Pattern filenamePattern = Pattern.compile("(?<charset>[^']+)'(?<lang>[a-z]{2,8}(-[a-z0-9-]+)?)?'(?<filename>.+)",
                Pattern.CASE_INSENSITIVE);
        final Matcher matcher = filenamePattern.matcher(parameter);
        if (matcher.matches()) {
            final String filename = matcher.group("filename");
            final String charset = matcher.group("charset");
            return URLDecoder.decode(filename.replaceAll("%25", "%"), charset);
        } else {
            return parameter;
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
        if (step != null) {
            return getValueOfArgument(step.getAllArguments().get(key), _default);
        } else {
            return null;
        }
    }

    private String getValueOfArgument(JobArgument<?> arg, String _default) {
        return Optional.ofNullable(arg).map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString)
                .orElse(_default);
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

    private Config readConfig() throws SOSMissingDataException {
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
        return config;
    }

    private List<KeyStoreFile> getTrustStoreFilesFromOrder() {
        String trustStores = getDecrytedValueOfArgument(JOB_ARGUMENT_TRUSTSTORE_FILE);
        if (SOSString.isEmpty(trustStores)) {
            return null;
        }

        String[] arr = trustStores.split(JOB_ARGUMENT_DELIMITER_REGEX);
        String password = getDecrytedValueOfArgument(JOB_ARGUMENT_TRUSTSTORE_PWD, "");
        String pass = SOSString.isEmpty(password) ? null : password;
        return Arrays.asList(arr).stream().peek(String::trim).map(path -> {
            if (SOSString.isEmpty(path)) {
                return null;
            }
            KeyStoreFile f = new KeyStoreFile();
            f.setPath(SOSPath.toAbsolutePath(path));
            if (!Files.exists(f.getPath())) {
                step.getLogger().warn("[order][TrustStore][%s]not found", f.getPath());
                return null;
            }

            f.setType(KeyStoreType.PKCS12);
            f.setPassword(pass);
            return f;
        }).filter(Objects::nonNull).toList();
    }

    private List<KeyStoreFile> getTrustStoreFilesFromConfig(Config config) {
        try {
            if (step.getLogger().isDebugEnabled()) {
                step.getLogger().debug("read Truststore from: %s", config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY).get(0).getString(
                        PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH));
            }
            return config.getConfigList(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_ARRAY).stream().map(item -> {
                String path = item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_FILEPATH);
                if (SOSString.isEmpty(path)) {
                    return null;
                }

                KeyStoreFile f = new KeyStoreFile();
                f.setPath(SOSPath.toAbsolutePath(path));
                if (!Files.exists(f.getPath())) {
                    step.getLogger().warn("[config][TrustStore][%s]not found", f.getPath());
                    return null;
                }

                f.setType(KeyStoreType.PKCS12);
                f.setPassword(item.getString(PRIVATE_CONF_JS7_PARAM_TRUSTSTORES_SUB_STOREPWD));
                return f;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (ConfigException e) {
            step.getLogger().debug("[config]no truststore credentials found in private.conf.");
            return null;
        }

    }

    private KeyStoreFile getKeyStoreFileFromOrder() {
        String path = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_FILE);
        if (SOSString.isEmpty(path)) {
            return null;
        }

        String alias = getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_ALIAS, "");

        KeyStoreFile f = new KeyStoreFile();
        f.setPath(SOSPath.toAbsolutePath(path));
        if (!Files.exists(f.getPath())) {
            step.getLogger().warn("[order][KeyStore][%s]not found", f.getPath());
            return null;
        }
        f.setType(KeyStoreType.PKCS12);
        f.setPassword(getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_STORE_PASSWD, ""));
        f.setKeyPassword(getDecrytedValueOfArgument(JOB_ARGUMENT_KEYSTORE_KEY_PASSWD, ""));
        f.setAliases(SOSString.isEmpty(alias) ? null : List.of(alias));
        return f;
    }

    private KeyStoreFile getKeyStoreFileFromConfig(Config config) {
        KeyStoreFile f = new KeyStoreFile();
        f.setType(KeyStoreType.PKCS12);

        try {
            String keystorePath = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH);
            if (step.getLogger().isDebugEnabled()) {
                step.getLogger().debug("[config][%s]read KeyStore...", config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_FILEPATH));
            }
            if (SOSString.isEmpty(keystorePath)) {
                return null;
            }
            f.setPath(SOSPath.toAbsolutePath(keystorePath));
            if (!Files.exists(f.getPath())) {
                step.getLogger().warn("[config][KeyStore][%s]not found", f.getPath());
                return null;
            }
        } catch (ConfigException e) {
            step.getLogger().debug("[config]no keystore file found in private.conf.");
            return null;
        }

        try {
            f.setPassword(config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_STOREPWD));
        } catch (ConfigException e) {
            step.getLogger().debug("[config]no keystore store-password found in private.conf.");
        }

        try {
            f.setKeyPassword(config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_KEYPWD));
        } catch (ConfigException e) {
            step.getLogger().debug("[config]no keystore key-password found in private.conf.");
        }

        try {
            String alias = config.getString(PRIVATE_CONF_JS7_PARAM_KEYSTORE_ALIAS);
            f.setAliases(SOSString.isEmpty(alias) ? null : List.of(alias));
        } catch (ConfigException e) {
            step.getLogger().debug("[config]no (key-)alias found in private.conf.");
        }

        return f;
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

    private void setResponseHeaders(HttpResponse<?> response) {
        if (response == null) {
            return;
        }
        responseHeaders = client.toJoinedValueResponseHeaders(response);
    }

}
