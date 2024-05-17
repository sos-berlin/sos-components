
package com.sos.joc.model.reporting.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report result data item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobName",
    "workflowName",
    "startTime",
    "endTime",
    "orderState",
    "state",
    "duration"
})
public class ReportResultDataItem {

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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endTime;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orderState")
    private Long orderState;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private Long state;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("duration")
    private Long duration;

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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public Date getEndTime() {
        return endTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orderState")
    public Long getOrderState() {
        return orderState;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("orderState")
    public void setOrderState(Long orderState) {
        this.orderState = orderState;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public Long getState() {
        return state;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(Long state) {
        this.state = state;
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
        return new ToStringBuilder(this).append("jobName", jobName).append("workflowName", workflowName).append("startTime", startTime).append("endTime", endTime).append("orderState", orderState).append("state", state).append("duration", duration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(duration).append(workflowName).append(startTime).append(endTime).append(state).append(orderState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportResultDataItem) == false) {
            return false;
        }
        ReportResultDataItem rhs = ((ReportResultDataItem) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(duration, rhs.duration).append(workflowName, rhs.workflowName).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(state, rhs.state).append(orderState, rhs.orderState).isEquals();
    }

}
