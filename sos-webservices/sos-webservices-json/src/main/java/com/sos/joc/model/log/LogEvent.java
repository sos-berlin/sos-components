
package com.sos.joc.model.log;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * log event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timestamp",
    "host",
    "product",
    "clusterId",
    "instanceId",
    "role",
    "thread",
    "level",
    "logger",
    "message",
    "thrown"
})
public class LogEvent {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date timestamp;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    private String host;
    @JsonProperty("product")
    private Product product;
    @JsonProperty("clusterId")
    @JsonAlias({
        "controllerId",
        "agentId"
    })
    private String clusterId;
    @JsonProperty("instanceId")
    @JsonAlias({
        "nodeId",
        "subagentId",
        "jocId"
    })
    private String instanceId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    private String role;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("thread")
    private String thread;
    @JsonProperty("level")
    private Level level;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("logger")
    private String logger;
    @JsonProperty("message")
    private String message;
    @JsonProperty("thrown")
    private String thrown;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("product")
    public Product getProduct() {
        return product;
    }

    @JsonProperty("product")
    public void setProduct(Product product) {
        this.product = product;
    }

    @JsonProperty("clusterId")
    public String getClusterId() {
        return clusterId;
    }

    @JsonProperty("clusterId")
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @JsonProperty("instanceId")
    public String getInstanceId() {
        return instanceId;
    }

    @JsonProperty("instanceId")
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("thread")
    public String getThread() {
        return thread;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("thread")
    public void setThread(String thread) {
        this.thread = thread;
    }

    @JsonProperty("level")
    public Level getLevel() {
        return level;
    }

    @JsonProperty("level")
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("logger")
    public String getLogger() {
        return logger;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("logger")
    public void setLogger(String logger) {
        this.logger = logger;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }
    
    @JsonProperty("thrown")
    public String getThrown() {
        return thrown;
    }

    @JsonProperty("thrown")
    public void setThrown(String thrown) {
        this.thrown = thrown;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timestamp", timestamp).append("host", host).append("product", product).append("clusterId", clusterId).append("instanceId", instanceId).append("role", role).append("thread", thread).append("level", level).append("logger", logger).append("message", message).append("thrown", thrown).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(product).append(instanceId).append(role).append(logger).append(host).append(clusterId).append(message).append(thrown).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogEvent) == false) {
            return false;
        }
        LogEvent rhs = ((LogEvent) other);
        return new EqualsBuilder().append(product, rhs.product).append(instanceId, rhs.instanceId).append(role, rhs.role).append(logger, rhs.logger).append(host, rhs.host).append(clusterId, rhs.clusterId).append(message, rhs.message).append(thrown, rhs.thrown).isEquals();
    }

}
