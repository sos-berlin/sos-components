
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
 * ExpectNotice
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotice'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardName"
})
public class ExpectNotice
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardName")
    @JsonAlias({
        "boardPath",
        "boardName"
    })
    private String noticeBoardName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectNotice() {
    }

    /**
     * 
     * @param noticeBoardName
     */
    public ExpectNotice(String noticeBoardName) {
        super();
        this.noticeBoardName = noticeBoardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardName")
    public String getNoticeBoardName() {
        return noticeBoardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardName")
    public void setNoticeBoardName(String noticeBoardName) {
        this.noticeBoardName = noticeBoardName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardName", noticeBoardName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardName, rhs.noticeBoardName).isEquals();
    }

}
