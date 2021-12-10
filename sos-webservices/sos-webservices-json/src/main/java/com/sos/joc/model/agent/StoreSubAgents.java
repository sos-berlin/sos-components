
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
 * store/deploy sub agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "subagents",
    "scheduingType",
    "auditLog"
})
public class StoreSubAgents {

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagents")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<SubAgent> subagents = new LinkedHashSet<SubAgent>();
    /**
     * AgentClusterSchedulingType
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduingType")
    private AgentClusterSchedulingType scheduingType = AgentClusterSchedulingType.fromValue("ROUND_ROBIN");
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagents")
    public Set<SubAgent> getSubagents() {
        return subagents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagents")
    public void setSubagents(Set<SubAgent> subagents) {
        this.subagents = subagents;
    }

    /**
     * AgentClusterSchedulingType
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduingType")
    public AgentClusterSchedulingType getScheduingType() {
        return scheduingType;
    }

    /**
     * AgentClusterSchedulingType
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduingType")
    public void setScheduingType(AgentClusterSchedulingType scheduingType) {
        this.scheduingType = scheduingType;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("subagents", subagents).append("scheduingType", scheduingType).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(scheduingType).append(controllerId).append(auditLog).append(subagents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreSubAgents) == false) {
            return false;
        }
        StoreSubAgents rhs = ((StoreSubAgents) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(scheduingType, rhs.scheduingType).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(subagents, rhs.subagents).isEquals();
    }

}
