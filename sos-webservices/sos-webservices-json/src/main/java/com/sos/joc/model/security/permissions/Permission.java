
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * PermissionRec
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "permissionPath",
    "excluded",
    "additionalProperties"
})
public class Permission {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPath")
    private String permissionPath;
    @JsonProperty("excluded")
    private Boolean excluded;
    @JsonProperty("additionalProperties")
    private Object additionalProperties;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Permission() {
    }

    /**
     * 
     * @param excluded
     * @param permissionPath
     * @param additionalProperties
     */
    public Permission(String permissionPath, Boolean excluded, Object additionalProperties) {
        super();
        this.permissionPath = permissionPath;
        this.excluded = excluded;
        this.additionalProperties = additionalProperties;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPath")
    public String getPermissionPath() {
        return permissionPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPath")
    public void setPermissionPath(String permissionPath) {
        this.permissionPath = permissionPath;
    }

    @JsonProperty("excluded")
    public Boolean getExcluded() {
        return excluded;
    }

    @JsonProperty("excluded")
    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    @JsonProperty("additionalProperties")
    public Object getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonProperty("additionalProperties")
    public void setAdditionalProperties(Object additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("permissionPath", permissionPath).append("excluded", excluded).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excluded).append(permissionPath).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Permission) == false) {
            return false;
        }
        Permission rhs = ((Permission) other);
        return new EqualsBuilder().append(excluded, rhs.excluded).append(permissionPath, rhs.permissionPath).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
