
package com.sos.joc.model.encipherment;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter for an assignment of a certificate to an agent for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias",
    "agentId"
})
public class AgentAssignments {

    @JsonProperty("certAlias")
    private String certAlias;
    @JsonProperty("agentId")
    private List<String> agentId = new ArrayList<String>();

    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @JsonProperty("agentId")
    public List<String> getAgentId() {
        return agentId;
    }

    @JsonProperty("agentId")
    public void setAgentId(List<String> agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certAlias", certAlias).append("agentId", agentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentAssignments) == false) {
            return false;
        }
        AgentAssignments rhs = ((AgentAssignments) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(agentId, rhs.agentId).isEquals();
    }

}
