package com.sos.auth.shiro.db;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "SOS_USER")
public class SOSUserDBItem {

    private Long id;
    private String sosUserName;
    private String sosUserPassword;

    private List<SOSUser2RoleDBItem> sosUserRoleDBItems;
    private List<SOSUserPermissionDBItem> sosUserPermissionDBItems;

    public SOSUserDBItem() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "userId")
    public List<SOSUser2RoleDBItem> getSOSUserRoleDBItems() {
        return sosUserRoleDBItems;
    }

    public void setSOSUserRoleDBItems(List<SOSUser2RoleDBItem> sosUserRoleDBItems) {
        this.sosUserRoleDBItems = sosUserRoleDBItems;
    }

    @OneToMany(mappedBy = "userId")
    public List<SOSUserPermissionDBItem> getSOSUserPermissionDBItems() {
        return sosUserPermissionDBItems;
    }

    public void setSOSUserPermissionDBItems(List<SOSUserPermissionDBItem> sosUserPermissionDBItems) {
        this.sosUserPermissionDBItems = sosUserPermissionDBItems;
    }

    @Column(name = "[SOS_USER_NAME`", nullable = false)
    public void setSosUserName(String sosUserName) {
        this.sosUserName = sosUserName;
    }

    @Column(name = "[SOS_USER_NAME`", nullable = false)
    public String getSosUserName() {
        return sosUserName;
    }

    @Column(name = "[SOS_USER_PASSWORD`", nullable = false)
    public void setSosUserPassword(String sosUserPassword) {
        this.sosUserPassword = sosUserPassword;
    }

    @Column(name = "[SOS_USER_PASSWORD`", nullable = false)
    public String getSosUserPassword() {
        return sosUserPassword;
    }

}
