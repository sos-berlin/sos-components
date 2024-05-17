
package com.sos.joc.model.reporting.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report result data
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentName",
    "jobName",
    "workflowName",
    "count",
    "startTime",
    "duration",
    "data"
})
public class ReportResultData {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    private String workflowName;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    private Long count;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startTime;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    private Long duration;
    @JsonProperty("data")
    private List<ReportResultDataItem> data = new ArrayList<ReportResultDataItem>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public Long getCount() {
        return count;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public void setCount(Long count) {
        this.count = count;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    public Date getStartTime() {
        return startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
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

    @JsonProperty("data")
    public List<ReportResultDataItem> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<ReportResultDataItem> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("jobName", jobName).append("workflowName", workflowName).append("count", count).append("startTime", startTime).append("duration", duration).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(duration).append(data).append(count).append(agentName).append(workflowName).append(startTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportResultData) == false) {
            return false;
        }
        ReportResultData rhs = ((ReportResultData) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(duration, rhs.duration).append(data, rhs.data).append(count, rhs.count).append(agentName, rhs.agentName).append(workflowName, rhs.workflowName).append(startTime, rhs.startTime).isEquals();
    }

}
