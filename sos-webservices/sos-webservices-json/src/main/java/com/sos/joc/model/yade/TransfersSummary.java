
package com.sos.joc.model.yade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * yade summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "running",
    "suspended",
    "setback",
    "waitingForResource"
})
public class TransfersSummary {

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
    @JsonProperty("setback")
    private Integer setback;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForResource")
    private Integer waitingForResource;

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
    @JsonProperty("setback")
    public Integer getSetback() {
        return setback;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("setback")
    public void setSetback(Integer setback) {
        this.setback = setback;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForResource")
    public Integer getWaitingForResource() {
        return waitingForResource;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForResource")
    public void setWaitingForResource(Integer waitingForResource) {
        this.waitingForResource = waitingForResource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("running", running).append("suspended", suspended).append("setback", setback).append("waitingForResource", waitingForResource).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(running).append(waitingForResource).append(suspended).append(setback).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransfersSummary) == false) {
            return false;
        }
        TransfersSummary rhs = ((TransfersSummary) other);
        return new EqualsBuilder().append(running, rhs.running).append(waitingForResource, rhs.waitingForResource).append(suspended, rhs.suspended).append(setback, rhs.setback).isEquals();
    }

}
