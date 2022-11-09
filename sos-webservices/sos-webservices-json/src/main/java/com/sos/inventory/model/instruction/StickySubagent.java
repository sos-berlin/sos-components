
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * StickySubagent
 * <p>
 * instruction with fixed property 'TYPE':'StickySubagent'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "agentName",
    "subagentClusterId",
    "subagentClusterIdExpr",
    "subworkflow"
})
public class StickySubagent
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
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
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subworkflow")
    private Instructions subworkflow;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StickySubagent() {
    }

    /**
     * 
     * @param subworkflow
     * @param agentName
     * @param subagentClusterIdExpr
     * @param subagentClusterId
     * 
     * @param positionString
     */
    public StickySubagent(String agentName, String subagentClusterId, String subagentClusterIdExpr, Instructions subworkflow) {
        super();
        this.agentName = agentName;
        this.subagentClusterId = subagentClusterId;
        this.subagentClusterIdExpr = subagentClusterIdExpr;
        this.subworkflow = subworkflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * 
     * (Required)
     * 
     */
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

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subworkflow")
    public Instructions getSubworkflow() {
        return subworkflow;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subworkflow")
    public void setSubworkflow(Instructions subworkflow) {
        this.subworkflow = subworkflow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentName", agentName).append("subagentClusterId", subagentClusterId).append("subagentClusterIdExpr", subagentClusterIdExpr).append("subworkflow", subworkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentName).append(subagentClusterIdExpr).append(subagentClusterId).append(subworkflow).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StickySubagent) == false) {
            return false;
        }
        StickySubagent rhs = ((StickySubagent) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentName, rhs.agentName).append(subagentClusterIdExpr, rhs.subagentClusterIdExpr).append(subagentClusterId, rhs.subagentClusterId).append(subworkflow, rhs.subworkflow).isEquals();
    }

}
