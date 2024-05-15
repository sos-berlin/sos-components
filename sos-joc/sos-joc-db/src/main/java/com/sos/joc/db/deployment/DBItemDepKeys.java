package com.sos.joc.db.deployment;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_DEP_KEYS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ACCOUNT]", "[KEY_TYPE]", "[SECLVL]" }) })
public class DBItemDepKeys extends DBItem {

    private static final long serialVersionUID = 5376577176035147194L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DEP_KEYS_SEQUENCE)
    private Long id;

    /* 0=PRIVATE, 1=PUBLIC */
    @Column(name = "[KEY_TYPE]", nullable = false)
    private Integer keyType;

    /* 0=PGP, 1=RSA, 2=ECDSA */
    @Column(name = "[KEY_ALG]", nullable = false)
    private Integer keyAlgorithm;

    @Column(name = "[KEY]", nullable = true)
    private String key;

    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;

    @Column(name = "[ACCOUNT]", nullable = true)
    private String account;

    /* 0=LOW, 1=MEDIUM, 2=HIGH */
    @Column(name = "[SECLVL]", nullable = false)
    private Integer secLvl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getKeyType() {
        return keyType;
    }

    public void setKeyType(Integer type) {
        this.keyType = type;
    }

    public Integer getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(Integer keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getSecLvl() {
        return secLvl;
    }

    public void setSecLvl(Integer secLvl) {
        this.secLvl = secLvl;
    }

}
