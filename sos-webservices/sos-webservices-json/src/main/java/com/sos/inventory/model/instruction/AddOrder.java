
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
    "endPositions"
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
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> startPosition = null;
    @JsonProperty("endPositions")
    private List<List<Object>> endPositions = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param endPositions
     * @param workflowName
     * @param arguments
     * @param position
     * @param state
     * 
     * @param remainWhenTerminated
     * @param startPosition
     * @param positionString
     */
    public AddOrder(String workflowName, Variables arguments, Boolean remainWhenTerminated, List<Object> startPosition, List<List<Object>> endPositions, List<Object> position, String positionString, InstructionState state) {
        super(, position, positionString, state);
        this.workflowName = workflowName;
        this.arguments = arguments;
        this.remainWhenTerminated = remainWhenTerminated;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
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

    @JsonProperty("endPositions")
    public List<List<Object>> getEndPositions() {
        return endPositions;
    }

    @JsonProperty("endPositions")
    public void setEndPositions(List<List<Object>> endPositions) {
        this.endPositions = endPositions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowName", workflowName).append("arguments", arguments).append("remainWhenTerminated", remainWhenTerminated).append("startPosition", startPosition).append("endPositions", endPositions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowName).append(arguments).append(endPositions).append(remainWhenTerminated).append(startPosition).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowName, rhs.workflowName).append(arguments, rhs.arguments).append(endPositions, rhs.endPositions).append(remainWhenTerminated, rhs.remainWhenTerminated).append(startPosition, rhs.startPosition).isEquals();
    }

}
