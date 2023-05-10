package com.sos.joc.db.authentication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_IDENTITY_SERVICES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_NAME]"}) })
@SequenceGenerator(name = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE, allocationSize = 1)

public class DBItemIamIdentityService {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_TYPE]", nullable = false)
    private String identityServiceType;

    @Column(name = "[IDENTITY_SERVICE_NAME]", nullable = false)
    private String identityServiceName;

    @Column(name = "[AUTHENTICATION_SCHEME]", nullable = false)
    private String authenticationScheme;

    @Column(name = "[SECOND_FACTOR]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean secondFactor;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[DISABLED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean disabled;

    @Column(name = "[REQUIRED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean required;

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

    public void setDisabled(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.disabled = val;
    }

    public Boolean getRequired() {

        return required;
    }

    public void setRequired(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.required = val;
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

    public String getAuthenticationScheme() {
        if ("SINGLE".equals(authenticationScheme)) {
            authenticationScheme = "SINGLE-FACTOR";
        }
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }     

    public Boolean getSecondFactor() {
        return secondFactor;
    }

    public void setSecondFactor(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.secondFactor = val;
    }

}
