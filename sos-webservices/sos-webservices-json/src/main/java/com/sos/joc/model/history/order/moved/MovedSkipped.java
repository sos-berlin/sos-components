
package com.sos.joc.model.history.order.moved;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Moved Skipped
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobName",
    "reason"
})
public class MovedSkipped {

    @JsonProperty("jobName")
    private String jobName;
    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    private MovedSkippedReason reason;

    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    public MovedSkippedReason getReason() {
        return reason;
    }

    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    public void setReason(MovedSkippedReason reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobName", jobName).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(reason).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MovedSkipped) == false) {
            return false;
        }
        MovedSkipped rhs = ((MovedSkipped) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(reason, rhs.reason).isEquals();
    }

}
