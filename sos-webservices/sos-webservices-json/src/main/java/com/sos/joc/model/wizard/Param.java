
package com.sos.joc.model.wizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "required",
    "defaultValue",
    "description"
})
public class Param {

    @JsonProperty("name")
    private String name;
    @JsonProperty("required")
    private Boolean required;
    @JsonProperty("defaultValue")
    private String defaultValue;
    /**
     * string in html format
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("string in html format")
    private String description;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

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

    @JsonProperty("defaultValue")
    public String getDefaultValue() {
        return defaultValue;
    }

    @JsonProperty("defaultValue")
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * string in html format
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * string in html format
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("required", required).append("defaultValue", defaultValue).append("description", description).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(required).append(defaultValue).toHashCode();
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
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(required, rhs.required).append(defaultValue, rhs.defaultValue).isEquals();
    }

}
