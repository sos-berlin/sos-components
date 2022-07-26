
package com.sos.inventory.model.jobtemplate;

import java.util.List;
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
    "description",
    "facet",
    "list",
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
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    private String description;
    /**
     * a regular expression to check the value of the parameter
     * 
     */
    @JsonProperty("facet")
    @JsonPropertyDescription("a regular expression to check the value of the parameter")
    private String facet;
    /**
     * enumeration of possible parameter values
     * 
     */
    @JsonProperty("list")
    @JsonPropertyDescription("enumeration of possible parameter values")
    private List<String> list = null;
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
     * @param description
     * @param type
     * @param list
     * @param message
     * @param facet
     */
    public Parameter(ParameterType type, Object _default, String description, String facet, List<String> list, String message) {
        super();
        this.type = type;
        this._default = _default;
        this.description = description;
        this.facet = facet;
        this.list = list;
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

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
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
     * enumeration of possible parameter values
     * 
     */
    @JsonProperty("list")
    public List<String> getList() {
        return list;
    }

    /**
     * enumeration of possible parameter values
     * 
     */
    @JsonProperty("list")
    public void setList(List<String> list) {
        this.list = list;
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
        return new ToStringBuilder(this).append("type", type).append("_default", _default).append("description", description).append("facet", facet).append("list", list).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_default).append(description).append(type).append(list).append(message).append(facet).toHashCode();
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
        return new EqualsBuilder().append(_default, rhs._default).append(description, rhs.description).append(type, rhs.type).append(list, rhs.list).append(message, rhs.message).append(facet, rhs.facet).isEquals();
    }

}
