
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
 * jobStreams
 * <p>
 * List of all jobStreams and their folders
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jobstreams"
})
public class ImportJobStreams {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobstreams")
    private List<JobStream> jobstreams = new ArrayList<JobStream>();

    /**
     * filename
     * <p>
     * 
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
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("jobstreams")
    public List<JobStream> getJobstreams() {
        return jobstreams;
    }

    @JsonProperty("jobstreams")
    public void setJobstreams(List<JobStream> jobstreams) {
        this.jobstreams = jobstreams;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jobstreams", jobstreams).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(jobstreams).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ImportJobStreams) == false) {
            return false;
        }
        ImportJobStreams rhs = ((ImportJobStreams) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(jobstreams, rhs.jobstreams).isEquals();
    }

}
