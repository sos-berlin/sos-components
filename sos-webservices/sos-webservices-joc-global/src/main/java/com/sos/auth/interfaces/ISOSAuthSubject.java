package com.sos.auth.interfaces;

import java.util.Map;
import java.util.Set;

import com.sos.auth.common.AuthFolder;
import com.sos.auth.records.UniqueRole;

public interface ISOSAuthSubject {

    public Boolean hasRole(String role);

    public boolean isPermitted(String permission);

    public boolean is4EyesPermitted(String permission);

    public Boolean isAuthenticated();

    public Boolean isForcePasswordChange();

    public Map<UniqueRole, Set<AuthFolder>> getMapOfFolderPermissions();
    public Map<String, Set<AuthFolder>> getFolderPermissionsOfRole(UniqueRole role);

    public Map<UniqueRole, Set<String>> getMapOfAccountPermissions();
    public Set<String> getAccountPermissionsOfRole(UniqueRole role);

    public Set<String> getListOfAccountPermissions();

    public Set<String> getListOfAccountRoles();
    
    public Set<String> getListOf4EyesRolePermissions();

    public ISOSSession getSession();

}
