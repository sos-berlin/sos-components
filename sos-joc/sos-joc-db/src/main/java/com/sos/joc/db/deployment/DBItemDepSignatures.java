package com.sos.joc.db.deployment;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_DEP_SIGNATURES)
@Proxy(lazy = false)
public class DBItemDepSignatures extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DEP_SIGNATURES_SEQUENCE)
    private Long id;

    @Column(name = "[INV_CID]", nullable = true)
    private Long invConfigurationId;

    @Column(name = "[DEP_HID]", nullable = true)
    private Long depHistoryId;

    @Column(name = "[SIGNATURE]", nullable = false)
    private String signature;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvConfigurationId() {
        return invConfigurationId;
    }

    public void setInvConfigurationId(Long invConfigurationId) {
        this.invConfigurationId = invConfigurationId;
    }

    public Long getDepHistoryId() {
        return depHistoryId;
    }

    public void setDepHistoryId(Long depHistoryId) {
        this.depHistoryId = depHistoryId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

}