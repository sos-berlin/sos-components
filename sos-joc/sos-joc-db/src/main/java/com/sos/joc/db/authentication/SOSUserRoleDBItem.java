package com.sos.joc.db.authentication;

import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "SOS_USER_ROLE")
public class SOSUserRoleDBItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[SOS_USER_ROLE`", nullable = false)
    private String sosUserRole;

    public SOSUserRoleDBItem() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSosUserRole(String sosUserRole) {
        this.sosUserRole = sosUserRole;
    }

    public String getSosUserRole() {
        return sosUserRole;
    }

}
