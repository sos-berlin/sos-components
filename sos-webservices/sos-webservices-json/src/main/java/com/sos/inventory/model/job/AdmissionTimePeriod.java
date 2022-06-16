
package com.sos.inventory.model.job;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.inventory.model.common.ClassHelper;


/**
 * admissionTimePeriod
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "duration"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
@JsonSubTypes({ 
        @JsonSubTypes.Type(value = MonthlyDatePeriod.class, name = "MonthlyDatePeriod"),
        @JsonSubTypes.Type(value = MonthlyWeekdayPeriod.class, name = "MonthlyWeekdayPeriod"),
        @JsonSubTypes.Type(value = MonthlyLastDatePeriod.class, name = "MonthlyLastDatePeriod"),
        @JsonSubTypes.Type(value = MonthlyLastWeekdayPeriod.class, name = "MonthlyLastWeekdayPeriod"),
        @JsonSubTypes.Type(value = WeekdayPeriod.class, name = "WeekdayPeriod"),
		@JsonSubTypes.Type(value = DailyPeriod.class, name = "DailyPeriod")})
public abstract class AdmissionTimePeriod
    extends ClassHelper
{

    /**
     * admissionTimePeriodType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    private AdmissionTimePeriodType tYPE;
    
    @JsonProperty("duration")
    private Long duration;
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AdmissionTimePeriod() {
    }

    /**
     * 
     * @param tYPE
     */
    public AdmissionTimePeriod(AdmissionTimePeriodType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    /**
     * admissionTimePeriodType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    public AdmissionTimePeriodType getTYPE() {
        return tYPE;
    }

    /**
     * admissionTimePeriodType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(AdmissionTimePeriodType tYPE) {
        this.tYPE = tYPE;
    }
    
    @JsonProperty("duration")
    public Long getDuration() {
        return duration;
    }
    
    @JsonProperty("duration")
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).append("duration", duration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).append(duration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdmissionTimePeriod) == false) {
            return false;
        }
        AdmissionTimePeriod rhs = ((AdmissionTimePeriod) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).append(duration, rhs.duration).isEquals();
    }

}
