
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
    "accountNames",
    "approved",
    "rejected",
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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    private List<String> accountNames = new ArrayList<String>();
    @JsonProperty("approved")
    private Boolean approved = false;
    @JsonProperty("rejected")
    private Boolean rejected = false;
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
     * @param identityServiceName
     * @param approved
     * @param auditLog
     * @param rejected
     * @param confirmed
     * @param accountNames
     */
    public Fido2RegistrationsFilter(String identityServiceName, List<String> accountNames, Boolean approved, Boolean rejected, Boolean confirmed, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accountNames = accountNames;
        this.approved = approved;
        this.rejected = rejected;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    public List<String> getAccountNames() {
        return accountNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }

    @JsonProperty("approved")
    public Boolean getApproved() {
        return approved;
    }

    @JsonProperty("approved")
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @JsonProperty("rejected")
    public Boolean getRejected() {
        return rejected;
    }

    @JsonProperty("rejected")
    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accountNames", accountNames).append("approved", approved).append("rejected", rejected).append("confirmed", confirmed).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(approved).append(auditLog).append(rejected).append(confirmed).append(accountNames).toHashCode();
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
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(approved, rhs.approved).append(auditLog, rhs.auditLog).append(rejected, rhs.rejected).append(confirmed, rhs.confirmed).append(accountNames, rhs.accountNames).isEquals();
    }

}
