
package com.sos.joc.model.history.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.event.EventType;
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
    "controllerDatetime",
    "agentDatetime",
    "orderId",
    "logLevel",
    "logEvent",
    "position",
    "agentId",
    "agentName",
    "agentUrl",
    "subagentClusterId",
    "job",
    "taskId",
    "returnCode",
    "error",
    "locks",
    "expectNotices",
    "postNotice",
    "retrying"
})
public class OrderLogEntry {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    private String controllerDatetime;
    @JsonProperty("agentDatetime")
    private String agentDatetime;
    /**
     * 
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
    @JsonProperty("agentId")
    @JsonAlias({
        "agentPath"
    })
    private String agentId;
    @JsonProperty("agentName")
    private String agentName;
    @JsonProperty("agentUrl")
    private String agentUrl;
    @JsonProperty("subagentClusterId")
    private String subagentClusterId;
    @JsonProperty("job")
    private String job;
    @JsonProperty("taskId")
    private Long taskId;
    @JsonProperty("returnCode")
    private Long returnCode;
    @JsonProperty("error")
    private OrderLogEntryError error;
    @JsonProperty("locks")
    private List<Lock> locks = new ArrayList<Lock>();
    @JsonProperty("expectNotices")
    private ExpectNotices expectNotices;
    @JsonProperty("postNotice")
    private PostNotice postNotice;
    @JsonProperty("retrying")
    private Retrying retrying;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    public String getControllerDatetime() {
        return controllerDatetime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    public void setControllerDatetime(String controllerDatetime) {
        this.controllerDatetime = controllerDatetime;
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

    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @JsonProperty("agentUrl")
    public String getAgentUrl() {
        return agentUrl;
    }

    @JsonProperty("agentUrl")
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
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
    public OrderLogEntryError getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(OrderLogEntryError error) {
        this.error = error;
    }

    @JsonProperty("locks")
    public List<Lock> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(List<Lock> locks) {
        this.locks = locks;
    }

    @JsonProperty("expectNotices")
    public ExpectNotices getExpectNotices() {
        return expectNotices;
    }

    @JsonProperty("expectNotices")
    public void setExpectNotices(ExpectNotices expectNotices) {
        this.expectNotices = expectNotices;
    }

    @JsonProperty("postNotice")
    public PostNotice getPostNotice() {
        return postNotice;
    }

    @JsonProperty("postNotice")
    public void setPostNotice(PostNotice postNotice) {
        this.postNotice = postNotice;
    }

    @JsonProperty("retrying")
    public Retrying getRetrying() {
        return retrying;
    }

    @JsonProperty("retrying")
    public void setRetrying(Retrying retrying) {
        this.retrying = retrying;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerDatetime", controllerDatetime).append("agentDatetime", agentDatetime).append("orderId", orderId).append("logLevel", logLevel).append("logEvent", logEvent).append("position", position).append("agentId", agentId).append("agentName", agentName).append("agentUrl", agentUrl).append("subagentClusterId", subagentClusterId).append("job", job).append("taskId", taskId).append("returnCode", returnCode).append("error", error).append("locks", locks).append("expectNotices", expectNotices).append("postNotice", postNotice).append("retrying", retrying).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(expectNotices).append(agentId).append(orderId).append(agentName).append(error).append(postNotice).append(locks).append(agentDatetime).append(logEvent).append(returnCode).append(controllerDatetime).append(logLevel).append(retrying).append(position).append(agentUrl).append(subagentClusterId).append(job).append(taskId).toHashCode();
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
        return new EqualsBuilder().append(expectNotices, rhs.expectNotices).append(agentId, rhs.agentId).append(orderId, rhs.orderId).append(agentName, rhs.agentName).append(error, rhs.error).append(postNotice, rhs.postNotice).append(locks, rhs.locks).append(agentDatetime, rhs.agentDatetime).append(logEvent, rhs.logEvent).append(returnCode, rhs.returnCode).append(controllerDatetime, rhs.controllerDatetime).append(logLevel, rhs.logLevel).append(retrying, rhs.retrying).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(subagentClusterId, rhs.subagentClusterId).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
