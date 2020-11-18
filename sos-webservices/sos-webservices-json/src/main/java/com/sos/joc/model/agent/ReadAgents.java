
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "onlyEnabledAgents"
})
public class ReadAgents {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("onlyEnabledAgents")
    private Boolean onlyEnabledAgents = false;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("onlyEnabledAgents")
    public Boolean getOnlyEnabledAgents() {
        return onlyEnabledAgents;
    }

    @JsonProperty("onlyEnabledAgents")
    public void setOnlyEnabledAgents(Boolean onlyEnabledAgents) {
        this.onlyEnabledAgents = onlyEnabledAgents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("onlyEnabledAgents", onlyEnabledAgents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(onlyEnabledAgents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadAgents) == false) {
            return false;
        }
        ReadAgents rhs = ((ReadAgents) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(onlyEnabledAgents, rhs.onlyEnabledAgents).isEquals();
    }

}
