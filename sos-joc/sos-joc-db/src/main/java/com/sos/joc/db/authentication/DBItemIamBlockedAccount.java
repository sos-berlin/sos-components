package com.sos.joc.db.authentication;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_BLOCKLIST)

@SequenceGenerator(name = DBLayer.TABLE_IAM_BLOCKLIST_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_BLOCKLIST_SEQUENCE, allocationSize = 1)

public class DBItemIamBlockedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_BLOCKLIST_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_IAM_BLOCKLIST_SEQUENCE)
    @Column(name = "[ID]")
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
