
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

    @JsonProperty("TYPE")
    private String tYPE;
    /**
     * in seconds
     * 
     */
    @JsonProperty("secondOfWeek")
    @JsonPropertyDescription("in seconds")
    private Integer secondOfWeek;
    /**
     * in seconds
     * 
     */
    @JsonProperty("duration")
    @JsonPropertyDescription("in seconds")
    private Integer duration;

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
    public AdmissionTimePeriod(String tYPE, Integer secondOfWeek, Integer duration) {
        super();
        this.tYPE = tYPE;
        this.secondOfWeek = secondOfWeek;
        this.duration = duration;
    }

    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("secondOfWeek")
    public Integer getSecondOfWeek() {
        return secondOfWeek;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("secondOfWeek")
    public void setSecondOfWeek(Integer secondOfWeek) {
        this.secondOfWeek = secondOfWeek;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("duration")
    public void setDuration(Integer duration) {
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
