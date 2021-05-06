
package com.sos.inventory.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * parameter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "default"
})
public class Parameter {

    /**
     * parameterType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ParameterType type;
    @JsonProperty("default")
    private Object _default;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Parameter() {
    }

    /**
     * 
     * @param _default
     * @param type
     */
    public Parameter(ParameterType type, Object _default) {
        super();
        this.type = type;
        this._default = _default;
    }

    /**
     * parameterType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ParameterType getType() {
        return type;
    }

    /**
     * parameterType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ParameterType type) {
        this.type = type;
    }

    @JsonProperty("default")
    public Object getDefault() {
        return _default;
    }

    @JsonProperty("default")
    public void setDefault(Object _default) {
        this._default = _default;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("_default", _default).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(_default).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Parameter) == false) {
            return false;
        }
        Parameter rhs = ((Parameter) other);
        return new EqualsBuilder().append(type, rhs.type).append(_default, rhs._default).isEquals();
    }

}
