package com.sos.joc.db.encipherment;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_ENC_CERTIFICATE, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ALIAS]" }) })
public class DBItemEncCertificate extends DBItem {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ALIAS]", nullable = false)
    private String alias;
    
    @Column(name = "[CERT]", nullable = false)
    private String certificate;
    
    @Column(name = "[PK_PATH]", nullable = true)
    private String privateKeyPath;

    
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getCertificate() {
        return certificate;
    }
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
    
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

}
