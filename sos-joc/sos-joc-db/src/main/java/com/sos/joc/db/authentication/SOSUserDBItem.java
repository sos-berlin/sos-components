package com.sos.joc.db.authentication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SOS_USER")
public class SOSUserDBItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[SOS_USER_NAME`", nullable = false)
    private String sosUserName;

    @Column(name = "[SOS_USER_PASSWORD`", nullable = false)
    private String sosUserPassword;

    public SOSUserDBItem() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSosUserName(String sosUserName) {
        this.sosUserName = sosUserName;
    }

    public String getSosUserName() {
        return sosUserName;
    }

    public void setSosUserPassword(String sosUserPassword) {
        this.sosUserPassword = sosUserPassword;
    }

    public String getSosUserPassword() {
        return sosUserPassword;
    }

}
