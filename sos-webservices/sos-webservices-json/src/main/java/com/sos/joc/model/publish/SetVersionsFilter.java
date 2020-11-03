
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
 * set versions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployments",
    "auditLog"
})
public class SetVersionsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployments")
    private List<DeploymentVersion> deployments = new ArrayList<DeploymentVersion>();
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
    @JsonProperty("deployments")
    public List<DeploymentVersion> getDeployments() {
        return deployments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployments")
    public void setDeployments(List<DeploymentVersion> deployments) {
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
        return new ToStringBuilder(this).append("deployments", deployments).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(deployments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetVersionsFilter) == false) {
            return false;
        }
        SetVersionsFilter rhs = ((SetVersionsFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(deployments, rhs.deployments).isEquals();
    }

}
