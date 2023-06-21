
package com.sos.joc.model.security.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OIDC Token Answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "error",
    "errorDescription",
    "accessToken",
    "expiresIn",
    "scope",
    "tokenType",
    "idToken"
})
public class OidcTokenAnswer {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private String error;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("errorDescription")
    private String errorDescription;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accessToken")
    private String accessToken;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expiresIn")
    private String expiresIn;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("scope")
    private String scope;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tokenType")
    private String tokenType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("idToken")
    private String idToken;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcTokenAnswer() {
    }

    /**
     * 
     * @param expiresIn
     * @param errorDescription
     * @param scope
     * @param idToken
     * @param error
     * @param accessToken
     * @param tokenType
     */
    public OidcTokenAnswer(String error, String errorDescription, String accessToken, String expiresIn, String scope, String tokenType, String idToken) {
        super();
        this.error = error;
        this.errorDescription = errorDescription;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.tokenType = tokenType;
        this.idToken = idToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public String getError() {
        return error;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("errorDescription")
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("errorDescription")
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expiresIn")
    public String getExpiresIn() {
        return expiresIn;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expiresIn")
    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tokenType")
    public String getTokenType() {
        return tokenType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("tokenType")
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("idToken")
    public String getIdToken() {
        return idToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("idToken")
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("error", error).append("errorDescription", errorDescription).append("accessToken", accessToken).append("expiresIn", expiresIn).append("scope", scope).append("tokenType", tokenType).append("idToken", idToken).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(expiresIn).append(errorDescription).append(scope).append(idToken).append(error).append(accessToken).append(tokenType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcTokenAnswer) == false) {
            return false;
        }
        OidcTokenAnswer rhs = ((OidcTokenAnswer) other);
        return new EqualsBuilder().append(expiresIn, rhs.expiresIn).append(errorDescription, rhs.errorDescription).append(scope, rhs.scope).append(idToken, rhs.idToken).append(error, rhs.error).append(accessToken, rhs.accessToken).append(tokenType, rhs.tokenType).isEquals();
    }

}
