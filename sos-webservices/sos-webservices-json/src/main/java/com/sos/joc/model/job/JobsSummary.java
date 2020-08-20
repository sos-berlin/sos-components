
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pending",
    "running"
})
public class JobsSummary {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    private Integer pending;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("running")
    private Integer running;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    public Integer getPending() {
        return pending;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    public void setPending(Integer pending) {
        this.pending = pending;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("running")
    public Integer getRunning() {
        return running;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("running")
    public void setRunning(Integer running) {
        this.running = running;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pending", pending).append("running", running).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(running).append(pending).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobsSummary) == false) {
            return false;
        }
        JobsSummary rhs = ((JobsSummary) other);
        return new EqualsBuilder().append(running, rhs.running).append(pending, rhs.pending).isEquals();
    }

}
