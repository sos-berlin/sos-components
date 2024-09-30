
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Inventory search response item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "name",
    "objectType",
    "ordering",
    "group"
})
public class ResponseBaseSearchItem {

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
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
    @JsonProperty("ordering")
    private Integer ordering;
    @JsonProperty("group")
    private String group;

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

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
    
    
    @JsonIgnore
    public String getLowerCaseName() {
        return name == null ? "" : name.toLowerCase();
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
    
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }
    
    @JsonProperty("group")
    public String getGroup() {
        return group;
    }
    
    @JsonProperty("group")
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("name", name).append("objectType", objectType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(path).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseBaseSearchItem) == false) {
            return false;
        }
        ResponseBaseSearchItem rhs = ((ResponseBaseSearchItem) other);
        return new EqualsBuilder().append(name, rhs.name).append(path, rhs.path).append(objectType, rhs.objectType).isEquals();
    }

}
