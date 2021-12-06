
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "subagentId"
})
public class AgentTaskOrder {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("subagentId")
    private String subagentId;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderId", orderId).append("subagentId", subagentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(subagentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentTaskOrder) == false) {
            return false;
        }
        AgentTaskOrder rhs = ((AgentTaskOrder) other);
        return new EqualsBuilder().append(orderId, rhs.orderId).append(subagentId, rhs.subagentId).isEquals();
    }

}
