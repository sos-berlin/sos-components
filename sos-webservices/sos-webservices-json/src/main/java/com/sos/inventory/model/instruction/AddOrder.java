
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;
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
    "orderName",
    "workflowName",
    "arguments",
    "deleteWhenTerminated"
})
public class AddOrder
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderName")
    private String orderName;
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
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment arguments;
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
     * @param deleteWhenTerminated
     * @param workflowName
     * @param arguments
     * @param orderName
     */
    public AddOrder(String orderName, String workflowName, Environment arguments, Boolean deleteWhenTerminated) {
        super();
        this.orderName = orderName;
        this.workflowName = workflowName;
        this.arguments = arguments;
        this.deleteWhenTerminated = deleteWhenTerminated;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderName")
    public void setOrderName(String orderName) {
        this.orderName = orderName;
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
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Environment getArguments() {
        return arguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Environment arguments) {
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderName", orderName).append("workflowName", workflowName).append("arguments", arguments).append("deleteWhenTerminated", deleteWhenTerminated).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowName).append(arguments).append(deleteWhenTerminated).append(orderName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowName, rhs.workflowName).append(arguments, rhs.arguments).append(deleteWhenTerminated, rhs.deleteWhenTerminated).append(orderName, rhs.orderName).isEquals();
    }

}
