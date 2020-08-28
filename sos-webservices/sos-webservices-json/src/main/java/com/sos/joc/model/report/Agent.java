
package com.sos.joc.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "agent",
    "numOfSuccessfulTasks",
    "numOfJobs"
})
public class Agent {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("Url of an Agent")
    private String agent;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfSuccessfulTasks")
    private Long numOfSuccessfulTasks;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfJobs")
    private Long numOfJobs;

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    public String getAgent() {
        return agent;
    }

    /**
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfSuccessfulTasks")
    public Long getNumOfSuccessfulTasks() {
        return numOfSuccessfulTasks;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfSuccessfulTasks")
    public void setNumOfSuccessfulTasks(Long numOfSuccessfulTasks) {
        this.numOfSuccessfulTasks = numOfSuccessfulTasks;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfJobs")
    public Long getNumOfJobs() {
        return numOfJobs;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfJobs")
    public void setNumOfJobs(Long numOfJobs) {
        this.numOfJobs = numOfJobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("agent", agent).append("numOfSuccessfulTasks", numOfSuccessfulTasks).append("numOfJobs", numOfJobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfSuccessfulTasks).append(agent).append(jobschedulerId).append(numOfJobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Agent) == false) {
            return false;
        }
        Agent rhs = ((Agent) other);
        return new EqualsBuilder().append(numOfSuccessfulTasks, rhs.numOfSuccessfulTasks).append(agent, rhs.agent).append(jobschedulerId, rhs.jobschedulerId).append(numOfJobs, rhs.numOfJobs).isEquals();
    }

}
