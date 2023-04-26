
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO2 Identity Provider
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "iamFido2RPName",
    "iamIconUrl"
})
public class Fido2IdentityProvider {

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
    @JsonProperty("iamFido2RPName")
    private String iamFido2RPName;
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
    public Fido2IdentityProvider() {
    }

    /**
     * 
     * @param identityServiceName
     * @param iamIconUrl
     * @param iamFido2RPName
     */
    public Fido2IdentityProvider(String identityServiceName, String iamFido2RPName, String iamIconUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamFido2RPName = iamFido2RPName;
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
    @JsonProperty("iamFido2RPName")
    public String getIamFido2RPName() {
        return iamFido2RPName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2RPName")
    public void setIamFido2RPName(String iamFido2RPName) {
        this.iamFido2RPName = iamFido2RPName;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamFido2RPName", iamFido2RPName).append("iamIconUrl", iamIconUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(iamIconUrl).append(iamFido2RPName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2IdentityProvider) == false) {
            return false;
        }
        Fido2IdentityProvider rhs = ((Fido2IdentityProvider) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(iamIconUrl, rhs.iamIconUrl).append(iamFido2RPName, rhs.iamFido2RPName).isEquals();
    }

}
