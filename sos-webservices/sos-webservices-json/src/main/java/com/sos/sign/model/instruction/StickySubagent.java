
package com.sos.sign.model.instruction;

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
    "agentPath",
    "subagentSelectionIdExpr",
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
    @JsonProperty("agentPath")
    @JsonAlias({
        "agentId",
        "agentName"
    })
    private String agentPath;
    @JsonProperty("subagentSelectionIdExpr")
    @JsonAlias({
        "subagentClusterIdExpr"
    })
    private String subagentSelectionIdExpr;
    /**
     * instructions
     * <p>
     * 
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
     * @param agentPath
     * @param subagentSelectionIdExpr
     * @param subworkflow
     */
    public StickySubagent(String agentPath, String subagentSelectionIdExpr, Instructions subworkflow) {
        super();
        this.agentPath = agentPath;
        this.subagentSelectionIdExpr = subagentSelectionIdExpr;
        this.subworkflow = subworkflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("subagentSelectionIdExpr")
    public String getSubagentSelectionIdExpr() {
        return subagentSelectionIdExpr;
    }

    @JsonProperty("subagentSelectionIdExpr")
    public void setSubagentSelectionIdExpr(String subagentSelectionIdExpr) {
        this.subagentSelectionIdExpr = subagentSelectionIdExpr;
    }

    /**
     * instructions
     * <p>
     * 
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
     * 
     */
    @JsonProperty("subworkflow")
    public void setSubworkflow(Instructions subworkflow) {
        this.subworkflow = subworkflow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentPath", agentPath).append("subagentSelectionIdExpr", subagentSelectionIdExpr).append("subworkflow", subworkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(subagentSelectionIdExpr).append(subworkflow).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(subagentSelectionIdExpr, rhs.subagentSelectionIdExpr).append(subworkflow, rhs.subworkflow).isEquals();
    }

}
