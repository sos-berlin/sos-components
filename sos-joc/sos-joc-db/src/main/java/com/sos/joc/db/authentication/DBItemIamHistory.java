package com.sos.joc.db.authentication;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_IAM_HISTORY)
@Proxy(lazy = false)
public class DBItemIamHistory {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_HISTORY_SEQUENCE)
    private Long id;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[LOGIN_DATE]", nullable = false)
    private Date loginDate;

    @Column(name = "[LOGIN_SUCCESS]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
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
