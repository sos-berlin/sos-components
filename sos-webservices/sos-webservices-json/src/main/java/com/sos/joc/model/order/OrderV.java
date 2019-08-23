
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.ConfigurationState;
import com.sos.joc.model.common.NameValuePair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "orderId",
    "workflow",
    "priority",
    "params",
    "_type",
    "surveyDate",
    "state",
    "title",
    "job",
    "stateText",
    "configurationStatus",
    "endState",
    "processingState",
    "nextStartTime",
    "nextStartNever",
    "historyId",
    "startedAt",
    "processedBy",
    "taskId",
    "inProcessSince",
    "setback",
    "lock",
    "processClass",
    "runTimeIsTemporary",
    "documentation"
})
public class OrderV {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    @JsonProperty("orderId")
    private String orderId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflow;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    private Integer priority;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * order type
     * <p>
     * the type of the order
     * 
     */
    @JsonProperty("_type")
    @JsonPropertyDescription("the type of the order")
    private OrderType _type;
    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date surveyDate;
    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("the name of the node")
    private String state;
    @JsonProperty("title")
    private String title;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    @JsonProperty("stateText")
    private String stateText;
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    private ConfigurationState configurationStatus;
    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    @JsonPropertyDescription("the name of the end node")
    private String endState;
    /**
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("processingState")
    private OrderState processingState;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStartTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date nextStartTime;
    @JsonProperty("nextStartNever")
    private Boolean nextStartNever;
    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    @JsonPropertyDescription("for all orders except pending orders")
    private String historyId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startedAt;
    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    @JsonPropertyDescription("ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent")
    private String processedBy;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("inProcessSince")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date inProcessSince;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("setback")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date setback;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String lock;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String processClass;
    @JsonProperty("runTimeIsTemporary")
    private Boolean runTimeIsTemporary = false;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String documentation;

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

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public List<NameValuePair> getParams() {
        return params;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    /**
     * order type
     * <p>
     * the type of the order
     * 
     */
    @JsonProperty("_type")
    public OrderType get_type() {
        return _type;
    }

    /**
     * order type
     * <p>
     * the type of the order
     * 
     */
    @JsonProperty("_type")
    public void set_type(OrderType _type) {
        this._type = _type;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("stateText")
    public String getStateText() {
        return stateText;
    }

    @JsonProperty("stateText")
    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    public ConfigurationState getConfigurationStatus() {
        return configurationStatus;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    public void setConfigurationStatus(ConfigurationState configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    public String getEndState() {
        return endState;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    public void setEndState(String endState) {
        this.endState = endState;
    }

    /**
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("processingState")
    public OrderState getProcessingState() {
        return processingState;
    }

    /**
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("processingState")
    public void setProcessingState(OrderState processingState) {
        this.processingState = processingState;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStartTime")
    public Date getNextStartTime() {
        return nextStartTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStartTime")
    public void setNextStartTime(Date nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    @JsonProperty("nextStartNever")
    public Boolean getNextStartNever() {
        return nextStartNever;
    }

    @JsonProperty("nextStartNever")
    public void setNextStartNever(Boolean nextStartNever) {
        this.nextStartNever = nextStartNever;
    }

    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    public String getHistoryId() {
        return historyId;
    }

    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    public String getProcessedBy() {
        return processedBy;
    }

    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("inProcessSince")
    public Date getInProcessSince() {
        return inProcessSince;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("inProcessSince")
    public void setInProcessSince(Date inProcessSince) {
        this.inProcessSince = inProcessSince;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("setback")
    public Date getSetback() {
        return setback;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("setback")
    public void setSetback(Date setback) {
        this.setback = setback;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    public String getLock() {
        return lock;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    public void setLock(String lock) {
        this.lock = lock;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    @JsonProperty("runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    @JsonProperty("runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("orderId", orderId).append("workflow", workflow).append("priority", priority).append("params", params).append("_type", _type).append("surveyDate", surveyDate).append("state", state).append("title", title).append("job", job).append("stateText", stateText).append("configurationStatus", configurationStatus).append("endState", endState).append("processingState", processingState).append("nextStartTime", nextStartTime).append("nextStartNever", nextStartNever).append("historyId", historyId).append("startedAt", startedAt).append("processedBy", processedBy).append("taskId", taskId).append("inProcessSince", inProcessSince).append("setback", setback).append("lock", lock).append("processClass", processClass).append("runTimeIsTemporary", runTimeIsTemporary).append("documentation", documentation).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(endState).append(startedAt).append(processClass).append(title).append(path).append(historyId).append(lock).append(state).append(processedBy).append(setback).append(nextStartNever).append(inProcessSince).append(workflow).append(surveyDate).append(documentation).append(_type).append(priority).append(params).append(processingState).append(configurationStatus).append(stateText).append(nextStartTime).append(runTimeIsTemporary).append(job).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderV) == false) {
            return false;
        }
        OrderV rhs = ((OrderV) other);
        return new EqualsBuilder().append(orderId, rhs.orderId).append(endState, rhs.endState).append(startedAt, rhs.startedAt).append(processClass, rhs.processClass).append(title, rhs.title).append(path, rhs.path).append(historyId, rhs.historyId).append(lock, rhs.lock).append(state, rhs.state).append(processedBy, rhs.processedBy).append(setback, rhs.setback).append(nextStartNever, rhs.nextStartNever).append(inProcessSince, rhs.inProcessSince).append(workflow, rhs.workflow).append(surveyDate, rhs.surveyDate).append(documentation, rhs.documentation).append(_type, rhs._type).append(priority, rhs.priority).append(params, rhs.params).append(processingState, rhs.processingState).append(configurationStatus, rhs.configurationStatus).append(stateText, rhs.stateText).append(nextStartTime, rhs.nextStartTime).append(runTimeIsTemporary, rhs.runTimeIsTemporary).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
