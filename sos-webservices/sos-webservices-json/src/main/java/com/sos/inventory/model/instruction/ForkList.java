
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
    "workflow",
    "result",
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
    /**
     * 
     * (Required)
     * 
     */
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
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment result;
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
     * @param result
     * @param childToId
     * @param workflow
     * @param children
     * @param joinIfFailed
     */
    public ForkList(String children, String childToId, Instructions workflow, Environment result, Boolean joinIfFailed) {
        super();
        this.children = children;
        this.childToId = childToId;
        this.workflow = workflow;
        this.result = result;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("childToId")
    public String getChildToId() {
        return childToId;
    }

    /**
     * 
     * (Required)
     * 
     */
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

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    public Environment getResult() {
        return result;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    public void setResult(Environment result) {
        this.result = result;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToId", childToId).append("workflow", workflow).append("result", result).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(result).append(childToId).append(workflow).append(children).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(result, rhs.result).append(childToId, rhs.childToId).append(workflow, rhs.workflow).append(children, rhs.children).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
