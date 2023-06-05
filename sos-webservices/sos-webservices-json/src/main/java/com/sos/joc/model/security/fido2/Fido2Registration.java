
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "accountName",
    "email",
    "origin",
    "publicKey",
    "jwk",
    "credentialId",
    "clientDataJSON",
    "deferred",
    "confirmed",
    "auditLog"
})
public class Fido2Registration {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    private String email;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("origin")
    private String origin;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("publicKey")
    private String publicKey;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jwk")
    private String jwk;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    private String credentialId;
    @JsonProperty("clientDataJSON")
    private String clientDataJSON;
    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    @JsonPropertyDescription("true if the account is deferred")
    private Boolean deferred;
    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    @JsonPropertyDescription("true if the registration is confirmed")
    private Boolean confirmed;
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
    public Fido2Registration() {
    }

    /**
     * 
     * @param clientDataJSON
     * @param deferred
     * @param identityServiceName
     * @param auditLog
     * @param accountName
     * @param origin
     * @param credentialId
     * @param publicKey
     * @param confirmed
     * @param email
     * @param jwk
     */
    public Fido2Registration(String identityServiceName, String accountName, String email, String origin, String publicKey, String jwk, String credentialId, String clientDataJSON, Boolean deferred, Boolean confirmed, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accountName = accountName;
        this.email = email;
        this.origin = origin;
        this.publicKey = publicKey;
        this.jwk = jwk;
        this.credentialId = credentialId;
        this.clientDataJSON = clientDataJSON;
        this.deferred = deferred;
        this.confirmed = confirmed;
        this.auditLog = auditLog;
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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("origin")
    public String getOrigin() {
        return origin;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("origin")
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("publicKey")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jwk")
    public String getJwk() {
        return jwk;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jwk")
    public void setJwk(String jwk) {
        this.jwk = jwk;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    public String getCredentialId() {
        return credentialId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    @JsonProperty("clientDataJSON")
    public String getClientDataJSON() {
        return clientDataJSON;
    }

    @JsonProperty("clientDataJSON")
    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }

    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    public Boolean getDeferred() {
        return deferred;
    }

    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    public void setDeferred(Boolean deferred) {
        this.deferred = deferred;
    }

    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    public Boolean getConfirmed() {
        return confirmed;
    }

    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accountName", accountName).append("email", email).append("origin", origin).append("publicKey", publicKey).append("jwk", jwk).append("credentialId", credentialId).append("clientDataJSON", clientDataJSON).append("deferred", deferred).append("confirmed", confirmed).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(clientDataJSON).append(deferred).append(identityServiceName).append(auditLog).append(accountName).append(origin).append(credentialId).append(publicKey).append(confirmed).append(email).append(jwk).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2Registration) == false) {
            return false;
        }
        Fido2Registration rhs = ((Fido2Registration) other);
        return new EqualsBuilder().append(clientDataJSON, rhs.clientDataJSON).append(deferred, rhs.deferred).append(identityServiceName, rhs.identityServiceName).append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(origin, rhs.origin).append(credentialId, rhs.credentialId).append(publicKey, rhs.publicKey).append(confirmed, rhs.confirmed).append(email, rhs.email).append(jwk, rhs.jwk).isEquals();
    }

}
