package com.sos.auth.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;

public abstract class ASOSAuthSubject implements ISOSAuthSubject {
    
    private Boolean authenticated = false;
    protected Boolean isForcePasswordChange = false;
    protected Map<String, List<String>> mapOfFolderPermissions;
    protected Set<String> setOfAccountPermissions;
    protected Set<String> setOfRoles;
    protected Set<String> setOf4EyesRolePermissions;
    
    @Override
    public Boolean hasRole(String role) {
        return setOfRoles != null && setOfRoles.contains(role);
    }

    @Override
    public Boolean isPermitted(String permission) {
        permission = permission + ":";
        if (setOfAccountPermissions != null) {
            for (String accountPermission : setOfAccountPermissions) {
                accountPermission = accountPermission + ":";
                if (permission.startsWith(accountPermission)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Boolean is4EyesPermitted(String permission) {
        permission = permission + ":";
        if (setOf4EyesRolePermissions != null) {
            for (String accountPermission : setOf4EyesRolePermissions) {
                accountPermission = accountPermission + ":";
                if (permission.startsWith(accountPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        if (mapOfFolderPermissions == null) {
            return Collections.emptyMap();
        }
        return mapOfFolderPermissions;
    }

    @Override
    public Boolean isForcePasswordChange() {
        return isForcePasswordChange;
    }

    @Override
    public Set<String> getListOfAccountPermissions() {
        return setOfAccountPermissions;
    }
    
    @Override
    public Set<String> getListOf4EyesRolePermissions() {
        if (setOf4EyesRolePermissions == null) {
            return Collections.emptySet();
        }
        return setOf4EyesRolePermissions;
    }

    @Override
    public Set<String> getListOfAccountRoles() {
        return this.setOfRoles;
    }

    @Override
    public abstract ISOSSession getSession();

}
