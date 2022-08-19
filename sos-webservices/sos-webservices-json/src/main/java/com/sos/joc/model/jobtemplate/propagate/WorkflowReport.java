
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate propagate Workflow report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPath",
    "state",
    "jobs"
})
public class WorkflowReport {

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String workflowPath;
    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobReportState state;
    @JsonProperty("jobs")
    private JobReports jobs;

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public JobReportState getState() {
        return state;
    }

    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(JobReportState state) {
        this.state = state;
    }

    @JsonProperty("jobs")
    public JobReports getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(JobReports jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowPath", workflowPath).append("state", state).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(workflowPath).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowReport) == false) {
            return false;
        }
        WorkflowReport rhs = ((WorkflowReport) other);
        return new EqualsBuilder().append(state, rhs.state).append(workflowPath, rhs.workflowPath).append(jobs, rhs.jobs).isEquals();
    }

}
