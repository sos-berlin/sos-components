package com.sos.joc.db.authentication;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_IAM_BLOCKLIST)
@Proxy(lazy = false)
public class DBItemIamBlockedAccount {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_BLOCKLIST_SEQUENCE)
    private Long id;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Column(name = "[COMMENT]", nullable = false)
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SINCE]", nullable = false)
    private Date since;

    public DBItemIamBlockedAccount() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

}
