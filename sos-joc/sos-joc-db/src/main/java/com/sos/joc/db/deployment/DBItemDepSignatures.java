package com.sos.joc.db.deployment;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_SIGNATURES)
@SequenceGenerator(name = DBLayer.TABLE_DEP_SIGNATURES_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_SIGNATURES_SEQUENCE, allocationSize = 1)
public class DBItemDepSignatures extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_SIGNATURES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
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