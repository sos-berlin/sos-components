
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * forkList
 * <p>
 * instruction with fixed property 'TYPE':'ForkList'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "children",
    "childToArguments",
    "workflow"
})
public class ForkList
    extends Instruction
{

    @JsonProperty("children")
    private String children;
    @JsonProperty("childToArguments")
    private String childToArguments;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    private Instructions workflow;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ForkList() {
    }

    /**
     * 
     * @param workflow
     * @param children
     * @param tYPE
     * @param childToArguments
     */
    public ForkList(String children, String childToArguments, Instructions workflow, InstructionType tYPE) {
        super(tYPE);
        this.children = children;
        this.childToArguments = childToArguments;
        this.workflow = workflow;
    }

    @JsonProperty("children")
    public String getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(String children) {
        this.children = children;
    }

    @JsonProperty("childToArguments")
    public String getChildToArguments() {
        return childToArguments;
    }

    @JsonProperty("childToArguments")
    public void setChildToArguments(String childToArguments) {
        this.childToArguments = childToArguments;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public Instructions getWorkflow() {
        return workflow;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(Instructions workflow) {
        this.workflow = workflow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToArguments", childToArguments).append("workflow", workflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflow).append(children).append(childToArguments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ForkList) == false) {
            return false;
        }
        ForkList rhs = ((ForkList) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflow, rhs.workflow).append(children, rhs.children).append(childToArguments, rhs.childToArguments).isEquals();
    }

}
