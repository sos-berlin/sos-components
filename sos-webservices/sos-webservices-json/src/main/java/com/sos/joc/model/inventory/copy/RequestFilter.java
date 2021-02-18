
package com.sos.joc.model.inventory.copy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for copy
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "newPath",
    "suffix",
    "prefix",
    "shallowCopy"
})
public class RequestFilter
    extends com.sos.joc.model.inventory.common.RequestFilter
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
    @JsonProperty("shallowCopy")
    private Boolean shallowCopy = false;

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

    @JsonProperty("shallowCopy")
    public Boolean getShallowCopy() {
        return shallowCopy;
    }

    @JsonProperty("shallowCopy")
    public void setShallowCopy(Boolean shallowCopy) {
        this.shallowCopy = shallowCopy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("newPath", newPath).append("suffix", suffix).append("prefix", prefix).append("shallowCopy", shallowCopy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(newPath).append(suffix).append(prefix).append(shallowCopy).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(newPath, rhs.newPath).append(suffix, rhs.suffix).append(prefix, rhs.prefix).append(shallowCopy, rhs.shallowCopy).isEquals();
    }

}
