package com.sos.jitl.jobs.sap.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.jitl.jobs.sap.common.bean.Job;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.ResponseJobs;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.Schedule;
import com.sos.jitl.jobs.sap.common.bean.ScheduleLog;
import com.sos.js7.job.exception.JobArgumentException;

import jakarta.ws.rs.core.UriBuilder;

public class HttpClient extends SOSRestApiClient {

    private final static String csrfTokenHeaderKey = "X-CSRF-Token";
    private final static String sapClientQueryKey = "sap-client";

    private final ISOSLogger logger;
    private final URI uri;
    private final String sapClient;

    private String csrfToken = null;

    public HttpClient(CommonJobArguments jobArgs, ISOSLogger logger) throws JobArgumentException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, SOSSSLException, IOException {
        this.uri = jobArgs.getUri().getValue();
        this.sapClient = jobArgs.getMandant().getValue();
        this.logger = logger;

        setProperties(jobArgs);
    }

    /** @see https://help.sap.com/viewer/c5bc243e9e824373a5e8de7c37b1dee1/Cloud/en-US/0e432781e0c646a09602a4aab786734d.html
     * @return
     * @throws SocketException
     * @throws SOSException */
    public String fetchCSRFToken() throws SocketException, SOSException {
        clearHeaders();
        addHeader(csrfTokenHeaderKey, "fetch");
        addCookieHeader();
        URI uri = setUriPath();
        logger.info("Fetch token: %s '%s'", HttpMethod.GET.name(), uri);
        logger.debug(printHttpRequestHeaders());
        byte[] response = getRestService(uri, byte[].class);
        logger.debug(printStatusLine());
        logger.debug(printHttpResponseHeaders());
        String token = getResponseHeader(csrfTokenHeaderKey);
        int httpReplyCode = statusCode();
        // token is sent even http code != 200
        if (httpReplyCode != 200) {
            String errorMessage = getErrorMessage(response);
            logger.warn("%d %s%s", httpReplyCode, getHttpResponse().getStatusLine().getReasonPhrase(), errorMessage.isEmpty() ? "" : ": "
                    + errorMessage);
        }
        if (token != null) {
            csrfToken = token;
            return token;
        } else {
            throw new SOSBadRequestException(statusCode(), "Response-Header " + csrfTokenHeaderKey + " is missing");
        }
        // if (httpReplyCode == 200) {
        // csrfToken = getResponseHeader(csrfTokenHeaderKey);
        // return csrfToken;
        // } else {
        // String errorMessage = getErrorMessage(response);
        // throw new SOSBadRequestException(httpReplyCode, String.format("%d %s%s", httpReplyCode, getHttpResponse().getStatusLine()
        // .getReasonPhrase(), errorMessage.isEmpty() ? errorMessage : ": " + errorMessage));
        // }
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/b4d3719173f641b583c97ff0e8f0a7fa.html
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseJobs retrieveJobs() throws JsonParseException, JsonMappingException, SocketException, IOException, SOSException {
        return retrieveJobs(null, null);
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/b4d3719173f641b583c97ff0e8f0a7fa.html
     * @param pageSize
     * @param offset
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseJobs retrieveJobs(Integer pageSize, Integer offset) throws JsonParseException, JsonMappingException, SocketException, IOException,
            SOSException {
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
        logger.info("Retrieve Jobs: %s '%s'", HttpMethod.GET.name(), url.toString());
        return getJsonObject(HttpMethod.GET, url, null, ResponseJobs.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_bdj_psq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/2c1ecb6dae0c42b4a850f7c07d1b7124.html
     * @param body
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseJob createJob(Job body) throws JsonParseException, JsonMappingException, SocketException, IOException, SOSException {
        return createJob(Globals.objectMapper.writeValueAsBytes(body));
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_bdj_psq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/2c1ecb6dae0c42b4a850f7c07d1b7124.html
     * @param body
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public <B> ResponseJob createJob(B body) throws JsonParseException, JsonMappingException, SocketException, IOException, SOSException {
        if (body == null) {
            throw new JobArgumentException("Request body is missing");
        }
        // if (csrfToken == null) {
        // fetchCSRFToken();
        // }
        URI url = setUriPath("scheduler/jobs");
        logger.info("Create Job: %s '%s'", HttpMethod.POST.name(), url.toString());
        return getJsonObject(HttpMethod.POST, url, body, ResponseJob.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_kdn_dxq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/251658d477804d8cb53ef94d0ec231ce.html
     * @param jobId
     * @param jobName
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseJob retrieveJob(Long jobId, String jobName) throws JsonParseException, JsonMappingException, SocketException, IOException,
            SOSException {
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
        logger.info("Retrieve Job: %s '%s'", HttpMethod.GET.name(), url.toString());
        return getJsonObject(HttpMethod.GET, url, null, ResponseJob.class);
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
        // if (csrfToken == null) {
        // fetchCSRFToken();
        // }
        Map<String, Object> uriParameter = new HashMap<>(1);
        uriParameter.put("jobId", jobId);
        URI url = setUriPath("scheduler/jobs/{jobId}", uriParameter);
        logger.info("Delete Job: %s '%s'", HttpMethod.DELETE.name(), url.toString());
        try {
            getJson(HttpMethod.DELETE, url, null);
        } catch (SOSBadRequestException e) {
            if (Arrays.asList(400, 404).contains(e.getHttpCode())) {
                logger.info("Job '%d' already deleted", jobId);
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_vjw_mvq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/66ab3c1404e34a3b9f04b968ecb3fd5f.html
     * @param jobId
     * @param body
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseSchedule createSchedule(Long jobId, Schedule body) throws JsonParseException, JsonMappingException, SocketException, IOException,
            SOSException {
        return createSchedule(jobId, Globals.objectMapper.writeValueAsBytes(body));
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_vjw_mvq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/fa16c72ffb31438faa5d896741f52e73.html
     * @param jobId
     * @param body
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public <B> ResponseSchedule createSchedule(Long jobId, B body) throws JsonParseException, JsonMappingException, SocketException, IOException,
            SOSException {
        if (jobId == null) {
            throw new JobArgumentException("jobId is missing");
        }
        if (body == null) {
            throw new JobArgumentException("Request body is missing");
        }
        // if (csrfToken == null) {
        // fetchCSRFToken();
        // }
        Map<String, Object> uriParameter = new HashMap<>(1);
        uriParameter.put("jobId", jobId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules", uriParameter);
        logger.info("Create Schedule: %s '%s'", HttpMethod.POST.name(), url.toString());
        return getJsonObject(HttpMethod.POST, url, body, ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_n24_cyq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/66ab3c1404e34a3b9f04b968ecb3fd5f.html
     * @param jobId
     * @param scheduleId
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseSchedule retrieveSchedule(Long jobId, String scheduleId) throws JsonParseException, JsonMappingException, SocketException,
            IOException, SOSException {
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
        logger.info("Retrieve Schedule: %s '%s'", HttpMethod.GET.name(), url.toString());
        return getJsonObject(HttpMethod.GET, url, null, ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/6b94445c94ae495c83a19646e7c3fd56/2.0.01/en-US/5ef38ae037434042baf25dc79598bf8d.html#loio5ef38ae037434042baf25dc79598bf8d__section_hvh_gwq_xt
     * @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/0a4d9395180f482db46b8a5375fa6f7f.html
     * @param jobId
     * @param scheduleId
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ResponseSchedule activateSchedule(Long jobId, String scheduleId) throws JsonParseException, JsonMappingException, SocketException,
            IOException, SOSException {
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
        logger.info("Activate Schedule: %s '%s'", HttpMethod.PUT.name(), url.toString());
        return getJsonObject(HttpMethod.PUT, url, Globals.objectMapper.writeValueAsBytes(new Schedule().withActive(true)), ResponseSchedule.class);
    }

    /** @see https://help.sap.com/viewer/07b57c2f4b944bcd8470d024723a1631/Cloud/en-US/e49a4b2a8b2d43d3a9e4629ba29521f4.html
     * @param jobId
     * @param scheduleId
     * @param runId
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws SocketException
     * @throws IOException
     * @throws SOSException */
    public ScheduleLog retrieveScheduleLog(Long jobId, String scheduleId, String runId) throws JsonParseException, JsonMappingException,
            SocketException, IOException, SOSException {
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
        logger.info("Retrieve Schedule Run Log: %s '%s'", HttpMethod.GET.name(), url.toString());
        return getJsonObject(HttpMethod.GET, url, null, ScheduleLog.class);
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
        // if (csrfToken == null) {
        // fetchCSRFToken();
        // }
        Map<String, Object> uriParameter = new HashMap<>(2);
        uriParameter.put("jobId", jobId);
        uriParameter.put("scheduleId", scheduleId);
        URI url = setUriPath("scheduler/jobs/{jobId}/schedules/{scheduleId}", uriParameter);
        logger.info("Delete Schedule: %s '%s'", HttpMethod.DELETE.name(), url.toString());
        try {
            getJson(HttpMethod.DELETE, url, null);
        } catch (SOSBadRequestException e) {
            if (Arrays.asList(400, 404).contains(e.getHttpCode())) {
                logger.info("Schedule '%d/%s' already deleted", jobId, scheduleId);
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    private void setProperties(CommonJobArguments jobArgs) throws JobArgumentException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, SOSSSLException {
        setAutoCloseHttpClient(false);
        setAllowAllHostnameVerifier(jobArgs.getHostnameVerification().getValue());
        setConnectionTimeout(jobArgs.getConnectionTimeout().getValue().intValue() * 1000);
        setSocketTimeout(jobArgs.getSocketTimeout().getValue().intValue() * 1000);
        setSSLContext(null, null, readTruststore(jobArgs));
        setBasicAuthorization(getBasicAuthorization(jobArgs));
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

    private static String getBasicAuthorization(CommonJobArguments jobArgs) {
        return new String(Base64.getEncoder().encode((jobArgs.getUser().getValue() + ":" + jobArgs.getPwd().getValue()).getBytes()));
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

    // private <T extends JsonStructure, B> T getJsonObject(HttpMethod method, URI uri, B postBody) throws SocketException, SOSException {
    // return getJsonStructure(getJson(method, uri, postBody));
    // }

    private <T, B> T getJsonObject(HttpMethod method, URI uri, B postBody, Class<T> clazz) throws JsonParseException, JsonMappingException,
            SocketException, IOException, SOSException {
        return getJsonObject(getJson(method, uri, postBody), clazz);
    }

    private <B> byte[] getJson(HttpMethod method, URI uri, B postBody) throws SocketException, SOSException {
        clearHeaders();
        if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) {
            addHeader("Content-Type", "application/json");
        }
        // is default: addHeader("Accept", "application/json");
        if (csrfToken != null) {
            addHeader(csrfTokenHeaderKey, csrfToken);
        }
        addCookieHeader();
        if (logger.isDebugEnabled()) {
            logger.debug(printHttpRequestHeaders());
            if (postBody != null) {
                if (postBody instanceof String) {
                    logger.debug("Request Body: %s", (String) postBody);
                } else if (postBody instanceof byte[]) {
                    logger.debug("Request Body: %s", new String((byte[]) postBody, StandardCharsets.UTF_8));
                }
            }
        }
        return getJsonFromResponse(executeRestService(method, uri, postBody, byte[].class));
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

    private byte[] getJsonFromResponse(byte[] response) throws SOSBadRequestException {
        if (logger.isDebugEnabled()) {
            logger.debug(printStatusLine());
            logger.debug(printHttpResponseHeaders());
        }
        int httpReplyCode = statusCode();
        switch (httpReplyCode) {
        case 200:
        case 201:
            if (logger.isDebugEnabled() && response != null) {
                logger.debug("Response Body: %s", new String(response, StandardCharsets.UTF_8));
            }
            return response;
        default:
            String errorMessage = getErrorMessage(response);
            throw new SOSBadRequestException(httpReplyCode, String.format("%d %s%s", httpReplyCode, getHttpResponse().getStatusLine()
                    .getReasonPhrase(), errorMessage.isEmpty() ? "" : ": " + errorMessage));
        }
    }

    private <T> String getErrorMessage(byte[] response) {
        String contentType = getResponseHeader("Content-Type");
        if (contentType != null && contentType.contains("application/json")) {
            JsonObject err = getJsonStructure(response);
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

}
