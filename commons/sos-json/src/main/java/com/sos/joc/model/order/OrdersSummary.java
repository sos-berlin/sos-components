
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job chain order summary
 * <p>
 * only relevant for order jobs and is empty if job's order queue is empty
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pending",
    "running",
    "suspended",
    "setback",
    "waitingForResource",
    "blacklist"
})
public class OrdersSummary {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    @JacksonXmlProperty(localName = "pending")
    private Integer pending;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("running")
    @JacksonXmlProperty(localName = "running")
    private Integer running;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    @JacksonXmlProperty(localName = "suspended")
    private Integer suspended;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("setback")
    @JacksonXmlProperty(localName = "setback")
    private Integer setback;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForResource")
    @JacksonXmlProperty(localName = "waitingForResource")
    private Integer waitingForResource;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    private Integer blacklist;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    @JacksonXmlProperty(localName = "pending")
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
    @JacksonXmlProperty(localName = "pending")
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
    @JacksonXmlProperty(localName = "running")
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
    @JacksonXmlProperty(localName = "running")
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
    @JacksonXmlProperty(localName = "suspended")
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
    @JacksonXmlProperty(localName = "suspended")
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
    @JacksonXmlProperty(localName = "setback")
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
    @JacksonXmlProperty(localName = "setback")
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
    @JacksonXmlProperty(localName = "waitingForResource")
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
    @JacksonXmlProperty(localName = "waitingForResource")
    public void setWaitingForResource(Integer waitingForResource) {
        this.waitingForResource = waitingForResource;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    public Integer getBlacklist() {
        return blacklist;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    public void setBlacklist(Integer blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pending", pending).append("running", running).append("suspended", suspended).append("setback", setback).append("waitingForResource", waitingForResource).append("blacklist", blacklist).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(running).append(waitingForResource).append(pending).append(blacklist).append(suspended).append(setback).toHashCode();
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
        return new EqualsBuilder().append(running, rhs.running).append(waitingForResource, rhs.waitingForResource).append(pending, rhs.pending).append(blacklist, rhs.blacklist).append(suspended, rhs.suspended).append(setback, rhs.setback).isEquals();
    }

}
