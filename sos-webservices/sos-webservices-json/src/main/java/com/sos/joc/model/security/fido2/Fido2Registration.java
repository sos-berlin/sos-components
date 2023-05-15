
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
    "publicKey",
    "cipherType",
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
    @JsonProperty("publicKey")
    private String publicKey;
    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("cipherType")
    private CipherTypes cipherType;
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
     * @param deferred
     * @param identityServiceName
     * @param auditLog
     * @param accountName
     * @param publicKey
     * @param confirmed
     * @param email
     * @param cipherType
     */
    public Fido2Registration(String identityServiceName, String accountName, String email, String publicKey, CipherTypes cipherType, Boolean deferred, Boolean confirmed, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accountName = accountName;
        this.email = email;
        this.publicKey = publicKey;
        this.cipherType = cipherType;
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
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("cipherType")
    public CipherTypes getCipherType() {
        return cipherType;
    }

    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("cipherType")
    public void setCipherType(CipherTypes cipherType) {
        this.cipherType = cipherType;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accountName", accountName).append("email", email).append("publicKey", publicKey).append("cipherType", cipherType).append("deferred", deferred).append("confirmed", confirmed).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deferred).append(identityServiceName).append(auditLog).append(accountName).append(publicKey).append(confirmed).append(email).append(cipherType).toHashCode();
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
        return new EqualsBuilder().append(deferred, rhs.deferred).append(identityServiceName, rhs.identityServiceName).append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(publicKey, rhs.publicKey).append(confirmed, rhs.confirmed).append(email, rhs.email).append(cipherType, rhs.cipherType).isEquals();
    }

}
