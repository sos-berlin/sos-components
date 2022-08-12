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
@Table(name = DBLayer.TABLE_IAM_HISTORY_DETAILS)

@SequenceGenerator(name = DBLayer.TABLE_IAM_HISTORY_DETAILS_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_HISTORY_DETAILS_SEQUENCE, allocationSize = 1)

public class DBItemIamHistoryDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_HISTORY_DETAILS_SEQUENCE)
    @Column(name = "[ID]")
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
