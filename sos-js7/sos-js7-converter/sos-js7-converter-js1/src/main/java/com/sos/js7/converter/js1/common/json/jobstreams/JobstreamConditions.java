
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStream Conditions
 * <p>
 * List of all Conditions using an event
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobStream",
    "jobs"
})
public class JobstreamConditions {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    @JsonProperty("jobs")
    private List<ConditionRef> jobs = new ArrayList<ConditionRef>();

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
    public List<ConditionRef> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<ConditionRef> jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobStream", jobStream).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStream).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobstreamConditions) == false) {
            return false;
        }
        JobstreamConditions rhs = ((JobstreamConditions) other);
        return new EqualsBuilder().append(jobStream, rhs.jobStream).append(jobs, rhs.jobs).isEquals();
    }

}
