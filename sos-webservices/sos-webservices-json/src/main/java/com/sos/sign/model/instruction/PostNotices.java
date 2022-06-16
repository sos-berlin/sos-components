
package com.sos.sign.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * PostNotices
 * <p>
 * instruction with fixed property 'TYPE':'PostNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "boardPaths"
})
public class PostNotices
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
    private List<String> boardPaths = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostNotices() {
    }

    /**
     * 
     * @param boardPaths
     * @param tYPE
     */
    public PostNotices(List<String> boardPaths, InstructionType tYPE) {
        super(tYPE);
        this.boardPaths = boardPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPaths")
    public List<String> getBoardPaths() {
        return boardPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPaths")
    public void setBoardPaths(List<String> boardPaths) {
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
        if ((other instanceof PostNotices) == false) {
            return false;
        }
        PostNotices rhs = ((PostNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardPaths, rhs.boardPaths).isEquals();
    }

}
