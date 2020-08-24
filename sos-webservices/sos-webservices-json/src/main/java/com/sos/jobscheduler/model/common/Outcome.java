
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
    "returnCode",
    "keyValues"
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
    @JsonProperty("returnCode")
    private Integer returnCode;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("keyValues")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables keyValues;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Outcome() {
    }

    /**
     * 
     * @param returnCode
     * @param tYPE
     * @param keyValues
     * @param required
     */
    public Outcome(Object required, OutcomeType tYPE, Integer returnCode, Variables keyValues) {
        super();
        this.required = required;
        this.tYPE = tYPE;
        this.returnCode = returnCode;
        this.keyValues = keyValues;
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

    @JsonProperty("returnCode")
    public Integer getReturnCode() {
        return returnCode;
    }

    @JsonProperty("returnCode")
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("keyValues")
    public Variables getKeyValues() {
        return keyValues;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("keyValues")
    public void setKeyValues(Variables keyValues) {
        this.keyValues = keyValues;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("required", required).append("tYPE", tYPE).append("returnCode", returnCode).append("keyValues", keyValues).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(returnCode).append(tYPE).append(keyValues).append(required).toHashCode();
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
        return new EqualsBuilder().append(returnCode, rhs.returnCode).append(tYPE, rhs.tYPE).append(keyValues, rhs.keyValues).append(required, rhs.required).isEquals();
    }

}
