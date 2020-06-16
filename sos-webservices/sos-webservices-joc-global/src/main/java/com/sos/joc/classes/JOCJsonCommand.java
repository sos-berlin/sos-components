package com.sos.joc.classes;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSConnectionResetException;
import com.sos.commons.httpclient.exception.SOSNoResponseException;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConflictException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JobSchedulerServiceUnavailableException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.UnknownJobSchedulerAgentException;

public class JOCJsonCommand extends SOSRestApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOCJsonCommand.class);
    private static final String MASTER_API_PATH = "/master/api";
    private UriBuilder uriBuilder;
    private JOCResourceImpl jocResourceImpl;
    private String url = null;
    private String csrfToken = null;
    
    public JOCJsonCommand() {
        setProperties();
    }

    public JOCJsonCommand(JOCResourceImpl jocResourceImpl) {
        this.jocResourceImpl = jocResourceImpl;
        this.url = jocResourceImpl.getUrl();
        this.csrfToken = jocResourceImpl.getAccessToken();
        setClientCertificate(SSLContext.keystore);
        setProperties();
    }
    
    public JOCJsonCommand(JOCResourceImpl jocResourceImpl, String path) {
        this.jocResourceImpl = jocResourceImpl;
        this.url = jocResourceImpl.getUrl();
        this.csrfToken = jocResourceImpl.getAccessToken();
        setClientCertificate(SSLContext.keystore);
        setProperties();
        setUriBuilder(jocResourceImpl.getUrl(), path);
    }
    
    public JOCJsonCommand(JOCJsonCommand jocJsonCommand) {
        this.jocResourceImpl = jocJsonCommand.getJOCResourceImpl();
        this.url = jocResourceImpl.getUrl();
        this.csrfToken = jocResourceImpl.getAccessToken();
        setClientCertificate(SSLContext.keystore);
        setProperties();
        this.uriBuilder = jocJsonCommand.getUriBuilder();
    }
    
    public JOCJsonCommand(DBItemInventoryInstance dbItemInventoryInstance, String csrfToken) {
        setClientCertificate(SSLContext.keystore);
        this.url = dbItemInventoryInstance.getUri();
        this.csrfToken = csrfToken;
        setProperties();
    }
    
    public JOCJsonCommand(URI uri, String csrfToken) {
        setClientCertificate(SSLContext.keystore);
        this.url = uri.toString();
        this.csrfToken = csrfToken;
        setProperties();
    }

    public void setJOCResourceImpl(JOCResourceImpl jocResourceImpl) {
        this.jocResourceImpl = jocResourceImpl;
        this.url = jocResourceImpl.getUrl();
        this.csrfToken = jocResourceImpl.getAccessToken();
    }
    
    public JOCResourceImpl getJOCResourceImpl() {
        return jocResourceImpl;
    }
    
    public void setUriBuilderForCommands() {
    	setUriBuilderForCommands(url);
    }
    
    public void setUriBuilderForCommands(String url) {
        setUriBuilder(url, MASTER_API_PATH + "/command");
    }
    
    public void setUriBuilderForOrders() {
        setUriBuilderForOrders(url);
    }
    
    public void setUriBuilderForOrders(String url) {
        setUriBuilder(url, MASTER_API_PATH + "/order");
    }
    
    public void setUriBuilderForEvents() {
        setUriBuilderForEvents(url);
    }
    
    public void setUriBuilderForEvents(String url) {
        setUriBuilder(url, MASTER_API_PATH + "/event");
    }

    public void setUriBuilderForProcessClasses() {
        setUriBuilderForProcessClasses(url);
    }
    
    public void setUriBuilderForProcessClasses(String url) {
        setUriBuilder(url, MASTER_API_PATH + "/processClass");
    }

    public void setUriBuilderForJobs() {
        setUriBuilderForJobs(url);
    }
    
    public void setUriBuilderForJobs(String url) {
        setUriBuilder(url, MASTER_API_PATH + "/job");
    }
    
    public void setUriBuilderForOverview() {
        setUriBuilder(url, MASTER_API_PATH);
    }
    
    public void setUriBuilderForCluster() {
        setUriBuilder(url, MASTER_API_PATH + "/cluster");
    }
    
    public URI getUriForJobPathAsUrlParam(String jobPath, Integer limit) {
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(MASTER_API_PATH + "/job/{path}");
        uriBuilder.queryParam("return", "History");
        uriBuilder.queryParam("limit", limit);
        return uriBuilder.buildFromEncoded(jobPath.replaceFirst("^/+", ""));
    }
    
    public void setUriBuilderForMainLog(boolean snapshot) { ///api/master/log?snapshot=true
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(MASTER_API_PATH + "/log");
        if (snapshot) {
            uriBuilder.queryParam("snapshot", true);
        }
    }
    
    public void setToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }
    
    public void setUriBuilder(String path) {
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(path);
    }
    
    public void setUriBuilder(String url, String path) {
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(path);
    }

    public void setUriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder;
    }
    
    public UriBuilder replaceUriBuilder(String url, URI uri) {
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(uri.getPath());
        uriBuilder.replaceQuery(uri.getQuery());
        return uriBuilder;
    }

    public URI getURI() {
        return uriBuilder.build();
    }
    
    public String getSchemeAndAuthority() {
        URI uri = uriBuilder.build();
        return uri.getScheme()+"://"+uri.getAuthority();
    }
    
    public static String urlEncodedPath(String value) {
        return UriBuilder.fromPath("{path}").buildFromEncoded(value).toString();
    }
    
    public void addJobHistoryQuery(Integer limit) {
        uriBuilder.queryParam("return", "History");
        uriBuilder.queryParam("limit", limit);
    }

    public void addOrderCompactQuery(boolean compact) {
        //String returnQuery = (compact) ? "OrdersComplemented/OrderOverview" : "OrdersComplemented/OrderDetailed";
        //Workaround: JOC-84 
        String returnQuery = "OrdersComplemented/OrderDetailed";
        uriBuilder.queryParam("return", returnQuery);
    }
    
    public void addJobCompactQuery(boolean compact) {
        String returnQuery = (compact) ? "JobOverview" : "JobDetailed";
        uriBuilder.queryParam("return", returnQuery);
    }

    public void addOrderStatisticsQuery() {
        addOrderStatisticsQuery(null);
    }
    
    public void addOrderStatisticsQuery(Boolean isDistributed) {
        uriBuilder.queryParam("return", "JocOrderStatistics");
        if (isDistributed != null) {
            uriBuilder.queryParam("isDistributed", isDistributed);
        }
    }
    
    public void addEventTimeout(Integer timeout) {
        if (timeout == null) {
            timeout = 0;
        }
        uriBuilder.queryParam("timeout", timeout);
    }
    
    public void replaceEventQuery(Long eventId, Integer timeout) {
        uriBuilder.replaceQueryParam("after", eventId);
        uriBuilder.replaceQueryParam("timeout", timeout);
    }
    
    public void addEventQuery(Long eventId, Integer timeout) {
        addEventQuery(eventId, timeout, null);
    }
    
    public void addEventQuery(Long eventId, Integer timeout, String event) {
        if (event != null && !event.isEmpty()) {
            uriBuilder.queryParam("return", event);
        }
        if (timeout == null) {
            timeout = 0;
        }
        uriBuilder.queryParam("timeout", timeout);
        uriBuilder.queryParam("after", eventId);
    }
    
    public void addOrderEventQuery(Long eventId, Integer timeout) {
        addEventQuery(eventId, timeout, "OrderEvent");
    }

    public void addProcessClassCompactQuery(boolean compact) {
        String returnQuery = (compact) ? "ProcessClassOverview" : "ProcessClassDetailed";
        uriBuilder.queryParam("return", returnQuery);
    }

    public void addJobDescriptionQuery() {
        uriBuilder.queryParam("return", "JobDescription");
    }

    public <T extends JsonStructure> T getJsonObjectFromPost(String postBody) throws JocException {
    	return getJsonStructure(getJsonStringFromPost(postBody));
    }
    
	public <T> T getJsonObjectFromPost(String postBody, Class<T> clazz) throws JocException {
		return getJsonObject(getJsonStringFromPost(postBody), clazz);
	}
    
    public String getJsonStringFromPost(String postBody) throws JocException {
        try {
            return getJsonStringFromPost(getURI(), postBody);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(e);
        }
    }
    
    public String getJsonStringFromPost(URI uri, String postBody) throws JocException {
        addHeader("Content-Type", "application/json");
        addHeader("Accept", "application/json");
        addHeader("X-CSRF-Token", getCsrfToken());
        if (postBody == null) {
            postBody = "";
        }
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()), "JS-PostBody: " + postBody);
        LOGGER.debug("JS-URL: " + (uri == null ? "null" : uri.toString()), "JS-PostBody: " + postBody);
        try {
            String response = postRestService(uri, postBody);
            return getJsonStringFromResponse(response, uri, jocError);
        } catch (SOSConnectionResetException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionResetException(jocError, e);
            }
        } catch (SOSConnectionRefusedException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionRefusedException(jocError, e);
            }
        } catch (SOSNoResponseException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerNoResponseException(jocError, e);
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(jocError, e);
        }
    }
    
    public <T extends JsonStructure> T getJsonObjectFromPostWithRetry(String postBody) throws JocException {
    	return getJsonStructure(getJsonStringFromPostWithRetry(postBody));
    }
    
    public <T> T getJsonObjectFromPostWithRetry(String postBody, Class<T> clazz) throws JocException {
		return getJsonObject(getJsonStringFromPostWithRetry(postBody), clazz);
	}
    
    public String getJsonStringFromPostWithRetry(String postBody) throws JocException {
    	return getJsonStringFromPostWithRetry(getURI(), postBody);
    }
    
    public String getJsonStringFromPostWithRetry(URI uri, String postBody) throws JocException {
        try {
            return getJsonStringFromPost(uri, postBody);
        } catch (JobSchedulerConnectionRefusedException | JobSchedulerConnectionResetException | JobSchedulerServiceUnavailableException e) {
            String url = null;
            if (jocResourceImpl != null) {
                url = jocResourceImpl.retrySchedulerInstance(); 
            }
            if (url != null) {
                uri = replaceUriBuilder(url, uri).build();
                return getJsonStringFromPost(uri, postBody);
            } else {
                throw e;
            }
        } catch (JocException e) {
            throw e;
        }
    }
    
    public Path getFilePathFromGet(URI uri, String prefix, String acceptHeader, boolean withGzipEncoding) throws JocException {
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            addHeader("Accept", acceptHeader);
        }
        addHeader("Accept-Encoding", "gzip");
        addHeader("X-CSRF-Token", getCsrfToken());
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()));
        try {
            return getFilePathFromResponse(getFilePathByRestService(uri, prefix, withGzipEncoding), uri, jocError);
        } catch (SOSConnectionRefusedException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionRefusedException(jocError, e);
            }
        } catch (SOSConnectionResetException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionResetException(jocError, e);
            }
        } catch (SOSNoResponseException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerNoResponseException(jocError, e);
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(jocError, e);
        }
    }
    
    public StreamingOutput getStreamingOutputFromGet(String acceptHeader, boolean withGzipEncoding) throws JocException {
        return getStreamingOutputFromGet(getURI(), acceptHeader, withGzipEncoding);
    }
    
    public StreamingOutput getStreamingOutputFromGet(URI uri, String acceptHeader, boolean withGzipEncoding) throws JocException {
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            addHeader("Accept", acceptHeader);
        }
        if (withGzipEncoding) {
            addHeader("Accept-Encoding", "gzip");
        }
        addHeader("X-CSRF-Token", getCsrfToken());
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()));
        try {
            return getStreamingOutputFromResponse(getStreamingOutputByRestService(uri, withGzipEncoding), uri, jocError);
        } catch (SOSConnectionRefusedException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionRefusedException(jocError, e);
            }
        } catch (SOSConnectionResetException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionResetException(jocError, e);
            }
        } catch (SOSNoResponseException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerNoResponseException(jocError, e);
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(jocError, e);
        }
    }

    public <T extends JsonStructure> T getJsonObjectFromGet() throws JocException {
    	return getJsonStructure(getJsonStringFromGet());
    }
    
    public <T> T getJsonObjectFromGet(Class<T> clazz) throws JocException {
		return getJsonObject(getJsonStringFromGet(), clazz);
	}
    
    public String getJsonStringFromGet() throws JocException {
        try {
            return getJsonStringFromGet(getURI());
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(e);
        }
    }

    public String getJsonStringFromGet(URI uri) throws JocException {
        addHeader("Accept", "application/json");
        addHeader("X-CSRF-Token", getCsrfToken());
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()));
        try {
            //LOGGER.info(uri.toString());
            String response = getRestService(uri);
            //LOGGER.info(response);
            return getJsonStringFromResponse(response, uri, jocError);
        } catch (SOSConnectionRefusedException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionRefusedException(jocError, e);
            }
        } catch (SOSConnectionResetException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerConnectionResetException(jocError, e);
            }
        } catch (SOSNoResponseException e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JobSchedulerNoResponseException(jocError, e);
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerBadRequestException(jocError, e);
        }
    }
    
    public <T extends JsonStructure> T getJsonObjectFromGetWithRetry() throws JocException {
        return getJsonStructure(getJsonStringFromGetWithRetry());
    }
    
    public <T> T getJsonObjectFromGetWithRetry(Class<T> clazz) throws JocException {
		return getJsonObject(getJsonStringFromGetWithRetry(), clazz);
	}
    
    public String getJsonStringFromGetWithRetry() throws JocException {
    	return getJsonStringFromGetWithRetry(getURI());
    }
    
    public String getJsonStringFromGetWithRetry(URI uri) throws JocException {
        try {
            return getJsonStringFromGet(uri);
        } catch (JobSchedulerConnectionRefusedException | JobSchedulerConnectionResetException | JobSchedulerServiceUnavailableException e) {
            String url = null;
            if (jocResourceImpl != null) {
                url = jocResourceImpl.retrySchedulerInstance(); 
            }
            if (url != null) {
                uri = replaceUriBuilder(url, uri).build();
                return getJsonStringFromGet(uri);
            } else {
                throw e;
            }
        } catch (JocException e) {
            throw e;
        }
    }
    
    public String getCsrfToken() {
        if (csrfToken == null || csrfToken.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return csrfToken;
    }
    
    private void setProperties() {
        setAllowAllHostnameVerifier(!Globals.withHostnameVerification);
        setConnectionTimeout(Globals.httpConnectionTimeout);
        setSocketTimeout(Globals.httpSocketTimeout);
    }
    
    private <T extends JsonStructure> T getJsonStructure(String jsonStr) {
    	JsonReader rdr = Json.createReader(new StringReader(jsonStr));
    	@SuppressWarnings("unchecked")
        T json = (T) rdr.read();
        rdr.close();
        return json;
    }
    
	private <T> T getJsonObject(String jsonStr, Class<T> clazz) throws JobSchedulerInvalidResponseDataException {
		try {
			if (jsonStr == null) {
				return null;
			}
			return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(jsonStr,
					clazz);
		} catch (Exception e) {
			throw new JobSchedulerInvalidResponseDataException(e);
		}
	}

    private String getJsonStringFromResponse(String response, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = statusCode();
        String contentType = getResponseHeader("Content-Type");
        if (response == null) {
            response = "";
        }
        
        // TODO Async call while JobScheduler is terminating 
//        if (response.contains("com.sos.scheduler.engine.common.async.CallQueue$ClosedException")) {
//            throw new JobSchedulerConnectionResetException(response);
//        }

        try {
            switch (httpReplyCode) {
            case 200:
                if (contentType.contains("application/json")) {
                    if (response.isEmpty()) {
                        throw new JobSchedulerNoResponseException("Unexpected empty response");
                    }
                    LOGGER.debug(response.toString());
                    return response;
                } else {
                    throw new JobSchedulerInvalidResponseDataException(String.format("Unexpected content type '%1$s'. Response: %2$s", contentType,
                            response));
                }
            case 201:
                return response;
            case 400:
                if ("Unknown Agent".equalsIgnoreCase(response)) { //TODO from JS1
                    throw new UnknownJobSchedulerAgentException(uri.toString().replaceFirst(".*/(https?://.*)$", "$1"));
                }
//              if (type.equals("Problem")) {  //TODO all are problems. Use code later, but yet not always inside the answer
//                  throw new JobSchedulerObjectNotExistException(msg);
//              }
                throw new JobSchedulerBadRequestException(getJsonErrorMessage(contentType, response));
            case 409:
                throw new JobSchedulerConflictException(getJsonErrorMessage(contentType, response));
            case 503:
                //TODO consider code=MasterIsNotYetReady for passive cluster node
                throw new JobSchedulerServiceUnavailableException(getJsonErrorMessage(contentType, response));
            default:
                throw new JobSchedulerBadRequestException(httpReplyCode + " " + getHttpResponse().getStatusLine().getReasonPhrase());
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            throw e;
        }
    }
    
    private Path getFilePathFromResponse(Path response, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = statusCode();
        try {
            switch (httpReplyCode) {
            case 200:
                try {
                    if (response == null || Files.size(response) <= 0) {
                        throw new JobSchedulerNoResponseException("Unexpected empty response");
                    }
                } catch (IOException e) {
                    throw new JobSchedulerNoResponseException("Unexpected empty response");
                }
                return response;
            default:
                throw new JobSchedulerBadRequestException(httpReplyCode + " " + getHttpResponse().getStatusLine().getReasonPhrase());
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            try {
                Files.deleteIfExists(response);
            } catch (IOException e1) {}
            throw e;
        }
    }
    
    private StreamingOutput getStreamingOutputFromResponse(StreamingOutput streamingOutPut, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = statusCode();
        try {
            switch (httpReplyCode) {
            case 200:
                if (streamingOutPut == null ) {
                    throw new JobSchedulerNoResponseException("Unexpected empty response");
                }
                return streamingOutPut;
            default:
                throw new JobSchedulerBadRequestException(httpReplyCode + " " + getHttpResponse().getStatusLine().getReasonPhrase());
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            throw e;
        }
    }
    
    private String getJsonErrorMessage(String contentType, String response) {
        if (contentType.contains("application/json") && !response.isEmpty()) {
            JsonReader rdr = Json.createReader(new StringReader(response));
            JsonObject json = rdr.readObject();
            rdr.close();
//            String type = json.getString("TYPE", "");
            String msg = json.getString("message", null);
            String code = json.getString("code", null);
            if (msg == null) {
                msg = response; 
            } else {
                if (code != null) {
                   msg = code + ": " + msg; 
                }
            }
            return msg;
        } else {
            return response;
        }
    }
}
