
package com.sos.joc.model.job;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * task object which is enqueued
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "taskId",
    "enqueued",
    "plannedStart"
})
public class QueuedTask {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    private String taskId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("enqueued")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "enqueued")
    private Date enqueued;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStart")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "plannedStart")
    private Date plannedStart;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public String getTaskId() {
        return taskId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("enqueued")
    @JacksonXmlProperty(localName = "enqueued")
    public Date getEnqueued() {
        return enqueued;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("enqueued")
    @JacksonXmlProperty(localName = "enqueued")
    public void setEnqueued(Date enqueued) {
        this.enqueued = enqueued;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStart")
    @JacksonXmlProperty(localName = "plannedStart")
    public Date getPlannedStart() {
        return plannedStart;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("plannedStart")
    @JacksonXmlProperty(localName = "plannedStart")
    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("taskId", taskId).append("enqueued", enqueued).append("plannedStart", plannedStart).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(enqueued).append(plannedStart).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof QueuedTask) == false) {
            return false;
        }
        QueuedTask rhs = ((QueuedTask) other);
        return new EqualsBuilder().append(enqueued, rhs.enqueued).append(plannedStart, rhs.plannedStart).append(taskId, rhs.taskId).isEquals();
    }

}
