package com.sos.auth.common;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.model.common.Folder;

public class AuthFolder extends Folder {
    
    private final String controllerId;
    
    public AuthFolder(String controllerId, String folder, Boolean recursive) {
        this.controllerId = controllerId;
        setFolder(folder);
        setRecursive(recursive);
    }
    
    public AuthFolder(DBItemIamPermissionWithName item) {
        this.controllerId = item.getControllerId();
        setFolder(item.getFolderPermission());
        setRecursive(item.getRecursive());
    }

    public String getControllerId() {
        return Optional.ofNullable(controllerId).orElse("");
    }
    
    @Override
    public String toString() {
        return Optional.ofNullable(controllerId).map(id -> id.isEmpty() ? "" : id + ":").orElse("") + getFolder() + (getRecursive() ? "/*" : "");
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuthFolder) == false) {
            return false;
        }
        AuthFolder rhs = ((AuthFolder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(controllerId, rhs.controllerId).isEquals();
    }

}
