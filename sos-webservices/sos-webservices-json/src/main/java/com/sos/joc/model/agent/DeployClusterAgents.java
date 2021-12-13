
package com.sos.joc.model.agent;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * deploy cluster agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "clusterAgents",
    "auditLog"
})
public class DeployClusterAgents {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterAgents")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<DeployClusterAgent> clusterAgents = new LinkedHashSet<DeployClusterAgent>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterAgents")
    public Set<DeployClusterAgent> getClusterAgents() {
        return clusterAgents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterAgents")
    public void setClusterAgents(Set<DeployClusterAgent> clusterAgents) {
        this.clusterAgents = clusterAgents;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("clusterAgents", clusterAgents).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(auditLog).append(clusterAgents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployClusterAgents) == false) {
            return false;
        }
        DeployClusterAgents rhs = ((DeployClusterAgents) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(clusterAgents, rhs.clusterAgents).isEquals();
    }

}
