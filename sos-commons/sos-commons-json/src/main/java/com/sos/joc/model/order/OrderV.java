
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobChain",
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
    "historyId",
    "startedAt",
    "processedBy",
    "taskId",
    "inProcessSince",
    "setback",
    "lock",
    "processClass",
    "runTimeIsTemporary"
})
public class OrderV {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "jobChain")
    private String jobChain;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    @JacksonXmlProperty(localName = "priority")
    private Integer priority;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    @JacksonXmlProperty(localName = "param")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * order type
     * <p>
     * the type of the order
     * 
     */
    @JsonProperty("_type")
    @JsonPropertyDescription("the type of the order")
    @JacksonXmlProperty(localName = "_type")
    private OrderType _type;
    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("the name of the node")
    @JacksonXmlProperty(localName = "state")
    private String state;
    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    private String title;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "job")
    private String job;
    @JsonProperty("stateText")
    @JacksonXmlProperty(localName = "stateText")
    private String stateText;
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    private ConfigurationState configurationStatus;
    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    @JsonPropertyDescription("the name of the end node")
    @JacksonXmlProperty(localName = "endState")
    private String endState;
    /**
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("processingState")
    @JacksonXmlProperty(localName = "processingState")
    private OrderState processingState;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("nextStartTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "nextStartTime")
    private Date nextStartTime;
    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    @JsonPropertyDescription("for all orders except pending orders")
    @JacksonXmlProperty(localName = "historyId")
    private String historyId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("startedAt")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "startedAt")
    private Date startedAt;
    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    @JsonPropertyDescription("ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent")
    @JacksonXmlProperty(localName = "processedBy")
    private String processedBy;
    /**
     * ONLY for running order
     * 
     */
    @JsonProperty("taskId")
    @JsonPropertyDescription("ONLY for running order")
    @JacksonXmlProperty(localName = "taskId")
    private String taskId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("inProcessSince")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "inProcessSince")
    private Date inProcessSince;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("setback")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "setback")
    private Date setback;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "lock")
    private String lock;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "processClass")
    private String processClass;
    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    private Boolean runTimeIsTemporary = false;

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
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
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    @JacksonXmlProperty(localName = "priority")
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
    @JacksonXmlProperty(localName = "priority")
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
    @JacksonXmlProperty(localName = "param")
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
    @JacksonXmlProperty(localName = "param")
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
    @JacksonXmlProperty(localName = "_type")
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
    @JacksonXmlProperty(localName = "_type")
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
    @JacksonXmlProperty(localName = "surveyDate")
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
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public String getState() {
        return state;
    }

    /**
     * the name of the node
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("stateText")
    @JacksonXmlProperty(localName = "stateText")
    public String getStateText() {
        return stateText;
    }

    @JsonProperty("stateText")
    @JacksonXmlProperty(localName = "stateText")
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
    @JacksonXmlProperty(localName = "configurationStatus")
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
    @JacksonXmlProperty(localName = "configurationStatus")
    public void setConfigurationStatus(ConfigurationState configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    @JacksonXmlProperty(localName = "endState")
    public String getEndState() {
        return endState;
    }

    /**
     * the name of the end node
     * 
     */
    @JsonProperty("endState")
    @JacksonXmlProperty(localName = "endState")
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
    @JacksonXmlProperty(localName = "processingState")
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
    @JacksonXmlProperty(localName = "processingState")
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
    @JacksonXmlProperty(localName = "nextStartTime")
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
    @JacksonXmlProperty(localName = "nextStartTime")
    public void setNextStartTime(Date nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    public String getHistoryId() {
        return historyId;
    }

    /**
     * for all orders except pending orders
     * 
     */
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
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
    @JacksonXmlProperty(localName = "startedAt")
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
    @JacksonXmlProperty(localName = "startedAt")
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    @JacksonXmlProperty(localName = "processedBy")
    public String getProcessedBy() {
        return processedBy;
    }

    /**
     * ONLY for running or blacklist order, contains Host/port of an active cluster member or URL of a JobScheduler Agent
     * 
     */
    @JsonProperty("processedBy")
    @JacksonXmlProperty(localName = "processedBy")
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    /**
     * ONLY for running order
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    public String getTaskId() {
        return taskId;
    }

    /**
     * ONLY for running order
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
     * 
     */
    @JsonProperty("inProcessSince")
    @JacksonXmlProperty(localName = "inProcessSince")
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
    @JacksonXmlProperty(localName = "inProcessSince")
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
    @JacksonXmlProperty(localName = "setback")
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
    @JacksonXmlProperty(localName = "setback")
    public void setSetback(Date setback) {
        this.setback = setback;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    @JacksonXmlProperty(localName = "lock")
    public String getLock() {
        return lock;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("lock")
    @JacksonXmlProperty(localName = "lock")
    public void setLock(String lock) {
        this.lock = lock;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    @JsonProperty("runTimeIsTemporary")
    @JacksonXmlProperty(localName = "runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("orderId", orderId).append("jobChain", jobChain).append("priority", priority).append("params", params).append("_type", _type).append("surveyDate", surveyDate).append("state", state).append("title", title).append("job", job).append("stateText", stateText).append("configurationStatus", configurationStatus).append("endState", endState).append("processingState", processingState).append("nextStartTime", nextStartTime).append("historyId", historyId).append("startedAt", startedAt).append("processedBy", processedBy).append("taskId", taskId).append("inProcessSince", inProcessSince).append("setback", setback).append("lock", lock).append("processClass", processClass).append("runTimeIsTemporary", runTimeIsTemporary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(endState).append(startedAt).append(processClass).append(title).append(path).append(historyId).append(lock).append(state).append(processedBy).append(setback).append(inProcessSince).append(surveyDate).append(jobChain).append(_type).append(priority).append(params).append(processingState).append(configurationStatus).append(stateText).append(nextStartTime).append(runTimeIsTemporary).append(job).append(taskId).toHashCode();
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
        return new EqualsBuilder().append(orderId, rhs.orderId).append(endState, rhs.endState).append(startedAt, rhs.startedAt).append(processClass, rhs.processClass).append(title, rhs.title).append(path, rhs.path).append(historyId, rhs.historyId).append(lock, rhs.lock).append(state, rhs.state).append(processedBy, rhs.processedBy).append(setback, rhs.setback).append(inProcessSince, rhs.inProcessSince).append(surveyDate, rhs.surveyDate).append(jobChain, rhs.jobChain).append(_type, rhs._type).append(priority, rhs.priority).append(params, rhs.params).append(processingState, rhs.processingState).append(configurationStatus, rhs.configurationStatus).append(stateText, rhs.stateText).append(nextStartTime, rhs.nextStartTime).append(runTimeIsTemporary, rhs.runTimeIsTemporary).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
