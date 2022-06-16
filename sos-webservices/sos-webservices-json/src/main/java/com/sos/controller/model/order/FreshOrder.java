
package com.sos.controller.model.order;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
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
    "startPosition",
    "endPosition"
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
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> startPosition = null;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> endPosition = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FreshOrder() {
    }

    /**
     * 
     * @param workflowPath
     * @param endPosition
     * @param scheduledFor
     * @param arguments
     * @param id
     * @param startPosition
     */
    public FreshOrder(String id, String workflowPath, Long scheduledFor, Variables arguments, List<Object> startPosition, List<Object> endPosition) {
        super();
        this.id = id;
        this.workflowPath = workflowPath;
        this.scheduledFor = scheduledFor;
        this.arguments = arguments;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
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
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public List<Object> getStartPosition() {
        return startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public void setStartPosition(List<Object> startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public List<Object> getEndPosition() {
        return endPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public void setEndPosition(List<Object> endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflowPath", workflowPath).append("scheduledFor", scheduledFor).append("arguments", arguments).append("startPosition", startPosition).append("endPosition", endPosition).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPath).append(endPosition).append(scheduledFor).append(arguments).append(id).append(startPosition).toHashCode();
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
        return new EqualsBuilder().append(workflowPath, rhs.workflowPath).append(endPosition, rhs.endPosition).append(scheduledFor, rhs.scheduledFor).append(arguments, rhs.arguments).append(id, rhs.id).append(startPosition, rhs.startPosition).isEquals();
    }

}
