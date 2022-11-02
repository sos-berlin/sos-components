
package com.sos.joc.model.history.order.moved;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Retrying
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "toPosition",
    "skipped"
})
public class Moved {

    @JsonProperty("toPosition")
    private String toPosition;
    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("skipped")
    private MovedSkipped skipped;

    @JsonProperty("toPosition")
    public String getToPosition() {
        return toPosition;
    }

    @JsonProperty("toPosition")
    public void setToPosition(String toPosition) {
        this.toPosition = toPosition;
    }

    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("skipped")
    public MovedSkipped getSkipped() {
        return skipped;
    }

    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("skipped")
    public void setSkipped(MovedSkipped skipped) {
        this.skipped = skipped;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("toPosition", toPosition).append("skipped", skipped).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(toPosition).append(skipped).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Moved) == false) {
            return false;
        }
        Moved rhs = ((Moved) other);
        return new EqualsBuilder().append(toPosition, rhs.toPosition).append(skipped, rhs.skipped).isEquals();
    }

}
