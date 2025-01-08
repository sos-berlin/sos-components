
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
 * ExpectNotices
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "boardPaths",
    "whenNotAnnounced"
})
public class ExpectNotices
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
    public ExpectNotices() {
    }

    /**
     * 
     * @param boardPaths
     */
    public ExpectNotices(String boardPaths) {
        super();
        this.boardPaths = boardPaths;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardPaths", boardPaths).append("whenNotAnnounced", whenNotAnnounced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(whenNotAnnounced).append(boardPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectNotices) == false) {
            return false;
        }
        ExpectNotices rhs = ((ExpectNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(whenNotAnnounced, rhs.whenNotAnnounced).append(boardPaths, rhs.boardPaths).isEquals();
    }

}
