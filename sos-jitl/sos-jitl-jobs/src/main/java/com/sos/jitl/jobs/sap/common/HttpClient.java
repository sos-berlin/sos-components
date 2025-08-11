package com.sos.jitl.jobs.sap.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.ssl.SslContextFactory;
import com.sos.jitl.jobs.sap.common.bean.Job;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.ResponseJobs;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.Schedule;
import com.sos.jitl.jobs.sap.common.bean.ScheduleLog;
import com.sos.js7.job.exception.JobArgumentException;

import jakarta.ws.rs.core.UriBuilder;

public class HttpClient {

    private final static String csrfTokenHeaderKey = "X-CSRF-Token";
    private final static String sapClientQueryKey = "sap-client";
    private static enum HTTP_METHOD {GET, POST, PUT, DELETE}; 
    private final ISOSLogger logger;
    private final URI uri;
    private final String sapClient;
    private BaseHttpClient client;
    private BaseHttpClient.Builder baseHttpClientBuilder;

    private String csrfToken = null;
    private List<String> cookies;
    

    public HttpClient(CommonJobArguments jobArgs, ISOSLogger logger) throws JobArgumentException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, SOSSSLException, IOException, KeyManagementException {
        this.uri = jobArgs.getUri().getValue();
        this.sapClient = jobArgs.getMandant().getValue();
        this.logger = logger;
        initHttpClientBuilder(jobArgs);
    }

