package com.sos.joc.db.authentication;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_IAM_PERMISSIONS)
public class DBItemIamPermission {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_PERMISSIONS_SEQUENCE)
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]", nullable = false)
    private Long identityServiceId;

    @Column(name = "[ROLE_ID]")
    private Long roleId;

    @Column(name = "[ACCOUNT_ID]")
    private Long accountId;

    @Column(name = "[ACCOUNT_PERMISSION]")
    private String accountPermission;

    @Column(name = "[CONTROLLER_ID]")
    private String controllerId;

    @Column(name = "[FOLDER_PERMISSION]")
    private String folderPermission;

    @Column(name = "[EXCLUDED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean excluded;

    @Column(name = "[RECURSIVE]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
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

    public void setExcluded(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.excluded = val;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.recursive = val;
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
