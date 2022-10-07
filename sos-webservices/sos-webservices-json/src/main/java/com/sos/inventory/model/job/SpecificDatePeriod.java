
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time SpecificDatePeriod
 * <p>
 * admission time period with fixed property 'TYPE':'SpecificDatePeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "secondsSinceLocalEpoch"
})
public class SpecificDatePeriod
    extends AdmissionTimePeriod
{

    /**
     * epoch seconds
     * (Required)
     * 
     */
    @JsonProperty("secondsSinceLocalEpoch")
    @JsonPropertyDescription("epoch seconds")
    private Long secondsSinceLocalEpoch;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SpecificDatePeriod() {
    }

    /**
     * 
     * @param duration
     * @param secondsSinceLocalEpoch
     * @param tYPE
     */
    public SpecificDatePeriod(Long secondsSinceLocalEpoch, AdmissionTimePeriodType tYPE, Long duration) {
        super(tYPE, duration);
        this.secondsSinceLocalEpoch = secondsSinceLocalEpoch;
    }

    /**
     * epoch seconds
     * (Required)
     * 
     */
    @JsonProperty("secondsSinceLocalEpoch")
    public Long getSecondsSinceLocalEpoch() {
        return secondsSinceLocalEpoch;
    }

    /**
     * epoch seconds
     * (Required)
     * 
     */
    @JsonProperty("secondsSinceLocalEpoch")
    public void setSecondsSinceLocalEpoch(Long secondsSinceLocalEpoch) {
        this.secondsSinceLocalEpoch = secondsSinceLocalEpoch;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("secondsSinceLocalEpoch", secondsSinceLocalEpoch).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(secondsSinceLocalEpoch).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SpecificDatePeriod) == false) {
            return false;
        }
        SpecificDatePeriod rhs = ((SpecificDatePeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(secondsSinceLocalEpoch, rhs.secondsSinceLocalEpoch).isEquals();
    }

}
