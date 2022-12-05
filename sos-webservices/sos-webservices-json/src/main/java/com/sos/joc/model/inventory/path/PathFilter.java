
package com.sos.joc.model.inventory.path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delivers path of an inventory object by name and object type
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "objectType",
    "useDrafts"
})
public class PathFilter {

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
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
    @JsonProperty("useDrafts")
    private Boolean useDrafts = false;

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
     * configuration types
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("useDrafts")
    public Boolean getUseDrafts() {
        return useDrafts;
    }

    @JsonProperty("useDrafts")
    public void setUseDrafts(Boolean useDrafts) {
        this.useDrafts = useDrafts;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("objectType", objectType).append("useDrafts", useDrafts).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(useDrafts).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PathFilter) == false) {
            return false;
        }
        PathFilter rhs = ((PathFilter) other);
        return new EqualsBuilder().append(name, rhs.name).append(useDrafts, rhs.useDrafts).append(objectType, rhs.objectType).isEquals();
    }

}
