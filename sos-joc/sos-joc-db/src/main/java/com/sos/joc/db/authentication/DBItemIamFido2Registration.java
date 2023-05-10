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
        "[ACCOUNT_NAME]" }) })

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

    @Column(name = "[ACCESS_TOKEN]", nullable = false)
    private String accessToken;

    @Column(name = "[PUBLIC_KEY]", nullable = false)
    private String publicKey;

    @Column(name = "[EMAIL]", nullable = false)
    private String email;

    @Column(name = "[APPROVED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean approved;

    @Column(name = "[REJECTED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean rejected;

    @Column(name = "[CONFIRMED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean confirmed;
    
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

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
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

}
