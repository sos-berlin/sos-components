package com.sos.joc.db.deployment;

import java.time.LocalDateTime;

import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_DEP_SIGNATURES)
public class DBItemDepSignatures extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_DEP_SIGNATURES_SEQUENCE)
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
    @SOSCurrentTimestampUtc
    private LocalDateTime modified;

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

    public LocalDateTime getModified() {
        return modified;
    }

}