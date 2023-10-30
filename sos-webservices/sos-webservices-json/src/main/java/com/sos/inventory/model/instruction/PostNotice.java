
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PostNotice
 * <p>
 * instruction with fixed property 'TYPE':'PostNotice'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardName"
})
public class PostNotice
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
    public PostNotice() {
    }

    /**
     * 
     * @param noticeBoardName
     */
    public PostNotice(String noticeBoardName) {
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
        if ((other instanceof PostNotice) == false) {
            return false;
        }
        PostNotice rhs = ((PostNotice) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardName, rhs.noticeBoardName).isEquals();
    }

}
