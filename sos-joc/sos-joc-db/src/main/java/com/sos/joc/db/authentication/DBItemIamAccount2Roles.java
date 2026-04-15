package com.sos.joc.db.authentication;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_IAM_ACCOUNT2ROLES)
public class DBItemIamAccount2Roles {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_ACCOUNT2ROLES_SEQUENCE)
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
