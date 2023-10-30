
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * agent reports
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "agents",
    "totalNumOfSuccessfulTasks",
    "totalNumOfJobs"
})
public class AgentReports {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    private List<AgentReport> agents = new ArrayList<AgentReport>();
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfSuccessfulTasks")
    private Long totalNumOfSuccessfulTasks;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfJobs")
    private Long totalNumOfJobs;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    public List<AgentReport> getAgents() {
        return agents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    public void setAgents(List<AgentReport> agents) {
        this.agents = agents;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfSuccessfulTasks")
    public Long getTotalNumOfSuccessfulTasks() {
        return totalNumOfSuccessfulTasks;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfSuccessfulTasks")
    public void setTotalNumOfSuccessfulTasks(Long totalNumOfSuccessfulTasks) {
        this.totalNumOfSuccessfulTasks = totalNumOfSuccessfulTasks;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfJobs")
    public Long getTotalNumOfJobs() {
        return totalNumOfJobs;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfJobs")
    public void setTotalNumOfJobs(Long totalNumOfJobs) {
        this.totalNumOfJobs = totalNumOfJobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("agents", agents).append("totalNumOfSuccessfulTasks", totalNumOfSuccessfulTasks).append("totalNumOfJobs", totalNumOfJobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(totalNumOfSuccessfulTasks).append(deliveryDate).append(totalNumOfJobs).append(agents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentReports) == false) {
            return false;
        }
        AgentReports rhs = ((AgentReports) other);
        return new EqualsBuilder().append(totalNumOfSuccessfulTasks, rhs.totalNumOfSuccessfulTasks).append(deliveryDate, rhs.deliveryDate).append(totalNumOfJobs, rhs.totalNumOfJobs).append(agents, rhs.agents).isEquals();
    }

}
