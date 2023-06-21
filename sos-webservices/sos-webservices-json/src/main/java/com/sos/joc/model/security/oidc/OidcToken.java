
package com.sos.joc.model.security.oidc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OIDC Token
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestUrl",
    "grantType",
    "code",
    "scope",
    "tokenEndpointMethods",
    "refreshToken",
    "redirectUri",
    "codeVerifier",
    "clientId",
    "identityServiceName",
    "auditLog"
})
public class OidcToken {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    private String requestUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("grantType")
    private String grantType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("code")
    private String code;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("tokenEndpointMethods")
    private List<TokenEndpointMethods> tokenEndpointMethods = new ArrayList<TokenEndpointMethods>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("refreshToken")
    private String refreshToken;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("redirectUri")
    private String redirectUri;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("codeVerifier")
    private String codeVerifier;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("clientId")
    private String clientId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcToken() {
    }

    /**
     * 
     * @param redirectUri
     * @param identityServiceName
     * @param code
     * @param clientId
     * @param auditLog
     * @param codeVerifier
     * @param requestUrl
     * @param scope
     * @param tokenEndpointMethods
     * @param grantType
     * @param refreshToken
     */
    public OidcToken(String requestUrl, String grantType, String code, String scope, List<TokenEndpointMethods> tokenEndpointMethods, String refreshToken, String redirectUri, String codeVerifier, String clientId, String identityServiceName, AuditParams auditLog) {
        super();
        this.requestUrl = requestUrl;
        this.grantType = grantType;
        this.code = code;
        this.scope = scope;
        this.tokenEndpointMethods = tokenEndpointMethods;
        this.refreshToken = refreshToken;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
        this.clientId = clientId;
        this.identityServiceName = identityServiceName;
        this.auditLog = auditLog;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("grantType")
    public String getGrantType() {
        return grantType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("grantType")
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scope")
    public String getScope() {
        return scope;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scope")
    public void setScope(String scope) {
        this.scope = scope;
    }

    @JsonProperty("tokenEndpointMethods")
    public List<TokenEndpointMethods> getTokenEndpointMethods() {
        return tokenEndpointMethods;
    }

    @JsonProperty("tokenEndpointMethods")
    public void setTokenEndpointMethods(List<TokenEndpointMethods> tokenEndpointMethods) {
        this.tokenEndpointMethods = tokenEndpointMethods;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("refreshToken")
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("refreshToken")
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("redirectUri")
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("redirectUri")
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("codeVerifier")
    public String getCodeVerifier() {
        return codeVerifier;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("codeVerifier")
    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestUrl", requestUrl).append("grantType", grantType).append("code", code).append("scope", scope).append("tokenEndpointMethods", tokenEndpointMethods).append("refreshToken", refreshToken).append("redirectUri", redirectUri).append("codeVerifier", codeVerifier).append("clientId", clientId).append("identityServiceName", identityServiceName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(redirectUri).append(identityServiceName).append(code).append(clientId).append(auditLog).append(codeVerifier).append(requestUrl).append(scope).append(tokenEndpointMethods).append(grantType).append(refreshToken).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcToken) == false) {
            return false;
        }
        OidcToken rhs = ((OidcToken) other);
        return new EqualsBuilder().append(redirectUri, rhs.redirectUri).append(identityServiceName, rhs.identityServiceName).append(code, rhs.code).append(clientId, rhs.clientId).append(auditLog, rhs.auditLog).append(codeVerifier, rhs.codeVerifier).append(requestUrl, rhs.requestUrl).append(scope, rhs.scope).append(tokenEndpointMethods, rhs.tokenEndpointMethods).append(grantType, rhs.grantType).append(refreshToken, rhs.refreshToken).isEquals();
    }

}
