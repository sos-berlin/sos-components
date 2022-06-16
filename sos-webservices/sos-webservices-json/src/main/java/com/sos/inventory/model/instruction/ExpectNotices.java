
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
 * ExpectNotices
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardNames"
})
public class ExpectNotices
    extends Instruction
{

    @JsonProperty("noticeBoardNames")
    @JsonAlias({
        "boardPaths"
    })
    private String noticeBoardNames;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectNotices() {
    }

    /**
     * 
     * @param noticeBoardNames
     * @param tYPE
     */
    public ExpectNotices(String noticeBoardNames, InstructionType tYPE) {
        super(tYPE);
        this.noticeBoardNames = noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public String getNoticeBoardNames() {
        return noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(String noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardNames", noticeBoardNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardNames).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardNames, rhs.noticeBoardNames).isEquals();
    }

}
