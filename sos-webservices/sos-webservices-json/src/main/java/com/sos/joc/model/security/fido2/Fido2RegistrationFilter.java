
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registration Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "rpName",
    "accountName",
    "auditLog"
})
public class Fido2RegistrationFilter {

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
     * 
     */
    @JsonProperty("rpName")
    private String rpName;
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
    public Fido2RegistrationFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param auditLog
     * @param accountName
     * @param rpName
     */
    public Fido2RegistrationFilter(String identityServiceName, String rpName, String accountName, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.rpName = rpName;
        this.accountName = accountName;
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
     * 
     */
    @JsonProperty("rpName")
    public String getRpName() {
        return rpName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("rpName")
    public void setRpName(String rpName) {
        this.rpName = rpName;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("rpName", rpName).append("accountName", accountName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(auditLog).append(accountName).append(rpName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2RegistrationFilter) == false) {
            return false;
        }
        Fido2RegistrationFilter rhs = ((Fido2RegistrationFilter) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(rpName, rhs.rpName).isEquals();
    }

}
