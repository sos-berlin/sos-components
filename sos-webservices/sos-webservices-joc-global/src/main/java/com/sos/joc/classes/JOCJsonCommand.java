package com.sos.joc.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSConnectionResetException;
import com.sos.commons.httpclient.exception.SOSNoResponseException;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.exceptions.ControllerConflictException;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.ControllerNoResponseException;
import com.sos.joc.exceptions.ControllerServiceUnavailableException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.UnknownJobSchedulerAgentException;

import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriBuilder;

public class JOCJsonCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOCJsonCommand.class);
    private static final String CONTROLLER_API_PATH = "/controller/api";
    private UriBuilder uriBuilder;
    private JOCResourceImpl jocResourceImpl;
    private String url = null;
    private String csrfToken = null;
    private BaseHttpClient client;
    private BaseHttpClient.Builder baseHttpClientBuilder;
    public static Map<String, String> urlMapper = Collections.emptyMap();
    
    public JOCJsonCommand() {
        init();
    }

    public JOCJsonCommand(DBItemInventoryJSInstance dbItemInventoryInstance, String csrfToken) {
        this.url = urlMapper.getOrDefault(dbItemInventoryInstance.getUri(), dbItemInventoryInstance.getUri());
        this.csrfToken = csrfToken;
        init();
    }
    
    public JOCJsonCommand(String uri, String csrfToken) {
        this.url = urlMapper.getOrDefault(uri, uri);
        this.csrfToken = csrfToken;
        init();
    }

    public JOCResourceImpl getJOCResourceImpl() {
        return jocResourceImpl;
    }
    
    public void setUriBuilderForCommands() {
    	setUriBuilder(url, CONTROLLER_API_PATH + "/command");
    }
    
    public void setUriBuilderForOverview() {
        setUriBuilder(url, CONTROLLER_API_PATH);
    }
    
    public void setUriBuilderForCluster() {
        setUriBuilder(url, CONTROLLER_API_PATH + "/cluster");
    }
    
    public URI getUriForJobPathAsUrlParam(String jobPath, Integer limit) {
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(CONTROLLER_API_PATH + "/job/{path}");
        uriBuilder.queryParam("return", "History");
        uriBuilder.queryParam("limit", limit);
        return uriBuilder.buildFromEncoded(jobPath.replaceFirst("^/+", ""));
    }
    
    public void setUriBuilderForMainLog(boolean snapshot) { ///api/controller/log?snapshot=true
        uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.path(CONTROLLER_API_PATH + "/log");
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
    
    private String getJsonStringFromPost(String postBody) throws JocException {
        try {
            return getJsonStringFromPost(getURI(), postBody);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(e);
        }
    }
    
    private String getJsonStringFromPost(URI uri, String postBody) throws JocException {
        if (postBody == null) {
            postBody = "";
        }
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()), "JS-PostBody: " + postBody);
        LOGGER.debug("JS-URL: " + (uri == null ? "null" : uri.toString()), "JS-PostBody: " + postBody);
        try {
            createClient();
            HttpExecutionResult<String> result = client.executePOST(uri, 
                    Map.of("Content-Type", "application/json", "Accept", "application/json", "X-CSRF-Token", getCsrfToken()), 
                    postBody);
            return getJsonStringFromResponse(result, uri, jocError);
        } catch (SOSConnectionResetException e) {
            throw new ControllerConnectionResetException(e.toString(), e);
        } catch (SOSConnectionRefusedException e) {
            throw new ControllerConnectionRefusedException(e.toString(), e);
        } catch (SOSNoResponseException e) {
            throw new ControllerNoResponseException(e.toString(), e);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(jocError, e);
        }
    }
    
    public StreamingOutput getStreamingOutputFromGet(String acceptHeader, boolean withGzipEncoding) {
        try {
            return getStreamingOutputFromGet(getURI(), acceptHeader, withGzipEncoding, getCsrfToken());
        } catch (SOSNoResponseException e) {
            throw new ControllerNoResponseException(e.toString(), e);
        }
    }
    
    private StreamingOutput getStreamingOutputFromGet(URI uri, String acceptHeader, boolean withGzipEncoding, String csrfToken)
            throws SOSNoResponseException {
        Map<String,String> headers = new HashMap<String, String>();
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            headers.put("Accept", acceptHeader);
        }
        if (withGzipEncoding) {
            headers.put("Accept-Encoding", "gzip");
        }
        headers.put("X-CSRF-Token", csrfToken);
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()));
        StreamingOutput fileStream = null;
        try {
            createClient();
            HttpExecutionResult<InputStream> result = client.executeGET(uri, headers, HttpResponse.BodyHandlers.ofInputStream());
            if (result != null && result.response() != null) {
                final InputStream instream = result.response().body();
                fileStream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        if (withGzipEncoding) {
                            output = new GZIPOutputStream(output);
                        }
                        try {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = instream.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            output.flush();
                        } finally {
                            try {
                                output.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                };
            }
            return fileStream;
        } catch (Exception e) {
            throw new SOSNoResponseException(e);
        } catch (Throwable e) {
            throw e;
        }
    }

    public <T> T getJsonObjectFromGet(Class<T> clazz) {
		return getJsonObject(getJsonStringFromGet(), clazz);
	}
    
    private String getJsonStringFromGet() throws JocException {
        try {
            return getJsonStringFromGet(getURI());
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(e);
        }
    }

    public String getJsonStringFromGet(URI uri) {
        JocError jocError = new JocError();
        jocError.appendMetaInfo("JS-URL: " + (uri == null ? "null" : uri.toString()));
        try {
            createClient();
            HttpExecutionResult<String> result = client.executeGET(uri,
                    client.mergeWithDefaultHeaders(Map.of("Accept", "application/json", "X-CSRF-Token", getCsrfToken())),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return getJsonStringFromResponse(result, uri, jocError);
        } catch (SOSConnectionRefusedException e) {
            throw new ControllerConnectionRefusedException(e.toString(), e);
        } catch (SOSConnectionResetException e) {
            throw new ControllerConnectionResetException(e.toString(), e);
        } catch (SOSNoResponseException e) {
            throw new ControllerNoResponseException(e.toString(), e);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(jocError, e);
        }
    }
    
    public String getCsrfToken() {
        if (csrfToken == null || csrfToken.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return csrfToken;
    }
    
    private void init() throws ControllerConnectionRefusedException {
        baseHttpClientBuilder = BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofMillis(Globals.httpConnectionTimeout))
                .withLogger(new SLF4JLogger(LOGGER)).withSSLContext(SSLContext.getInstance().getSSLContext());
        if (url.startsWith("https:") && SSLContext.getInstance().getTrustStore() == null) {
            throw new ControllerConnectionRefusedException("Couldn't find required truststore");
        }
        ProxyCoupled evt = Proxies.getJOCCredentials(this.url);
        if (evt != null) {
            //LOGGER.info(String.format("ProxyCoupled event exists for %s with %s:%s", this.url, evt.getUser(), evt.getPwd()));
            baseHttpClientBuilder.withAuth(evt.getUser(), evt.getPwd());
        } else {
            baseHttpClientBuilder.withAuth(ProxyUser.JOC.getUser(), ProxyUser.JOC.getPwd());
        }
    }
    
    private <T extends JsonStructure> T getJsonStructure(String jsonStr) {
    	JsonReader rdr = Json.createReader(new StringReader(jsonStr));
        try {
            @SuppressWarnings("unchecked")
            T json = (T) rdr.read();
            return json;
        } catch (Exception e) {
            //LOGGER.error(jsonStr);
            throw e;
        } finally {
            rdr.close();
        }
    }
    
    private <T> T getJsonObject(String jsonStr, Class<T> clazz) throws ControllerInvalidResponseDataException {
        try {
            if (jsonStr == null) {
                return null;
            }
            return Globals.objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new ControllerInvalidResponseDataException(e);
        }
    }

    private String getJsonStringFromResponse(HttpExecutionResult<String> result, URI uri, JocError jocError) {
        int httpReplyCode = result.response().statusCode();
        List<String> contentTypes = result.response().headers().allValues("Content-Type");
        String response = result.response().body();
        if (response == null) {
            response = "";
        }
        String actualContentType = result.response().headers().firstValue("Content-Type").orElse("");
        // TODO Async call while JobScheduler is terminating 
//        if (response.contains("com.sos.scheduler.engine.common.async.CallQueue$ClosedException")) {
//            throw new JobSchedulerConnectionResetException(response);
//        }
        try {
            switch (httpReplyCode) {
            case 200:
                if (contentTypes.contains("application/json")) {
                    if (response.isEmpty()) {
                        throw new ControllerNoResponseException("Unexpected empty response");
                    }
                    LOGGER.debug(response.toString());
                    return response;
                } else {
                    throw new ControllerInvalidResponseDataException(String.format("Unexpected content type '%1$s'. Response: %2$s", 
                            actualContentType, response));
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
                throw new JocBadRequestException(getJsonErrorMessage(actualContentType, response, uri));
            case 409:
                throw new ControllerConflictException(getJsonErrorMessage(actualContentType, response, uri));
            case 503:
                //TODO consider code=ControllerIsNotYetReady for passive cluster node
                throw new ControllerServiceUnavailableException(getJsonErrorMessage(actualContentType, response, uri));
            default:
                throw new JocBadRequestException(httpReplyCode + " " + HttpUtils.getReasonPhrase(httpReplyCode));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            throw e;
        }
    }
    
    private String getJsonErrorMessage(String contentType, String response, URI uri) {
        if (!response.isEmpty()) {
            if (contentType.contains("application/json")) {
                JsonReader rdr = Json.createReader(new StringReader(response));
                JsonObject json = rdr.readObject();
                rdr.close();
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
        } else {
            return uri.toString();
        }
    }
    
    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            client = baseHttpClientBuilder.build();
        }
        return client;
    }
    
}
