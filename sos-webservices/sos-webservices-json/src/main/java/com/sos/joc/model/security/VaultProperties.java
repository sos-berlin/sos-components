
package com.sos.joc.model.security;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Vault Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamVaultUrl",
    "iamVaultAuthenticationMethodPath",
    "iamVaultTruststorePath",
    "iamVaultTruststorePassword",
    "iamVaultTruststoreType",
    "iamVaultApplicationToken"
})
public class VaultProperties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultUrl")
    private String iamVaultUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultAuthenticationMethodPath")
    private String iamVaultAuthenticationMethodPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePath")
    private String iamVaultTruststorePath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePassword")
    private String iamVaultTruststorePassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststoreType")
    private String iamVaultTruststoreType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultApplicationToken")
    private String iamVaultApplicationToken;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public VaultProperties() {
    }

    /**
     * 
     * @param iamVaultAuthenticationMethodPath
     * @param iamVaultUrl
     * @param iamVaultTruststorePath
     * @param iamVaultTruststorePassword
     * @param iamVaultTruststoreType
     * @param iamVaultApplicationToken
     */
    public VaultProperties(String iamVaultUrl, String iamVaultAuthenticationMethodPath, String iamVaultTruststorePath, String iamVaultTruststorePassword, String iamVaultTruststoreType, String iamVaultApplicationToken) {
        super();
        this.iamVaultUrl = iamVaultUrl;
        this.iamVaultAuthenticationMethodPath = iamVaultAuthenticationMethodPath;
        this.iamVaultTruststorePath = iamVaultTruststorePath;
        this.iamVaultTruststorePassword = iamVaultTruststorePassword;
        this.iamVaultTruststoreType = iamVaultTruststoreType;
        this.iamVaultApplicationToken = iamVaultApplicationToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultUrl")
    public String getIamVaultUrl() {
        return iamVaultUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultUrl")
    public void setIamVaultUrl(String iamVaultUrl) {
        this.iamVaultUrl = iamVaultUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultAuthenticationMethodPath")
    public String getIamVaultAuthenticationMethodPath() {
        return iamVaultAuthenticationMethodPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultAuthenticationMethodPath")
    public void setIamVaultAuthenticationMethodPath(String iamVaultAuthenticationMethodPath) {
        this.iamVaultAuthenticationMethodPath = iamVaultAuthenticationMethodPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePath")
    public String getIamVaultTruststorePath() {
        return iamVaultTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePath")
    public void setIamVaultTruststorePath(String iamVaultTruststorePath) {
        this.iamVaultTruststorePath = iamVaultTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePassword")
    public String getIamVaultTruststorePassword() {
        return iamVaultTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststorePassword")
    public void setIamVaultTruststorePassword(String iamVaultTruststorePassword) {
        this.iamVaultTruststorePassword = iamVaultTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststoreType")
    public String getIamVaultTruststoreType() {
        return iamVaultTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultTruststoreType")
    public void setIamVaultTruststoreType(String iamVaultTruststoreType) {
        this.iamVaultTruststoreType = iamVaultTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultApplicationToken")
    public String getIamVaultApplicationToken() {
        return iamVaultApplicationToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamVaultApplicationToken")
    public void setIamVaultApplicationToken(String iamVaultApplicationToken) {
        this.iamVaultApplicationToken = iamVaultApplicationToken;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamVaultUrl", iamVaultUrl).append("iamVaultAuthenticationMethodPath", iamVaultAuthenticationMethodPath).append("iamVaultTruststorePath", iamVaultTruststorePath).append("iamVaultTruststorePassword", iamVaultTruststorePassword).append("iamVaultTruststoreType", iamVaultTruststoreType).append("iamVaultApplicationToken", iamVaultApplicationToken).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamVaultAuthenticationMethodPath).append(iamVaultUrl).append(iamVaultTruststorePath).append(additionalProperties).append(iamVaultTruststorePassword).append(iamVaultTruststoreType).append(iamVaultApplicationToken).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VaultProperties) == false) {
            return false;
        }
        VaultProperties rhs = ((VaultProperties) other);
        return new EqualsBuilder().append(iamVaultAuthenticationMethodPath, rhs.iamVaultAuthenticationMethodPath).append(iamVaultUrl, rhs.iamVaultUrl).append(iamVaultTruststorePath, rhs.iamVaultTruststorePath).append(additionalProperties, rhs.additionalProperties).append(iamVaultTruststorePassword, rhs.iamVaultTruststorePassword).append(iamVaultTruststoreType, rhs.iamVaultTruststoreType).append(iamVaultApplicationToken, rhs.iamVaultApplicationToken).isEquals();
    }

}
