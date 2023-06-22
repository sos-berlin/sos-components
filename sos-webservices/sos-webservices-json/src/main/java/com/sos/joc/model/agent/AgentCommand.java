
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "force",
    "lostDirector",
    "auditLog"
})
public class AgentCommand {

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
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * only relevant for reset agent
     * 
     */
    @JsonProperty("force")
    @JsonPropertyDescription("only relevant for reset agent")
    private Boolean force = false;
    /**
     * only relevant for /agent/cluster/confirm_node_loss
     * 
     */
    @JsonProperty("lostDirector")
    @JsonPropertyDescription("only relevant for /agent/cluster/confirm_node_loss")
    private AgentCommand.LostDirector lostDirector;
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * only relevant for reset agent
     * 
     */
    @JsonProperty("force")
    public Boolean getForce() {
        return force;
    }

    /**
     * only relevant for reset agent
     * 
     */
    @JsonProperty("force")
    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     * only relevant for /agent/cluster/confirm_node_loss
     * 
     */
    @JsonProperty("lostDirector")
    public AgentCommand.LostDirector getLostDirector() {
        return lostDirector;
    }

    /**
     * only relevant for /agent/cluster/confirm_node_loss
     * 
     */
    @JsonProperty("lostDirector")
    public void setLostDirector(AgentCommand.LostDirector lostDirector) {
        this.lostDirector = lostDirector;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("force", force).append("lostDirector", lostDirector).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(force).append(controllerId).append(auditLog).append(lostDirector).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentCommand) == false) {
            return false;
        }
        AgentCommand rhs = ((AgentCommand) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(force, rhs.force).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(lostDirector, rhs.lostDirector).isEquals();
    }

    public enum LostDirector {

        PRIMARY_DIRECTOR("PRIMARY_DIRECTOR"),
        SECONDARY_DIRECTOR("SECONDARY_DIRECTOR");
        private final String value;
        private final static Map<String, AgentCommand.LostDirector> CONSTANTS = new HashMap<String, AgentCommand.LostDirector>();

        static {
            for (AgentCommand.LostDirector c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LostDirector(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static AgentCommand.LostDirector fromValue(String value) {
            AgentCommand.LostDirector constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
