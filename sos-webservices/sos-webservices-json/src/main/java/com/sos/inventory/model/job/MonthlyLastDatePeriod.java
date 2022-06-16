
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time MonthlyLastDatePeriod
 * <p>
 * admission time period with fixed property 'TYPE':'MonthlyLastDatePeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "lastSecondOfMonth"
})
public class MonthlyLastDatePeriod
    extends AdmissionTimePeriod
{

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("lastSecondOfMonth")
    @JsonPropertyDescription("in seconds")
    private Long lastSecondOfMonth;

    /**
     * No args constructor for use in serialization
     * 
     */
    public MonthlyLastDatePeriod() {
    }

    /**
     * 
     * @param lastSecondOfMonth
     */
    public MonthlyLastDatePeriod(Long lastSecondOfMonth) {
        super();
        this.lastSecondOfMonth = lastSecondOfMonth;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("lastSecondOfMonth")
    public Long getLastSecondOfMonth() {
        return lastSecondOfMonth;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("lastSecondOfMonth")
    public void setLastSecondOfMonth(Long lastSecondOfMonth) {
        this.lastSecondOfMonth = lastSecondOfMonth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lastSecondOfMonth", lastSecondOfMonth).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(lastSecondOfMonth).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonthlyLastDatePeriod) == false) {
            return false;
        }
        MonthlyLastDatePeriod rhs = ((MonthlyLastDatePeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(lastSecondOfMonth, rhs.lastSecondOfMonth).isEquals();
    }

}
