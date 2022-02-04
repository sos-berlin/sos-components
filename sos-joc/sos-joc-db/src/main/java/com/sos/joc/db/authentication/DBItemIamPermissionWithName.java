package com.sos.joc.db.authentication;

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
