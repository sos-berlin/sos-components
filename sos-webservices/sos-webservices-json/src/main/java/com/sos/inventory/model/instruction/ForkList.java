
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.workflow.BranchWorkflow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "subagentClusterId",
    "subagentClusterIdExpr",
    "subagentIdVariable",
    "workflow",
    "joinIfFailed"
})
public class ForkList
    extends Instruction
{

    @JsonProperty("children")
    private String children;
    @JsonProperty("childToId")
    private String childToId;
    @JsonProperty("agentName")
    @JsonAlias({
        "agentId",
        "agentPath"
    })
    private String agentName;
    @JsonProperty("subagentClusterId")
    @JsonAlias({
        "subagentSelectionId"
    })
    private String subagentClusterId;
    @JsonProperty("subagentClusterIdExpr")
    @JsonAlias({
        "subagentSelectionIdExpr"
    })
    private String subagentClusterIdExpr;
    @JsonProperty("subagentIdVariable")
    private String subagentIdVariable = "js7ForkListSubagentId";
    /**
     * workflow in forks
     * <p>
     * 
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
     * @param subagentIdVariable
     * @param agentName
     * @param subagentClusterIdExpr
     * @param subagentClusterId
     * @param joinIfFailed
     */
    public ForkList(String children, String childToId, String agentName, String subagentClusterId, String subagentClusterIdExpr, String subagentIdVariable, BranchWorkflow workflow, Boolean joinIfFailed) {
        super();
        this.children = children;
        this.childToId = childToId;
        this.agentName = agentName;
        this.subagentClusterId = subagentClusterId;
        this.subagentClusterIdExpr = subagentClusterIdExpr;
        this.subagentIdVariable = subagentIdVariable;
        this.workflow = workflow;
        this.joinIfFailed = joinIfFailed;
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

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
    }

    @JsonProperty("subagentClusterIdExpr")
    public String getSubagentClusterIdExpr() {
        return subagentClusterIdExpr;
    }

    @JsonProperty("subagentClusterIdExpr")
    public void setSubagentClusterIdExpr(String subagentClusterIdExpr) {
        this.subagentClusterIdExpr = subagentClusterIdExpr;
    }

    @JsonProperty("subagentIdVariable")
    public String getSubagentIdVariable() {
        return subagentIdVariable;
    }

    @JsonProperty("subagentIdVariable")
    public void setSubagentIdVariable(String subagentIdVariable) {
        this.subagentIdVariable = subagentIdVariable;
    }

    /**
     * workflow in forks
     * <p>
     * 
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToId", childToId).append("agentName", agentName).append("subagentClusterId", subagentClusterId).append("subagentClusterIdExpr", subagentClusterIdExpr).append("subagentIdVariable", subagentIdVariable).append("workflow", workflow).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(childToId).append(workflow).append(children).append(subagentIdVariable).append(agentName).append(subagentClusterIdExpr).append(subagentClusterId).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(childToId, rhs.childToId).append(workflow, rhs.workflow).append(children, rhs.children).append(subagentIdVariable, rhs.subagentIdVariable).append(agentName, rhs.agentName).append(subagentClusterIdExpr, rhs.subagentClusterIdExpr).append(subagentClusterId, rhs.subagentClusterId).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
