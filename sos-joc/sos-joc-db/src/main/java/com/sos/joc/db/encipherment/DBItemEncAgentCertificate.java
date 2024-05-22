package com.sos.joc.db.encipherment;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_ENC_AGENT_CERTIFICATES)
@Proxy(lazy = false)
public class DBItemEncAgentCertificate extends DBItem {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;
    
    // pk of ENC_CERTIFICATES (ENC_CERTIFICATES.ALIAS)
    @Id
    @Column(name = "[CERT_ALIAS]", nullable = false)
    private String certAlias;
    
    
    public String getAgentId() {
        return agentId;
    }
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getCertAlias() {
        return certAlias;
    }
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }
    
}
