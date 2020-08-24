
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentCluster"
})
public class AgentClusterPath {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("agentCluster")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String agentCluster;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("agentCluster")
    public String getAgentCluster() {
        return agentCluster;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("agentCluster")
    public void setAgentCluster(String agentCluster) {
        this.agentCluster = agentCluster;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentCluster", agentCluster).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentCluster).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentClusterPath) == false) {
            return false;
        }
        AgentClusterPath rhs = ((AgentClusterPath) other);
        return new EqualsBuilder().append(agentCluster, rhs.agentCluster).isEquals();
    }

}
