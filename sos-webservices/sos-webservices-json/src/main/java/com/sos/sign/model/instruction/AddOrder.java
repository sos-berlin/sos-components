
package com.sos.sign.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * AddOrder
 * <p>
 * instruction with fixed property 'TYPE':'AdOrder'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "orderId",
    "workflowPath",
    "arguments",
    "deleteWhenTerminated",
    "startPosition",
    "endPositions"
})
public class AddOrder
    extends Instruction
{

    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#I$js7EpochSecond-$orderName"
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    @JsonPropertyDescription("'#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ \"#I$js7EpochSecond-$orderName\"")
    private String orderId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JsonAlias({
        "workflowName"
    })
    private String workflowPath;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    @JsonProperty("deleteWhenTerminated")
    private Boolean deleteWhenTerminated = true;
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
     * @param orderId
     * @param workflowPath
     * @param deleteWhenTerminated
     * @param arguments
     * @param tYPE
     * @param startPosition
     */
    public AddOrder(String orderId, String workflowPath, Variables arguments, Boolean deleteWhenTerminated, List<Object> startPosition, List<List<Object>> endPositions, InstructionType tYPE) {
        super(tYPE);
        this.orderId = orderId;
        this.workflowPath = workflowPath;
        this.arguments = arguments;
        this.deleteWhenTerminated = deleteWhenTerminated;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
    }

    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#I$js7EpochSecond-$orderName"
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#I$js7EpochSecond-$orderName"
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
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

    @JsonProperty("deleteWhenTerminated")
    public Boolean getDeleteWhenTerminated() {
        return deleteWhenTerminated;
    }

    @JsonProperty("deleteWhenTerminated")
    public void setDeleteWhenTerminated(Boolean deleteWhenTerminated) {
        this.deleteWhenTerminated = deleteWhenTerminated;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("workflowPath", workflowPath).append("arguments", arguments).append("deleteWhenTerminated", deleteWhenTerminated).append("startPosition", startPosition).append("endPositions", endPositions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(endPositions).append(orderId).append(workflowPath).append(deleteWhenTerminated).append(arguments).append(startPosition).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(endPositions, rhs.endPositions).append(orderId, rhs.orderId).append(workflowPath, rhs.workflowPath).append(deleteWhenTerminated, rhs.deleteWhenTerminated).append(arguments, rhs.arguments).append(startPosition, rhs.startPosition).isEquals();
    }

}
