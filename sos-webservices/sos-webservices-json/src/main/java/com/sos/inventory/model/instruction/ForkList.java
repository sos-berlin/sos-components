
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * ForkList
 * <p>
 * instruction with fixed property 'TYPE':'ForkList'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "children",
    "childToId",
    "workflow"
})
public class ForkList
    extends Instruction
{

    @JsonProperty("children")
    private String children;
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
     */
    public ForkList(String children, String childToId, Instructions workflow) {
        super();
        this.children = children;
        this.childToId = childToId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToId", childToId).append("workflow", workflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(childToId).append(workflow).append(children).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(childToId, rhs.childToId).append(workflow, rhs.workflow).append(children, rhs.children).isEquals();
    }

}
