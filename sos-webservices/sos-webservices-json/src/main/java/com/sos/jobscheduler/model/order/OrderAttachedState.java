
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
    "agentRefPath"
})
public class OrderAttachedState {

    /**
     * Attaching, Attached, ...
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("Attaching, Attached, ...")
    private String tYPE;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("agentRefPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String agentRefPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderAttachedState() {
    }

    /**
     * 
     * @param agentRefPath
     * @param tYPE
     */
    public OrderAttachedState(String tYPE, String agentRefPath) {
        super();
        this.tYPE = tYPE;
        this.agentRefPath = agentRefPath;
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

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("agentRefPath")
    public String getAgentRefPath() {
        return agentRefPath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("agentRefPath")
    public void setAgentRefPath(String agentRefPath) {
        this.agentRefPath = agentRefPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("agentRefPath", agentRefPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(agentRefPath).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(agentRefPath, rhs.agentRefPath).isEquals();
    }

}
