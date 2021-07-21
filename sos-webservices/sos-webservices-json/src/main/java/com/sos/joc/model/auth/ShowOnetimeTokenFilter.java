
package com.sos.joc.model.auth;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the filter properties to show one time token(s)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentIds",
    "controllerId"
})
public class ShowOnetimeTokenFilter {

    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentIds", agentIds).append("controllerId", controllerId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(agentIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowOnetimeTokenFilter) == false) {
            return false;
        }
        ShowOnetimeTokenFilter rhs = ((ShowOnetimeTokenFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(agentIds, rhs.agentIds).isEquals();
    }

}
