
package com.sos.joc.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.job.TaskCause;
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
    "cause",
    "numOfSuccessfulTasks"
})
public class Agent {

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("Url of an Agent")
    @JacksonXmlProperty(localName = "agent")
    private String agent;
    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * (Required)
     * 
     */
    @JsonProperty("cause")
    @JsonPropertyDescription("For order jobs only cause=order possible")
    @JacksonXmlProperty(localName = "cause")
    private TaskCause cause;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfSuccessfulTasks")
    @JacksonXmlProperty(localName = "numOfSuccessfulTasks")
    private Long numOfSuccessfulTasks;

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
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public String getAgent() {
        return agent;
    }

    /**
     * Url of an Agent
     * (Required)
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * (Required)
     * 
     */
    @JsonProperty("cause")
    @JacksonXmlProperty(localName = "cause")
    public TaskCause getCause() {
        return cause;
    }

    /**
     * task cause
     * <p>
     * For order jobs only cause=order possible
     * (Required)
     * 
     */
    @JsonProperty("cause")
    @JacksonXmlProperty(localName = "cause")
    public void setCause(TaskCause cause) {
        this.cause = cause;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("numOfSuccessfulTasks")
    @JacksonXmlProperty(localName = "numOfSuccessfulTasks")
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
    @JacksonXmlProperty(localName = "numOfSuccessfulTasks")
    public void setNumOfSuccessfulTasks(Long numOfSuccessfulTasks) {
        this.numOfSuccessfulTasks = numOfSuccessfulTasks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("agent", agent).append("cause", cause).append("numOfSuccessfulTasks", numOfSuccessfulTasks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfSuccessfulTasks).append(cause).append(agent).append(jobschedulerId).toHashCode();
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
        return new EqualsBuilder().append(numOfSuccessfulTasks, rhs.numOfSuccessfulTasks).append(cause, rhs.cause).append(agent, rhs.agent).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
