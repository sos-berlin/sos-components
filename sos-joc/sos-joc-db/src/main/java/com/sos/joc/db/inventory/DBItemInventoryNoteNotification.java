package com.sos.joc.db.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_NOTE_NOTIFICATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ACCOUNT_NAME]", "[CID]" }) })
@Proxy(lazy = false)
public class DBItemInventoryNoteNotification extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ACCOUNT_NAME]", nullable = false)
    private String accountName;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;
    
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String val) {
        accountName = val;
    }
    
    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }
}