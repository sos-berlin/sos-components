
package com.sos.joc.model.security.properties.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Keycloak Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamKeycloakUrl",
    "iamKeycloakAuthenticationMethodPath",
    "iamKeycloakTruststorePath",
    "iamKeycloakTruststorePassword",
    "iamKeycloakTruststoreType",
    "iamKeycloakClientSecret",
    "iamKeycloakClientId",
    "iamKeycloakAdminAccount",
    "iamKeycloakAdminPassword",
    "iamKeycloakRealm"
})
public class KeycloakProperties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakUrl")
    private String iamKeycloakUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAuthenticationMethodPath")
    private String iamKeycloakAuthenticationMethodPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePath")
    private String iamKeycloakTruststorePath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePassword")
    private String iamKeycloakTruststorePassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststoreType")
    private String iamKeycloakTruststoreType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientSecret")
    private String iamKeycloakClientSecret;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientId")
    private String iamKeycloakClientId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminAccount")
    private String iamKeycloakAdminAccount;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminPassword")
    private String iamKeycloakAdminPassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakRealm")
    private String iamKeycloakRealm;

    /**
     * No args constructor for use in serialization
     * 
     */
    public KeycloakProperties() {
    }

    /**
     * 
     * @param iamKeycloakAuthenticationMethodPath
     * @param iamKeycloakTruststorePath
     * @param iamKeycloakUrl
     * @param iamKeycloakClientSecret
     * @param iamKeycloakTruststorePassword
     * @param iamKeycloakAdminPassword
     * @param iamKeycloakRealm
     * @param iamKeycloakAdminAccount
     * @param iamKeycloakTruststoreType
     * @param iamKeycloakClientId
     */
    public KeycloakProperties(String iamKeycloakUrl, String iamKeycloakAuthenticationMethodPath, String iamKeycloakTruststorePath, String iamKeycloakTruststorePassword, String iamKeycloakTruststoreType, String iamKeycloakClientSecret, String iamKeycloakClientId, String iamKeycloakAdminAccount, String iamKeycloakAdminPassword, String iamKeycloakRealm) {
        super();
        this.iamKeycloakUrl = iamKeycloakUrl;
        this.iamKeycloakAuthenticationMethodPath = iamKeycloakAuthenticationMethodPath;
        this.iamKeycloakTruststorePath = iamKeycloakTruststorePath;
        this.iamKeycloakTruststorePassword = iamKeycloakTruststorePassword;
        this.iamKeycloakTruststoreType = iamKeycloakTruststoreType;
        this.iamKeycloakClientSecret = iamKeycloakClientSecret;
        this.iamKeycloakClientId = iamKeycloakClientId;
        this.iamKeycloakAdminAccount = iamKeycloakAdminAccount;
        this.iamKeycloakAdminPassword = iamKeycloakAdminPassword;
        this.iamKeycloakRealm = iamKeycloakRealm;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakUrl")
    public String getIamKeycloakUrl() {
        return iamKeycloakUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakUrl")
    public void setIamKeycloakUrl(String iamKeycloakUrl) {
        this.iamKeycloakUrl = iamKeycloakUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAuthenticationMethodPath")
    public String getIamKeycloakAuthenticationMethodPath() {
        return iamKeycloakAuthenticationMethodPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAuthenticationMethodPath")
    public void setIamKeycloakAuthenticationMethodPath(String iamKeycloakAuthenticationMethodPath) {
        this.iamKeycloakAuthenticationMethodPath = iamKeycloakAuthenticationMethodPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePath")
    public String getIamKeycloakTruststorePath() {
        return iamKeycloakTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePath")
    public void setIamKeycloakTruststorePath(String iamKeycloakTruststorePath) {
        this.iamKeycloakTruststorePath = iamKeycloakTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePassword")
    public String getIamKeycloakTruststorePassword() {
        return iamKeycloakTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststorePassword")
    public void setIamKeycloakTruststorePassword(String iamKeycloakTruststorePassword) {
        this.iamKeycloakTruststorePassword = iamKeycloakTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststoreType")
    public String getIamKeycloakTruststoreType() {
        return iamKeycloakTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakTruststoreType")
    public void setIamKeycloakTruststoreType(String iamKeycloakTruststoreType) {
        this.iamKeycloakTruststoreType = iamKeycloakTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientSecret")
    public String getIamKeycloakClientSecret() {
        return iamKeycloakClientSecret;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientSecret")
    public void setIamKeycloakClientSecret(String iamKeycloakClientSecret) {
        this.iamKeycloakClientSecret = iamKeycloakClientSecret;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientId")
    public String getIamKeycloakClientId() {
        return iamKeycloakClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakClientId")
    public void setIamKeycloakClientId(String iamKeycloakClientId) {
        this.iamKeycloakClientId = iamKeycloakClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminAccount")
    public String getIamKeycloakAdminAccount() {
        return iamKeycloakAdminAccount;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminAccount")
    public void setIamKeycloakAdminAccount(String iamKeycloakAdminAccount) {
        this.iamKeycloakAdminAccount = iamKeycloakAdminAccount;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminPassword")
    public String getIamKeycloakAdminPassword() {
        return iamKeycloakAdminPassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakAdminPassword")
    public void setIamKeycloakAdminPassword(String iamKeycloakAdminPassword) {
        this.iamKeycloakAdminPassword = iamKeycloakAdminPassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakRealm")
    public String getIamKeycloakRealm() {
        return iamKeycloakRealm;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamKeycloakRealm")
    public void setIamKeycloakRealm(String iamKeycloakRealm) {
        this.iamKeycloakRealm = iamKeycloakRealm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamKeycloakUrl", iamKeycloakUrl).append("iamKeycloakAuthenticationMethodPath", iamKeycloakAuthenticationMethodPath).append("iamKeycloakTruststorePath", iamKeycloakTruststorePath).append("iamKeycloakTruststorePassword", iamKeycloakTruststorePassword).append("iamKeycloakTruststoreType", iamKeycloakTruststoreType).append("iamKeycloakClientSecret", iamKeycloakClientSecret).append("iamKeycloakClientId", iamKeycloakClientId).append("iamKeycloakAdminAccount", iamKeycloakAdminAccount).append("iamKeycloakAdminPassword", iamKeycloakAdminPassword).append("iamKeycloakRealm", iamKeycloakRealm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamKeycloakAuthenticationMethodPath).append(iamKeycloakTruststorePath).append(iamKeycloakUrl).append(iamKeycloakClientSecret).append(iamKeycloakTruststorePassword).append(iamKeycloakAdminPassword).append(iamKeycloakRealm).append(iamKeycloakAdminAccount).append(iamKeycloakTruststoreType).append(iamKeycloakClientId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof KeycloakProperties) == false) {
            return false;
        }
        KeycloakProperties rhs = ((KeycloakProperties) other);
        return new EqualsBuilder().append(iamKeycloakAuthenticationMethodPath, rhs.iamKeycloakAuthenticationMethodPath).append(iamKeycloakTruststorePath, rhs.iamKeycloakTruststorePath).append(iamKeycloakUrl, rhs.iamKeycloakUrl).append(iamKeycloakClientSecret, rhs.iamKeycloakClientSecret).append(iamKeycloakTruststorePassword, rhs.iamKeycloakTruststorePassword).append(iamKeycloakAdminPassword, rhs.iamKeycloakAdminPassword).append(iamKeycloakRealm, rhs.iamKeycloakRealm).append(iamKeycloakAdminAccount, rhs.iamKeycloakAdminAccount).append(iamKeycloakTruststoreType, rhs.iamKeycloakTruststoreType).append(iamKeycloakClientId, rhs.iamKeycloakClientId).isEquals();
    }

}
