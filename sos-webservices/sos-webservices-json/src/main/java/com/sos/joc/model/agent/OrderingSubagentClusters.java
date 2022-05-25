
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
    "predecessorSubagentClusterId"
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentClusterId")
    private String predecessorSubagentClusterId;

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentClusterId")
    public String getPredecessorSubagentClusterId() {
        return predecessorSubagentClusterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorSubagentClusterId")
    public void setPredecessorSubagentClusterId(String predecessorSubagentClusterId) {
        this.predecessorSubagentClusterId = predecessorSubagentClusterId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("subagentClusterId", subagentClusterId).append("predecessorSubagentClusterId", predecessorSubagentClusterId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(predecessorSubagentClusterId).append(subagentClusterId).toHashCode();
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
        return new EqualsBuilder().append(predecessorSubagentClusterId, rhs.predecessorSubagentClusterId).append(subagentClusterId, rhs.subagentClusterId).isEquals();
    }

}
