
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
 * filter to show assignments of a certificate to one or more agents for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentIds",
    "certAliases"
})
public class ShowAgentAssignmentsRequestFilter {

    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    @JsonProperty("certAliases")
    private List<String> certAliases = new ArrayList<String>();

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    @JsonProperty("certAliases")
    public List<String> getCertAliases() {
        return certAliases;
    }

    @JsonProperty("certAliases")
    public void setCertAliases(List<String> certAliases) {
        this.certAliases = certAliases;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentIds", agentIds).append("certAliases", certAliases).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAliases).append(agentIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowAgentAssignmentsRequestFilter) == false) {
            return false;
        }
        ShowAgentAssignmentsRequestFilter rhs = ((ShowAgentAssignmentsRequestFilter) other);
        return new EqualsBuilder().append(certAliases, rhs.certAliases).append(agentIds, rhs.agentIds).isEquals();
    }

}
