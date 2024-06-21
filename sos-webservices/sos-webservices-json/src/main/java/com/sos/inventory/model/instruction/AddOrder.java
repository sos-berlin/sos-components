
package com.sos.inventory.model.instruction;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * AddOrder
 * <p>
 * instruction with fixed property 'TYPE':'AddOrder'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "workflowName",
    "arguments",
    "remainWhenTerminated",
    "startPosition",
    "endPositions",
    "blockPosition",
    "forceJobAdmission",
    "tags"
})
public class AddOrder
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    @JsonAlias({
        "workflowPath"
    })
    private String workflowName;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    @JsonProperty("remainWhenTerminated")
    private Boolean remainWhenTerminated = false;
    @JsonProperty("startPosition")
    private Object startPosition;
    @JsonProperty("endPositions")
    private List<Object> endPositions = null;
    @JsonProperty("blockPosition")
    private Object blockPosition;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission = false;
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param blockPosition
     * @param workflowName
     * 
     * @param remainWhenTerminated
     * @param startPosition
     * @param tags
     * @param endPositions
     * @param forceJobAdmission
     * @param arguments
     */
    public AddOrder(String workflowName, Variables arguments, Boolean remainWhenTerminated, Object startPosition, List<Object> endPositions, Object blockPosition, Boolean forceJobAdmission, Set<String> tags) {
        super();
        this.workflowName = workflowName;
        this.arguments = arguments;
        this.remainWhenTerminated = remainWhenTerminated;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
        this.blockPosition = blockPosition;
        this.forceJobAdmission = forceJobAdmission;
        this.tags = tags;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
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

    @JsonProperty("remainWhenTerminated")
    public Boolean getRemainWhenTerminated() {
        return remainWhenTerminated;
    }

    @JsonProperty("remainWhenTerminated")
    public void setRemainWhenTerminated(Boolean remainWhenTerminated) {
        this.remainWhenTerminated = remainWhenTerminated;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("startPosition")
    public Object getStartPosition() {
        if (startPosition != null) {
            if (startPosition instanceof String && ((String) startPosition).isEmpty()) {
                return null;
            } else if (startPosition instanceof List<?> && ((List<Object>) startPosition).isEmpty()) {
                return null;
            }
        }
        return startPosition;
    }

    @JsonProperty("startPosition")
    public void setStartPosition(Object startPosition) {
        this.startPosition = startPosition;
    }

    @JsonProperty("endPositions")
    public List<Object> getEndPositions() {
        return endPositions;
    }

    @JsonProperty("endPositions")
    public void setEndPositions(List<Object> endPositions) {
        this.endPositions = endPositions;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("blockPosition")
    public Object getBlockPosition() {
        if (blockPosition != null) {
            if (blockPosition instanceof String && ((String) blockPosition).isEmpty()) {
                return null;
            } else if (blockPosition instanceof List<?> && ((List<Object>) blockPosition).isEmpty()) {
                return null;
            }
        }
        return blockPosition;
    }

    @JsonProperty("blockPosition")
    public void setBlockPosition(Object blockPosition) {
        this.blockPosition = blockPosition;
    }

    @JsonProperty("forceJobAdmission")
    public Boolean getForceJobAdmission() {
        return forceJobAdmission;
    }

    @JsonProperty("forceJobAdmission")
    public void setForceJobAdmission(Boolean forceJobAdmission) {
        this.forceJobAdmission = forceJobAdmission;
    }

    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowName", workflowName).append("arguments", arguments).append("remainWhenTerminated", remainWhenTerminated).append("startPosition", startPosition).append("endPositions", endPositions).append("blockPosition", blockPosition).append("forceJobAdmission", forceJobAdmission).append("tags", tags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(endPositions).append(blockPosition).append(forceJobAdmission).append(workflowName).append(arguments).append(remainWhenTerminated).append(startPosition).append(tags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddOrder) == false) {
            return false;
        }
        AddOrder rhs = ((AddOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(endPositions, rhs.endPositions).append(blockPosition, rhs.blockPosition).append(forceJobAdmission, rhs.forceJobAdmission).append(workflowName, rhs.workflowName).append(arguments, rhs.arguments).append(remainWhenTerminated, rhs.remainWhenTerminated).append(startPosition, rhs.startPosition).append(tags, rhs.tags).isEquals();
    }

}
