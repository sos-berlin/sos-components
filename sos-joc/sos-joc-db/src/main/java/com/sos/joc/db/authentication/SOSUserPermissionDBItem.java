package com.sos.joc.db.authentication;

import javax.persistence.*;

@Entity
@Table(name = "SOS_USER_PERMISSION")
public class SOSUserPermissionDBItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[ROLE_ID]")
    private Long roleId;

    @Column(name = "[USER_ID]")
    private Long userId;

    @Column(name = "[SOS_USER_PERMISSION`", nullable = false)
    private String sosUserPermission;

    public SOSUserPermissionDBItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSosUserPermission(String sosUserPermission) {
        this.sosUserPermission = sosUserPermission;
    }

    public String getSosUserPermission() {
        return sosUserPermission;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

}
