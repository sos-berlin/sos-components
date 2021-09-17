
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
    "childToId",
    "workflow",
    "joinIfFailed"
})
public class ForkList
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    private String children;
    @JsonProperty("childToArguments")
    private String childToArguments;
    @JsonProperty("childToId")
    private String childToId;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    private Instructions workflow;
    @JsonProperty("joinIfFailed")
    private Boolean joinIfFailed = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ForkList() {
    }

    /**
     * 
     * @param childToId
     * @param workflow
     * @param children
     * @param tYPE
     * @param childToArguments
     * @param joinIfFailed
     */
    public ForkList(String children, String childToArguments, String childToId, Instructions workflow, Boolean joinIfFailed, InstructionType tYPE) {
        super(tYPE);
        this.children = children;
        this.childToArguments = childToArguments;
        this.childToId = childToId;
        this.workflow = workflow;
        this.joinIfFailed = joinIfFailed;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("children")
    public String getChildren() {
        return children;
    }

    /**
     * 
     * (Required)
     * 
     */
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

    @JsonProperty("childToId")
    public String getChildToId() {
        return childToId;
    }

    @JsonProperty("childToId")
    public void setChildToId(String childToId) {
        this.childToId = childToId;
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

    @JsonProperty("joinIfFailed")
    public Boolean getJoinIfFailed() {
        return joinIfFailed;
    }

    @JsonProperty("joinIfFailed")
    public void setJoinIfFailed(Boolean joinIfFailed) {
        this.joinIfFailed = joinIfFailed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToArguments", childToArguments).append("childToId", childToId).append("workflow", workflow).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(childToId).append(workflow).append(children).append(childToArguments).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(childToId, rhs.childToId).append(workflow, rhs.workflow).append(children, rhs.children).append(childToArguments, rhs.childToArguments).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
