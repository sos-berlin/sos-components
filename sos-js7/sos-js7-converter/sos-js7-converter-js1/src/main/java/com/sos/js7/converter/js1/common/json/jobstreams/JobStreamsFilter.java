
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStreams Filter
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "job",
    "session",
    "folder",
    "folders",
    "jobStream",
    "jobStreams",
    "status",
    "limit",
    "auditLog"
})
public class JobStreamsFilter {

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
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String job;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    private String session;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String folder;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    @JsonProperty("jobStreams")
    private List<String> jobStreams = new ArrayList<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    private String status;
    @JsonProperty("limit")
    private Integer limit = 10000;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

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
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public String getSession() {
        return session;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
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

    @JsonProperty("jobStreams")
    public List<String> getJobStreams() {
        return jobStreams;
    }

    @JsonProperty("jobStreams")
    public void setJobStreams(List<String> jobStreams) {
        this.jobStreams = jobStreams;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("job", job).append("session", session).append("folder", folder).append("folders", folders).append("jobStream", jobStream).append("jobStreams", jobStreams).append("status", status).append("limit", limit).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(folders).append(jobStreams).append(auditLog).append(session).append(jobStream).append(limit).append(jobschedulerId).append(job).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamsFilter) == false) {
            return false;
        }
        JobStreamsFilter rhs = ((JobStreamsFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(folders, rhs.folders).append(jobStreams, rhs.jobStreams).append(auditLog, rhs.auditLog).append(session, rhs.session).append(jobStream, rhs.jobStream).append(limit, rhs.limit).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).append(status, rhs.status).isEquals();
    }

}
