package com.sos.joc.classes.audit;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.model.audit.CategoryType;


@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
//    "timestamp",
    "thread",
    "account",
    "ipAddress",
    "md5AccessToken",
    "requestUrl",
    "requestBody",
    "responseHeaders",
    "response"
})
public class JocAuditTrail extends JocAuditLog {

    private static final Logger AUDIT_TRAIL_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_TRAIL_LOGGER);
    private static final Logger LOGGER = LoggerFactory.getLogger(JocAuditTrail.class);
    private static final String EMPTY_STRING = "";
    
    @JsonIgnore
    private boolean isEnabled = false;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("response")
    private String response;
    
    @JsonProperty("responseHeaders")
    private Map<String, Object> responseHeaders = new HashMap<>();
    
    @JsonProperty("md5AccessToken")
    private String md5AccessToken;
    
    @JsonProperty("thread")
    private String thread;
    
//    @JsonProperty("timestamp")
//    private Date timestamp;
    
    public JocAuditTrail() {
        super(null, null, null);
        this.isEnabled = false;
    }
    
    public JocAuditTrail(String user, String request, CategoryType category) {
        super(user, request, category);
        setEnabled();
    }
    
    public JocAuditTrail(String user, String request, String params, Optional<String> accessToken, Optional<String> ipAddress, CategoryType category) {
        super(user, request, params, category);
        setEnabled();
        setIpAddress(ipAddress);
        setMd5AccessToken(accessToken);
        this.thread = Thread.currentThread().getName();
    }
    
    private void setEnabled() {
        this.isEnabled = JocCockpitProperties.auditTrailLoggerIsDefined.get() && AUDIT_TRAIL_LOGGER.isInfoEnabled();
    }
    
    private void setMd5AccessToken(Optional<String> accessToken) {
        this.md5AccessToken = accessToken.map(t -> {
            try {
                return SOSString.hashMD5(t);
            } catch (Exception e) {
                LOGGER.error("", e);
                return "";
            }
        }).orElse("");
    }
    
    private void setIpAddress(Optional<String> ipAddress) {
        this.ipAddress = ipAddress.map(ip -> {
            if ("127.0.0.1".equals(ip)) {
                return SOSShell.getLocalHostNameOptional().flatMap(SOSShell::getHostAddressOptional).orElse(ip);
            }
            return ip;
        }).orElse("");
    }
    
    public void setResponseHeaders(Map<String, Object> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
    
    public void addResponseHeader(String key, Object value) {
        this.responseHeaders.put(key, value);
    }
    
    public void addContentTypeHeader(Object value) {
        this.responseHeaders.put("Content-Type", value);
    }
    
    public void setResponse(String response) {
        if (isEnabled && !md5AccessToken.isEmpty()) {
            this.response = response;
        }
    }

    public void setResponse(byte[] response) {
        if (response != null && isEnabled && !md5AccessToken.isEmpty()) {
            setResponse(new String(response, StandardCharsets.UTF_8));
        }
    }

    public void setResponse(Object response) {
        if (response != null && isEnabled && !md5AccessToken.isEmpty()) {
            try {
                setResponse(Globals.objectMapper.writeValueAsString(response).replaceAll("(\"accessToken\")\\s*:\\s*\"[^\"]*\"", "$1:\"\""));
            } catch (JsonProcessingException e) {
                setResponse(EMPTY_STRING);
                LOGGER.error("", e);
            }
        }
    }
    
    public void log() {
        if (isEnabled && !md5AccessToken.isEmpty()) {
        //if (!md5AccessToken.isEmpty()) {    
            try {
                //timestamp = Date.from(Instant.now());
                AUDIT_TRAIL_LOGGER.info(Globals.objectMapper.writeValueAsString(this).replaceFirst("^\\{", ""));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
