
package com.sos.joc.model.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
@JsonPropertyOrder({
    "deliveryDate",
    "agents",
    "totalNumOfSuccessfulTasks"
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
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "deliveryDate")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "agents")
    private List<Agent> agents = new ArrayList<Agent>();
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfSuccessfulTasks")
    @JacksonXmlProperty(localName = "totalNumOfSuccessfulTasks")
    private Long totalNumOfSuccessfulTasks;

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalNumOfSuccessfulTasks")
    @JacksonXmlProperty(localName = "totalNumOfSuccessfulTasks")
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
    @JacksonXmlProperty(localName = "totalNumOfSuccessfulTasks")
    public void setTotalNumOfSuccessfulTasks(Long totalNumOfSuccessfulTasks) {
        this.totalNumOfSuccessfulTasks = totalNumOfSuccessfulTasks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("agents", agents).append("totalNumOfSuccessfulTasks", totalNumOfSuccessfulTasks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(totalNumOfSuccessfulTasks).append(deliveryDate).append(agents).toHashCode();
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
        return new EqualsBuilder().append(totalNumOfSuccessfulTasks, rhs.totalNumOfSuccessfulTasks).append(deliveryDate, rhs.deliveryDate).append(agents, rhs.agents).isEquals();
    }

}
