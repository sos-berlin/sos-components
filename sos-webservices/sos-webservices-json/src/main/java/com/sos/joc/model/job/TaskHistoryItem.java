
package com.sos.joc.model.job;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * task in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "controllerId",
    "job",
    "workflow",
    "orderId",
    "startTime",
    "endTime",
    "position",
    "state",
    "criticality",
    "taskId",
    "agentUrl",
    "exitCode",
    "error"
})
public class TaskHistoryItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    private String job;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    private String workflow;
    @JsonProperty("orderId")
    private String orderId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endTime;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;
    /**
     * orderHistory state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private HistoryState state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("criticality")
    private String criticality;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    private String agentUrl;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    private Integer exitCode;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public Date getStartTime() {
        return startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public Date getEndTime() {
        return endTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * orderHistory state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public HistoryState getState() {
        return state;
    }

    /**
     * orderHistory state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(HistoryState state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("criticality")
    public String getCriticality() {
        return criticality;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("criticality")
    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    public String getAgentUrl() {
        return agentUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentUrl")
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("controllerId", controllerId).append("job", job).append("workflow", workflow).append("orderId", orderId).append("startTime", startTime).append("endTime", endTime).append("position", position).append("state", state).append("criticality", criticality).append("taskId", taskId).append("agentUrl", agentUrl).append("exitCode", exitCode).append("error", error).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(surveyDate).append(controllerId).append(workflow).append(orderId).append(criticality).append(error).append(exitCode).append(startTime).append(endTime).append(position).append(state).append(agentUrl).append(job).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskHistoryItem) == false) {
            return false;
        }
        TaskHistoryItem rhs = ((TaskHistoryItem) other);
        return new EqualsBuilder().append(surveyDate, rhs.surveyDate).append(controllerId, rhs.controllerId).append(workflow, rhs.workflow).append(orderId, rhs.orderId).append(criticality, rhs.criticality).append(error, rhs.error).append(exitCode, rhs.exitCode).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(position, rhs.position).append(state, rhs.state).append(agentUrl, rhs.agentUrl).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
