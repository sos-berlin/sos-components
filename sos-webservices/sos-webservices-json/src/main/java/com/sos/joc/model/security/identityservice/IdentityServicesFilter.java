
package com.sos.joc.model.security.identityservice;

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
 * IdentityServices Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceNames",
    "auditLog"
})
public class IdentityServicesFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNames")
    private List<String> identityServiceNames = new ArrayList<String>();
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
    public IdentityServicesFilter() {
    }

    /**
     * 
     * @param auditLog
     * @param identityServiceNames
     */
    public IdentityServicesFilter(List<String> identityServiceNames, AuditParams auditLog) {
        super();
        this.identityServiceNames = identityServiceNames;
        this.auditLog = auditLog;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNames")
    public List<String> getIdentityServiceNames() {
        return identityServiceNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNames")
    public void setIdentityServiceNames(List<String> identityServiceNames) {
        this.identityServiceNames = identityServiceNames;
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
        return new ToStringBuilder(this).append("identityServiceNames", identityServiceNames).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(identityServiceNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityServicesFilter) == false) {
            return false;
        }
        IdentityServicesFilter rhs = ((IdentityServicesFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(identityServiceNames, rhs.identityServiceNames).isEquals();
    }

}
