
package com.sos.controller.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.OrderPositions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fresh Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "workflowPath",
    "scheduledFor",
    "arguments",
    "positions"
})
public class FreshOrder {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    private Long scheduledFor;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    @JsonPropertyDescription("start and end position")
    private OrderPositions positions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FreshOrder() {
    }

    /**
     * 
     * @param workflowPath
     * @param scheduledFor
     * @param arguments
     * @param positions
     * @param id
     */
    public FreshOrder(String id, String workflowPath, Long scheduledFor, Variables arguments, OrderPositions positions) {
        super();
        this.id = id;
        this.workflowPath = workflowPath;
        this.scheduledFor = scheduledFor;
        this.arguments = arguments;
        this.positions = positions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    public Long getScheduledFor() {
        return scheduledFor;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    public OrderPositions getPositions() {
        return positions;
    }

    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    public void setPositions(OrderPositions positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflowPath", workflowPath).append("scheduledFor", scheduledFor).append("arguments", arguments).append("positions", positions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(arguments).append(positions).append(id).append(workflowPath).append(scheduledFor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FreshOrder) == false) {
            return false;
        }
        FreshOrder rhs = ((FreshOrder) other);
        return new EqualsBuilder().append(arguments, rhs.arguments).append(positions, rhs.positions).append(id, rhs.id).append(workflowPath, rhs.workflowPath).append(scheduledFor, rhs.scheduledFor).isEquals();
    }

}
