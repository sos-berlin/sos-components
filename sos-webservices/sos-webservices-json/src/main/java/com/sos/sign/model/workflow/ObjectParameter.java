
package com.sos.sign.model.workflow;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.workflow.ListParameterType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * object type parameter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE"
})
public class ObjectParameter {

    @JsonProperty("TYPE")
    private String tYPE = "Object";
    @JsonIgnore
    private Map<String, ListParameterType> additionalProperties = new HashMap<String, ListParameterType>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ObjectParameter() {
    }

    /**
     * 
     * @param tYPE
     */
    public ObjectParameter(String tYPE) {
        super();
        this.tYPE = tYPE;
    }

    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonAnyGetter
    public Map<String, ListParameterType> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, ListParameterType value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ObjectParameter) == false) {
            return false;
        }
        ObjectParameter rhs = ((ObjectParameter) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
