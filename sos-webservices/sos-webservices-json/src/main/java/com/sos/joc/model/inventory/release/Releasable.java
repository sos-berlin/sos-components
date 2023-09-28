
package com.sos.joc.model.inventory.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * releasable
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "objectType"
})
public class Releasable {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("objectType", objectType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Releasable) == false) {
            return false;
        }
        Releasable rhs = ((Releasable) other);
        return new EqualsBuilder().append(path, rhs.path).append(objectType, rhs.objectType).isEquals();
    }

}
