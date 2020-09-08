package com.sos.joc.db.deployment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_KEYS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ACCOUNT]", "[SECLVL]" }) })
@SequenceGenerator(name = DBLayer.TABLE_DEP_KEYS_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_KEYS_SEQUENCE, allocationSize = 1)
public class DBItemDepKeys extends DBItem {

    private static final long serialVersionUID = 5376577176035147194L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_KEYS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /* 0=PRIVATE, 1=PUBLIC */
    @Column(name = "[KEY_TYPE]", nullable = false)
    private Integer keyType;

    /* 0=PGP, 1=RSA */
    @Column(name = "[KEY_ALG]", nullable = false)
    private Integer keyAlgorythm;

    @Column(name = "[KEY]", nullable = false)
    private String key;

    @Column(name = "[CERTIFICATE]", nullable = false)
    private String certificate;

    @Column(name = "[ACCOUNT]", nullable = false)
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

    public Integer getKeyAlgorythm() {
        return keyAlgorythm;
    }
    public void setKeyAlgorythm(Integer keyAlgorythm) {
        this.keyAlgorythm = keyAlgorythm;
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
