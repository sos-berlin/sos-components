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
@Table(name = DBLayer.TABLE_IAM_FIDO2_DEVICES)

@SequenceGenerator(name = DBLayer.TABLE_IAM_FIDO2_DEVICES_SEQUENCE, sequenceName = DBLayer.TABLE_IAM_FIDO2_DEVICES_SEQUENCE, allocationSize = 1)

public class DBItemIamFido2Devices {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_IAM_ACCOUNTS_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[ACCOUNT_ID]", nullable = false)
    private Long accountId;

    @Column(name = "[PUBLIC_KEY]", nullable = false)
    private String publicKey;

    @Column(name = "[ALGORITHM]", nullable = false)
    private String algorithm;

    @Column(name = "[CREDENTIAL_ID]", nullable = false)
    private String credentialId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

}
