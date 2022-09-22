
package com.sos.joc.model.security.properties.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Openid Connect Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamOidcAuthenticationUrl",
    "iamOidcClientId",
    "iamOidcName",
    "iamOidcClientSecret",
    "iamOidcSessionRenewalUrl",
    "iamOidcTokenVerificationUrl",
    "iamOidcCertificateUrl",
    "iamOidcCertificateIssuer",
    "iamOidcCertificateExpirationDate",
    "iamOidcTruststorePath",
    "iamOidcTruststorePassword",
    "iamOidcTruststoreType"
})
public class OidcProperties {

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
    @JsonProperty("iamOidcClientSecret")
    private String iamOidcClientSecret;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcSessionRenewalUrl")
    private String iamOidcSessionRenewalUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTokenVerificationUrl")
    private String iamOidcTokenVerificationUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateUrl")
    private String iamOidcCertificateUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateIssuer")
    private String iamOidcCertificateIssuer;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateExpirationDate")
    private String iamOidcCertificateExpirationDate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    private String iamOidcTruststorePath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    private String iamOidcTruststorePassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    private String iamOidcTruststoreType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcProperties() {
    }

    /**
     * 
     * @param iamOidcClientSecret
     * @param iamOidcSessionRenewalUrl
     * @param iamOidcTokenVerificationUrl
     * @param iamOidcClientId
     * @param iamOidcAuthenticationUrl
     * @param iamOidcName
     * @param iamOidcCertificateIssuer
     * @param iamOidcTruststorePath
     * @param iamOidcCertificateExpirationDate
     * @param iamOidcTruststorePassword
     * @param iamOidcTruststoreType
     * @param iamOidcCertificateUrl
     */
    public OidcProperties(String iamOidcAuthenticationUrl, String iamOidcClientId, String iamOidcName, String iamOidcClientSecret, String iamOidcSessionRenewalUrl, String iamOidcTokenVerificationUrl, String iamOidcCertificateUrl, String iamOidcCertificateIssuer, String iamOidcCertificateExpirationDate, String iamOidcTruststorePath, String iamOidcTruststorePassword, String iamOidcTruststoreType) {
        super();
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcName = iamOidcName;
        this.iamOidcClientSecret = iamOidcClientSecret;
        this.iamOidcSessionRenewalUrl = iamOidcSessionRenewalUrl;
        this.iamOidcTokenVerificationUrl = iamOidcTokenVerificationUrl;
        this.iamOidcCertificateUrl = iamOidcCertificateUrl;
        this.iamOidcCertificateIssuer = iamOidcCertificateIssuer;
        this.iamOidcCertificateExpirationDate = iamOidcCertificateExpirationDate;
        this.iamOidcTruststorePath = iamOidcTruststorePath;
        this.iamOidcTruststorePassword = iamOidcTruststorePassword;
        this.iamOidcTruststoreType = iamOidcTruststoreType;
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
    @JsonProperty("iamOidcSessionRenewalUrl")
    public String getIamOidcSessionRenewalUrl() {
        return iamOidcSessionRenewalUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcSessionRenewalUrl")
    public void setIamOidcSessionRenewalUrl(String iamOidcSessionRenewalUrl) {
        this.iamOidcSessionRenewalUrl = iamOidcSessionRenewalUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTokenVerificationUrl")
    public String getIamOidcTokenVerificationUrl() {
        return iamOidcTokenVerificationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTokenVerificationUrl")
    public void setIamOidcTokenVerificationUrl(String iamOidcTokenVerificationUrl) {
        this.iamOidcTokenVerificationUrl = iamOidcTokenVerificationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateUrl")
    public String getIamOidcCertificateUrl() {
        return iamOidcCertificateUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateUrl")
    public void setIamOidcCertificateUrl(String iamOidcCertificateUrl) {
        this.iamOidcCertificateUrl = iamOidcCertificateUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateIssuer")
    public String getIamOidcCertificateIssuer() {
        return iamOidcCertificateIssuer;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateIssuer")
    public void setIamOidcCertificateIssuer(String iamOidcCertificateIssuer) {
        this.iamOidcCertificateIssuer = iamOidcCertificateIssuer;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateExpirationDate")
    public String getIamOidcCertificateExpirationDate() {
        return iamOidcCertificateExpirationDate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcCertificateExpirationDate")
    public void setIamOidcCertificateExpirationDate(String iamOidcCertificateExpirationDate) {
        this.iamOidcCertificateExpirationDate = iamOidcCertificateExpirationDate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    public String getIamOidcTruststorePath() {
        return iamOidcTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    public void setIamOidcTruststorePath(String iamOidcTruststorePath) {
        this.iamOidcTruststorePath = iamOidcTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    public String getIamOidcTruststorePassword() {
        return iamOidcTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    public void setIamOidcTruststorePassword(String iamOidcTruststorePassword) {
        this.iamOidcTruststorePassword = iamOidcTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    public String getIamOidcTruststoreType() {
        return iamOidcTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    public void setIamOidcTruststoreType(String iamOidcTruststoreType) {
        this.iamOidcTruststoreType = iamOidcTruststoreType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamOidcAuthenticationUrl", iamOidcAuthenticationUrl).append("iamOidcClientId", iamOidcClientId).append("iamOidcName", iamOidcName).append("iamOidcClientSecret", iamOidcClientSecret).append("iamOidcSessionRenewalUrl", iamOidcSessionRenewalUrl).append("iamOidcTokenVerificationUrl", iamOidcTokenVerificationUrl).append("iamOidcCertificateUrl", iamOidcCertificateUrl).append("iamOidcCertificateIssuer", iamOidcCertificateIssuer).append("iamOidcCertificateExpirationDate", iamOidcCertificateExpirationDate).append("iamOidcTruststorePath", iamOidcTruststorePath).append("iamOidcTruststorePassword", iamOidcTruststorePassword).append("iamOidcTruststoreType", iamOidcTruststoreType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamOidcSessionRenewalUrl).append(iamOidcTokenVerificationUrl).append(iamOidcClientId).append(iamOidcCertificateIssuer).append(iamOidcCertificateExpirationDate).append(iamOidcTruststorePassword).append(iamOidcClientSecret).append(iamOidcAuthenticationUrl).append(iamOidcName).append(iamOidcTruststorePath).append(iamOidcTruststoreType).append(iamOidcCertificateUrl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcProperties) == false) {
            return false;
        }
        OidcProperties rhs = ((OidcProperties) other);
        return new EqualsBuilder().append(iamOidcSessionRenewalUrl, rhs.iamOidcSessionRenewalUrl).append(iamOidcTokenVerificationUrl, rhs.iamOidcTokenVerificationUrl).append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcCertificateIssuer, rhs.iamOidcCertificateIssuer).append(iamOidcCertificateExpirationDate, rhs.iamOidcCertificateExpirationDate).append(iamOidcTruststorePassword, rhs.iamOidcTruststorePassword).append(iamOidcClientSecret, rhs.iamOidcClientSecret).append(iamOidcAuthenticationUrl, rhs.iamOidcAuthenticationUrl).append(iamOidcName, rhs.iamOidcName).append(iamOidcTruststorePath, rhs.iamOidcTruststorePath).append(iamOidcTruststoreType, rhs.iamOidcTruststoreType).append(iamOidcCertificateUrl, rhs.iamOidcCertificateUrl).isEquals();
    }

}
