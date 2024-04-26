package com.sos.joc.db.inventory;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_CERTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[KEY_TYPE]", "[CA]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_CERTS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_CERTS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryCertificate extends DBItem {

    private static final long serialVersionUID = 5376578176235147194L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_CERTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /* 0=PRIVATE, 1=PUBLIC */
    @Column(name = "[KEY_TYPE]", nullable = false)
    private Integer keyType;

    /* 1=RSA, 2=ECDSA */
    @Column(name = "[KEY_ALG]", nullable = false)
    private Integer keyAlgorithm;

    @Column(name = "[PEM]", nullable = false)
    private String pem;

    @Column(name = "[CA]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean ca;

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
    public void setKeyType(Integer keyType) {
        this.keyType = keyType;
    }
    
    public Integer getKeyAlgorithm() {
        return keyAlgorithm;
    }
    public void setKeyAlgorithm(Integer keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getPem() {
        return pem;
    }
    public void setPem(String pem) {
        this.pem = pem;
    }

    public boolean isCa() {
        return ca;
    }
    public void setCa(boolean ca) {
        this.ca = ca;
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
