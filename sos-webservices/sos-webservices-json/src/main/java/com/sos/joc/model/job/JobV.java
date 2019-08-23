
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.ConfigurationState;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersSummary;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "orderQueue",
    "allTasks",
    "allSteps",
    "state",
    "stateText",
    "locks",
    "temporary",
    "numOfRunningTasks",
    "runningTasks",
    "numOfQueuedTasks",
    "taskQueue",
    "params",
    "configurationStatus",
    "error",
    "ordersSummary",
    "nextStartTime",
    "nextStartNever",
    "delayUntil",
    "runTimeIsTemporary"
})
public class JobV {

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
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    @JsonProperty("name")
    private String name;
    /**
     * Only for /job/orderQueue
     * 
     */
    @JsonProperty("orderQueue")
    @JsonPropertyDescription("Only for /job/orderQueue")
    private List<OrderV> orderQueue = new ArrayList<OrderV>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allTasks")
    private Integer allTasks;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allSteps")
    private Integer allSteps;
    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobState state;
    @JsonProperty("stateText")
    private String stateText;
    /**
     * job locks (volatile)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    private List<LockUseV> locks = new ArrayList<LockUseV>();
    @JsonProperty("temporary")
    private Boolean temporary;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRunningTasks")
    private Integer numOfRunningTasks;
    @JsonProperty("runningTasks")
    private List<RunningTask> runningTasks = new ArrayList<RunningTask>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfQueuedTasks")
    private Integer numOfQueuedTasks;
    @JsonProperty("taskQueue")
    private List<QueuedTask> taskQueue = new ArrayList<QueuedTask>();
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    private ConfigurationState configurationStatus;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    @JsonPropertyDescription("only relevant for order jobs and is empty if job's order queue is empty")
    private OrdersSummary ordersSummary;
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("delayUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date delayUntil;
    @JsonProperty("runTimeIsTemporary")
    private Boolean runTimeIsTemporary = false;

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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Only for /job/orderQueue
     * 
     */
    @JsonProperty("orderQueue")
    public List<OrderV> getOrderQueue() {
        return orderQueue;
    }

    /**
     * Only for /job/orderQueue
     * 
     */
    @JsonProperty("orderQueue")
    public void setOrderQueue(List<OrderV> orderQueue) {
        this.orderQueue = orderQueue;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allTasks")
    public Integer getAllTasks() {
        return allTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allTasks")
    public void setAllTasks(Integer allTasks) {
        this.allTasks = allTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allSteps")
    public Integer getAllSteps() {
        return allSteps;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("allSteps")
    public void setAllSteps(Integer allSteps) {
        this.allSteps = allSteps;
    }

    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public JobState getState() {
        return state;
    }

    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(JobState state) {
        this.state = state;
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
     * job locks (volatile)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    public List<LockUseV> getLocks() {
        return locks;
    }

    /**
     * job locks (volatile)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    public void setLocks(List<LockUseV> locks) {
        this.locks = locks;
    }

    @JsonProperty("temporary")
    public Boolean getTemporary() {
        return temporary;
    }

    @JsonProperty("temporary")
    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRunningTasks")
    public Integer getNumOfRunningTasks() {
        return numOfRunningTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfRunningTasks")
    public void setNumOfRunningTasks(Integer numOfRunningTasks) {
        this.numOfRunningTasks = numOfRunningTasks;
    }

    @JsonProperty("runningTasks")
    public List<RunningTask> getRunningTasks() {
        return runningTasks;
    }

    @JsonProperty("runningTasks")
    public void setRunningTasks(List<RunningTask> runningTasks) {
        this.runningTasks = runningTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfQueuedTasks")
    public Integer getNumOfQueuedTasks() {
        return numOfQueuedTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfQueuedTasks")
    public void setNumOfQueuedTasks(Integer numOfQueuedTasks) {
        this.numOfQueuedTasks = numOfQueuedTasks;
    }

    @JsonProperty("taskQueue")
    public List<QueuedTask> getTaskQueue() {
        return taskQueue;
    }

    @JsonProperty("taskQueue")
    public void setTaskQueue(List<QueuedTask> taskQueue) {
        this.taskQueue = taskQueue;
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
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public void setError(Err error) {
        this.error = error;
    }

    /**
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    public OrdersSummary getOrdersSummary() {
        return ordersSummary;
    }

    /**
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    public void setOrdersSummary(OrdersSummary ordersSummary) {
        this.ordersSummary = ordersSummary;
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("delayUntil")
    public Date getDelayUntil() {
        return delayUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("delayUntil")
    public void setDelayUntil(Date delayUntil) {
        this.delayUntil = delayUntil;
    }

    @JsonProperty("runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    @JsonProperty("runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("orderQueue", orderQueue).append("allTasks", allTasks).append("allSteps", allSteps).append("state", state).append("stateText", stateText).append("locks", locks).append("temporary", temporary).append("numOfRunningTasks", numOfRunningTasks).append("runningTasks", runningTasks).append("numOfQueuedTasks", numOfQueuedTasks).append("taskQueue", taskQueue).append("params", params).append("configurationStatus", configurationStatus).append("error", error).append("ordersSummary", ordersSummary).append("nextStartTime", nextStartTime).append("nextStartNever", nextStartNever).append("delayUntil", delayUntil).append("runTimeIsTemporary", runTimeIsTemporary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(allSteps).append(temporary).append(nextStartNever).append(surveyDate).append(allTasks).append(numOfQueuedTasks).append(params).append(error).append(locks).append(taskQueue).append(path).append(orderQueue).append(configurationStatus).append(stateText).append(name).append(nextStartTime).append(numOfRunningTasks).append(state).append(ordersSummary).append(runTimeIsTemporary).append(delayUntil).append(runningTasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobV) == false) {
            return false;
        }
        JobV rhs = ((JobV) other);
        return new EqualsBuilder().append(allSteps, rhs.allSteps).append(temporary, rhs.temporary).append(nextStartNever, rhs.nextStartNever).append(surveyDate, rhs.surveyDate).append(allTasks, rhs.allTasks).append(numOfQueuedTasks, rhs.numOfQueuedTasks).append(params, rhs.params).append(error, rhs.error).append(locks, rhs.locks).append(taskQueue, rhs.taskQueue).append(path, rhs.path).append(orderQueue, rhs.orderQueue).append(configurationStatus, rhs.configurationStatus).append(stateText, rhs.stateText).append(name, rhs.name).append(nextStartTime, rhs.nextStartTime).append(numOfRunningTasks, rhs.numOfRunningTasks).append(state, rhs.state).append(ordersSummary, rhs.ordersSummary).append(runTimeIsTemporary, rhs.runTimeIsTemporary).append(delayUntil, rhs.delayUntil).append(runningTasks, rhs.runningTasks).isEquals();
    }

}
