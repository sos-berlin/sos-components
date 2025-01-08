
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ConsumeNotices
 * <p>
 * instruction with fixed property 'TYPE':'ConsumeNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardNames",
    "subworkflow",
    "whenNotAnnounced"
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
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    private WhenNotAnnouced whenNotAnnounced = WhenNotAnnouced.fromValue("Wait");

    /**
     * No args constructor for use in serialization
     * 
     */
    public ConsumeNotices() {
    }

    /**
     * 
     * @param subworkflow
     * @param noticeBoardNames
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

    /**
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    public WhenNotAnnouced getWhenNotAnnounced() {
        return whenNotAnnounced;
    }

    /**
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    public void setWhenNotAnnounced(WhenNotAnnouced whenNotAnnounced) {
        this.whenNotAnnounced = whenNotAnnounced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardNames", noticeBoardNames).append("subworkflow", subworkflow).append("whenNotAnnounced", whenNotAnnounced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(subworkflow).append(noticeBoardNames).append(whenNotAnnounced).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(subworkflow, rhs.subworkflow).append(noticeBoardNames, rhs.noticeBoardNames).append(whenNotAnnounced, rhs.whenNotAnnounced).isEquals();
    }

}
