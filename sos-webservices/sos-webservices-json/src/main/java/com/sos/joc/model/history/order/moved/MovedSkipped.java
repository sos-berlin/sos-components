
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
    "job",
    "reason"
})
public class MovedSkipped {

    @JsonProperty("job")
    private String job;
    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    private MovedSkippedReason reason;

    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
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
        return new ToStringBuilder(this).append("job", job).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(reason).toHashCode();
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
        return new EqualsBuilder().append(job, rhs.job).append(reason, rhs.reason).isEquals();
    }

}
