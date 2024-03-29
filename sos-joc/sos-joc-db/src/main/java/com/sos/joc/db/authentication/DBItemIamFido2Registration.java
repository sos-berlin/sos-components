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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[IDENTITY_SERVICE_ID]",
        "[ACCOUNT_NAME]" , "[ORIGIN]" }) })

@SequenceGenerator(name = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS_SEQUENCE, allocationSize = 1)

public class DBItemIamFido2Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_FIDO2_REGISTRATIONS_SEQUENCE)
    @Column(name = "[ID]")
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
    @Type(type = "numeric_boolean")
    private Boolean deferred;

    @Column(name = "[CONFIRMED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean confirmed;

    @Column(name = "[COMPLETED]", nullable = false)
    @Type(type = "numeric_boolean")
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
