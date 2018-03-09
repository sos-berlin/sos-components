
package com.sos.joc.model.job;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
    "job",
    "startTime",
    "endTime",
    "state",
    "taskId",
    "clusterMember",
    "steps",
    "exitCode",
    "error",
    "agent"
})
public class TaskHistoryItem {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    private String job;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "startTime")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "endTime")
    private Date endTime;
    /**
     * orderHistory state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private HistoryState state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    @JacksonXmlProperty(localName = "taskId")
    private String taskId;
    @JsonProperty("clusterMember")
    @JacksonXmlProperty(localName = "clusterMember")
    private String clusterMember;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "steps")
    private Integer steps;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    @JacksonXmlProperty(localName = "exitCode")
    private Integer exitCode;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    private Err error;
    /**
     * agent url
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("agent url")
    @JacksonXmlProperty(localName = "agent")
    private String agent;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    @JacksonXmlProperty(localName = "startTime")
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
    @JacksonXmlProperty(localName = "startTime")
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
    @JacksonXmlProperty(localName = "endTime")
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
    @JacksonXmlProperty(localName = "endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * orderHistory state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
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
    @JacksonXmlProperty(localName = "state")
    public void setState(HistoryState state) {
        this.state = state;
    }

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

    @JsonProperty("clusterMember")
    @JacksonXmlProperty(localName = "clusterMember")
    public String getClusterMember() {
        return clusterMember;
    }

    @JsonProperty("clusterMember")
    @JacksonXmlProperty(localName = "clusterMember")
    public void setClusterMember(String clusterMember) {
        this.clusterMember = clusterMember;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "steps")
    public Integer getSteps() {
        return steps;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "steps")
    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    @JacksonXmlProperty(localName = "exitCode")
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
    @JacksonXmlProperty(localName = "exitCode")
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
    @JacksonXmlProperty(localName = "error")
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
    @JacksonXmlProperty(localName = "error")
    public void setError(Err error) {
        this.error = error;
    }

    /**
     * agent url
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public String getAgent() {
        return agent;
    }

    /**
     * agent url
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("jobschedulerId", jobschedulerId).append("job", job).append("startTime", startTime).append("endTime", endTime).append("state", state).append("taskId", taskId).append("clusterMember", clusterMember).append("steps", steps).append("exitCode", exitCode).append("error", error).append("agent", agent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agent).append(surveyDate).append(error).append(steps).append(clusterMember).append(exitCode).append(startTime).append(endTime).append(state).append(jobschedulerId).append(job).append(taskId).toHashCode();
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
        return new EqualsBuilder().append(agent, rhs.agent).append(surveyDate, rhs.surveyDate).append(error, rhs.error).append(steps, rhs.steps).append(clusterMember, rhs.clusterMember).append(exitCode, rhs.exitCode).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(state, rhs.state).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).append(taskId, rhs.taskId).isEquals();
    }

}
