
package com.sos.joc.model.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.agent.ClusterAgent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controllers
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "controllers",
    "agents",
    "currentSecurityLevel"
})
public class Controllers {

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
    @JsonProperty("controllers")
    private List<Controller> controllers = new ArrayList<Controller>();
    @JsonProperty("agents")
    private List<ClusterAgent> agents = new ArrayList<ClusterAgent>();
    @JsonProperty("currentSecurityLevel")
    private Object currentSecurityLevel;

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
    @JsonProperty("controllers")
    public List<Controller> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("agents")
    public List<ClusterAgent> getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    public void setAgents(List<ClusterAgent> agents) {
        this.agents = agents;
    }

    @JsonProperty("currentSecurityLevel")
    public Object getCurrentSecurityLevel() {
        return currentSecurityLevel;
    }

    @JsonProperty("currentSecurityLevel")
    public void setCurrentSecurityLevel(Object currentSecurityLevel) {
        this.currentSecurityLevel = currentSecurityLevel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("controllers", controllers).append("agents", agents).append("currentSecurityLevel", currentSecurityLevel).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(deliveryDate).append(currentSecurityLevel).append(agents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Controllers) == false) {
            return false;
        }
        Controllers rhs = ((Controllers) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(deliveryDate, rhs.deliveryDate).append(currentSecurityLevel, rhs.currentSecurityLevel).append(agents, rhs.agents).isEquals();
    }

}
