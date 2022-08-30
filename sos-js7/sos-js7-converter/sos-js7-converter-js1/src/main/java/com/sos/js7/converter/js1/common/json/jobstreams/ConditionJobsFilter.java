
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.job.JobPath;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * condition Jobs Filter
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "folder",
    "jobStream",
    "jobs",
    "compact",
    "expression",
    "session"
})
public class ConditionJobsFilter {

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
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String folder;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    @JsonProperty("jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("compact")
    private Boolean compact;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    private String expression;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    private String session;

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

    @JsonProperty("jobs")
    public List<JobPath> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<JobPath> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    public String getExpression() {
        return expression;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("expression")
    public void setExpression(String expression) {
        this.expression = expression;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("folder", folder).append("jobStream", jobStream).append("jobs", jobs).append("compact", compact).append("expression", expression).append("session", session).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(expression).append(compact).append(session).append(jobs).append(jobStream).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConditionJobsFilter) == false) {
            return false;
        }
        ConditionJobsFilter rhs = ((ConditionJobsFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(expression, rhs.expression).append(compact, rhs.compact).append(session, rhs.session).append(jobs, rhs.jobs).append(jobStream, rhs.jobStream).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
