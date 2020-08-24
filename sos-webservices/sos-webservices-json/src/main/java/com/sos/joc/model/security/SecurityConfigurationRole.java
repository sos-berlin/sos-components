
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "role",
    "permissions",
    "folders"
})
public class SecurityConfigurationRole {

    @JsonProperty("role")
    private String role;
    @JsonProperty("permissions")
    private List<SecurityConfigurationPermission> permissions = new ArrayList<SecurityConfigurationPermission>();
    @JsonProperty("folders")
    private List<SecurityConfigurationFolder> folders = new ArrayList<SecurityConfigurationFolder>();

    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("permissions")
    public List<SecurityConfigurationPermission> getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(List<SecurityConfigurationPermission> permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("folders")
    public List<SecurityConfigurationFolder> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(List<SecurityConfigurationFolder> folders) {
        this.folders = folders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("role", role).append("permissions", permissions).append("folders", folders).toString();
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
