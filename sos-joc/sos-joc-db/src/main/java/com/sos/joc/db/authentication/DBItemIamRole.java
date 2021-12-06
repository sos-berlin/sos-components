package com.sos.joc.db.authentication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_ROLES)
@SequenceGenerator(name = DBLayer.TABLE_IAM_ROLES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_ROLES_SEQUENCE, allocationSize = 1)

public class DBItemIamRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;
    
    @Column(name = "[IDENTITY_SERVICE_ID]")
    private Long identityServiceId;
    
    @Column(name = "[ROLE_NAME`", nullable = false)
    private String roleName;

    public DBItemIamRole() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

}
