
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ordering subagents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subagentId",
    "predecessorSubagentId"
})
public class OrderingSubagents {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentId")
    private String subagentId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentId")
    private String predecessorSubagentId;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentId")
    public String getPredecessorSubagentId() {
        return predecessorSubagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentId")
    public void setPredecessorSubagentId(String predecessorSubagentId) {
        this.predecessorSubagentId = predecessorSubagentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("subagentId", subagentId).append("predecessorSubagentId", predecessorSubagentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(predecessorSubagentId).append(subagentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderingSubagents) == false) {
            return false;
        }
        OrderingSubagents rhs = ((OrderingSubagents) other);
        return new EqualsBuilder().append(predecessorSubagentId, rhs.predecessorSubagentId).append(subagentId, rhs.subagentId).isEquals();
    }

}
