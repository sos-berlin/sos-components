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
@Table(name = DBLayer.TABLE_IAM_IDENTITY_SERVICES)
@SequenceGenerator(name = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE, allocationSize = 1)

public class DBItemIamIdentityService {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_TYPE`", nullable = false)
    private String identityServiceType;

    @Column(name = "[IDENTITY_SERVICE_NAME`", nullable = false)
    private String identityServiceName;

    @Column(name = "[ORDERING`", nullable = false)
    private Integer ordering;

    @Column(name = "[DISABLED`", nullable = false)
    private Boolean disabled;

    @Column(name = "[REQUIRED`", nullable = false)
    private Boolean requierd;

    public DBItemIamIdentityService() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getRequierd() {
        return requierd;
    }

    public void setRequierd(Boolean requierd) {
        this.requierd = requierd;
    }


    public String getIdentityServiceType() {
        return identityServiceType;
    }

    public void setIdentityServiceType(String identityServiceType) {
        this.identityServiceType = identityServiceType;
    }

    public String getIdentityServiceName() {
        return identityServiceName;
    }

    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

}
