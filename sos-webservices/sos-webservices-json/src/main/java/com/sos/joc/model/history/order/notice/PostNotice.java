
package com.sos.joc.model.history.order.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PostNotice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "endOfLife"
})
public class PostNotice
    extends BaseNotice
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    private String endOfLife;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    public String getEndOfLife() {
        return endOfLife;
    }

    /**
     * 
     * (Required)
     * 
     */
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
