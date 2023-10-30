
package com.sos.joc.model.history.order.retry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Retrying
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "delayedUntil"
})
public class Retrying {

    @JsonProperty("delayedUntil")
    private String delayedUntil;

    @JsonProperty("delayedUntil")
    public String getDelayedUntil() {
        return delayedUntil;
    }

    @JsonProperty("delayedUntil")
    public void setDelayedUntil(String delayedUntil) {
        this.delayedUntil = delayedUntil;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("delayedUntil", delayedUntil).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(delayedUntil).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Retrying) == false) {
            return false;
        }
        Retrying rhs = ((Retrying) other);
        return new EqualsBuilder().append(delayedUntil, rhs.delayedUntil).isEquals();
    }

}
