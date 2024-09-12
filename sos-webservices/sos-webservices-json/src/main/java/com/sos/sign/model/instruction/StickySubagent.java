
package com.sos.sign.model.instruction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * StickySubagent
 * <p>
 * instruction with fixed property 'TYPE':'StickySubagent'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "agentPath",
    "subagentBundleIdExpr",
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
    @JsonProperty("subagentBundleIdExpr")
    @JsonAlias({
        "subagentClusterIdExpr",
        "subagentSelectionIdExpr"
    })
    private String subagentBundleIdExpr;
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
     * @param subworkflow
     * @param subagentBundleIdExpr
     */
    public StickySubagent(String agentPath, String subagentBundleIdExpr, Instructions subworkflow) {
        super();
        this.agentPath = agentPath;
        this.subagentBundleIdExpr = subagentBundleIdExpr;
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

    @JsonProperty("subagentBundleIdExpr")
    public String getSubagentBundleIdExpr() {
        return subagentBundleIdExpr;
    }

    @JsonProperty("subagentBundleIdExpr")
    public void setSubagentBundleIdExpr(String subagentBundleIdExpr) {
        this.subagentBundleIdExpr = subagentBundleIdExpr;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentPath", agentPath).append("subagentBundleIdExpr", subagentBundleIdExpr).append("subworkflow", subworkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(subworkflow).append(subagentBundleIdExpr).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(subworkflow, rhs.subworkflow).append(subagentBundleIdExpr, rhs.subagentBundleIdExpr).isEquals();
    }

}
