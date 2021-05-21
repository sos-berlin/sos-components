
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "path"
})
public class InventoryObject {

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private ConfigurationType type;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public ConfigurationType getType() {
        return type;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(ConfigurationType type) {
        this.type = type;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
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
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InventoryObject) == false) {
            return false;
        }
        InventoryObject rhs = ((InventoryObject) other);
        return new EqualsBuilder().append(type, rhs.type).append(path, rhs.path).isEquals();
    }

}
