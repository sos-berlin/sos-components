
package com.sos.joc.model.history.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.history.order.attached.Attached;
import com.sos.joc.model.history.order.caught.Caught;
import com.sos.joc.model.history.order.cycle.Cycle;
import com.sos.joc.model.history.order.moved.Moved;
import com.sos.joc.model.history.order.notice.ConsumeNotices;
import com.sos.joc.model.history.order.notice.ExpectNotices;
import com.sos.joc.model.history.order.notice.PostNotice;
import com.sos.joc.model.history.order.retry.Retrying;
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
    "returnMessage",
    "msg",
    "error",
    "locks",
    "expectNotices",
    "consumeNotices",
    "postNotice",
    "retrying",
    "caught",
    "moved",
    "attached",
    "stopped",
    "resumed",
    "arguments",
    "returnValues",
    "question",
    "cycle"
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
     * order history log entry log level
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    private OrderLogEntryLogLevel logLevel;
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
    @JsonProperty("returnMessage")
    private String returnMessage;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("error")
    private OrderLogEntryError error;
    @JsonProperty("locks")
    private List<Lock> locks = new ArrayList<Lock>();
    /**
     * ExpectNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("expectNotices")
    private ExpectNotices expectNotices;
    /**
     * ConsumeNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("consumeNotices")
    private ConsumeNotices consumeNotices;
    /**
     * PostNotice
     * <p>
     * 
     * 
     */
    @JsonProperty("postNotice")
    private PostNotice postNotice;
    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("retrying")
    private Retrying retrying;
    /**
     * Caught
     * <p>
     * 
     * 
     */
    @JsonProperty("caught")
    private Caught caught;
    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("moved")
    private Moved moved;
    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("attached")
    private Attached attached;
    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("stopped")
    private OrderLogEntryInstruction stopped;
    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("resumed")
    private OrderLogEntryInstruction resumed;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("returnValues")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables returnValues;
    @JsonProperty("question")
    private String question;
    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    private Cycle cycle;

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
     * order history log entry log level
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public OrderLogEntryLogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * order history log entry log level
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(OrderLogEntryLogLevel logLevel) {
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

    @JsonProperty("returnMessage")
    public String getReturnMessage() {
        return returnMessage;
    }

    @JsonProperty("returnMessage")
    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    @JsonProperty("msg")
    public String getMsg() {
        return msg;
    }

    @JsonProperty("msg")
    public void setMsg(String msg) {
        this.msg = msg;
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

    /**
     * ExpectNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("expectNotices")
    public ExpectNotices getExpectNotices() {
        return expectNotices;
    }

    /**
     * ExpectNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("expectNotices")
    public void setExpectNotices(ExpectNotices expectNotices) {
        this.expectNotices = expectNotices;
    }

    /**
     * ConsumeNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("consumeNotices")
    public ConsumeNotices getConsumeNotices() {
        return consumeNotices;
    }

    /**
     * ConsumeNotices
     * <p>
     * 
     * 
     */
    @JsonProperty("consumeNotices")
    public void setConsumeNotices(ConsumeNotices consumeNotices) {
        this.consumeNotices = consumeNotices;
    }

    /**
     * PostNotice
     * <p>
     * 
     * 
     */
    @JsonProperty("postNotice")
    public PostNotice getPostNotice() {
        return postNotice;
    }

    /**
     * PostNotice
     * <p>
     * 
     * 
     */
    @JsonProperty("postNotice")
    public void setPostNotice(PostNotice postNotice) {
        this.postNotice = postNotice;
    }

    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("retrying")
    public Retrying getRetrying() {
        return retrying;
    }

    /**
     * Retrying
     * <p>
     * 
     * 
     */
    @JsonProperty("retrying")
    public void setRetrying(Retrying retrying) {
        this.retrying = retrying;
    }

    /**
     * Caught
     * <p>
     * 
     * 
     */
    @JsonProperty("caught")
    public Caught getCaught() {
        return caught;
    }

    /**
     * Caught
     * <p>
     * 
     * 
     */
    @JsonProperty("caught")
    public void setCaught(Caught caught) {
        this.caught = caught;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("moved")
    public Moved getMoved() {
        return moved;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("moved")
    public void setMoved(Moved moved) {
        this.moved = moved;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("attached")
    public Attached getAttached() {
        return attached;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("attached")
    public void setAttached(Attached attached) {
        this.attached = attached;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("stopped")
    public OrderLogEntryInstruction getStopped() {
        return stopped;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("stopped")
    public void setStopped(OrderLogEntryInstruction stopped) {
        this.stopped = stopped;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("resumed")
    public OrderLogEntryInstruction getResumed() {
        return resumed;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("resumed")
    public void setResumed(OrderLogEntryInstruction resumed) {
        this.resumed = resumed;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("returnValues")
    public Variables getReturnValues() {
        return returnValues;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("returnValues")
    public void setReturnValues(Variables returnValues) {
        this.returnValues = returnValues;
    }

    @JsonProperty("question")
    public String getQuestion() {
        return question;
    }

    @JsonProperty("question")
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public Cycle getCycle() {
        return cycle;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerDatetime", controllerDatetime).append("agentDatetime", agentDatetime).append("orderId", orderId).append("logLevel", logLevel).append("logEvent", logEvent).append("position", position).append("agentId", agentId).append("agentName", agentName).append("agentUrl", agentUrl).append("subagentClusterId", subagentClusterId).append("job", job).append("taskId", taskId).append("returnCode", returnCode).append("returnMessage", returnMessage).append("msg", msg).append("error", error).append("locks", locks).append("expectNotices", expectNotices).append("consumeNotices", consumeNotices).append("postNotice", postNotice).append("retrying", retrying).append("caught", caught).append("moved", moved).append("attached", attached).append("stopped", stopped).append("resumed", resumed).append("arguments", arguments).append("returnValues", returnValues).append("question", question).append("cycle", cycle).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(msg).append(caught).append(agentId).append(orderId).append(moved).append(returnMessage).append(error).append(locks).append(cycle).append(agentDatetime).append(returnValues).append(returnCode).append(controllerDatetime).append(logLevel).append(expectNotices).append(stopped).append(question).append(agentName).append(consumeNotices).append(postNotice).append(logEvent).append(retrying).append(attached).append(arguments).append(position).append(agentUrl).append(subagentClusterId).append(job).append(resumed).append(taskId).toHashCode();
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
        return new EqualsBuilder().append(msg, rhs.msg).append(caught, rhs.caught).append(agentId, rhs.agentId).append(orderId, rhs.orderId).append(moved, rhs.moved).append(returnMessage, rhs.returnMessage).append(error, rhs.error).append(locks, rhs.locks).append(cycle, rhs.cycle).append(agentDatetime, rhs.agentDatetime).append(returnValues, rhs.returnValues).append(returnCode, rhs.returnCode).append(controllerDatetime, rhs.controllerDatetime).append(logLevel, rhs.logLevel).append(expectNotices, rhs.expectNotices).append(stopped, rhs.stopped).append(question, rhs.question).append(agentName, rhs.agentName).append(consumeNotices, rhs.consumeNotices).append(postNotice, rhs.postNotice).append(logEvent, rhs.logEvent).append(retrying, rhs.retrying).append(attached, rhs.attached).append(arguments, rhs.arguments).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(subagentClusterId, rhs.subagentClusterId).append(job, rhs.job).append(resumed, rhs.resumed).append(taskId, rhs.taskId).isEquals();
    }

}
