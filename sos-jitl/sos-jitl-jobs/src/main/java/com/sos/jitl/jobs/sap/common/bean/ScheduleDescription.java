
package com.sos.jitl.jobs.sap.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ScheduleDescription
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowName",
    "jobLabel",
    "orderId",
    "created"
})
public class ScheduleDescription {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    private String workflowName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobLabel")
    private String jobLabel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created")
    private Long created;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ScheduleDescription() {
    }

    /**
     * 
     * @param jobLabel
     * @param orderId
     * @param created
     * @param workflowName
     */
    public ScheduleDescription(String workflowName, String jobLabel, String orderId, Long created) {
        super();
        this.workflowName = workflowName;
        this.jobLabel = jobLabel;
        this.orderId = orderId;
        this.created = created;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public ScheduleDescription withWorkflowName(String workflowName) {
        this.workflowName = workflowName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobLabel")
    public String getJobLabel() {
        return jobLabel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobLabel")
    public void setJobLabel(String jobLabel) {
        this.jobLabel = jobLabel;
    }

    public ScheduleDescription withJobLabel(String jobLabel) {
        this.jobLabel = jobLabel;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ScheduleDescription withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created")
    public Long getCreated() {
        return created;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("created")
    public void setCreated(Long created) {
        this.created = created;
    }

    public ScheduleDescription withCreated(Long created) {
        this.created = created;
        return this;
    }
    
    public String string() {
        List<String> s = new ArrayList<>(4);
        if (workflowName != null) {
            s.add("workflowName=" + workflowName);
        }
        if (jobLabel != null) {
            s.add("jobLabel=" + jobLabel);
        }
        if (orderId != null) {
            s.add("orderId=" + orderId);
        }
        if (created != null) {
            s.add("created=" + Instant.ofEpochMilli(created).toString());
        }
        return String.join(", ", s);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowName", workflowName).append("jobLabel", jobLabel).append("orderId", orderId).append("created", created).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobLabel).append(workflowName).append(orderId).append(created).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleDescription) == false) {
            return false;
        }
        ScheduleDescription rhs = ((ScheduleDescription) other);
        return new EqualsBuilder().append(jobLabel, rhs.jobLabel).append(workflowName, rhs.workflowName).append(orderId, rhs.orderId).append(created, rhs.created).isEquals();
    }

}
