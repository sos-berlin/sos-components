package com.sos.joc.db.authentication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES)
@SequenceGenerator(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE, allocationSize = 1)

public class DBItemIamAccount2Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[ROLE_ID]", nullable = false)
    private Long roleId;

    @Column(name = "[ACCOUNT_ID]", nullable = false)
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
