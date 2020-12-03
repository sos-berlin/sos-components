
package com.sos.jobscheduler.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * outcome
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "required",
    "TYPE",
    "namedValues"
})
public class Outcome {

    @JsonProperty("required")
    private Object required;
    /**
     * outcomeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private OutcomeType tYPE;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables namedValues;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Outcome() {
    }

    /**
     * 
     * @param namedValues
     * @param tYPE
     * @param required
     */
    public Outcome(Object required, OutcomeType tYPE, Variables namedValues) {
        super();
        this.required = required;
        this.tYPE = tYPE;
        this.namedValues = namedValues;
    }

    @JsonProperty("required")
    public Object getRequired() {
        return required;
    }

    @JsonProperty("required")
    public void setRequired(Object required) {
        this.required = required;
    }

    /**
     * outcomeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public OutcomeType getTYPE() {
        return tYPE;
    }

    /**
     * outcomeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(OutcomeType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    public Variables getNamedValues() {
        return namedValues;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    public void setNamedValues(Variables namedValues) {
        this.namedValues = namedValues;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("required", required).append("tYPE", tYPE).append("namedValues", namedValues).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(required).append(namedValues).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Outcome) == false) {
            return false;
        }
        Outcome rhs = ((Outcome) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(required, rhs.required).append(namedValues, rhs.namedValues).isEquals();
    }

}
