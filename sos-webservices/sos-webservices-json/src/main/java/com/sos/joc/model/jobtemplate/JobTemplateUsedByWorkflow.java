
package com.sos.joc.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "path",
    "deployed",
    "state",
    "jobs"
})
public class JobTemplateUsedByWorkflow {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    @JsonProperty("deployed")
    private Boolean deployed = false;
    /**
     * JobTemplate state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobTemplateWorkflowState state;
    @JsonProperty("jobs")
    private JobTemplateUsedByJobs jobs;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * JobTemplate state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public JobTemplateWorkflowState getState() {
        return state;
    }

    /**
     * JobTemplate state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(JobTemplateWorkflowState state) {
        this.state = state;
    }

    @JsonProperty("jobs")
    public JobTemplateUsedByJobs getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(JobTemplateUsedByJobs jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("path", path).append("deployed", deployed).append("state", state).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(path).append(deployed).append(state).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplateUsedByWorkflow) == false) {
            return false;
        }
        JobTemplateUsedByWorkflow rhs = ((JobTemplateUsedByWorkflow) other);
        return new EqualsBuilder().append(name, rhs.name).append(path, rhs.path).append(deployed, rhs.deployed).append(state, rhs.state).append(jobs, rhs.jobs).isEquals();
    }

}
