
package com.sos.jitl.jobs.sap.common.bean;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * create job
 * <p>
 * e.g. POST /scheduler/jobs
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "schedules"
})
public class Job
    extends AbstractJob
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedules")
    private List<Schedule> schedules = new ArrayList<Schedule>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedules")
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedules")
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public Job withSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
        return this;
    }

    @Override
    public Job withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public Job withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public Job withAction(String action) {
        super.withAction(action);
        return this;
    }

    @Override
    public Job withActive(Boolean active) {
        super.withActive(active);
        return this;
    }

    @Override
    public Job withHttpMethod(AbstractJob.HttpMethod httpMethod) {
        super.withHttpMethod(httpMethod);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("schedules", schedules).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(schedules).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Job) == false) {
            return false;
        }
        Job rhs = ((Job) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(schedules, rhs.schedules).isEquals();
    }

}
