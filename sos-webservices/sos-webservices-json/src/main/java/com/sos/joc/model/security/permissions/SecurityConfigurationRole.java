
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
    "folders",
    "permissions"
})
public class SecurityConfigurationRole {

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
     * @param folders
     * @param permissions
     */
    public SecurityConfigurationRole(SecurityConfigurationFolders folders, IniPermissions permissions) {
        super();
        this.folders = folders;
        this.permissions = permissions;
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
        return new ToStringBuilder(this).append("folders", folders).append("permissions", permissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(permissions).append(folders).toHashCode();
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
        return new EqualsBuilder().append(permissions, rhs.permissions).append(folders, rhs.folders).isEquals();
    }

}
