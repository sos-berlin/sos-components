
package com.sos.joc.model.joc;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * joc log entry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timestamp",
    "logger",
    "thread",
    "logLevel",
    "source",
    "message",
    "thrown"
})
public class LogEntry {

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
    @JsonProperty("logger")
    private String logger;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("thread")
    private String thread;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    private LogLevel logLevel;
    /**
     * main(joc.log), service-cluster, service-history, etc.
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("main(joc.log), service-cluster, service-history, etc.")
    private String source;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    private String message;
    @JsonProperty("thrown")
    private String thrown;

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
    @JsonProperty("logger")
    public String getLogger() {
        return logger;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logger")
    public void setLogger(String logger) {
        this.logger = logger;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("thread")
    public String getThread() {
        return thread;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("thread")
    public void setThread(String thread) {
        this.thread = thread;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * main(joc.log), service-cluster, service-history, etc.
     * (Required)
     * 
     */
    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    /**
     * main(joc.log), service-cluster, service-history, etc.
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * (Required)
     * 
     */
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
        return new ToStringBuilder(this).append("timestamp", timestamp).append("logger", logger).append("thread", thread).append("logLevel", logLevel).append("source", source).append("message", message).append("thrown", thrown).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(logLevel).append(logger).append(thrown).append(thread).append(source).append(message).append(timestamp).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogEntry) == false) {
            return false;
        }
        LogEntry rhs = ((LogEntry) other);
        return new EqualsBuilder().append(logLevel, rhs.logLevel).append(logger, rhs.logger).append(thrown, rhs.thrown).append(thread, rhs.thread).append(source, rhs.source).append(message, rhs.message).append(timestamp, rhs.timestamp).isEquals();
    }

}
