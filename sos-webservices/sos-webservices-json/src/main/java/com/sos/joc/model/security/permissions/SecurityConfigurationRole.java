
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.IniPermissions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Role
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "role",
    "folders",
    "permissions"
})
public class SecurityConfigurationRole {

    @JsonProperty("role")
    private String role;
    @JsonProperty("folders")
    private SecurityConfigurationFolders folders;
    /**
     *  Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("permissions")
    private IniPermissions permissions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfigurationRole() {
    }

    /**
     * 
     * @param role
     * @param folders
     * @param permissions
     */
    public SecurityConfigurationRole(String role, SecurityConfigurationFolders folders, IniPermissions permissions) {
        super();
        this.role = role;
        this.folders = folders;
        this.permissions = permissions;
    }

    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("folders")
    public SecurityConfigurationFolders getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(SecurityConfigurationFolders folders) {
        this.folders = folders;
    }

    /**
     *  Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("permissions")
    public IniPermissions getPermissions() {
        return permissions;
    }

    /**
     *  Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("permissions")
    public void setPermissions(IniPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("role", role).append("folders", folders).append("permissions", permissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(role).append(folders).append(permissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationRole) == false) {
            return false;
        }
        SecurityConfigurationRole rhs = ((SecurityConfigurationRole) other);
        return new EqualsBuilder().append(role, rhs.role).append(folders, rhs.folders).append(permissions, rhs.permissions).isEquals();
    }

}
