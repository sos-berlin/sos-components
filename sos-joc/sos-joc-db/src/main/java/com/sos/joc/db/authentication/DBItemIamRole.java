package com.sos.joc.db.authentication;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_IAM_ROLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_ID]", "[ROLE_NAME]" }) })
@Proxy(lazy = false)
public class DBItemIamRole {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_ROLES_SEQUENCE)
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]", nullable = false)
    private Long identityServiceId;

    @Column(name = "[ROLE_NAME]", nullable = false)
    private String roleName;

    @Column(name = "[ORDERING]", nullable = true)
    private Integer ordering;

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

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

}
