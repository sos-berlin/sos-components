
package com.sos.sign.model.instruction;

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
    "deleteWhenTerminated"
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
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param orderId
     * @param workflowPath
     * @param deleteWhenTerminated
     * @param arguments
     * @param tYPE
     */
    public AddOrder(String orderId, String workflowPath, Variables arguments, Boolean deleteWhenTerminated, InstructionType tYPE) {
        super(tYPE);
        this.orderId = orderId;
        this.workflowPath = workflowPath;
        this.arguments = arguments;
        this.deleteWhenTerminated = deleteWhenTerminated;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("workflowPath", workflowPath).append("arguments", arguments).append("deleteWhenTerminated", deleteWhenTerminated).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(arguments).append(orderId).append(workflowPath).append(deleteWhenTerminated).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(arguments, rhs.arguments).append(orderId, rhs.orderId).append(workflowPath, rhs.workflowPath).append(deleteWhenTerminated, rhs.deleteWhenTerminated).isEquals();
    }

}
