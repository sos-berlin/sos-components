
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.JobSchedulerObjectType;
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
    "nodeId",
    "fromNodeId",
    "taskId",
    "state",
    "nodeTransition"
})
public class EventSnapshot {

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JsonPropertyDescription("unique id of an event, monoton increasing, id/1000=milliseconds of UTC time")
    @JacksonXmlProperty(localName = "eventId")
    private String eventId;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    /**
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed")
    @JacksonXmlProperty(localName = "eventType")
    private String eventType;
    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    private JobSchedulerObjectType objectType;
    /**
     * comes with events OrderNodeChanged, OrderStepStarted, OrderFinished
     * 
     */
    @JsonProperty("nodeId")
    @JsonPropertyDescription("comes with events OrderNodeChanged, OrderStepStarted, OrderFinished")
    @JacksonXmlProperty(localName = "nodeId")
    private String nodeId;
    /**
     * comes with event OrderNodeChanged
     * 
     */
    @JsonProperty("fromNodeId")
    @JsonPropertyDescription("comes with event OrderNodeChanged")
    @JacksonXmlProperty(localName = "fromNodeId")
    private String fromNodeId;
    /**
     * comes with event OrderStepStarted
     * 
     */
    @JsonProperty("taskId")
    @JsonPropertyDescription("comes with event OrderStepStarted")
    @JacksonXmlProperty(localName = "taskId")
    private String taskId;
    /**
     * comes with event ...State
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("comes with event ...State")
    @JacksonXmlProperty(localName = "state")
    private String state;
    /**
     * comes with event OrderStepEnded
     * 
     */
    @JsonProperty("nodeTransition")
    @JsonPropertyDescription("comes with event OrderStepEnded")
    @JacksonXmlProperty(localName = "nodeTransition")
    private NodeTransition nodeTransition;

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public String getEventId() {
        return eventId;
    }

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JacksonXmlProperty(localName = "eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JacksonXmlProperty(localName = "eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    public JobSchedulerObjectType getObjectType() {
        return objectType;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    public void setObjectType(JobSchedulerObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * comes with events OrderNodeChanged, OrderStepStarted, OrderFinished
     * 
     */
    @JsonProperty("nodeId")
    @JacksonXmlProperty(localName = "nodeId")
    public String getNodeId() {
        return nodeId;
    }

    /**
     * comes with events OrderNodeChanged, OrderStepStarted, OrderFinished
     * 
     */
    @JsonProperty("nodeId")
    @JacksonXmlProperty(localName = "nodeId")
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * comes with event OrderNodeChanged
     * 
     */
    @JsonProperty("fromNodeId")
    @JacksonXmlProperty(localName = "fromNodeId")
    public String getFromNodeId() {
        return fromNodeId;
    }

    /**
     * comes with event OrderNodeChanged
     * 
     */
    @JsonProperty("fromNodeId")
    @JacksonXmlProperty(localName = "fromNodeId")
    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    /**
     * comes with event OrderStepStarted
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public String getTaskId() {
        return taskId;
    }

    /**
     * comes with event OrderStepStarted
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * comes with event ...State
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public String getState() {
        return state;
    }

    /**
     * comes with event ...State
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * comes with event OrderStepEnded
     * 
     */
    @JsonProperty("nodeTransition")
    @JacksonXmlProperty(localName = "nodeTransition")
    public NodeTransition getNodeTransition() {
        return nodeTransition;
    }

    /**
     * comes with event OrderStepEnded
     * 
     */
    @JsonProperty("nodeTransition")
    @JacksonXmlProperty(localName = "nodeTransition")
    public void setNodeTransition(NodeTransition nodeTransition) {
        this.nodeTransition = nodeTransition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("path", path).append("eventType", eventType).append("objectType", objectType).append("nodeId", nodeId).append("fromNodeId", fromNodeId).append("taskId", taskId).append("state", state).append("nodeTransition", nodeTransition).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(path).append(eventType).append(state).append(fromNodeId).append(nodeId).append(taskId).append(objectType).append(nodeTransition).toHashCode();
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
        return new EqualsBuilder().append(eventId, rhs.eventId).append(path, rhs.path).append(eventType, rhs.eventType).append(state, rhs.state).append(fromNodeId, rhs.fromNodeId).append(nodeId, rhs.nodeId).append(taskId, rhs.taskId).append(objectType, rhs.objectType).append(nodeTransition, rhs.nodeTransition).isEquals();
    }

}
