
package com.sos.joc.model.history.order.moved;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Moved
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "to",
    "skipped"
})
public class Moved {

    /**
     * Mover To
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("to")
    private MovedTo to;
    /**
     * Moved Skipped
     * <p>
     * 
     * 
     */
    @JsonProperty("skipped")
    private MovedSkipped skipped;

    /**
     * Mover To
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("to")
    public MovedTo getTo() {
        return to;
    }

    /**
     * Mover To
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("to")
    public void setTo(MovedTo to) {
        this.to = to;
    }

    /**
     * Moved Skipped
     * <p>
     * 
     * 
     */
    @JsonProperty("skipped")
    public MovedSkipped getSkipped() {
        return skipped;
    }

    /**
     * Moved Skipped
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
        return new ToStringBuilder(this).append("to", to).append("skipped", skipped).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(to).append(skipped).toHashCode();
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
        return new EqualsBuilder().append(to, rhs.to).append(skipped, rhs.skipped).isEquals();
    }

}
