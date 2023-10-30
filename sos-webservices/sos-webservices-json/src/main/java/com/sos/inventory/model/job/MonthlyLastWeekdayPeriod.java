
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * admission time MonthlyLastWeekdayPeriod
 * <p>
 * admission time period with fixed property 'TYPE':'MonthlyLastWeekdayPeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "secondOfWeeks"
})
public class MonthlyLastWeekdayPeriod
    extends AdmissionTimePeriod
{

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeeks")
    @JsonPropertyDescription("in seconds")
    private Long secondOfWeeks;

    /**
     * No args constructor for use in serialization
     * 
     */
    public MonthlyLastWeekdayPeriod() {
    }

    /**
     * 
     * @param secondOfWeeks
     */
    public MonthlyLastWeekdayPeriod(Long secondOfWeeks) {
        super();
        this.secondOfWeeks = secondOfWeeks;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeeks")
    public Long getSecondOfWeeks() {
        return secondOfWeeks;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeeks")
    public void setSecondOfWeeks(Long secondOfWeeks) {
        this.secondOfWeeks = secondOfWeeks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("secondOfWeeks", secondOfWeeks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(secondOfWeeks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonthlyLastWeekdayPeriod) == false) {
            return false;
        }
        MonthlyLastWeekdayPeriod rhs = ((MonthlyLastWeekdayPeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(secondOfWeeks, rhs.secondOfWeeks).isEquals();
    }

}
