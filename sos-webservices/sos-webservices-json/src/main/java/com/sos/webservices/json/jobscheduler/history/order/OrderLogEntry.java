
package com.sos.webservices.json.jobscheduler.history.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.event.EventType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order history log entry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timestamp",
    "orderId",
    "logLevel",
    "logEvent",
    "position",
    "agentPath",
    "agentUrl",
    "job",
    "taskId",
    "error"
})
public class OrderLogEntry {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    private Date timestamp;
    /**
     * 
     * (Required)
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    private String logLevel;
    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    private EventType logEvent;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;
    @JsonProperty("agentPath")
    private String agentPath;
    @JsonProperty("agentUrl")
    private String agentUrl;
    @JsonProperty("job")
    private String job;
    @JsonProperty("taskId")
    private Long taskId;
    @JsonProperty("error")
    private Error error;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderLogEntry() {
    }

    /**
     * 
     * @param agentPath
     * @param logLevel
     * @param orderId
     * @param position
     * @param agentUrl
     * @param job
     * @param error
     * @param taskId
     * @param timestamp
     * @param logEvent
     */
    public OrderLogEntry(Date timestamp, String orderId, String logLevel, EventType logEvent, String position, String agentPath, String agentUrl, String job, Long taskId, Error error) {
        super();
        this.timestamp = timestamp;
        this.orderId = orderId;
        this.logLevel = logLevel;
        this.logEvent = logEvent;
        this.position = position;
        this.agentPath = agentPath;
        this.agentUrl = agentUrl;
        this.job = job;
        this.taskId = taskId;
        this.error = error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    public EventType getLogEvent() {
        return logEvent;
    }

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    public void setLogEvent(EventType logEvent) {
        this.logEvent = logEvent;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(String position) {
        this.position = position;
    }

    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("agentUrl")
    public String getAgentUrl() {
        return agentUrl;
    }

    @JsonProperty("agentUrl")
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("taskId")
    public Long getTaskId() {
        return taskId;
    }

    @JsonProperty("taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @JsonProperty("error")
    public Error getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(Error error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timestamp", timestamp).append("orderId", orderId).append("logLevel", logLevel).append("logEvent", logEvent).append("position", position).append("agentPath", agentPath).append("agentUrl", agentUrl).append("job", job).append("taskId", taskId).append("error", error).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentPath).append(logLevel).append(orderId).append(position).append(agentUrl).append(job).append(error).append(taskId).append(timestamp).append(logEvent).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLogEntry) == false) {
            return false;
        }
        OrderLogEntry rhs = ((OrderLogEntry) other);
        return new EqualsBuilder().append(agentPath, rhs.agentPath).append(logLevel, rhs.logLevel).append(orderId, rhs.orderId).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(job, rhs.job).append(error, rhs.error).append(taskId, rhs.taskId).append(timestamp, rhs.timestamp).append(logEvent, rhs.logEvent).isEquals();
    }

}
