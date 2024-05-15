package com.sos.joc.db.authentication;

import java.util.Date;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_ID]",
        "[ACCOUNT_NAME]", "[ORIGIN]" }) })
public class DBItemIamFido2Registration {

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS_SEQUENCE)
    private Long id;

    @Column(name = "[IDENTITY_SERVICE_ID]", nullable = false)
    private Long identityServiceId;

    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Column(name = "[TOKEN]", nullable = false)
    private String token;

    @Column(name = "[PUBLIC_KEY]", nullable = true)
    private String publicKey;

    @Column(name = "[ALGORITHM]", nullable = true)
    private String algorithm;

    @Column(name = "[CREDENTIAL_ID]", nullable = true)
    private String credentialId;

    @Column(name = "[ORIGIN]", nullable = true)
    private String origin;

    @Column(name = "[EMAIL]", nullable = false)
    private String email;

    @Column(name = "[CHALLENGE]", nullable = false)
    private String challenge;

    @Column(name = "[DEFERRED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean deferred;

    @Column(name = "[CONFIRMED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean confirmed;

    @Column(name = "[COMPLETED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean completed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getDeferred() {
        return deferred;
    }

    public void setDeferred(Boolean deferred) {
        if (deferred == null) {
            this.deferred = false;
        } else {
            this.deferred = deferred;
        }
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        if (confirmed == null) {
            this.confirmed = false;
        } else {
            this.confirmed = confirmed;
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        if (completed == null) {
            this.completed = false;
        } else {
            this.completed = completed;
        }
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
