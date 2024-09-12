
package com.sos.joc.model.inventory.changes.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "objectType",
    "hasDependencies"
})
public class ChangeItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
    @JsonProperty("hasDependencies")
    private Boolean hasDependencies = false;

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public ConfigurationType getObjectType() {
        return objectType;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("hasDependencies")
    public Boolean getHasDependencies() {
        return hasDependencies;
    }

    @JsonProperty("hasDependencies")
    public void setHasDependencies(Boolean hasDependencies) {
        this.hasDependencies = hasDependencies;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("objectType", objectType).append("hasDependencies", hasDependencies).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(hasDependencies).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ChangeItem) == false) {
            return false;
        }
        ChangeItem rhs = ((ChangeItem) other);
        return new EqualsBuilder().append(name, rhs.name).append(hasDependencies, rhs.hasDependencies).append(objectType, rhs.objectType).isEquals();
    }

}
