
package com.sos.inventory.model.instruction.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Ticking
 * <p>
 * repeat with fixed property 'TYPE':'Ticking'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "interval"
})
public class Ticking
    extends Repeat
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    private Long interval;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Ticking() {
    }

    /**
     * 
     * @param interval
     */
    public Ticking(Long interval) {
        super();
        this.interval = interval;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    public Long getInterval() {
        return interval;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    public void setInterval(Long interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("interval", interval).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(interval).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Ticking) == false) {
            return false;
        }
        Ticking rhs = ((Ticking) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(interval, rhs.interval).isEquals();
    }

}
