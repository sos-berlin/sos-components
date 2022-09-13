
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
    "iamOidcName",
    "iamOidcdAuthenticationUrl"
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
    @JsonProperty("iamOidcName")
    private String iamOidcName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcdAuthenticationUrl")
    private String iamOidcdAuthenticationUrl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityProvider() {
    }

    /**
     * 
     * @param identityServiceName
     * @param iamOidcClientId
     * @param iamOidcName
     * @param iamOidcdAuthenticationUrl
     */
    public IdentityProvider(String identityServiceName, String iamOidcClientId, String iamOidcName, String iamOidcdAuthenticationUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcName = iamOidcName;
        this.iamOidcdAuthenticationUrl = iamOidcdAuthenticationUrl;
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
    @JsonProperty("iamOidcdAuthenticationUrl")
    public String getIamOidcdAuthenticationUrl() {
        return iamOidcdAuthenticationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcdAuthenticationUrl")
    public void setIamOidcdAuthenticationUrl(String iamOidcdAuthenticationUrl) {
        this.iamOidcdAuthenticationUrl = iamOidcdAuthenticationUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamOidcClientId", iamOidcClientId).append("iamOidcName", iamOidcName).append("iamOidcdAuthenticationUrl", iamOidcdAuthenticationUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(iamOidcClientId).append(iamOidcName).append(iamOidcdAuthenticationUrl).toHashCode();
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
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcName, rhs.iamOidcName).append(iamOidcdAuthenticationUrl, rhs.iamOidcdAuthenticationUrl).isEquals();
    }

}
