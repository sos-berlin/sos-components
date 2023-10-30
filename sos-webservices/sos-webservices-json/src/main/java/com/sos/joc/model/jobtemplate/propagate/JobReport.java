
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JobTemplate propagate Job report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobTemplatePath",
    "state",
    "actions"
})
public class JobReport {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplatePath")
    private String jobTemplatePath;
    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobReportState state;
    @JsonProperty("actions")
    private Actions actions;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplatePath")
    public String getJobTemplatePath() {
        return jobTemplatePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplatePath")
    public void setJobTemplatePath(String jobTemplatePath) {
        this.jobTemplatePath = jobTemplatePath;
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

    @JsonProperty("actions")
    public Actions getActions() {
        return actions;
    }

    @JsonProperty("actions")
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobTemplatePath", jobTemplatePath).append("state", state).append("actions", actions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(actions).append(jobTemplatePath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReport) == false) {
            return false;
        }
        JobReport rhs = ((JobReport) other);
        return new EqualsBuilder().append(state, rhs.state).append(actions, rhs.actions).append(jobTemplatePath, rhs.jobTemplatePath).isEquals();
    }

}
