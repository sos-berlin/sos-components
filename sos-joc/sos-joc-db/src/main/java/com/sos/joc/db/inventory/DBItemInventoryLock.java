package com.sos.joc.db.inventory;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryMeta.LockType;

@Entity
@Table(name = DBLayer.TABLE_INV_LOCKS)
public class DBItemInventoryLock extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[TYPE]", nullable = false)
    private Long type;

    @Column(name = "[MAX_NONEXCLUSIVE]", nullable = false)
    private Long maxNonExclusive;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Long getType() {
        return type;
    }
    
    @Transient
    public LockType getTypeAsEnum() {
        return LockType.fromValue(type);
    }

    public void setType(Long val) {
        type = val;
    }
    
    @Transient
    public void setType(LockType val) {
        setType(val == null ? null : val.value());
    }

    public Long getMaxNonExclusive() {
        return maxNonExclusive;
    }

    public void setMaxNonExclusive(Long val) {
        if (val == null) {
            val = 0L;
        }
        maxNonExclusive = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }
}
