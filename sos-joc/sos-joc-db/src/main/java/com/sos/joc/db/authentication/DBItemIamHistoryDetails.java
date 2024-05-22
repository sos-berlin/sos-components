package com.sos.joc.db.authentication;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_IAM_HISTORY_DETAILS)
@Proxy(lazy = false)
public class DBItemIamHistoryDetails {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_HISTORY_DETAILS_SEQUENCE)
    private Long id;

    @Column(name = "[IAM_HISTORY_ID]")
    private Long iamHistoryId;

    @Column(name = "[IDENTITY_SERVICE_NAME]", nullable = false)
    private String identityServiceName;

    @Column(name = "[MESSAGE]", nullable = false)
    private String message;

    public DBItemIamHistoryDetails() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIamHistoryId() {
        return iamHistoryId;
    }

    public void setIamHistoryId(Long iamHistoryId) {
        this.iamHistoryId = iamHistoryId;
    }

    public String getIdentityServiceName() {
        return identityServiceName;
    }

    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
