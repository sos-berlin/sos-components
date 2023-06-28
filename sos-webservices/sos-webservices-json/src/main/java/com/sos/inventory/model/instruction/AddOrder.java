
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
    "forceJobAdmission"
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
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param endPositions
     * @param forceJobAdmission
     * @param workflowName
     * @param arguments
     * @param remainWhenTerminated
     * @param startPosition
     */
    public AddOrder(String workflowName, Variables arguments, Boolean remainWhenTerminated, Object startPosition, List<Object> endPositions, Boolean forceJobAdmission) {
        super();
        this.workflowName = workflowName;
        this.arguments = arguments;
        this.remainWhenTerminated = remainWhenTerminated;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
        this.forceJobAdmission = forceJobAdmission;
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

    @JsonProperty("forceJobAdmission")
    public Boolean getForceJobAdmission() {
        return forceJobAdmission;
    }

    @JsonProperty("forceJobAdmission")
    public void setForceJobAdmission(Boolean forceJobAdmission) {
        this.forceJobAdmission = forceJobAdmission;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowName", workflowName).append("arguments", arguments).append("remainWhenTerminated", remainWhenTerminated).append("startPosition", startPosition).append("endPositions", endPositions).append("forceJobAdmission", forceJobAdmission).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(endPositions).append(forceJobAdmission).append(workflowName).append(arguments).append(remainWhenTerminated).append(startPosition).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(endPositions, rhs.endPositions).append(forceJobAdmission, rhs.forceJobAdmission).append(workflowName, rhs.workflowName).append(arguments, rhs.arguments).append(remainWhenTerminated, rhs.remainWhenTerminated).append(startPosition, rhs.startPosition).isEquals();
    }

}
