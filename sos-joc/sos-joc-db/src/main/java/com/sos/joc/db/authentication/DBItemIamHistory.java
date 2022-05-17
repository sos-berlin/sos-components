package com.sos.joc.db.authentication;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_HISTORY)

@SequenceGenerator(name = DBLayer.TABLE_IAM_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_HISTORY_SEQUENCE, allocationSize = 1)

public class DBItemIamHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_HISTORY_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[LOGIN_DATE]", nullable = false)
    private Date loginDate;

    @Column(name = "[LOGIN_SUCCESS]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean loginSuccess;

    public DBItemIamHistory() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Boolean getLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(Boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

}
