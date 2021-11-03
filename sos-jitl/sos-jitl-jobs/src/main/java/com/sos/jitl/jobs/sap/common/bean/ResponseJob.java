
package com.sos.jitl.jobs.sap.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * retrieve job
 * <p>
 * e.g. Response 201 of POST /scheduler/jobs
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobId",
    "user",
    "schedules"
})
public class ResponseJob
    extends AbstractJob
{

    @JsonProperty("jobId")
    @JsonAlias({
        "_id"
    })
    private Long jobId;
    @JsonProperty("schedules")
    private List<ResponseSchedule> schedules = new ArrayList<ResponseSchedule>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("jobId")
    public Long getJobId() {
        return jobId;
    }

    @JsonProperty("jobId")
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public ResponseJob withJobId(Long jobId) {
        this.jobId = jobId;
        return this;
    }

    @JsonProperty("schedules")
    public List<ResponseSchedule> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(List<ResponseSchedule> schedules) {
        this.schedules = schedules;
    }

    public ResponseJob withSchedules(List<ResponseSchedule> schedules) {
        this.schedules = schedules;
        return this;
    }

    @Override
    public ResponseJob withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public ResponseJob withAction(String action) {
        super.withAction(action);
        return this;
    }

    @Override
    public ResponseJob withActive(Boolean active) {
        super.withActive(active);
        return this;
    }

    @Override
    public ResponseJob withHttpMethod(AbstractJob.HttpMethod httpMethod) {
        super.withHttpMethod(httpMethod);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ResponseJob withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobId", jobId).append("schedules", schedules).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobId).append(additionalProperties).append(schedules).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseJob) == false) {
            return false;
        }
        ResponseJob rhs = ((ResponseJob) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobId, rhs.jobId).append(additionalProperties, rhs.additionalProperties).append(schedules, rhs.schedules).isEquals();
    }

}
