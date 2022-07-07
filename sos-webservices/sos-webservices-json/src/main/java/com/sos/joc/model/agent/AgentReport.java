
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "url",
    "numOfSuccessfulTasks",
    "numOfJobs"
})
public class AgentReport {

    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
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

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        if (url != null && !"/".equals(url) && url.endsWith("/")) {
            url = url.replaceFirst("/$", ""); 
        }
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("url", url).append("numOfSuccessfulTasks", numOfSuccessfulTasks).append("numOfJobs", numOfJobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfSuccessfulTasks).append(agentId).append(controllerId).append(numOfJobs).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentReport) == false) {
            return false;
        }
        AgentReport rhs = ((AgentReport) other);
        return new EqualsBuilder().append(numOfSuccessfulTasks, rhs.numOfSuccessfulTasks).append(agentId, rhs.agentId).append(controllerId, rhs.controllerId).append(numOfJobs, rhs.numOfJobs).append(url, rhs.url).isEquals();
    }

}
