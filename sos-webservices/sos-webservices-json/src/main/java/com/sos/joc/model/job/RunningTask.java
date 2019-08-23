
package com.sos.joc.model.job;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * running task
 * <p>
 * task object of an order job which is running
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "taskId",
    "pid",
    "startedAt",
    "enqueued",
    "idleSince",
    "steps",
    "_cause",
    "order"
})
public class RunningTask {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    @JsonProperty("pid")
    private Integer pid;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startedAt;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("enqueued")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date enqueued;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("idleSince")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date idleSince;
    @JsonProperty("steps")
    private Integer steps;
    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * 
     */
    @JsonProperty("_cause")
    @JsonPropertyDescription("For order jobs only cause=order possible")
    private TaskCause _cause;
    /**
     * order in task
     * <p>
     * Only relevant for order jobs; cause=order resp.
     * 
     */
    @JsonProperty("order")
    @JsonPropertyDescription("Only relevant for order jobs; cause=order resp.")
    private OrderOfTask order;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @JsonProperty("pid")
    public Integer getPid() {
        return pid;
    }

    @JsonProperty("pid")
    public void setPid(Integer pid) {
        this.pid = pid;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    public Date getStartedAt() {
        return startedAt;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("enqueued")
    public Date getEnqueued() {
        return enqueued;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("enqueued")
    public void setEnqueued(Date enqueued) {
        this.enqueued = enqueued;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("idleSince")
    public Date getIdleSince() {
        return idleSince;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("idleSince")
    public void setIdleSince(Date idleSince) {
        this.idleSince = idleSince;
    }

    @JsonProperty("steps")
    public Integer getSteps() {
        return steps;
    }

    @JsonProperty("steps")
    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * 
     */
    @JsonProperty("_cause")
    public TaskCause get_cause() {
        return _cause;
    }

    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * 
     */
    @JsonProperty("_cause")
    public void set_cause(TaskCause _cause) {
        this._cause = _cause;
    }

    /**
     * order in task
     * <p>
     * Only relevant for order jobs; cause=order resp.
     * 
     */
    @JsonProperty("order")
    public OrderOfTask getOrder() {
        return order;
    }

    /**
     * order in task
     * <p>
     * Only relevant for order jobs; cause=order resp.
     * 
     */
    @JsonProperty("order")
    public void setOrder(OrderOfTask order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("taskId", taskId).append("pid", pid).append("startedAt", startedAt).append("enqueued", enqueued).append("idleSince", idleSince).append("steps", steps).append("_cause", _cause).append("order", order).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idleSince).append(enqueued).append(_cause).append(startedAt).append(pid).append(steps).append(taskId).append(order).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningTask) == false) {
            return false;
        }
        RunningTask rhs = ((RunningTask) other);
        return new EqualsBuilder().append(idleSince, rhs.idleSince).append(enqueued, rhs.enqueued).append(_cause, rhs._cause).append(startedAt, rhs.startedAt).append(pid, rhs.pid).append(steps, rhs.steps).append(taskId, rhs.taskId).append(order, rhs.order).isEquals();
    }

}
