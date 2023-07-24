
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "workflowPath",
    "arguments",
    "deleteWhenTerminated",
    "startPosition",
    "stopPositions",
    "innerBlock",
    "forceJobAdmission"
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
    @JsonProperty("startPosition")
    private Object startPosition;
    @JsonProperty("stopPositions")
    @JsonAlias({
        "endPositions"
    })
    private List<Object> stopPositions = null;
    @JsonProperty("innerBlock")
    @JsonAlias({
        "blockPosition"
    })
    private Object innerBlock;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param stopPositions
     * @param orderId
     * @param workflowPath
     * @param deleteWhenTerminated
     * @param forceJobAdmission
     * @param arguments
     * @param innerBlock
     * @param tYPE
     * @param startPosition
     */
    public AddOrder(String orderId, String workflowPath, Variables arguments, Boolean deleteWhenTerminated, Object startPosition, List<Object> stopPositions, Object innerBlock, Boolean forceJobAdmission, InstructionType tYPE) {
        super(tYPE);
        this.orderId = orderId;
        this.workflowPath = workflowPath;
        this.arguments = arguments;
        this.deleteWhenTerminated = deleteWhenTerminated;
        this.startPosition = startPosition;
        this.stopPositions = stopPositions;
        this.innerBlock = innerBlock;
        this.forceJobAdmission = forceJobAdmission;
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

    @JsonProperty("stopPositions")
    public List<Object> getStopPositions() {
        return stopPositions;
    }

    @JsonProperty("stopPositions")
    public void setStopPositions(List<Object> stopPositions) {
        this.stopPositions = stopPositions;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("innerBlock")
    public Object getInnerBlock() {
        if (innerBlock != null) {
            if (innerBlock instanceof String && ((String) innerBlock).isEmpty()) {
                return null;
            } else if (innerBlock instanceof List<?> && ((List<Object>) innerBlock).isEmpty()) {
                return null;
            }
        }
        return innerBlock;
    }

    @JsonProperty("innerBlock")
    public void setInnerBlock(Object innerBlock) {
        this.innerBlock = innerBlock;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("workflowPath", workflowPath).append("arguments", arguments).append("deleteWhenTerminated", deleteWhenTerminated).append("startPosition", startPosition).append("stopPositions", stopPositions).append("innerBlock", innerBlock).append("forceJobAdmission", forceJobAdmission).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(stopPositions).append(orderId).append(workflowPath).append(deleteWhenTerminated).append(forceJobAdmission).append(arguments).append(innerBlock).append(startPosition).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(stopPositions, rhs.stopPositions).append(orderId, rhs.orderId).append(workflowPath, rhs.workflowPath).append(deleteWhenTerminated, rhs.deleteWhenTerminated).append(forceJobAdmission, rhs.forceJobAdmission).append(arguments, rhs.arguments).append(innerBlock, rhs.innerBlock).append(startPosition, rhs.startPosition).isEquals();
    }

}
