
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.model.common.IEventObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * event snapshot
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "eventId",
    "eventType",
    "objectType",
    "workflow",
    "accessToken",
    "message"
})
public class EventSnapshot implements IEventObject
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * e.g. JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged, InventoryUpdated
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("e.g. JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged, InventoryUpdated")
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
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * e.g. JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged, InventoryUpdated
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * e.g. JOCStateChanged, ControllerStateChanged, WorkflowStateChanged, JobStateChanged, InventoryUpdated
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
        return new ToStringBuilder(this).append("path", path).append("eventId", eventId).append("eventType", eventType).append("objectType", objectType).append("workflow", workflow).append("accessToken", "***").append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(eventId).append(workflow).append(eventType).append(accessToken).append(message).append(objectType).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(eventId, rhs.eventId).append(workflow, rhs.workflow).append(eventType, rhs.eventType).append(accessToken, rhs.accessToken).append(message, rhs.message).append(objectType, rhs.objectType).isEquals();
    }

}
