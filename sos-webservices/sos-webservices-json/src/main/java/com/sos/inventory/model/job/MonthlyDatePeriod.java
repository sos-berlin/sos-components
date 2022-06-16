
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time MonthlyDatePeriod
 * <p>
 * admission time period with fixed property 'TYPE':'MonthlyDatePeriod'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "secondOfMonth"
})
public class MonthlyDatePeriod
    extends AdmissionTimePeriod
{

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfMonth")
    @JsonPropertyDescription("in seconds")
    private Long secondOfMonth;

    /**
     * No args constructor for use in serialization
     * 
     */
    public MonthlyDatePeriod() {
    }

    /**
     * 
     * @param secondOfMonth
     */
    public MonthlyDatePeriod(Long secondOfMonth) {
        super();
        this.secondOfMonth = secondOfMonth;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfMonth")
    public Long getSecondOfMonth() {
        return secondOfMonth;
    }

    /**
     * in seconds
     * (Required)
     * 
     */
    @JsonProperty("secondOfMonth")
    public void setSecondOfMonth(Long secondOfMonth) {
        this.secondOfMonth = secondOfMonth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("secondOfMonth", secondOfMonth).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(secondOfMonth).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonthlyDatePeriod) == false) {
            return false;
        }
        MonthlyDatePeriod rhs = ((MonthlyDatePeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(secondOfMonth, rhs.secondOfMonth).isEquals();
    }

}
