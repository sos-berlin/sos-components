
package com.sos.joc.model.descriptor.restore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for descriptor restore
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "newPath",
    "objectType",
    "suffix",
    "prefix"
})
public class RequestFilter
    extends com.sos.joc.model.descriptor.common.RequestFilter
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    private String newPath;
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
    @JsonProperty("suffix")
    private String suffix;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    private String prefix;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    public String getNewPath() {
        return newPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("newPath")
    public void setNewPath(String newPath) {
        this.newPath = newPath;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("newPath", newPath).append("objectType", objectType).append("suffix", suffix).append("prefix", prefix).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(newPath).append(suffix).append(prefix).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilter) == false) {
            return false;
        }
        RequestFilter rhs = ((RequestFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(newPath, rhs.newPath).append(suffix, rhs.suffix).append(prefix, rhs.prefix).append(objectType, rhs.objectType).isEquals();
    }

}
