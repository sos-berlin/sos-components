
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
    "mappings"
})
public class ShowAgentAssignmentsResponse {

    @JsonProperty("mappings")
    private List<AgentAssignments> mappings = new ArrayList<AgentAssignments>();

    @JsonProperty("mappings")
    public List<AgentAssignments> getMappings() {
        return mappings;
    }

    @JsonProperty("mappings")
    public void setMappings(List<AgentAssignments> mappings) {
        this.mappings = mappings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("mappings", mappings).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mappings).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowAgentAssignmentsResponse) == false) {
            return false;
        }
        ShowAgentAssignmentsResponse rhs = ((ShowAgentAssignmentsResponse) other);
        return new EqualsBuilder().append(mappings, rhs.mappings).isEquals();
    }

}
