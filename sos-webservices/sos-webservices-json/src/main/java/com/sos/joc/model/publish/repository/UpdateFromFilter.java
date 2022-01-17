
package com.sos.joc.model.publish.repository;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.Config;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter With Repository Object To Update Configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurations",
    "auditLog"
})
public class UpdateFromFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    private List<Config> configurations = new ArrayList<Config>();
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
    @JsonProperty("configurations")
    public List<Config> getConfigurations() {
        return configurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public void setConfigurations(List<Config> configurations) {
        this.configurations = configurations;
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
        return new ToStringBuilder(this).append("configurations", configurations).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UpdateFromFilter) == false) {
            return false;
        }
        UpdateFromFilter rhs = ((UpdateFromFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(configurations, rhs.configurations).isEquals();
    }

}
