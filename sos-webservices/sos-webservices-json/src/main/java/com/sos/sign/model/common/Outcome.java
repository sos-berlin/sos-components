
package com.sos.sign.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "message",
    "namedValues"
})
public class Outcome {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private String tYPE;
    @JsonProperty("message")
    private String message;
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
     * @param message
     */
    public Outcome(String tYPE, String message, Variables namedValues) {
        super();
        this.tYPE = tYPE;
        this.message = message;
        this.namedValues = namedValues;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("message", message).append("namedValues", namedValues).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(message).append(namedValues).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(message, rhs.message).append(namedValues, rhs.namedValues).isEquals();
    }

}
