
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Identity Provider
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "iamOidcClientId",
    "iamOidcClientSecret",
    "iamOidcName",
    "iamOidcAuthenticationUrl",
    "iamOidcLogoutUrl"
})
public class IdentityProvider {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    private String iamOidcClientId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    private String iamOidcClientSecret;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    private String iamOidcName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    private String iamOidcAuthenticationUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcLogoutUrl")
    private String iamOidcLogoutUrl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityProvider() {
    }

    /**
     * 
     * @param iamOidcClientSecret
     * @param identityServiceName
     * @param iamOidcClientId
     * @param iamOidcLogoutUrl
     * @param iamOidcAuthenticationUrl
     * @param iamOidcName
     */
    public IdentityProvider(String identityServiceName, String iamOidcClientId, String iamOidcClientSecret, String iamOidcName, String iamOidcAuthenticationUrl, String iamOidcLogoutUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcClientSecret = iamOidcClientSecret;
        this.iamOidcName = iamOidcName;
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
        this.iamOidcLogoutUrl = iamOidcLogoutUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    public String getIamOidcClientId() {
        return iamOidcClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    public void setIamOidcClientId(String iamOidcClientId) {
        this.iamOidcClientId = iamOidcClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    public String getIamOidcClientSecret() {
        return iamOidcClientSecret;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    public void setIamOidcClientSecret(String iamOidcClientSecret) {
        this.iamOidcClientSecret = iamOidcClientSecret;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    public String getIamOidcName() {
        return iamOidcName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    public void setIamOidcName(String iamOidcName) {
        this.iamOidcName = iamOidcName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    public String getIamOidcAuthenticationUrl() {
        return iamOidcAuthenticationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    public void setIamOidcAuthenticationUrl(String iamOidcAuthenticationUrl) {
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcLogoutUrl")
    public String getIamOidcLogoutUrl() {
        return iamOidcLogoutUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcLogoutUrl")
    public void setIamOidcLogoutUrl(String iamOidcLogoutUrl) {
        this.iamOidcLogoutUrl = iamOidcLogoutUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamOidcClientId", iamOidcClientId).append("iamOidcClientSecret", iamOidcClientSecret).append("iamOidcName", iamOidcName).append("iamOidcAuthenticationUrl", iamOidcAuthenticationUrl).append("iamOidcLogoutUrl", iamOidcLogoutUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamOidcClientSecret).append(identityServiceName).append(iamOidcClientId).append(iamOidcLogoutUrl).append(iamOidcAuthenticationUrl).append(iamOidcName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityProvider) == false) {
            return false;
        }
        IdentityProvider rhs = ((IdentityProvider) other);
        return new EqualsBuilder().append(iamOidcClientSecret, rhs.iamOidcClientSecret).append(identityServiceName, rhs.identityServiceName).append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcLogoutUrl, rhs.iamOidcLogoutUrl).append(iamOidcAuthenticationUrl, rhs.iamOidcAuthenticationUrl).append(iamOidcName, rhs.iamOidcName).isEquals();
    }

}
