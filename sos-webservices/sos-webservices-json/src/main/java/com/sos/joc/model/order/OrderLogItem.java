
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order log item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerDatetime",
    "orderId",
    "logLevel",
    "logEvent",
    "position",
    "agentDatetime",
    "agentPath",
    "agentUrl",
    "job",
    "taskId",
    "returnCode",
    "error",
    "lock"
})
public class OrderLogItem {

    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    @JsonPropertyDescription("datetime with timeOffset: format \"yyyy-MM-dd' 'HH:mm:ss.SSSZ\"")
    private String controllerDatetime;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    private OrderLogItem.LogEvent logEvent;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;
    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * 
     */
    @JsonProperty("agentDatetime")
    @JsonPropertyDescription("datetime with timeOffset: format \"yyyy-MM-dd' 'HH:mm:ss.SSSZ\"")
    private String agentDatetime;
    @JsonProperty("agentPath")
    private String agentPath;
    @JsonProperty("agentUrl")
    private String agentUrl;
    @JsonProperty("job")
    private String job;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCode")
    private Long returnCode;
    @JsonProperty("error")
    private OrderLogItemError error;
    @JsonProperty("lock")
    private Lock lock;

    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    public String getControllerDatetime() {
        return controllerDatetime;
    }

    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * (Required)
     * 
     */
    @JsonProperty("controllerDatetime")
    public void setControllerDatetime(String controllerDatetime) {
        this.controllerDatetime = controllerDatetime;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    public OrderLogItem.LogEvent getLogEvent() {
        return logEvent;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvent")
    public void setLogEvent(OrderLogItem.LogEvent logEvent) {
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

    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * 
     */
    @JsonProperty("agentDatetime")
    public String getAgentDatetime() {
        return agentDatetime;
    }

    /**
     * datetime with timeOffset: format "yyyy-MM-dd' 'HH:mm:ss.SSSZ"
     * 
     */
    @JsonProperty("agentDatetime")
    public void setAgentDatetime(String agentDatetime) {
        this.agentDatetime = agentDatetime;
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

    /**
     * non negative long
     * <p>
     * 
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
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCode")
    public Long getReturnCode() {
        return returnCode;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCode")
    public void setReturnCode(Long returnCode) {
        this.returnCode = returnCode;
    }

    @JsonProperty("error")
    public OrderLogItemError getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(OrderLogItemError error) {
        this.error = error;
    }

    @JsonProperty("lock")
    public Lock getLock() {
        return lock;
    }

    @JsonProperty("lock")
    public void setLock(Lock lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerDatetime", controllerDatetime).append("orderId", orderId).append("logLevel", logLevel).append("logEvent", logEvent).append("position", position).append("agentDatetime", agentDatetime).append("agentPath", agentPath).append("agentUrl", agentUrl).append("job", job).append("taskId", taskId).append("returnCode", returnCode).append("error", error).append("lock", lock).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(error).append(agentDatetime).append(logEvent).append(agentPath).append(returnCode).append(controllerDatetime).append(logLevel).append(lock).append(position).append(agentUrl).append(job).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLogItem) == false) {
            return false;
        }
        OrderLogItem rhs = ((OrderLogItem) other);
        return new EqualsBuilder().append(orderId, rhs.orderId).append(error, rhs.error).append(agentDatetime, rhs.agentDatetime).append(logEvent, rhs.logEvent).append(agentPath, rhs.agentPath).append(returnCode, rhs.returnCode).append(controllerDatetime, rhs.controllerDatetime).append(logLevel, rhs.logLevel).append(lock, rhs.lock).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

    public enum LogEvent {

        OrderAdded("OrderAdded"),
        OrderStarted("OrderStarted"),
        OrderFailed("OrderFailed"),
        OrderFailedinFork("OrderFailedinFork"),
        OrderFinished("OrderFinished"),
        OrderCancelled("OrderCancelled"),
        OrderForked("OrderForked"),
        OrderProcessingStarted("OrderProcessingStarted"),
        OrderRetrying("OrderRetrying"),
        OrderAwaiting("OrderAwaiting"),
        OrderOffered("OrderOffered"),
        OrderProcessed("OrderProcessed"),
        OrderResumed("OrderResumed"),
        OrderResumeMarked("OrderResumeMarked"),
        OrderMoved("OrderMoved"),
        OrderCatched("OrderCatched"),
        OrderAwoke("OrderAwoke"),
        OrderJoined("OrderJoined"),
        OrderSuspended("OrderSuspended"),
        OrderSuspendMarked("OrderSuspendMarked"),
        OrderBroken("OrderBroken"),
        OrderLockAcquired("OrderLockAcquired"),
        OrderLockQueued("OrderLockQueued"),
        OrderLockReleased("OrderLockReleased");
        private final String value;
        private final static Map<String, OrderLogItem.LogEvent> CONSTANTS = new HashMap<String, OrderLogItem.LogEvent>();

        static {
            for (OrderLogItem.LogEvent c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LogEvent(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static OrderLogItem.LogEvent fromValue(String value) {
            OrderLogItem.LogEvent constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