    /** @see https://help.sap.com/viewer/c5bc243e9e824373a5e8de7c37b1dee1/Cloud/en-US/0e432781e0c646a09602a4aab786734d.html
     * @return
     * @throws Exception */
    public String fetchCSRFToken() throws Exception {
        Map<String,String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(csrfTokenHeaderKey, "fetch");
        if(cookies != null) {
            addCookieHeader(requestHeaders);
        }
        URI uri = setUriPath();
        logger.info("Fetch token: GET '%s'", uri);
        createClient();
        HttpExecutionResult<byte[]> result = client.executeGET(uri, requestHeaders, BodyHandlers.ofByteArray());
        String token = result.response().headers().firstValue(csrfTokenHeaderKey).orElse(null);
        int httpReplyCode = result.response().statusCode();
        // token is sent even http code != 200
        if (httpReplyCode != 200) {
            String errorMessage = getErrorMessage(result.response());
            logger.warn("%d %s%s", httpReplyCode, HttpUtils.getReasonPhrase(httpReplyCode), errorMessage.isEmpty() ? "" : ": " + errorMessage);
        }
        if (token != null) {
            csrfToken = token;
            return token;
        } else {
            throw new SOSBadRequestException(httpReplyCode, "Response-Header " + csrfTokenHeaderKey + " is missing");
        }
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/b4d3719173f641b583c97ff0e8f0a7fa.html
     * @return
     * @throws Exception */
    public ResponseJobs retrieveJobs() throws Exception {
        return retrieveJobs(null, null);
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/b4d3719173f641b583c97ff0e8f0a7fa.html
     * @param pageSize
     * @param offset
     * @return
     * @throws Exception */
    public ResponseJobs retrieveJobs(Integer pageSize, Integer offset) throws Exception {
        if (pageSize == null) {
            pageSize = 10;
        }
        if (offset == null) {
            offset = 1;
        }
        Map<String, Object> queryParameter = new HashMap<>(2);
        queryParameter.put("pageSize", pageSize);
        queryParameter.put("offset", offset);
        URI url = setUriPath("scheduler/jobs", null, queryParameter);
        logger.info("Retrieve Jobs: GET '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.GET, url, null, ResponseJobs.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_bdj_psq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/2c1ecb6dae0c42b4a850f7c07d1b7124.html
     * @param body
     * @return
     * @throws Exception */
    public ResponseJob createJob(Job body) throws Exception {
        return createJob(Globals.objectMapper.writeValueAsBytes(body));
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_bdj_psq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/2c1ecb6dae0c42b4a850f7c07d1b7124.html
     * @param body
     * @return
     * @throws Exception */
    public <B> ResponseJob createJob(B body) throws Exception {
        if (body == null) {
            throw new JobArgumentException("Request body is missing");
        }
        URI url = setUriPath("scheduler/jobs");
        logger.info("Create Job: POST '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.POST, url, body, ResponseJob.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_kdn_dxq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/251658d477804d8cb53ef94d0ec231ce.html
     * @param jobId
     * @param jobName
     * @return
     * @throws Exception */
    public ResponseJob retrieveJob(Long jobId, String jobName) throws Exception {
        URI url = null;
        if (jobId != null) {
            Map<String, Object> uriParameter = new HashMap<>(1);
            uriParameter.put("jobId", jobId);
            Map<String, Object> queryParameter = new HashMap<>(1);
            queryParameter.put("displaySchedules", true);
            url = setUriPath("scheduler/jobs/{jobId}", uriParameter, queryParameter);
        } else if (jobName != null && !jobName.isEmpty()) {
            Map<String, Object> queryParameter = new HashMap<>(2);
            queryParameter.put("name", jobName);
            queryParameter.put("displaySchedules", true);
            url = setUriPath("scheduler/jobs", null, queryParameter);
        } else {
            throw new JobArgumentException("jobId and jobName are missing");
        }
        logger.info("Retrieve Job: GET '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.GET, url, null, ResponseJob.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_o12_x1r_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/cd8feb7e839a4fda87a5623d24adafbd.html
     * @param jobId
     * @return
     * @throws SocketException
     * @throws SOSException */
    public boolean deleteJob(Long jobId) throws SocketException, SOSException {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(1);
        uriParameter.put("jobId", jobId);
        URI url = setUriPath("scheduler/jobs/{jobId}", uriParameter);
        logger.info("Delete Job: DELETE '%s'", url.toString());
        try {
            getJson(HTTP_METHOD.DELETE, url, null);
        } catch (SOSBadRequestException e) {
            if (Arrays.asList(400, 404).contains(e.getHttpCode())) {
                logger.info("Job '%d' already deleted", jobId);
                return false;
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(e);
        }
        return true;
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_vjw_mvq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/66ab3c1404e34a3b9f04b968ecb3fd5f.html
     * @param jobId
     * @param body
     * @return
     * @throws Exception */
    public ResponseSchedule createSchedule(Long jobId, Schedule body) throws Exception {
        return createSchedule(jobId, Globals.objectMapper.writeValueAsBytes(body));
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_vjw_mvq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/fa16c72ffb31438faa5d896741f52e73.html
     * @param jobId
     * @param body
     * @return
     * @throws Exception */
    public <B> ResponseSchedule createSchedule(Long jobId, B body) throws Exception {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (body == null) {
            throw new JobArgumentException("Request body is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(1);
        uriParameter.put("jobId", jobId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules", uriParameter);
        logger.info("Create Schedule: POST '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.POST, url, body, ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_n24_cyq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/66ab3c1404e34a3b9f04b968ecb3fd5f.html
     * @param jobId
     * @param scheduleId
     * @return
     * @throws Exception */
    public ResponseSchedule retrieveSchedule(Long jobId, String scheduleId) throws Exception {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (scheduleId == null) {
            throw new JobArgumentException("scheduleId is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(2);
        uriParameter.put("jobId", jobId);
        uriParameter.put("scheduleId", scheduleId);
        Map<String, Object> queryParameter = new HashMap<>(1);
        queryParameter.put("displayLogs", true);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules/{scheduleId}", uriParameter, queryParameter);
        logger.info("Retrieve Schedule: GET '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.GET, url, null, ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_hvh_gwq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/0a4d9395180f482db46b8a5375fa6f7f.html
     * @param jobId
     * @param scheduleId
     * @return
     * @throws Exception */
    public ResponseSchedule activateSchedule(Long jobId, String scheduleId) throws Exception {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (scheduleId == null) {
            throw new JobArgumentException("scheduleId is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(2);
        uriParameter.put("jobId", jobId);
        uriParameter.put("scheduleId", scheduleId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules/{scheduleId}", uriParameter);
        logger.info("Activate Schedule: PUT '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.PUT, url, Globals.objectMapper.writeValueAsBytes(new Schedule().withActive(true)), ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/e49a4b2a8b2d43d3a9e4629ba29521f4.html
     * @param jobId
     * @param scheduleId
     * @param runId
     * @return
     * @throws Exception */
    public ScheduleLog retrieveScheduleLog(Long jobId, String scheduleId, String runId) throws Exception {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (scheduleId == null) {
            throw new JobArgumentException("scheduleId is missing");
        }
        if (runId == null) {
            throw new JobArgumentException("runId is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(3);
        uriParameter.put("jobId", jobId);
        uriParameter.put("scheduleId", scheduleId);
        uriParameter.put("runId", runId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules/{scheduleId}/runs/{runId}", uriParameter);
        logger.info("Retrieve Schedule Run Log: GET '%s'", url.toString());
        return getJsonObject(HTTP_METHOD.GET, url, null, ScheduleLog.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_usq_jbr_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/3066b6d1ed97423a85bacf67f04b20e4.html
     * @param jobId
     * @param scheduleId
     * @return
     * @throws SocketException
     * @throws SOSException */
    public boolean deleteSchedule(Long jobId, String scheduleId) throws SocketException, SOSException {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (scheduleId == null) {
            throw new JobArgumentException("scheduleId is missing");
        }
        Map<String, Object> uriParameter = new HashMap<>(2);
        uriParameter.put("jobId", jobId);
        uriParameter.put("scheduleId", scheduleId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules/{scheduleId}", uriParameter);
        logger.info("Delete Schedule: DELETE '%s'", url.toString());
        try {
            getJson(HTTP_METHOD.DELETE, url, null);
        } catch (SOSBadRequestException e) {
            if (Arrays.asList(400, 404).contains(e.getHttpCode())) {
                logger.info("Schedule '%d/%s' already deleted", jobId, scheduleId);
                return false;
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(e);
        }
        return true;
    }

    private static KeyStore readTruststore(CommonJobArguments jobArgs) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, JobArgumentException {
        if (jobArgs.getTruststorePath().getValue() == null) {
            return null;
        }
        Path truststorePath = jobArgs.getTruststorePath().getValue();
        if (Files.exists(truststorePath)) {
            KeyStore truststore = KeyStore.getInstance(jobArgs.getTruststoreType().getValue().value());
            try (InputStream in = Files.newInputStream(truststorePath)) {
                truststore.load(in, jobArgs.getTruststorePwd().getValue().toCharArray());
                return truststore;
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new JobArgumentException(String.format("truststore (%1$s) not found.", truststorePath.toString()));
        }
    }

    private URI setUriPath() {
        return setUriPath(null, null, null);
    }

    private URI setUriPath(String path) {
        return setUriPath(path, null, null);
    }

    private URI setUriPath(String path, Map<String, Object> uriParameter) {
        return setUriPath(path, uriParameter, null);
    }

    private URI setUriPath(String path, Map<String, Object> uriParameter, Map<String, Object> queryParameter) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uri);
        if (path != null) {
            uriBuilder.path(path);
        }
        if (sapClient != null) {
            uriBuilder.queryParam(sapClientQueryKey, sapClient);
        }
        if (queryParameter != null) {
            queryParameter.forEach((k, v) -> uriBuilder.queryParam(k, v));
        }
        if (uriParameter != null) {
            return uriBuilder.buildFromEncodedMap(uriParameter);
        } else {
            return uriBuilder.build();
        }
    }

    private <T, B> T getJsonObject(HTTP_METHOD method, URI uri, B postBody, Class<T> clazz) throws Exception {
        return getJsonObject(getJson(method, uri, postBody), clazz);
    }

    public <T, B> HttpExecutionResult<T> executeRestService(HTTP_METHOD method, URI uri, Map<String,String> requestHeaders, B body, 
            HttpResponse.BodyHandler<T> handler) throws Exception {
        HttpExecutionResult<T> result = null;
        switch (method) {
        case GET:
            result = client.executeGET(uri, requestHeaders, handler);
            break;
        case POST:
            result = client.executePOST(uri, requestHeaders, getBodyPublisher(body), handler);
            break;
        case PUT:
            result = client.executePUT(uri, requestHeaders, getBodyPublisher(body), handler);
            break;
        case DELETE:
            result = client.executeDELETE(uri, requestHeaders, handler);
            break;
        }
        if (result != null) {
            addCookies(result);
        }
        return result;
    }
    
    private <T> void addCookies(HttpExecutionResult<T> result) {
        List<String> set_cookie_HeaderValues = result.response().headers().allValues("set-cookie");
        if(set_cookie_HeaderValues != null) {
            if(cookies == null) {
                cookies = new ArrayList<String>();
            }
            cookies.addAll(set_cookie_HeaderValues);
        }
    }

    private <B> BodyPublisher getBodyPublisher(B body) {
        BodyPublisher bodyPublisher;
        if(body != null) {
            if(body instanceof String) {
                bodyPublisher = BodyPublishers.ofString((String)body);
            } else if (body instanceof byte[]) {
                bodyPublisher = BodyPublishers.ofByteArray((byte[])body);
            } else {
                // default
                bodyPublisher = BodyPublishers.noBody();
            }
        } else {
            bodyPublisher = BodyPublishers.noBody();
        }
        return bodyPublisher;
    }
    
    private <B> byte[] getJson(HTTP_METHOD method, URI uri, B postBody) throws SOSException, Exception {
        Map<String,String> requestHeaders = new HashMap<String, String>();
        if (HTTP_METHOD.POST.equals(method) || HTTP_METHOD.PUT.equals(method)) {
            requestHeaders.put("Content-Type", "application/json");
        }
        requestHeaders.put("Accept", "application/json");
        if (csrfToken != null) {
            requestHeaders.put(csrfTokenHeaderKey, csrfToken);
        }
        if(cookies != null) {
            addCookieHeader(requestHeaders);
        }
        createClient();
        return getJsonFromResponse(executeRestService(method, uri, requestHeaders, postBody, HttpResponse.BodyHandlers.ofByteArray()));
    }

    @SuppressWarnings("unchecked")
    private <T extends JsonStructure, B> T getJsonStructure(byte[] jsonStr) {
        if (jsonStr != null) {
            try {
                Reader reader = new StringReader(new String((byte[]) jsonStr, StandardCharsets.UTF_8));
                JsonReader rdr = Json.createReader(reader);
                try {
                    return (T) rdr.read();
                } catch (Exception e) {
                    throw e;
                } finally {
                    rdr.close();
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    private <T> T getJsonObject(byte[] jsonStr, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        if (jsonStr == null) {
            return null;
        }
        return Globals.objectMapper.readValue(jsonStr, clazz);
    }

    private byte[] getJsonFromResponse(HttpExecutionResult<byte[]> result) throws SOSBadRequestException {
        int httpReplyCode = result.response().statusCode();
        switch (httpReplyCode) {
        case 200:
        case 201:
            if (logger.isDebugEnabled() && result.response().body() != null) {
                logger.debug("Response Body: %s", new String(result.response().body(), StandardCharsets.UTF_8));
            }
            return result.response().body();
        default:
            String errorMessage = getErrorMessage(result.response());
            throw new SOSBadRequestException(httpReplyCode, String.format("%d %s%s", httpReplyCode, HttpUtils.getReasonPhrase(httpReplyCode),
                    errorMessage.isEmpty() ? "" : ": " + errorMessage));
        }
    }
    
    private String getErrorMessage(HttpResponse<byte[]> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse(null);
        if (contentType != null && contentType.contains("application/json")) {
            JsonObject err = getJsonStructure(response.body());
            if (err != null) {
                try {
                    return err.getJsonObject("error").getJsonObject("message").getString("value", "");
                } catch (Exception e) {
                    //
                }
            }
        }
        return "";
    }

    private void initHttpClientBuilder(CommonJobArguments jobArgs) throws JobArgumentException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException {
        baseHttpClientBuilder = BaseHttpClient.withBuilder();
        baseHttpClientBuilder.withConnectTimeout(Duration.ofSeconds(jobArgs.getConnectionTimeout().getValue()));
        baseHttpClientBuilder.withLogger(logger);
        KeyStore trustStore = readTruststore(jobArgs);
        if (trustStore != null) {
            baseHttpClientBuilder.withSSLContext(createSslContext(trustStore));
        }
        if (jobArgs.getUri().toString().startsWith("https:") && trustStore == null) {
            if (jobArgs.getTruststorePath().getValue() != null) {
                throw new JobArgumentException(String.format("truststore (%1$s) not found.", jobArgs.getTruststorePath().getValue().toString()));
            } else {
                throw new JobArgumentException("Couldn't find required truststore");
            }
        }
        baseHttpClientBuilder.withAuth(jobArgs.getUser().getValue(), jobArgs.getPwd().getValue());
    }
    
    public static SSLContext createSslContext(KeyStore sslTrustore) throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(sslTrustore);
        SSLContext sslContext = SSLContext.getInstance(SslContextFactory.DEFAULT_PROTOCOL);
        sslContext.init(null, factory.getTrustManagers(), null);
        return sslContext;
    }

    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            client = baseHttpClientBuilder.build();
        }
        return client;
    }
    
    private void addCookieHeader(Map<String,String> headers) {
        headers.put("Cookie", String.join("; ", cookies));
    }
    
    public void close() {
        if(client != null) {
            SOSClassUtil.closeQuietly(client);
            client = null;
        }
    }
}
