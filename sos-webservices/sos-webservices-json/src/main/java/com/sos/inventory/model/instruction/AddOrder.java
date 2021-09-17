
package com.sos.inventory.model.instruction;

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
    "remainWhenTerminated"
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
     * No args constructor for use in serialization
     * 
     */
    public AddOrder() {
    }

    /**
     * 
     * @param workflowName
     * @param arguments
     * @param remainWhenTerminated
     */
    public AddOrder(String workflowName, Variables arguments, Boolean remainWhenTerminated) {
        super();
        this.workflowName = workflowName;
        this.arguments = arguments;
        this.remainWhenTerminated = remainWhenTerminated;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowName", workflowName).append("arguments", arguments).append("remainWhenTerminated", remainWhenTerminated).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowName).append(arguments).append(remainWhenTerminated).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowName, rhs.workflowName).append(arguments, rhs.arguments).append(remainWhenTerminated, rhs.remainWhenTerminated).isEquals();
    }

}
