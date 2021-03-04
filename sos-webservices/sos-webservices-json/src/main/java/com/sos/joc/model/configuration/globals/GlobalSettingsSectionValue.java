
package com.sos.joc.model.configuration.globals;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster setting
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "value",
    "type",
    "default"
})
public class GlobalSettingsSectionValue {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("value")
    private String value;
    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private GlobalSettingsSectionValueType type;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("default")
    private String _default;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public GlobalSettingsSectionValueType getType() {
        return type;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(GlobalSettingsSectionValueType type) {
        this.type = type;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("default")
    public String getDefault() {
        return _default;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("default")
    public void setDefault(String _default) {
        this._default = _default;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("value", value).append("type", type).append("_default", _default).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(_default).append(type).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GlobalSettingsSectionValue) == false) {
            return false;
        }
        GlobalSettingsSectionValue rhs = ((GlobalSettingsSectionValue) other);
        return new EqualsBuilder().append(name, rhs.name).append(_default, rhs._default).append(type, rhs.type).append(value, rhs.value).isEquals();
    }

}
