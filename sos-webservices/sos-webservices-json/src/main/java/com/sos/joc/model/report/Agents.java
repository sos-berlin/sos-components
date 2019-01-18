
package com.sos.joc.model.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "deliveryDate",
    "agents",
    "totalNumOfSuccessfulTasks",
    "totalNumOfJobs"
})
public class Agents {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    private List<Agent> agents = new ArrayList<Agent>();
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
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     * @return
     *     The deliveryDate
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     * @param deliveryDate
     *     The deliveryDate
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The agents
     */
    @JsonProperty("agents")
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * 
     * (Required)
     * 
     * @param agents
     *     The agents
     */
    @JsonProperty("agents")
    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     * @return
     *     The totalNumOfSuccessfulTasks
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
     * @param totalNumOfSuccessfulTasks
     *     The totalNumOfSuccessfulTasks
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
     * @return
     *     The totalNumOfJobs
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
     * @param totalNumOfJobs
     *     The totalNumOfJobs
     */
    @JsonProperty("totalNumOfJobs")
    public void setTotalNumOfJobs(Long totalNumOfJobs) {
        this.totalNumOfJobs = totalNumOfJobs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(agents).append(totalNumOfSuccessfulTasks).append(totalNumOfJobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Agents) == false) {
            return false;
        }
        Agents rhs = ((Agents) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(agents, rhs.agents).append(totalNumOfSuccessfulTasks, rhs.totalNumOfSuccessfulTasks).append(totalNumOfJobs, rhs.totalNumOfJobs).isEquals();
    }

}
