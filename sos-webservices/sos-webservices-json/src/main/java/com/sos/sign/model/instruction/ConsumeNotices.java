
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.WhenNotAnnouced;
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
    "boardPaths",
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
    @JsonProperty("boardPaths")
    @JsonAlias({
        "noticeBoardNames"
    })
    private String boardPaths;
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
     * @param boardPaths
     * @param subworkflow
     */
    public ConsumeNotices(String boardPaths, Instructions subworkflow) {
        super();
        this.boardPaths = boardPaths;
        this.subworkflow = subworkflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPaths")
    public String getBoardPaths() {
        return boardPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPaths")
    public void setBoardPaths(String boardPaths) {
        this.boardPaths = boardPaths;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardPaths", boardPaths).append("subworkflow", subworkflow).append("whenNotAnnounced", whenNotAnnounced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(boardPaths).append(subworkflow).append(whenNotAnnounced).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardPaths, rhs.boardPaths).append(subworkflow, rhs.subworkflow).append(whenNotAnnounced, rhs.whenNotAnnounced).isEquals();
    }

}
