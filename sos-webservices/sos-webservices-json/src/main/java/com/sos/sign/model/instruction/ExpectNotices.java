
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExpectNotices
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "boardPaths"
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
     * No args constructor for use in serialization
     * 
     */
    public ExpectNotices() {
    }

    /**
     * 
     * @param boardPaths
     * @param tYPE
     */
    public ExpectNotices(String boardPaths, InstructionType tYPE) {
        super(tYPE);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardPaths", boardPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(boardPaths).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardPaths, rhs.boardPaths).isEquals();
    }

}
