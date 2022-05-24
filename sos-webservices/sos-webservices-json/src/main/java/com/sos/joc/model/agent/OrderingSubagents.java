
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
    "ordering"
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("subagentId", subagentId).append("ordering", ordering).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subagentId).append(ordering).toHashCode();
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
        return new EqualsBuilder().append(subagentId, rhs.subagentId).append(ordering, rhs.ordering).isEquals();
    }

}
