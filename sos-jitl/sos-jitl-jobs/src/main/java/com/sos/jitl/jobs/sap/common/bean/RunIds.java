
package com.sos.jitl.jobs.sap.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ids
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobId",
    "scheduleId",
    "runId"
})
public class RunIds {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    private Long jobId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    private String scheduleId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    private String runId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RunIds() {
    }

    /**
     * 
     * @param jobId
     * @param runId
     * @param scheduleId
     */
    public RunIds(Long jobId, String scheduleId, String runId) {
        super();
        this.jobId = jobId;
        this.scheduleId = scheduleId;
        this.runId = runId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public Long getJobId() {
        return jobId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobId")
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public RunIds withJobId(Long jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scheduleId")
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public RunIds withScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public String getRunId() {
        return runId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public void setRunId(String runId) {
        this.runId = runId;
    }

    public RunIds withRunId(String runId) {
        this.runId = runId;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobId", jobId).append("scheduleId", scheduleId).append("runId", runId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobId).append(runId).append(scheduleId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunIds) == false) {
            return false;
        }
        RunIds rhs = ((RunIds) other);
        return new EqualsBuilder().append(jobId, rhs.jobId).append(runId, rhs.runId).append(scheduleId, rhs.scheduleId).isEquals();
    }

}
