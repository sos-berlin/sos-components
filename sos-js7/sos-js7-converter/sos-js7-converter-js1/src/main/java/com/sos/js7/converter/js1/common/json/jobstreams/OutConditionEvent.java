
package com.sos.js7.converter.js1.common.json.jobstreams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Out-Condition-Event
 * <p>
 * Out Condition Event
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "command",
    "event",
    "exists",
    "globalEvent",
    "existsInJobStream",
    "jobStream"
})
public class OutConditionEvent {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    private String command;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("event")
    private String event;
    @JsonProperty("exists")
    private Boolean exists;
    @JsonProperty("globalEvent")
    private Boolean globalEvent;
    @JsonProperty("existsInJobStream")
    private Boolean existsInJobStream;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    public String getCommand() {
        return command;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("event")
    public String getEvent() {
        return event;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("event")
    public void setEvent(String event) {
        this.event = event;
    }

    @JsonProperty("exists")
    public Boolean getExists() {
        return exists;
    }

    @JsonProperty("exists")
    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    @JsonProperty("globalEvent")
    public Boolean getGlobalEvent() {
        return globalEvent;
    }

    @JsonProperty("globalEvent")
    public void setGlobalEvent(Boolean globalEvent) {
        this.globalEvent = globalEvent;
    }

    @JsonProperty("existsInJobStream")
    public Boolean getExistsInJobStream() {
        return existsInJobStream;
    }

    @JsonProperty("existsInJobStream")
    public void setExistsInJobStream(Boolean existsInJobStream) {
        this.existsInJobStream = existsInJobStream;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("command", command).append("event", event).append("exists", exists).append("globalEvent", globalEvent).append("existsInJobStream", existsInJobStream).append("jobStream", jobStream).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(existsInJobStream).append(jobStream).append(exists).append(id).append(event).append(globalEvent).append(command).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OutConditionEvent) == false) {
            return false;
        }
        OutConditionEvent rhs = ((OutConditionEvent) other);
        return new EqualsBuilder().append(existsInJobStream, rhs.existsInJobStream).append(jobStream, rhs.jobStream).append(exists, rhs.exists).append(id, rhs.id).append(event, rhs.event).append(globalEvent, rhs.globalEvent).append(command, rhs.command).isEquals();
    }

}
