
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ordering subagentclusters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "subagentClusterId",
    "predecessorSubagentClusterId"
})
public class OrderingSubagentClusters {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("subagentClusterId", subagentClusterId).append("predecessorSubagentClusterId", predecessorSubagentClusterId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(predecessorSubagentClusterId).append(subagentClusterId).append(controllerId).toHashCode();
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
        return new EqualsBuilder().append(predecessorSubagentClusterId, rhs.predecessorSubagentClusterId).append(subagentClusterId, rhs.subagentClusterId).append(controllerId, rhs.controllerId).isEquals();
    }

}
