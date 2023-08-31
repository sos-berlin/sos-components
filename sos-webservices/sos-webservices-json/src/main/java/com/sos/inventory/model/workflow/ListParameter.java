
package com.sos.inventory.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * list parameter
 * <p>
 * parameter type only Number, Boolean or String
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "default"
})
public class ListParameter {

    /**
     * parameterType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ListParameterType type;
    /**
     * this value has to have the data type of the 'type' attribute
     * 
     */
    @JsonProperty("default")
    @JsonPropertyDescription("this value has to have the data type of the 'type' attribute")
    private Object _default;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ListParameter() {
    }

    /**
     * 
     * @param _default
     * @param type
     */
    public ListParameter(ListParameterType type, Object _default) {
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
    public ListParameterType getType() {
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
    public void setType(ListParameterType type) {
        this.type = type;
    }

    /**
     * this value has to have the data type of the 'type' attribute
     * 
     */
    @JsonProperty("default")
    public Object getDefault() {
        return _default;
    }

    /**
     * this value has to have the data type of the 'type' attribute
     * 
     */
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
        if ((other instanceof ListParameter) == false) {
            return false;
        }
        ListParameter rhs = ((ListParameter) other);
        return new EqualsBuilder().append(type, rhs.type).append(_default, rhs._default).isEquals();
    }

}
