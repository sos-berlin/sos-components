package com.sos.joc.db.authentication;

import java.util.Optional;

public class DBItemIamPermissionWithName {

    private String controllerId;
    private Long roleId;
    private String accountPermission;
    private String folderPermission;
    private Boolean excluded;
    private Boolean recursive;
    private String roleName;

    public String getControllerId() {
        return controllerId;
    }
    
    public String getNonNullControllerId() {
        return Optional.ofNullable(controllerId).orElse("");
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }


    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getAccountPermission() {
        return accountPermission;
    }
    
    public Optional<String> getAccountPermissionWithControllerIdAndExludes() {
        if (accountPermission != null && !accountPermission.isEmpty()) {
            String permission = accountPermission.replace(":adminstration:", ":administration:"); // because of typo in the past
            if (controllerId != null && !controllerId.isEmpty()) {
                permission = controllerId + ":" + permission;
            }
            if (Boolean.TRUE == excluded) {
                permission = "-" + permission;
            }
            return Optional.of(permission);
        }
        return Optional.empty();
    }

    public void setAccountPermission(String accountPermission) {
        this.accountPermission = accountPermission;
    }

    public String getFolderPermission() {
        return folderPermission;
    }

    public void setFolderPermission(String folderPermission) {
        this.folderPermission = folderPermission;
    }

    public Boolean getExcluded() {
        return excluded;
    }

    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
