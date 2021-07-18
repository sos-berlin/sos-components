
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "boardName"
})
public class PostNotice
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    @JsonAlias({
        "boardPath"
    })
    private String boardName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostNotice() {
    }

    /**
     * 
     * @param position
     * 
     * @param boardName
     * @param positionString
     */
    public PostNotice(String boardName, List<Object> position, String positionString) {
        super(, position, positionString);
        this.boardName = boardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    public String getBoardName() {
        return boardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardName", boardName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(boardName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(boardName, rhs.boardName).isEquals();
    }

}
