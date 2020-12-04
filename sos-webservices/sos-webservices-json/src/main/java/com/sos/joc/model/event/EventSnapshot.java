
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * event snapshot
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "path",
    "eventType",
    "objectType",
    "workflow",
    "accessToken",
    "message"
})
public class EventSnapshot {

    /**
     * eventId/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JsonPropertyDescription("eventId/1000=milliseconds of UTC time")
    private Long eventId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("e.g. OrderStateChanged, OrderAdded, OrderTerminated, JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged")
    private String eventType;
    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private EventType objectType;
    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    private WorkflowId workflow;
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("message")
    private String message;

    /**
     * eventId/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * eventId/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public EventType getObjectType() {
        return objectType;
    }

    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(EventType objectType) {
        this.objectType = objectType;
    }

    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public WorkflowId getWorkflow() {
        return workflow;
    }

    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(WorkflowId workflow) {
        this.workflow = workflow;
    }

    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("path", path).append("eventType", eventType).append("objectType", objectType).append("workflow", workflow).append("accessToken", accessToken).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(path).append(workflow).append(eventType).append(accessToken).append(message).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventSnapshot) == false) {
            return false;
        }
        EventSnapshot rhs = ((EventSnapshot) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(path, rhs.path).append(workflow, rhs.workflow).append(eventType, rhs.eventType).append(accessToken, rhs.accessToken).append(message, rhs.message).append(objectType, rhs.objectType).isEquals();
    }

}
