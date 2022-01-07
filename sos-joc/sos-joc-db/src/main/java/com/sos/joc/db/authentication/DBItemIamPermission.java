package com.sos.joc.db.authentication;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_PERMISSIONS)
@SequenceGenerator(name = DBLayer.TABLE_IAM_PERMISSIONS_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_PERMISSIONS_SEQUENCE, allocationSize = 1)

public class DBItemIamPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_PERMISSIONS_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]")
    private Long identityServiceId;

    @Column(name = "[ROLE_ID]")
    private Long roleId;

    @Column(name = "[ACCOUNT_ID]")
    private Long accountId;

    @Column(name = "[ACCOUNT_PERMISSION]", nullable = false)
    private String accountPermission;

    @Column(name = "[CONTROLLER_ID]", nullable = true)
    private String controllerId;

    @Column(name = "[FOLDER_PERMISSION]", nullable = true)
    private String folderPermission;

    @Column(name = "[EXCLUDED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean excluded;

    @Column(name = "[RECURSIVE]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean recursive;

    public DBItemIamPermission() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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

    public String getAccountPermission() {
        return accountPermission;
    }

    public void setAccountPermission(String accountPermission) {
        this.accountPermission = accountPermission;
    }

    
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

}
