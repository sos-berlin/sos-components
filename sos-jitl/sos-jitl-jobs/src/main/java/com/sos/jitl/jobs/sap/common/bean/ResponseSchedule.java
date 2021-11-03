
package com.sos.jitl.jobs.sap.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * retrieve schedule
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobId",
    "scheduleId",
    "type",
    "nextRunAt",
    "logs"
})
public class ResponseSchedule
    extends Schedule
{

    @JsonProperty("jobId")
    private Integer jobId;
    @JsonProperty("scheduleId")
    private String scheduleId;
    /**
     * e.g. recurring, what other types exist?
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("e.g. recurring, what other types exist?")
    private String type;
    @JsonProperty("nextRunAt")
    private String nextRunAt;
    @JsonProperty("logs")
    private List<ScheduleLog> logs = new ArrayList<ScheduleLog>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("jobId")
    public Integer getJobId() {
        return jobId;
    }

    @JsonProperty("jobId")
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public ResponseSchedule withJobId(Integer jobId) {
        this.jobId = jobId;
        return this;
    }

    @JsonProperty("scheduleId")
    public String getScheduleId() {
        return scheduleId;
    }

    @JsonProperty("scheduleId")
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ResponseSchedule withScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    /**
     * e.g. recurring, what other types exist?
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * e.g. recurring, what other types exist?
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public ResponseSchedule withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("nextRunAt")
    public String getNextRunAt() {
        return nextRunAt;
    }

    @JsonProperty("nextRunAt")
    public void setNextRunAt(String nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public ResponseSchedule withNextRunAt(String nextRunAt) {
        this.nextRunAt = nextRunAt;
        return this;
    }

    @JsonProperty("logs")
    public List<ScheduleLog> getLogs() {
        return logs;
    }

    @JsonProperty("logs")
    public void setLogs(List<ScheduleLog> logs) {
        this.logs = logs;
    }

    public ResponseSchedule withLogs(List<ScheduleLog> logs) {
        this.logs = logs;
        return this;
    }

    @Override
    public ResponseSchedule withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public ResponseSchedule withActive(Boolean active) {
        super.withActive(active);
        return this;
    }

    @Override
    public ResponseSchedule withData(ScheduleData data) {
        super.withData(data);
        return this;
    }

    @Override
    public ResponseSchedule withTime(String time) {
        super.withTime(time);
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

    public ResponseSchedule withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobId", jobId).append("scheduleId", scheduleId).append("type", type).append("nextRunAt", nextRunAt).append("logs", logs).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobId).append(additionalProperties).append(type).append(nextRunAt).append(logs).append(scheduleId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseSchedule) == false) {
            return false;
        }
        ResponseSchedule rhs = ((ResponseSchedule) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobId, rhs.jobId).append(additionalProperties, rhs.additionalProperties).append(type, rhs.type).append(nextRunAt, rhs.nextRunAt).append(logs, rhs.logs).append(scheduleId, rhs.scheduleId).isEquals();
    }

}
