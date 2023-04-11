
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ConsumeNotices
 * <p>
 * instruction with fixed property 'TYPE':'ConsumeNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardNames",
    "subworkflow"
})
public class ConsumeNotices
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    @JsonAlias({
        "boardPaths"
    })
    private String noticeBoardNames;
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
    public ConsumeNotices() {
    }

    /**
     * 
     * @param noticeBoardNames
     * @param subworkflow
     */
    public ConsumeNotices(String noticeBoardNames, Instructions subworkflow) {
        super();
        this.noticeBoardNames = noticeBoardNames;
        this.subworkflow = subworkflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    public String getNoticeBoardNames() {
        return noticeBoardNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(String noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardNames", noticeBoardNames).append("subworkflow", subworkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(subworkflow).append(noticeBoardNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConsumeNotices) == false) {
            return false;
        }
        ConsumeNotices rhs = ((ConsumeNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(subworkflow, rhs.subworkflow).append(noticeBoardNames, rhs.noticeBoardNames).isEquals();
    }

}
