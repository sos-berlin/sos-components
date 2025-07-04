
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.oidc.OidcFlowTypes;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OIDC Identity Provider
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
    "iamOidcFlowType",
    "iamIconUrl"
})
public class OidcIdentityProvider {

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
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    private OidcFlowTypes iamOidcFlowType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    private String iamIconUrl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcIdentityProvider() {
    }

    /**
     * 
     * @param iamOidcClientSecret
     * @param identityServiceName
     * @param iamIconUrl
     * @param iamOidcClientId
     * @param iamOidcAuthenticationUrl
     * @param iamOidcName
     * @param iamOidcFlowType
     */
    public OidcIdentityProvider(String identityServiceName, String iamOidcClientId, String iamOidcClientSecret, String iamOidcName, String iamOidcAuthenticationUrl, OidcFlowTypes iamOidcFlowType, String iamIconUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcClientSecret = iamOidcClientSecret;
        this.iamOidcName = iamOidcName;
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
        this.iamOidcFlowType = iamOidcFlowType;
        this.iamIconUrl = iamIconUrl;
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
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    public OidcFlowTypes getIamOidcFlowType() {
        return iamOidcFlowType;
    }

    /**
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    public void setIamOidcFlowType(OidcFlowTypes iamOidcFlowType) {
        this.iamOidcFlowType = iamOidcFlowType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public String getIamIconUrl() {
        return iamIconUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public void setIamIconUrl(String iamIconUrl) {
        this.iamIconUrl = iamIconUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamOidcClientId", iamOidcClientId).append("iamOidcClientSecret", iamOidcClientSecret).append("iamOidcName", iamOidcName).append("iamOidcAuthenticationUrl", iamOidcAuthenticationUrl).append("iamOidcFlowType", iamOidcFlowType).append("iamIconUrl", iamIconUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamOidcClientSecret).append(identityServiceName).append(iamIconUrl).append(iamOidcClientId).append(iamOidcAuthenticationUrl).append(iamOidcName).append(iamOidcFlowType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcIdentityProvider) == false) {
            return false;
        }
        OidcIdentityProvider rhs = ((OidcIdentityProvider) other);
        return new EqualsBuilder().append(iamOidcClientSecret, rhs.iamOidcClientSecret).append(identityServiceName, rhs.identityServiceName).append(iamIconUrl, rhs.iamIconUrl).append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcAuthenticationUrl, rhs.iamOidcAuthenticationUrl).append(iamOidcName, rhs.iamOidcName).append(iamOidcFlowType, rhs.iamOidcFlowType).isEquals();
    }

}
