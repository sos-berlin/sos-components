
package com.sos.joc.model.security.fido2;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registrations Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "accounts",
    "deferred",
    "confirmed",
    "auditLog"
})
public class Fido2RegistrationsFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    @JsonProperty("accounts")
    private List<Fido2RegistrationAccount> accounts = new ArrayList<Fido2RegistrationAccount>();
    @JsonProperty("deferred")
    private Boolean deferred = false;
    @JsonProperty("confirmed")
    private Boolean confirmed = false;
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
    public Fido2RegistrationsFilter() {
    }

    /**
     * 
     * @param deferred
     * @param identityServiceName
     * @param auditLog
     * @param accounts
     * @param confirmed
     */
    public Fido2RegistrationsFilter(String identityServiceName, List<Fido2RegistrationAccount> accounts, Boolean deferred, Boolean confirmed, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accounts = accounts;
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

    @JsonProperty("accounts")
    public List<Fido2RegistrationAccount> getAccounts() {
        return accounts;
    }

    @JsonProperty("accounts")
    public void setAccounts(List<Fido2RegistrationAccount> accounts) {
        this.accounts = accounts;
    }

    @JsonProperty("deferred")
    public Boolean getDeferred() {
        return deferred;
    }

    @JsonProperty("deferred")
    public void setDeferred(Boolean deferred) {
        this.deferred = deferred;
    }

    @JsonProperty("confirmed")
    public Boolean getConfirmed() {
        return confirmed;
    }

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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accounts", accounts).append("deferred", deferred).append("confirmed", confirmed).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deferred).append(identityServiceName).append(accounts).append(auditLog).append(confirmed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2RegistrationsFilter) == false) {
            return false;
        }
        Fido2RegistrationsFilter rhs = ((Fido2RegistrationsFilter) other);
        return new EqualsBuilder().append(deferred, rhs.deferred).append(identityServiceName, rhs.identityServiceName).append(accounts, rhs.accounts).append(auditLog, rhs.auditLog).append(confirmed, rhs.confirmed).isEquals();
    }

}
