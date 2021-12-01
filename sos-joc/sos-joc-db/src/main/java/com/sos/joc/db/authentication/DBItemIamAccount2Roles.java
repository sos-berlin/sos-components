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
@Table(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES)
@SequenceGenerator(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE, allocationSize = 1)

public class DBItemIamAccount2Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[ROLE_ID]")
    private Long roleId;

    @Column(name = "[ACCOUNT_ID]")
    private Long accountId;

    public DBItemIamAccount2Roles() {

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

}
