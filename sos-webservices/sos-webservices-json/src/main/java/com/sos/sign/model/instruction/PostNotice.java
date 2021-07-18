
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
 * PostNotice
 * <p>
 * instruction with fixed property 'TYPE':'PostNotice'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "boardPath"
})
public class PostNotice
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    @JsonAlias({
        "boardName"
    })
    private String boardPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostNotice() {
    }

    /**
     * 
     * @param boardPath
     * @param tYPE
     */
    public PostNotice(String boardPath, InstructionType tYPE) {
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
        if ((other instanceof PostNotice) == false) {
            return false;
        }
        PostNotice rhs = ((PostNotice) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardPath, rhs.boardPath).isEquals();
    }

}
