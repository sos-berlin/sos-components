
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job Out-Condition
 * <p>
 * job Out Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "outconditions"
})
public class JobOutCondition {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String job;
    @JsonProperty("outconditions")
    private List<OutCondition> outconditions = new ArrayList<OutCondition>();

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

    @JsonProperty("outconditions")
    public List<OutCondition> getOutconditions() {
        return outconditions;
    }

    @JsonProperty("outconditions")
    public void setOutconditions(List<OutCondition> outconditions) {
        this.outconditions = outconditions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("outconditions", outconditions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(outconditions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobOutCondition) == false) {
            return false;
        }
        JobOutCondition rhs = ((JobOutCondition) other);
        return new EqualsBuilder().append(job, rhs.job).append(outconditions, rhs.outconditions).isEquals();
    }

}
