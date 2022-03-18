
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
    "clusterAgentIds",
    "subagentClusterIds",
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
    @JsonProperty("clusterAgentIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> clusterAgentIds = new LinkedHashSet<String>();
    @JsonProperty("subagentClusterIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> subagentClusterIds = new LinkedHashSet<String>();
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

    @JsonProperty("clusterAgentIds")
    public Set<String> getClusterAgentIds() {
        return clusterAgentIds;
    }

    @JsonProperty("clusterAgentIds")
    public void setClusterAgentIds(Set<String> clusterAgentIds) {
        this.clusterAgentIds = clusterAgentIds;
    }

    @JsonProperty("subagentClusterIds")
    public Set<String> getSubagentClusterIds() {
        return subagentClusterIds;
    }

    @JsonProperty("subagentClusterIds")
    public void setSubagentClusterIds(Set<String> subagentClusterIds) {
        this.subagentClusterIds = subagentClusterIds;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("clusterAgentIds", clusterAgentIds).append("subagentClusterIds", subagentClusterIds).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(auditLog).append(clusterAgentIds).append(subagentClusterIds).toHashCode();
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
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(clusterAgentIds, rhs.clusterAgentIds).append(subagentClusterIds, rhs.subagentClusterIds).isEquals();
    }

}
