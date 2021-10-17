
package com.sos.inventory.model.instruction.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Periodic
 * <p>
 * repeat with fixed property 'TYPE':'Periodic'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "period",
    "offsets"
})
public class Periodic
    extends Repeat
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("period")
    private Long period;
    /**
     * in seconds
     * 
     */
    @JsonProperty("offsets")
    @JsonPropertyDescription("in seconds")
    private List<Long> offsets = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Periodic() {
    }

    /**
     * 
     * @param period
     * @param offsets
     */
    public Periodic(Long period, List<Long> offsets) {
        super();
        this.period = period;
        this.offsets = offsets;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("period")
    public Long getPeriod() {
        return period;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("period")
    public void setPeriod(Long period) {
        this.period = period;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("offsets")
    public List<Long> getOffsets() {
        return offsets;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("offsets")
    public void setOffsets(List<Long> offsets) {
        this.offsets = offsets;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("period", period).append("offsets", offsets).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(offsets).append(period).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Periodic) == false) {
            return false;
        }
        Periodic rhs = ((Periodic) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(offsets, rhs.offsets).append(period, rhs.period).isEquals();
    }

}
