package com.sos.joc.classes.audit;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.model.audit.CategoryType;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
    "account",
    "ipAddress",
    "md5AccessToken",
    "requestUrl",
    "requestBody",
    "response"
})
public class JocAuditTrail extends JocAuditLog {

    private static final Logger AUDIT_TRAIL_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_TRAIL_LOGGER);
    private static final Logger LOGGER = LoggerFactory.getLogger(JocAuditTrail.class);
    
    @JsonIgnore
    private boolean isEnabled = false;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("response")
    private String response;
    
    @JsonProperty("md5AccessToken")
    private String md5AccessToken;
    
    public JocAuditTrail(String user, String request, CategoryType category) {
        super(user, request, category);
        setEnabled();
    }
    
    public JocAuditTrail(String user, String request, String params, String accessToken, String ipAddress, CategoryType category) {
        super(user, request, params, category);
        setEnabled();
        this.ipAddress = ipAddress;
        setMd5AccessToken(accessToken);
    }
    
    private void setEnabled() {
        this.isEnabled = JocCockpitProperties.auditTrailLoggerIsDefined.get() && AUDIT_TRAIL_LOGGER.isInfoEnabled();
    }
    
    private void setMd5AccessToken(String accessToken) {
        if (accessToken != null) {
            try {
                this.md5AccessToken = SOSString.hashMD5(accessToken);
            } catch (Exception e) {
                this.md5AccessToken = EMPTY_STRING;
                LOGGER.error("", e);
            }
        } else {
            this.md5AccessToken = EMPTY_STRING;
        }
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public void setResponse(byte[] response) {
        setResponse(new String(response, StandardCharsets.UTF_8));
    }
    
    public void setResponse(Object response) {
        try {
            setResponse(Globals.objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            setResponse(EMPTY_STRING);
            LOGGER.error("", e);
        }
    }
    
    @JsonProperty("ipAddress")
    public String getIpAddress() {
        return ipAddress;
    }
    
    @JsonProperty("response")
    public String getResponse() {
        return response;
    }
    
    @JsonProperty("md5AccessToken")
    public String getMd5AccessToken() {
        return md5AccessToken;
    }
    
    public void log() {
        if (isEnabled) {
            try {
                AUDIT_TRAIL_LOGGER.info(Globals.objectMapper.writeValueAsString(this).replaceFirst("^\\{", ""));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
