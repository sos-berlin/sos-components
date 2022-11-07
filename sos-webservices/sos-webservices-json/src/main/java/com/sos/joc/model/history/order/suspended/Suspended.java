
package com.sos.joc.model.history.order.suspended;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Suspended (stopped)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job"
})
public class Suspended {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    private String job;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Suspended) == false) {
            return false;
        }
        Suspended rhs = ((Suspended) other);
        return new EqualsBuilder().append(job, rhs.job).isEquals();
    }

}
