
package com.sos.jitl.jobs.sap.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * schedule
 * <p>
 * e.g. POST /scheduler/jobs/{jobId}/schedules
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "active",
    "data",
    "time"
})
public class Schedule {

    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    private Boolean active = false;
    /**
     * schedule data
     * <p>
     * 
     * 
     */
    @JsonProperty("data")
    private ScheduleData data;
    @JsonProperty("time")
    private String time = "now";

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Schedule withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Schedule withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * schedule data
     * <p>
     * 
     * 
     */
    @JsonProperty("data")
    public ScheduleData getData() {
        return data;
    }

    /**
     * schedule data
     * <p>
     * 
     * 
     */
    @JsonProperty("data")
    public void setData(ScheduleData data) {
        this.data = data;
    }

    public Schedule withData(ScheduleData data) {
        this.data = data;
        return this;
    }

    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    public Schedule withTime(String time) {
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("description", description).append("active", active).append("data", data).append("time", time).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(description).append(active).append(time).append(data).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Schedule) == false) {
            return false;
        }
        Schedule rhs = ((Schedule) other);
        return new EqualsBuilder().append(description, rhs.description).append(active, rhs.active).append(time, rhs.time).append(data, rhs.data).isEquals();
    }

}
