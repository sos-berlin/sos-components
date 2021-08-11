
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Post Notice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "endOfLife"
})
public class PostNotice
    extends DeleteNotice
{

    @JsonProperty("endOfLife")
    private String endOfLife;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostNotice() {
    }

    /**
     * 
     * @param boardPath
     * @param endOfLife
     * @param noticeId
     */
    public PostNotice(String endOfLife, String boardPath, String noticeId) {
        super(boardPath, noticeId);
        this.endOfLife = endOfLife;
    }

    @JsonProperty("endOfLife")
    public String getEndOfLife() {
        return endOfLife;
    }

    @JsonProperty("endOfLife")
    public void setEndOfLife(String endOfLife) {
        this.endOfLife = endOfLife;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("endOfLife", endOfLife).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(endOfLife).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
