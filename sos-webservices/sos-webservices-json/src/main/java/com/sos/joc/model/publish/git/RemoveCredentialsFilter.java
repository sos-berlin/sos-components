
package com.sos.joc.model.publish.git;

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
 * Filter To remove Git credentials from a specific JOC account
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "credentials",
    "auditLog"
})
public class RemoveCredentialsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    private List<RemoveCredentials> credentials = new ArrayList<RemoveCredentials>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    public List<RemoveCredentials> getCredentials() {
        return credentials;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    public void setCredentials(List<RemoveCredentials> credentials) {
        this.credentials = credentials;
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
        return new ToStringBuilder(this).append("credentials", credentials).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(credentials).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RemoveCredentialsFilter) == false) {
            return false;
        }
        RemoveCredentialsFilter rhs = ((RemoveCredentialsFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(credentials, rhs.credentials).isEquals();
    }

}
