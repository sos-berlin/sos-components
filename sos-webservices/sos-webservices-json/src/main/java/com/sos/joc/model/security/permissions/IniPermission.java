
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Ini Permissions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "excluded"
})
public class IniPermission {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    private String path;
    @JsonProperty("excluded")
    private Boolean excluded;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IniPermission() {
    }

    /**
     * 
     * @param excluded
     * @param path
     */
    public IniPermission(String path, Boolean excluded) {
        super();
        this.path = path;
        this.excluded = excluded;
    }

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

    @JsonProperty("excluded")
    public Boolean getExcluded() {
        return excluded;
    }

    @JsonProperty("excluded")
    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("excluded", excluded).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excluded).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IniPermission) == false) {
            return false;
        }
        IniPermission rhs = ((IniPermission) other);
        return new EqualsBuilder().append(excluded, rhs.excluded).append(path, rhs.path).isEquals();
    }

}
