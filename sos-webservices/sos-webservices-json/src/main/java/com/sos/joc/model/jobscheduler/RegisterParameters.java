
package com.sos.joc.model.jobscheduler;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * register params
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "controllers",
    "agents",
    "auditLog"
})
public class RegisterParameters {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<RegisterParameter> controllers = new ArrayList<RegisterParameter>();
    @JsonProperty("agents")
    private List<Agent> agents = new ArrayList<Agent>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
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
    @JsonProperty("controllers")
    public List<RegisterParameter> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<RegisterParameter> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("agents")
    public List<Agent> getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    public void setAgents(List<Agent> agents) {
        this.agents = agents;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("controllers", controllers).append("agents", agents).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(controllerId).append(auditLog).append(agents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterParameters) == false) {
            return false;
        }
        RegisterParameters rhs = ((RegisterParameters) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(agents, rhs.agents).isEquals();
    }

}
