package com.sos.joc.db.joc;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_JOC_APPROVERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ACCOUNT_NAME]" }) })
@Proxy(lazy = false)
public class DBItemJocApprover extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_APPROVERS_SEQUENCE)
    private Long id;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Column(name = "[FIRST_NAME]", nullable = false)
    private String firstName;

    @Column(name = "[LAST_NAME]", nullable = false)
    private String lastName;

    @Column(name = "[EMAIL]", nullable = false)
    private String email;

    @Column(name = "[ORDERING]", nullable = true)
    private Integer ordering;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String val) {
        accountName = val;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String val) {
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        firstName = val;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String val) {
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        lastName = val;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String val) {
        email = val;
    }
    
    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        if (val == null) {
            val = 0;
        }
        ordering = val;
    }
}