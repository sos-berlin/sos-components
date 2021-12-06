
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subagents"
})
public class ClusterAgent
    extends Agent
{

    @JsonProperty("subagents")
    private List<SubAgent> subagents = new ArrayList<SubAgent>();

    @JsonProperty("subagents")
    public List<SubAgent> getSubagents() {
        return subagents;
    }

    @JsonProperty("subagents")
    public void setSubagents(List<SubAgent> subagents) {
        this.subagents = subagents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("subagents", subagents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(subagents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterAgent) == false) {
            return false;
        }
        ClusterAgent rhs = ((ClusterAgent) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(subagents, rhs.subagents).isEquals();
    }

}
