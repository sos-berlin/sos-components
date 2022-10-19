
package com.sos.joc.model.history.order.caught;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Caught
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cause"
})
public class Caught {

    /**
     * CaughtCause
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cause")
    private CaughtCause cause;

    /**
     * CaughtCause
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cause")
    public CaughtCause getCause() {
        return cause;
    }

    /**
     * CaughtCause
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cause")
    public void setCause(CaughtCause cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cause", cause).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cause).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Caught) == false) {
            return false;
        }
        Caught rhs = ((Caught) other);
        return new EqualsBuilder().append(cause, rhs.cause).isEquals();
    }

}
