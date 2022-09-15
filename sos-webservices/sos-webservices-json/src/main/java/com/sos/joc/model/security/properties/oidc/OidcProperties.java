
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
    "iamOidcTokenVerificationUrl",
    "iamOidcLogoutUrl",
    "iamOidcProfileInformationUrl",
    "iamOidcSessionRenewalUrl",
    "iamOidcCertificateUrl",
    "iamOidcPublicKeyField",
    "iamOidcCertificateIssuer",
    "iamOidcCertificateExpirationDate",
    "iamOidcIsJwtToken",
    "iamOidcJwtEmailField",
    "iamOidcJwtClientIdField",
    "iamOidcJwtUrlField",
    "iamOidcJwtAlgorithmField",
    "iamOidcJwtPublicKeyField",
    "iamOidcJwtExpiredField"
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
    @JsonProperty("iamOidcTokenVerificationUrl")
    private String iamOidcTokenVerificationUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcLogoutUrl")
    private String iamOidcLogoutUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcProfileInformationUrl")
    private String iamOidcProfileInformationUrl;
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
    @JsonProperty("iamOidcCertificateUrl")
    private String iamOidcCertificateUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcPublicKeyField")
    private String iamOidcPublicKeyField;
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
    @JsonProperty("iamOidcIsJwtToken")
    private String iamOidcIsJwtToken;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtEmailField")
    private String iamOidcJwtEmailField;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtClientIdField")
    private String iamOidcJwtClientIdField;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtUrlField")
    private String iamOidcJwtUrlField;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtAlgorithmField")
    private String iamOidcJwtAlgorithmField;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtPublicKeyField")
    private String iamOidcJwtPublicKeyField;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtExpiredField")
    private String iamOidcJwtExpiredField;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcProperties() {
    }

    /**
     * 
     * @param iamOidcTokenVerificationUrl
     * @param iamOidcSessionRenewalUrl
     * @param iamOidcClientId
     * @param iamOidcLogoutUrl
     * @param iamOidcJwtUrlField
     * @param iamOidcPublicKeyField
     * @param iamOidcJwtPublicKeyField
     * @param iamOidcProfileInformationUrl
     * @param iamOidcCertificateIssuer
     * @param iamOidcCertificateExpirationDate
     * @param iamOidcJwtClientIdField
     * @param iamOidcClientSecret
     * @param iamOidcJwtExpiredField
     * @param iamOidcAuthenticationUrl
     * @param iamOidcName
     * @param iamOidcJwtEmailField
     * @param iamOidcIsJwtToken
     * @param iamOidcJwtAlgorithmField
     * @param iamOidcCertificateUrl
     */
    public OidcProperties(String iamOidcAuthenticationUrl, String iamOidcClientId, String iamOidcName, String iamOidcClientSecret, String iamOidcTokenVerificationUrl, String iamOidcLogoutUrl, String iamOidcProfileInformationUrl, String iamOidcSessionRenewalUrl, String iamOidcCertificateUrl, String iamOidcPublicKeyField, String iamOidcCertificateIssuer, String iamOidcCertificateExpirationDate, String iamOidcIsJwtToken, String iamOidcJwtEmailField, String iamOidcJwtClientIdField, String iamOidcJwtUrlField, String iamOidcJwtAlgorithmField, String iamOidcJwtPublicKeyField, String iamOidcJwtExpiredField) {
        super();
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcName = iamOidcName;
        this.iamOidcClientSecret = iamOidcClientSecret;
        this.iamOidcTokenVerificationUrl = iamOidcTokenVerificationUrl;
        this.iamOidcLogoutUrl = iamOidcLogoutUrl;
        this.iamOidcProfileInformationUrl = iamOidcProfileInformationUrl;
        this.iamOidcSessionRenewalUrl = iamOidcSessionRenewalUrl;
        this.iamOidcCertificateUrl = iamOidcCertificateUrl;
        this.iamOidcPublicKeyField = iamOidcPublicKeyField;
        this.iamOidcCertificateIssuer = iamOidcCertificateIssuer;
        this.iamOidcCertificateExpirationDate = iamOidcCertificateExpirationDate;
        this.iamOidcIsJwtToken = iamOidcIsJwtToken;
        this.iamOidcJwtEmailField = iamOidcJwtEmailField;
        this.iamOidcJwtClientIdField = iamOidcJwtClientIdField;
        this.iamOidcJwtUrlField = iamOidcJwtUrlField;
        this.iamOidcJwtAlgorithmField = iamOidcJwtAlgorithmField;
        this.iamOidcJwtPublicKeyField = iamOidcJwtPublicKeyField;
        this.iamOidcJwtExpiredField = iamOidcJwtExpiredField;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcProfileInformationUrl")
    public String getIamOidcProfileInformationUrl() {
        return iamOidcProfileInformationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcProfileInformationUrl")
    public void setIamOidcProfileInformationUrl(String iamOidcProfileInformationUrl) {
        this.iamOidcProfileInformationUrl = iamOidcProfileInformationUrl;
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
    @JsonProperty("iamOidcPublicKeyField")
    public String getIamOidcPublicKeyField() {
        return iamOidcPublicKeyField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcPublicKeyField")
    public void setIamOidcPublicKeyField(String iamOidcPublicKeyField) {
        this.iamOidcPublicKeyField = iamOidcPublicKeyField;
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
    @JsonProperty("iamOidcIsJwtToken")
    public String getIamOidcIsJwtToken() {
        return iamOidcIsJwtToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcIsJwtToken")
    public void setIamOidcIsJwtToken(String iamOidcIsJwtToken) {
        this.iamOidcIsJwtToken = iamOidcIsJwtToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtEmailField")
    public String getIamOidcJwtEmailField() {
        return iamOidcJwtEmailField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtEmailField")
    public void setIamOidcJwtEmailField(String iamOidcJwtEmailField) {
        this.iamOidcJwtEmailField = iamOidcJwtEmailField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtClientIdField")
    public String getIamOidcJwtClientIdField() {
        return iamOidcJwtClientIdField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtClientIdField")
    public void setIamOidcJwtClientIdField(String iamOidcJwtClientIdField) {
        this.iamOidcJwtClientIdField = iamOidcJwtClientIdField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtUrlField")
    public String getIamOidcJwtUrlField() {
        return iamOidcJwtUrlField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtUrlField")
    public void setIamOidcJwtUrlField(String iamOidcJwtUrlField) {
        this.iamOidcJwtUrlField = iamOidcJwtUrlField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtAlgorithmField")
    public String getIamOidcJwtAlgorithmField() {
        return iamOidcJwtAlgorithmField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtAlgorithmField")
    public void setIamOidcJwtAlgorithmField(String iamOidcJwtAlgorithmField) {
        this.iamOidcJwtAlgorithmField = iamOidcJwtAlgorithmField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtPublicKeyField")
    public String getIamOidcJwtPublicKeyField() {
        return iamOidcJwtPublicKeyField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtPublicKeyField")
    public void setIamOidcJwtPublicKeyField(String iamOidcJwtPublicKeyField) {
        this.iamOidcJwtPublicKeyField = iamOidcJwtPublicKeyField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtExpiredField")
    public String getIamOidcJwtExpiredField() {
        return iamOidcJwtExpiredField;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcJwtExpiredField")
    public void setIamOidcJwtExpiredField(String iamOidcJwtExpiredField) {
        this.iamOidcJwtExpiredField = iamOidcJwtExpiredField;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamOidcAuthenticationUrl", iamOidcAuthenticationUrl).append("iamOidcClientId", iamOidcClientId).append("iamOidcName", iamOidcName).append("iamOidcClientSecret", iamOidcClientSecret).append("iamOidcTokenVerificationUrl", iamOidcTokenVerificationUrl).append("iamOidcLogoutUrl", iamOidcLogoutUrl).append("iamOidcProfileInformationUrl", iamOidcProfileInformationUrl).append("iamOidcSessionRenewalUrl", iamOidcSessionRenewalUrl).append("iamOidcCertificateUrl", iamOidcCertificateUrl).append("iamOidcPublicKeyField", iamOidcPublicKeyField).append("iamOidcCertificateIssuer", iamOidcCertificateIssuer).append("iamOidcCertificateExpirationDate", iamOidcCertificateExpirationDate).append("iamOidcIsJwtToken", iamOidcIsJwtToken).append("iamOidcJwtEmailField", iamOidcJwtEmailField).append("iamOidcJwtClientIdField", iamOidcJwtClientIdField).append("iamOidcJwtUrlField", iamOidcJwtUrlField).append("iamOidcJwtAlgorithmField", iamOidcJwtAlgorithmField).append("iamOidcJwtPublicKeyField", iamOidcJwtPublicKeyField).append("iamOidcJwtExpiredField", iamOidcJwtExpiredField).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamOidcTokenVerificationUrl).append(iamOidcSessionRenewalUrl).append(iamOidcClientId).append(iamOidcLogoutUrl).append(iamOidcJwtUrlField).append(iamOidcPublicKeyField).append(iamOidcJwtPublicKeyField).append(iamOidcProfileInformationUrl).append(iamOidcCertificateIssuer).append(iamOidcCertificateExpirationDate).append(iamOidcJwtClientIdField).append(iamOidcClientSecret).append(iamOidcJwtExpiredField).append(iamOidcAuthenticationUrl).append(iamOidcName).append(iamOidcJwtEmailField).append(iamOidcIsJwtToken).append(iamOidcJwtAlgorithmField).append(iamOidcCertificateUrl).toHashCode();
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
        return new EqualsBuilder().append(iamOidcTokenVerificationUrl, rhs.iamOidcTokenVerificationUrl).append(iamOidcSessionRenewalUrl, rhs.iamOidcSessionRenewalUrl).append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcLogoutUrl, rhs.iamOidcLogoutUrl).append(iamOidcJwtUrlField, rhs.iamOidcJwtUrlField).append(iamOidcPublicKeyField, rhs.iamOidcPublicKeyField).append(iamOidcJwtPublicKeyField, rhs.iamOidcJwtPublicKeyField).append(iamOidcProfileInformationUrl, rhs.iamOidcProfileInformationUrl).append(iamOidcCertificateIssuer, rhs.iamOidcCertificateIssuer).append(iamOidcCertificateExpirationDate, rhs.iamOidcCertificateExpirationDate).append(iamOidcJwtClientIdField, rhs.iamOidcJwtClientIdField).append(iamOidcClientSecret, rhs.iamOidcClientSecret).append(iamOidcJwtExpiredField, rhs.iamOidcJwtExpiredField).append(iamOidcAuthenticationUrl, rhs.iamOidcAuthenticationUrl).append(iamOidcName, rhs.iamOidcName).append(iamOidcJwtEmailField, rhs.iamOidcJwtEmailField).append(iamOidcIsJwtToken, rhs.iamOidcIsJwtToken).append(iamOidcJwtAlgorithmField, rhs.iamOidcJwtAlgorithmField).append(iamOidcCertificateUrl, rhs.iamOidcCertificateUrl).isEquals();
    }

}
