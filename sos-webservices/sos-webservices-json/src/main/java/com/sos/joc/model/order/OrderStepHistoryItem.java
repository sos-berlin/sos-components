
package com.sos.joc.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * item of step history collection of one order run
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "node",
    "job",
    "step",
    "startTime",
    "endTime",
    "taskId",
    "clusterMember",
    "exitCode",
    "error",
    "agent"
})
public class OrderStepHistoryItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("node")
    private String node;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("step")
    private Integer step;
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
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskId")
    private Long taskId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterMember")
    private Integer clusterMember;
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
     * agent url
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("agent url")
    private String agent;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("node")
    public String getNode() {
        return node;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("node")
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("step")
    public Integer getStep() {
        return step;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("step")
    public void setStep(Integer step) {
        this.step = step;
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
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterMember")
    public Integer getClusterMember() {
        return clusterMember;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterMember")
    public void setClusterMember(Integer clusterMember) {
        this.clusterMember = clusterMember;
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

    /**
     * agent url
     * 
     */
    @JsonProperty("agent")
    public String getAgent() {
        return agent;
    }

    /**
     * agent url
     * 
     */
    @JsonProperty("agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("node", node).append("job", job).append("step", step).append("startTime", startTime).append("endTime", endTime).append("taskId", taskId).append("clusterMember", clusterMember).append("exitCode", exitCode).append("error", error).append("agent", agent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(node).append(agent).append(clusterMember).append(exitCode).append(step).append(startTime).append(endTime).append(job).append(error).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderStepHistoryItem) == false) {
            return false;
        }
        OrderStepHistoryItem rhs = ((OrderStepHistoryItem) other);
        return new EqualsBuilder().append(node, rhs.node).append(agent, rhs.agent).append(clusterMember, rhs.clusterMember).append(exitCode, rhs.exitCode).append(step, rhs.step).append(startTime, rhs.startTime).append(endTime, rhs.endTime).append(job, rhs.job).append(error, rhs.error).append(taskId, rhs.taskId).isEquals();
    }

}
