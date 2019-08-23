
package com.sos.joc.model.processClass;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * process
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "taskId",
    "pid",
    "runningSince",
    "agent"
})
public class Process {

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
     * (Required)
     * 
     */
    @JsonProperty("pid")
    private Integer pid;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("runningSince")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date runningSince;
    /**
     * url
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("url")
    private String agent;

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
     * (Required)
     * 
     */
    @JsonProperty("pid")
    public Integer getPid() {
        return pid;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
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
    @JsonProperty("runningSince")
    public Date getRunningSince() {
        return runningSince;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("runningSince")
    public void setRunningSince(Date runningSince) {
        this.runningSince = runningSince;
    }

    /**
     * url
     * 
     */
    @JsonProperty("agent")
    public String getAgent() {
        return agent;
    }

    /**
     * url
     * 
     */
    @JsonProperty("agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("taskId", taskId).append("pid", pid).append("runningSince", runningSince).append("agent", agent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pid).append(agent).append(job).append(runningSince).append(taskId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Process) == false) {
            return false;
        }
        Process rhs = ((Process) other);
        return new EqualsBuilder().append(pid, rhs.pid).append(agent, rhs.agent).append(job, rhs.job).append(runningSince, rhs.runningSince).append(taskId, rhs.taskId).isEquals();
    }

}
