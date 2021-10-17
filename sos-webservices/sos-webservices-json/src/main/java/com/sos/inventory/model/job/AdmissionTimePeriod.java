
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time period
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "secondOfWeek",
    "duration"
})
public class AdmissionTimePeriod {

    /**
     * only yet 'WeekdayPeriod'
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("only yet 'WeekdayPeriod'")
    private String tYPE;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("secondOfWeek")
    private Long secondOfWeek;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
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
     * @param duration
     * @param tYPE
     * @param secondOfWeek
     */
    public AdmissionTimePeriod(String tYPE, Long secondOfWeek, Long duration) {
        super();
        this.tYPE = tYPE;
        this.secondOfWeek = secondOfWeek;
        this.duration = duration;
    }

    /**
     * only yet 'WeekdayPeriod'
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * only yet 'WeekdayPeriod'
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * non negative long
     * <p>
     * 
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
     * 
     */
    @JsonProperty("secondOfWeek")
    public void setSecondOfWeek(Long secondOfWeek) {
        this.secondOfWeek = secondOfWeek;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    public Long getDuration() {
        return duration;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("secondOfWeek", secondOfWeek).append("duration", duration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(duration).append(tYPE).append(secondOfWeek).toHashCode();
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
        return new EqualsBuilder().append(duration, rhs.duration).append(tYPE, rhs.tYPE).append(secondOfWeek, rhs.secondOfWeek).isEquals();
    }

}
