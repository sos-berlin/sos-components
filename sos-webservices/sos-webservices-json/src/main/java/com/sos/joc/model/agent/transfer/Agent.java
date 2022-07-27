
package com.sos.joc.model.agent.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.agent.ClusterAgent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent schema for agents to transfer (ex-/import or converter for JobScheduler 1 objects)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentCluster",
    "standaloneAgent"
})
public class Agent {

    /**
     * cluster agent
     * <p>
     * 
     * 
     */
    @JsonProperty("agentCluster")
    private ClusterAgent agentCluster;
    /**
     * single agent
     * <p>
     * 
     * 
     */
    @JsonProperty("standaloneAgent")
    private com.sos.joc.model.agent.Agent standaloneAgent;

    /**
     * cluster agent
     * <p>
     * 
     * 
     */
    @JsonProperty("agentCluster")
    public ClusterAgent getAgentCluster() {
        return agentCluster;
    }

    /**
     * cluster agent
     * <p>
     * 
     * 
     */
    @JsonProperty("agentCluster")
    public void setAgentCluster(ClusterAgent agentCluster) {
        this.agentCluster = agentCluster;
    }

    /**
     * single agent
     * <p>
     * 
     * 
     */
    @JsonProperty("standaloneAgent")
    public com.sos.joc.model.agent.Agent getStandaloneAgent() {
        return standaloneAgent;
    }

    /**
     * single agent
     * <p>
     * 
     * 
     */
    @JsonProperty("standaloneAgent")
    public void setStandaloneAgent(com.sos.joc.model.agent.Agent standaloneAgent) {
        this.standaloneAgent = standaloneAgent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentCluster", agentCluster).append("standaloneAgent", standaloneAgent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(standaloneAgent).append(agentCluster).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Agent) == false) {
            return false;
        }
        Agent rhs = ((Agent) other);
        return new EqualsBuilder().append(standaloneAgent, rhs.standaloneAgent).append(agentCluster, rhs.agentCluster).isEquals();
    }

}
