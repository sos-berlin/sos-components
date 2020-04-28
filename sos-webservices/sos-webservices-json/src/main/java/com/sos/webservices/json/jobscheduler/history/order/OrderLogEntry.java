
package com.sos.webservices.json.jobscheduler.history.order;

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
    "masterDatetime",
    "agentDatetime",
    "orderId",
    "logLevel",
    "logEvent",
    "position",
    "agentPath",
    "agentUrl",
    "job",
    "taskId",
    "returnCode",
    "error"
})
public class OrderLogEntry {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masterDatetime")
    private String masterDatetime;
    @JsonProperty("agentDatetime")
    private String agentDatetime;
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
    @JsonProperty("returnCode")
    private Long returnCode;
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
     * @param returnCode
     * @param masterDatetime
     * @param logLevel
     * @param orderId
     * @param position
     * @param agentUrl
     * @param job
     * @param error
     * @param agentDatetime
     * @param taskId
     * @param logEvent
     */
    public OrderLogEntry(String masterDatetime, String agentDatetime, String orderId, String logLevel, EventType logEvent, String position, String agentPath, String agentUrl, String job, Long taskId, Long returnCode, Error error) {
        super();
        this.masterDatetime = masterDatetime;
        this.agentDatetime = agentDatetime;
        this.orderId = orderId;
        this.logLevel = logLevel;
        this.logEvent = logEvent;
        this.position = position;
        this.agentPath = agentPath;
        this.agentUrl = agentUrl;
        this.job = job;
        this.taskId = taskId;
        this.returnCode = returnCode;
        this.error = error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masterDatetime")
    public String getMasterDatetime() {
        return masterDatetime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masterDatetime")
    public void setMasterDatetime(String masterDatetime) {
        this.masterDatetime = masterDatetime;
    }

    @JsonProperty("agentDatetime")
    public String getAgentDatetime() {
        return agentDatetime;
    }

    @JsonProperty("agentDatetime")
    public void setAgentDatetime(String agentDatetime) {
        this.agentDatetime = agentDatetime;
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

    @JsonProperty("returnCode")
    public Long getReturnCode() {
        return returnCode;
    }

    @JsonProperty("returnCode")
    public void setReturnCode(Long returnCode) {
        this.returnCode = returnCode;
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
        return new ToStringBuilder(this).append("masterDatetime", masterDatetime).append("agentDatetime", agentDatetime).append("orderId", orderId).append("logLevel", logLevel).append("logEvent", logEvent).append("position", position).append("agentPath", agentPath).append("agentUrl", agentUrl).append("job", job).append("taskId", taskId).append("returnCode", returnCode).append("error", error).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(masterDatetime).append(orderId).append(error).append(agentDatetime).append(logEvent).append(agentPath).append(returnCode).append(logLevel).append(position).append(agentUrl).append(job).append(taskId).toHashCode();
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
        return new EqualsBuilder().append(masterDatetime, rhs.masterDatetime).append(orderId, rhs.orderId).append(error, rhs.error).append(agentDatetime, rhs.agentDatetime).append(logEvent, rhs.logEvent).append(agentPath, rhs.agentPath).append(returnCode, rhs.returnCode).append(logLevel, rhs.logLevel).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
