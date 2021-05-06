
package com.sos.inventory.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order or job requirements
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "parameters"
})
public class Requirements {

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
    public Requirements() {
    }

    /**
     * 
     * @param parameters
     */
    public Requirements(Parameters parameters) {
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
        if ((other instanceof Requirements) == false) {
            return false;
        }
        Requirements rhs = ((Requirements) other);
        return new EqualsBuilder().append(parameters, rhs.parameters).isEquals();
    }

}
