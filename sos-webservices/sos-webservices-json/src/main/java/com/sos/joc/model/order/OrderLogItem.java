
package com.sos.joc.model.order;

import java.util.Date;
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
public class OrderLogItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date timestamp;
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
    @JsonProperty("error")
    private OrderLogItemError error;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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

    @JsonProperty("error")
    public OrderLogItemError getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(OrderLogItemError error) {
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
        if ((other instanceof OrderLogItem) == false) {
            return false;
        }
        OrderLogItem rhs = ((OrderLogItem) other);
        return new EqualsBuilder().append(agentPath, rhs.agentPath).append(logLevel, rhs.logLevel).append(orderId, rhs.orderId).append(position, rhs.position).append(agentUrl, rhs.agentUrl).append(job, rhs.job).append(error, rhs.error).append(taskId, rhs.taskId).append(timestamp, rhs.timestamp).append(logEvent, rhs.logEvent).isEquals();
    }

    public enum LogEvent {

        ORDER_ADDED("OrderAdded"),
        ORDER_STARTED("OrderStarted"),
        ORDER_FAILED("OrderFailed"),
        ORDER_FAILEDIN_FORK("OrderFailedinFork"),
        ORDER_FINISHED("OrderFinished"),
        ORDER_CANCELLED("OrderCancelled"),
        ORDER_FORKED("OrderForked"),
        ORDER_PROCESSING_STARTED("OrderProcessingStarted"),
        ORDER_RETRYING("OrderRetrying"),
        ORDER_AWAITING("OrderAwaiting"),
        ORDER_OFFERED("OrderOffered"),
        ORDER_PROCESSED("OrderProcessed"),
        ORDER_RESUMED("OrderResumed"),
        ORDER_MOVED("OrderMoved"),
        ORDER_CATCHED("OrderCatched"),
        ORDER_AWOKE("OrderAwoke"),
        ORDER_JOINED("OrderJoined"),
        ORDER_SUSPENDED("OrderSuspended"),
        ORDER_BROKEN("OrderBroken");
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
