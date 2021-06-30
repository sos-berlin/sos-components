
package com.sos.inventory.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "default",
    "final"
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
    /**
     * this value has to have the data type of the 'type' attribute
     * 
     */
    @JsonProperty("default")
    @JsonPropertyDescription("this value has to have the data type of the 'type' attribute")
    private Object _default;
    @JsonProperty("final")
    private String _final;

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
     * @param _final
     */
    public Parameter(ParameterType type, Object _default, String _final) {
        super();
        this.type = type;
        this._default = _default;
        this._final = _final;
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

    @JsonProperty("final")
    public String getFinal() {
        return _final;
    }

    @JsonProperty("final")
    public void setFinal(String _final) {
        this._final = _final;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("_default", _default).append("_final", _final).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_default).append(type).append(_final).toHashCode();
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
        return new EqualsBuilder().append(_default, rhs._default).append(type, rhs.type).append(_final, rhs._final).isEquals();
    }

}
