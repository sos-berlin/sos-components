
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * order filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderId",
    "compact",
    "withoutWorkflowTags"
})
public class OrderFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
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
    @JsonProperty("orderId")
    private String orderId;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    @JsonPropertyDescription("if true then response doesn't contain 'workflowsTagPerWorkflow'")
    private Boolean withoutWorkflowTags = false;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
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
     * (Required)
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
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public Boolean getWithoutWorkflowTags() {
        return withoutWorkflowTags;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public void setWithoutWorkflowTags(Boolean withoutWorkflowTags) {
        this.withoutWorkflowTags = withoutWorkflowTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderId", orderId).append("compact", compact).append("withoutWorkflowTags", withoutWorkflowTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(withoutWorkflowTags).append(controllerId).append(compact).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderFilter) == false) {
            return false;
        }
        OrderFilter rhs = ((OrderFilter) other);
        return new EqualsBuilder().append(withoutWorkflowTags, rhs.withoutWorkflowTags).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(orderId, rhs.orderId).isEquals();
    }

}
