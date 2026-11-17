
package com.sos.jitl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * job param
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "required",
    "dataType",
    "defaultValue"
})
public class Param {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("required")
    private Boolean required = false;
    @JsonProperty("dataType")
    private GlobalSettingsSectionValueType dataType = GlobalSettingsSectionValueType.STRING;
    @JsonProperty("defaultValue")
    private Object defaultValue;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Param() {
    }

    /**
     * 
     * @param defaultValue
     * @param dataType
     * @param name
     * @param required
     */
    public Param(String name, Boolean required, GlobalSettingsSectionValueType dataType, Object defaultValue) {
        super();
        this.name = name;
        this.required = required;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
    }

    @JsonProperty("dataType")
    public GlobalSettingsSectionValueType getDataType() {
        return dataType;
    }

    @JsonProperty("dataType")
    public void setDataType(GlobalSettingsSectionValueType dataType) {
        this.dataType = dataType;
    }

    @JsonProperty("defaultValue")
    public Object getDefaultValue() {
        return defaultValue;
    }

    @JsonProperty("defaultValue")
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("required", required).append("dataType", dataType).append("defaultValue", defaultValue).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(required).append(defaultValue).append(dataType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Param) == false) {
            return false;
        }
        Param rhs = ((Param) other);
        return new EqualsBuilder().append(name, rhs.name).append(required, rhs.required).append(defaultValue, rhs.defaultValue).append(dataType, rhs.dataType).isEquals();
    }

}
