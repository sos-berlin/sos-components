
package com.sos.joc.model.publish;

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
 * ExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurations",
    "deployments",
    "auditLog"
})
public class ExportFilter {

    @JsonProperty("configurations")
    private List<Long> configurations = new ArrayList<Long>();
    @JsonProperty("deployments")
    private List<Long> deployments = new ArrayList<Long>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("configurations")
    public List<Long> getConfigurations() {
        return configurations;
    }

    @JsonProperty("configurations")
    public void setConfigurations(List<Long> configurations) {
        this.configurations = configurations;
    }

    @JsonProperty("deployments")
    public List<Long> getDeployments() {
        return deployments;
    }

    @JsonProperty("deployments")
    public void setDeployments(List<Long> deployments) {
        this.deployments = deployments;
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
        return new ToStringBuilder(this).append("configurations", configurations).append("deployments", deployments).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployments).append(auditLog).append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFilter) == false) {
            return false;
        }
        ExportFilter rhs = ((ExportFilter) other);
        return new EqualsBuilder().append(deployments, rhs.deployments).append(auditLog, rhs.auditLog).append(configurations, rhs.configurations).isEquals();
    }

}
