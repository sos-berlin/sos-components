
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time WeekdayPeriod
 * <p>
 * admission time period with fixed property 'TYPE':'WeekdayPeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "secondOfWeek"
})
public class WeekdayPeriod
    extends AdmissionTimePeriod
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeek")
    private Long secondOfWeek;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WeekdayPeriod() {
    }

    /**
     * 
     * @param secondOfWeek
     */
    public WeekdayPeriod(Long secondOfWeek) {
        super();
        this.secondOfWeek = secondOfWeek;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeek")
    public Long getSecondOfWeek() {
        return secondOfWeek;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("secondOfWeek")
    public void setSecondOfWeek(Long secondOfWeek) {
        this.secondOfWeek = secondOfWeek;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("secondOfWeek", secondOfWeek).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(secondOfWeek).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WeekdayPeriod) == false) {
            return false;
        }
        WeekdayPeriod rhs = ((WeekdayPeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(secondOfWeek, rhs.secondOfWeek).isEquals();
    }

}
