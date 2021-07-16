
package com.sos.joc.model.monitoring;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.MonitoringMonitorTypeText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "name",
    "configuration",
    "message",
    "error",
    "created"
})
public class MonitorItem {

    /**
     * notification monitor type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private MonitoringMonitorTypeText type;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    private String configuration;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    private String message;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    private String error;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;

    /**
     * notification monitor type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public MonitoringMonitorTypeText getType() {
        return type;
    }

    /**
     * notification monitor type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(MonitoringMonitorTypeText type) {
        this.type = type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public String getConfiguration() {
        return configuration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    public String getError() {
        return error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("name", name).append("configuration", configuration).append("message", message).append("error", error).append("created", created).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configuration).append(created).append(name).append(type).append(message).append(error).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonitorItem) == false) {
            return false;
        }
        MonitorItem rhs = ((MonitorItem) other);
        return new EqualsBuilder().append(configuration, rhs.configuration).append(created, rhs.created).append(name, rhs.name).append(type, rhs.type).append(message, rhs.message).append(error, rhs.error).isEquals();
    }

}
