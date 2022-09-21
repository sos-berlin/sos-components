package com.sos.auth.classes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement(name = "sosauth_current_user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SOSAuthCurrentAccountAnswer {

    private String account;
    private String role;
    private String permission;
    private boolean isPermittet;
    private boolean hasRole;
    private boolean isAuthenticated;
    private boolean isForcePasswordChange;
    private String accessToken;
    private String refreshToken;

    private String message;
    private Long sessionTimeout;
    private boolean enableTouch = true;
    private String callerIpAddress;
    private String callerHostName;
    private String identityService;

    public SOSAuthCurrentAccountAnswer() {
    }

    public SOSAuthCurrentAccountAnswer(String account) {
        this.account = account;
    }

    @XmlAttribute
    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }

    @XmlAttribute
    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

    @XmlAttribute
    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return this.account;
    }

    @XmlElement
    public void setIsPermitted(boolean isPermitted) {
        this.isPermittet = isPermitted;
    }

    public boolean getIsPermitted() {
        return this.isPermittet;
    }

    public boolean isPermitted() {
        return getIsPermitted();
    }

    @XmlElement
    public void setHasRole(boolean hasRole) {
        this.hasRole = hasRole;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @XmlElement
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean getHasRole() {
        return this.hasRole;
    }

    public boolean hasRole() {
        return getHasRole();
    }

    @XmlElement
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String accessToken() {
        return getAccessToken();
    }

    @XmlElement
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

    @XmlAttribute
    public void setMessage(String message) {
        this.message = message;
    }

    @XmlAttribute
    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Long getSessionTimeout() {
        return this.sessionTimeout;
    }

    @XmlAttribute
    public void setEnableTouch(boolean enableTouch) {
        this.enableTouch = enableTouch;
    }

    public boolean getEnableTouch() {
        return this.enableTouch;
    }

    @Override
    public String toString() {
        return String.format("Account: %s Role: %s hasRole: %s Permission: %s isPermitted: %s -- AccessToken=%s", this.account, this.role,
                this.hasRole, this.permission, this.isPermittet, this.accessToken);
    }

    public String getCallerIpAddress() {
        return callerIpAddress;
    }

    @XmlElement
    public void setCallerIpAddress(String callerIpAddress) {
        this.callerIpAddress = callerIpAddress;
    }

    public String getCallerHostName() {
        return callerHostName;
    }

    @XmlElement
    public void setCallerHostName(String callerHostName) {
        this.callerHostName = callerHostName;
    }

    @XmlAttribute
    public void setIdentityService(String identityService) {
        this.identityService = identityService;
    }

    public String getIdentityService() {
        return identityService;
    }

    @XmlElement
    public boolean isForcePasswordChange() {
        return isForcePasswordChange;
    }

    public void setIsForcePasswordChange(boolean isForcePasswordChange) {
        this.isForcePasswordChange = isForcePasswordChange;
    }

}