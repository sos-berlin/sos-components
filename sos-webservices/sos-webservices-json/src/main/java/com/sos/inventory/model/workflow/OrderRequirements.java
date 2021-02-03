
package com.sos.inventory.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * orderRequirements
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "parameters"
})
public class OrderRequirements {

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    private Parameters parameters;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderRequirements() {
    }

    /**
     * 
     * @param parameters
     */
    public OrderRequirements(Parameters parameters) {
        super();
        this.parameters = parameters;
    }

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("parameters", parameters).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(parameters).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderRequirements) == false) {
            return false;
        }
        OrderRequirements rhs = ((OrderRequirements) other);
        return new EqualsBuilder().append(parameters, rhs.parameters).isEquals();
    }

}
