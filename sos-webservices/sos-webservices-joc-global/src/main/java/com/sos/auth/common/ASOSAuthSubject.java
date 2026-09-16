package com.sos.auth.common;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.records.UniqueRole;

public abstract class ASOSAuthSubject implements ISOSAuthSubject {
    
    private Boolean authenticated = false;
    protected Boolean isForcePasswordChange = false;
    protected Map<UniqueRole, Set<AuthFolder>> folderPermissionsPerRole;
    protected Map<UniqueRole, Map<String, Set<String>>> accountPermissionsPerRole;
    protected Set<String> setOfRoles;
    protected Set<String> setOf4EyesRolePermissions;
    private Set<String> accountPermissions;
    
    @Override
    public Boolean hasRole(String role) {
        return setOfRoles != null && setOfRoles.contains(role);
    }

    @Override
    public boolean isPermitted(final String permission) {
        if (accountPermissionsPerRole != null) {
            if (accountPermissions == null) {
                accountPermissions = accountPermissionsPerRole.values().stream().map(Map::entrySet).flatMap(Set::stream).map(Map.Entry::getValue)
                        .flatMap(Set::stream).collect(Collectors.toSet());
            }
        }
        return isPermitted(permission, accountPermissions);
    }
    
    @Override
    public boolean is4EyesPermitted(String permission) {
        return isPermitted(permission, setOf4EyesRolePermissions);
    }
    
    private static boolean isPermitted(final String permission, Set<String> perms) {
        if (perms != null) {
            if (perms.stream().anyMatch(perm -> (permission + ":").startsWith(perm + ":"))) {
                return true; 
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
    public Map<UniqueRole, Set<AuthFolder>> getMapOfFolderPermissions() {
        if (folderPermissionsPerRole == null) {
            return Collections.emptyMap();
        }
        return folderPermissionsPerRole;
    }
    
    @Override
    public Map<String, Set<AuthFolder>> getFolderPermissionsOfRole(UniqueRole role) {
        if (folderPermissionsPerRole == null) {
            return Collections.emptyMap();
        }
        return folderPermissionsPerRole.getOrDefault(role, Collections.emptySet()).stream().collect(Collectors.groupingBy(AuthFolder::getControllerId,
                Collectors.toSet()));
    }
    
    @Override
    public Boolean isForcePasswordChange() {
        return isForcePasswordChange;
    }

    @Override
    public Set<String> getListOfAccountPermissions() {
        if (accountPermissionsPerRole == null) {
            return Collections.emptySet();
        }
        return accountPermissionsPerRole.values().stream().map(Map::entrySet).flatMap(Set::stream).map(Map.Entry::getValue).flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
    
    @Override
    public Map<UniqueRole, Map<String, Set<String>>> getMapOfAccountPermissions() {
        if (accountPermissionsPerRole == null) {
            return Collections.emptyMap();
        }
        return accountPermissionsPerRole;
    }
    
    @Override
    public Map<String, Set<String>> getAccountPermissionsOfRole(UniqueRole role) {
        if (accountPermissionsPerRole == null) {
            return Collections.emptyMap();
        }
        return accountPermissionsPerRole.getOrDefault(role, Collections.emptyMap());
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
