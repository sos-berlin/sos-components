
package com.sos.sign.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * order or job requirements
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "parameters",
    "allowUndeclared"
})
public class OrderPreparation {

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    private Parameters parameters;
    @JsonProperty("allowUndeclared")
    private Boolean allowUndeclared;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderPreparation() {
    }

    /**
     * 
     * @param allowUndeclared
     * @param parameters
     */
    public OrderPreparation(Parameters parameters, Boolean allowUndeclared) {
        super();
        this.parameters = parameters;
        this.allowUndeclared = allowUndeclared;
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

    @JsonProperty("allowUndeclared")
    public Boolean getAllowUndeclared() {
        return allowUndeclared;
    }

    @JsonProperty("allowUndeclared")
    public void setAllowUndeclared(Boolean allowUndeclared) {
        this.allowUndeclared = allowUndeclared;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("parameters", parameters).append("allowUndeclared", allowUndeclared).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(parameters).append(allowUndeclared).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderPreparation) == false) {
            return false;
        }
        OrderPreparation rhs = ((OrderPreparation) other);
        return new EqualsBuilder().append(parameters, rhs.parameters).append(allowUndeclared, rhs.allowUndeclared).isEquals();
    }

}
