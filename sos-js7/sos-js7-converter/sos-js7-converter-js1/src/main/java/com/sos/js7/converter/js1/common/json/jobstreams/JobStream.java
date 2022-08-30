
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStream
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jobschedulerId",
    "jobStreamId",
    "oldJobStreamName",
    "jobStream",
    "folder",
    "state",
    "jobstreamStarters",
    "jobs",
    "auditLog"
})
public class JobStream {

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    private Long jobStreamId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldJobStreamName")
    private String oldJobStreamName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    private String folder;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private String state;
    @JsonProperty("jobstreamStarters")
    private List<JobStreamStarter> jobstreamStarters = new ArrayList<JobStreamStarter>();
    @JsonProperty("jobs")
    private List<JobStreamJob> jobs = new ArrayList<JobStreamJob>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public Long getJobStreamId() {
        return jobStreamId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public void setJobStreamId(Long jobStreamId) {
        this.jobStreamId = jobStreamId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldJobStreamName")
    public String getOldJobStreamName() {
        return oldJobStreamName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldJobStreamName")
    public void setOldJobStreamName(String oldJobStreamName) {
        this.oldJobStreamName = oldJobStreamName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public String getJobStream() {
        return jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("jobstreamStarters")
    public List<JobStreamStarter> getJobstreamStarters() {
        return jobstreamStarters;
    }

    @JsonProperty("jobstreamStarters")
    public void setJobstreamStarters(List<JobStreamStarter> jobstreamStarters) {
        this.jobstreamStarters = jobstreamStarters;
    }

    @JsonProperty("jobs")
    public List<JobStreamJob> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<JobStreamJob> jobs) {
        this.jobs = jobs;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobschedulerId", jobschedulerId).append("jobStreamId", jobStreamId).append("oldJobStreamName", oldJobStreamName).append("jobStream", jobStream).append("folder", folder).append("state", state).append("jobstreamStarters", jobstreamStarters).append("jobs", jobs).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(auditLog).append(jobs).append(jobStream).append(jobstreamStarters).append(state).append(deliveryDate).append(jobschedulerId).append(jobStreamId).append(oldJobStreamName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStream) == false) {
            return false;
        }
        JobStream rhs = ((JobStream) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(auditLog, rhs.auditLog).append(jobs, rhs.jobs).append(jobStream, rhs.jobStream).append(jobstreamStarters, rhs.jobstreamStarters).append(state, rhs.state).append(deliveryDate, rhs.deliveryDate).append(jobschedulerId, rhs.jobschedulerId).append(jobStreamId, rhs.jobStreamId).append(oldJobStreamName, rhs.oldJobStreamName).isEquals();
    }

}
