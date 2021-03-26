
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pending",
    "inProgress",
    "running",
    "suspended",
    "waiting",
    "blocked",
    "failed",
    "terminated"
})
public class OrdersSummary {

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
    @JsonProperty("inProgress")
    private Integer inProgress;
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
    @JsonProperty("suspended")
    private Integer suspended;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waiting")
    private Integer waiting;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blocked")
    private Integer blocked;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("failed")
    private Integer failed;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("terminated")
    private Integer terminated;

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
    @JsonProperty("inProgress")
    public Integer getInProgress() {
        return inProgress;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("inProgress")
    public void setInProgress(Integer inProgress) {
        this.inProgress = inProgress;
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

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    public Integer getSuspended() {
        return suspended;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    public void setSuspended(Integer suspended) {
        this.suspended = suspended;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waiting")
    public Integer getWaiting() {
        return waiting;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waiting")
    public void setWaiting(Integer waiting) {
        this.waiting = waiting;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blocked")
    public Integer getBlocked() {
        return blocked;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blocked")
    public void setBlocked(Integer blocked) {
        this.blocked = blocked;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("failed")
    public Integer getFailed() {
        return failed;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("failed")
    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("terminated")
    public Integer getTerminated() {
        return terminated;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("terminated")
    public void setTerminated(Integer terminated) {
        this.terminated = terminated;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pending", pending).append("inProgress", inProgress).append("running", running).append("suspended", suspended).append("waiting", waiting).append("blocked", blocked).append("failed", failed).append("terminated", terminated).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(running).append(inProgress).append(waiting).append(blocked).append(pending).append(failed).append(suspended).append(terminated).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersSummary) == false) {
            return false;
        }
        OrdersSummary rhs = ((OrdersSummary) other);
        return new EqualsBuilder().append(running, rhs.running).append(inProgress, rhs.inProgress).append(waiting, rhs.waiting).append(blocked, rhs.blocked).append(pending, rhs.pending).append(failed, rhs.failed).append(suspended, rhs.suspended).append(terminated, rhs.terminated).isEquals();
    }

}
