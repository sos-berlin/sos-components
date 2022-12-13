package com.sos.auth.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public class SOSAuthCurrentAccountAnswer {

    private String account;
    @JsonProperty("role")
    private String role;

    @JsonProperty("permission")
    private String permission;

    @JsonProperty("isPermittet")
    private boolean isPermittet;

    @JsonProperty("hasRole")
    private boolean hasRole;

    @JsonProperty("isAuthenticated")
    private boolean isAuthenticated;

    @JsonProperty("forcePasswordChange")
    private boolean forcePasswordChange;

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("message")
    private String message;

    @JsonProperty("sessionTimeout")
    private Long sessionTimeout;

    @JsonProperty("enableTouch")
    private boolean enableTouch = true;

    @JsonProperty("callerIpAddress")
    private String callerIpAddress;

    @JsonProperty("callerHostName")
    private String callerHostName;

    @JsonProperty("identityService")
    private String identityService;
    
    @JsonIgnore
    private String apiCall;

    public SOSAuthCurrentAccountAnswer(String account) {
        this.account = account;
    }

    public SOSAuthCurrentAccountAnswer() {
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return this.account;
    }

    public void setIsPermitted(boolean isPermitted) {
        this.isPermittet = isPermitted;
    }

    public boolean getIsPermitted() {
        return this.isPermittet;
    }

    public boolean isPermitted() {
        return getIsPermitted();
    }

    public void setHasRole(boolean hasRole) {
        this.hasRole = hasRole;
    }

    public boolean getHasRole() {
        return this.hasRole;
    }

    public boolean hasRole() {
        return getHasRole();
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String accessToken() {
        return getAccessToken();
    }

    public void setIsAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public boolean getIsAuthenticated() {
        return this.isAuthenticated;
    }

    public boolean isAuthenticated() {
        return getIsAuthenticated();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Long getSessionTimeout() {
        return this.sessionTimeout;
    }

    public void setEnableTouch(boolean enableTouch) {
        this.enableTouch = enableTouch;
    }

    public boolean getEnableTouch() {
        return this.enableTouch;
    }

    public String getCallerIpAddress() {
        return callerIpAddress;
    }

    public void setCallerIpAddress(String callerIpAddress) {
        this.callerIpAddress = callerIpAddress;
    }

    public String getCallerHostName() {
        return callerHostName;
    }

    public void setCallerHostName(String callerHostName) {
        this.callerHostName = callerHostName;
    }

    public void setIdentityService(String identityService) {
        this.identityService = identityService;
    }

    public String getIdentityService() {
        return identityService;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    public boolean isForcePasswordChange() {
        return getForcePasswordChange();
    }
    
    public void setApiCall(String apiCall) {
        this.apiCall = apiCall;
    }

    public String getApiCall() {
        return apiCall;
    }

    @Override
    public String toString() {
        return String.format("Account: %s Role: %s hasRole: %s Permission: %s isPermitted: %s -- AccessToken=%s", this.account, this.role,
                this.hasRole, this.permission, this.isPermittet, this.accessToken);
    }

}