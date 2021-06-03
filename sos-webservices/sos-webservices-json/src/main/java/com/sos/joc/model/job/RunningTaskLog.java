
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * running task log
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "taskId",
    "complete",
    "eventId",
    "log"
})
public class RunningTaskLog
    extends RunningTaskLogFilter
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    private Boolean complete = false;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    @JsonProperty("log")
    private String log;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public Long getTaskId() {
        return taskId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public Boolean getComplete() {
        return complete;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("log")
    public String getLog() {
        return log;
    }

    @JsonProperty("log")
    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("taskId", taskId).append("complete", complete).append("eventId", eventId).append("log", log).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(eventId).append(complete).append(log).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningTaskLog) == false) {
            return false;
        }
        RunningTaskLog rhs = ((RunningTaskLog) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(eventId, rhs.eventId).append(complete, rhs.complete).append(log, rhs.log).append(taskId, rhs.taskId).isEquals();
    }

}
