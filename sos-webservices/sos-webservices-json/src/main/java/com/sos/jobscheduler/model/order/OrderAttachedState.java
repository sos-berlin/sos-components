
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderAttachedState
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "agentName"
})
public class OrderAttachedState {

    /**
     * Attaching, Attached, ...
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("Attaching, Attached, ...")
    private String tYPE;
    @JsonProperty("agentName")
    private String agentName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderAttachedState() {
    }

    /**
     * 
     * @param agentName
     * @param tYPE
     */
    public OrderAttachedState(String tYPE, String agentName) {
        super();
        this.tYPE = tYPE;
        this.agentName = agentName;
    }

    /**
     * Attaching, Attached, ...
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * Attaching, Attached, ...
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("agentName", agentName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(agentName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderAttachedState) == false) {
            return false;
        }
        OrderAttachedState rhs = ((OrderAttachedState) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(agentName, rhs.agentName).isEquals();
    }

}
