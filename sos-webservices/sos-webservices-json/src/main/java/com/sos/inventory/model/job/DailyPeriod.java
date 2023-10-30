
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * admission time DailyPeriod
 * <p>
 * admission time period with fixed property 'TYPE':'DailyPeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "secondOfDay"
})
public class DailyPeriod
    extends AdmissionTimePeriod
{

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfDay")
    @JsonPropertyDescription("in seconds")
    private Long secondOfDay;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DailyPeriod() {
    }

    /**
     * 
     * @param secondOfDay
     */
    public DailyPeriod(Long secondOfDay) {
        super();
        this.secondOfDay = secondOfDay;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfDay")
    public Long getSecondOfDay() {
        return secondOfDay;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfDay")
    public void setSecondOfDay(Long secondOfDay) {
        this.secondOfDay = secondOfDay;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("secondOfDay", secondOfDay).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(secondOfDay).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPeriod) == false) {
            return false;
        }
        DailyPeriod rhs = ((DailyPeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(secondOfDay, rhs.secondOfDay).isEquals();
    }

}
