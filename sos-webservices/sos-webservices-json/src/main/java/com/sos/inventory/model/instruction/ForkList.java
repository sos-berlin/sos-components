
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.workflow.BranchWorkflow;


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
    "agentName",
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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("childToId")
    private String childToId;
    @JsonProperty("agentName")
    @JsonAlias({
        "agentId",
        "agentPath"
    })
    private String agentName;
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
     * @param childToId
     * @param workflow
     * @param children
     * @param agentName
     * @param joinIfFailed
     */
    public ForkList(String children, String childToId, String agentName, BranchWorkflow workflow, Boolean joinIfFailed) {
        super();
        this.children = children;
        this.childToId = childToId;
        this.agentName = agentName;
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

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToId", childToId).append("agentName", agentName).append("workflow", workflow).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(childToId).append(agentName).append(workflow).append(children).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(childToId, rhs.childToId).append(agentName, rhs.agentName).append(workflow, rhs.workflow).append(children, rhs.children).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
