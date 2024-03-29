
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ExpectNotice
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotice'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "boardPath"
})
public class ExpectNotice
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    @JsonAlias({
        "boardName",
        "noticeBoardName"
    })
    private String boardPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectNotice() {
    }

    /**
     * 
     * @param boardPath
     * @param tYPE
     */
    public ExpectNotice(String boardPath, InstructionType tYPE) {
        super(tYPE);
        this.boardPath = boardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    public String getBoardPath() {
        return boardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    public void setBoardPath(String boardPath) {
        this.boardPath = boardPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardPath", boardPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(boardPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectNotice) == false) {
            return false;
        }
        ExpectNotice rhs = ((ExpectNotice) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardPath, rhs.boardPath).isEquals();
    }

}
