
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
    "final",
    "listParameters",
    "facet",
    "message"
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
     * list parameters
     * <p>
     * parameters only for parameter type 'List'
     * 
     */
    @JsonProperty("listParameters")
    @JsonPropertyDescription("parameters only for parameter type 'List'")
    private ListParameters listParameters;
    /**
     * a regular expression to check the value of the parameter
     * 
     */
    @JsonProperty("facet")
    @JsonPropertyDescription("a regular expression to check the value of the parameter")
    private String facet;
    /**
     * a message if the value doesn't match the facet
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("a message if the value doesn't match the facet")
    private String message;

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
     * @param message
     * @param facet
     * @param listParameters
     */
    public Parameter(ParameterType type, Object _default, String _final, ListParameters listParameters, String facet, String message) {
        super();
        this.type = type;
        this._default = _default;
        this._final = _final;
        this.listParameters = listParameters;
        this.facet = facet;
        this.message = message;
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

    /**
     * list parameters
     * <p>
     * parameters only for parameter type 'List'
     * 
     */
    @JsonProperty("listParameters")
    public ListParameters getListParameters() {
        return listParameters;
    }

    /**
     * list parameters
     * <p>
     * parameters only for parameter type 'List'
     * 
     */
    @JsonProperty("listParameters")
    public void setListParameters(ListParameters listParameters) {
        this.listParameters = listParameters;
    }

    /**
     * a regular expression to check the value of the parameter
     * 
     */
    @JsonProperty("facet")
    public String getFacet() {
        return facet;
    }

    /**
     * a regular expression to check the value of the parameter
     * 
     */
    @JsonProperty("facet")
    public void setFacet(String facet) {
        this.facet = facet;
    }

    /**
     * a message if the value doesn't match the facet
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * a message if the value doesn't match the facet
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("_default", _default).append("_final", _final).append("listParameters", listParameters).append("facet", facet).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_default).append(type).append(_final).append(message).append(facet).append(listParameters).toHashCode();
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
        return new EqualsBuilder().append(_default, rhs._default).append(type, rhs.type).append(_final, rhs._final).append(message, rhs.message).append(facet, rhs.facet).append(listParameters, rhs.listParameters).isEquals();
    }

}
