
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ordering subagentclusters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subagentClusterId",
    "ordering"
})
public class OrderingSubagentClusters {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusterId")
    private String subagentClusterId;
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
    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
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
        return new ToStringBuilder(this).append("subagentClusterId", subagentClusterId).append("ordering", ordering).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ordering).append(subagentClusterId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderingSubagentClusters) == false) {
            return false;
        }
        OrderingSubagentClusters rhs = ((OrderingSubagentClusters) other);
        return new EqualsBuilder().append(ordering, rhs.ordering).append(subagentClusterId, rhs.subagentClusterId).isEquals();
    }

}
