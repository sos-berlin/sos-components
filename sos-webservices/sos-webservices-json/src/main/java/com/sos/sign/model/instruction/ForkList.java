
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.sign.model.workflow.BranchWorkflow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "agentPath",
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
    @JsonProperty("agentPath")
    @JsonAlias({
        "agentId",
        "agentName"
    })
    private String agentPath;
    /**
     * workflow in forks
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    private BranchWorkflow workflow;
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
     * @param agentPath
     * @param childToId
     * @param workflow
     * @param children
     * @param tYPE
     * @param childToArguments
     * @param joinIfFailed
     */
    public ForkList(String children, String childToArguments, String childToId, String agentPath, BranchWorkflow workflow, Boolean joinIfFailed, InstructionType tYPE) {
        super(tYPE);
        this.children = children;
        this.childToArguments = childToArguments;
        this.childToId = childToId;
        this.agentPath = agentPath;
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

    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    /**
     * workflow in forks
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public BranchWorkflow getWorkflow() {
        return workflow;
    }

    /**
     * workflow in forks
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(BranchWorkflow workflow) {
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToArguments", childToArguments).append("childToId", childToId).append("agentPath", agentPath).append("workflow", workflow).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(childToId).append(workflow).append(children).append(childToArguments).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(childToId, rhs.childToId).append(workflow, rhs.workflow).append(children, rhs.children).append(childToArguments, rhs.childToArguments).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
