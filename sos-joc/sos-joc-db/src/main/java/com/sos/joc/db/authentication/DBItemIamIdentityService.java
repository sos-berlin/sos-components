package com.sos.joc.db.authentication;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.security.identityservice.IdentityServiceAuthenticationScheme;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_IAM_IDENTITY_SERVICES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_NAME]" }) })
public class DBItemIamIdentityService {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_IDENTITY_SERVICES_SEQUENCE)
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_TYPE]", nullable = false)
    private String identityServiceType;

    @Column(name = "[IDENTITY_SERVICE_NAME]", nullable = false)
    private String identityServiceName;

    @Column(name = "[SECOND_FACTOR_IS_ID]", nullable = false)
    private Long secondFactorIsId;

    @Column(name = "[AUTHENTICATION_SCHEME]", nullable = false)
    private String authenticationScheme;

    @Column(name = "[SECOND_FACTOR]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean secondFactor;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[DISABLED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean disabled;

    @Column(name = "[REQUIRED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
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

    public Long getSecondFactorIsId() {
        return secondFactorIsId;
    }

    public void setSecondFactorIsId(Long secondFactorIsId) {
        this.secondFactorIsId = secondFactorIsId;
    }

    @Transient
    public boolean isTwoFactor() {
        return authenticationScheme.equals(IdentityServiceAuthenticationScheme.TWO_FACTOR.value()) && secondFactorIsId != null;
    }

}
