package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.meta.LockType;

@Entity
@Table(name = DBLayer.TABLE_INV_LOCKS)
public class DBItemInventoryLock extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[MAX_NONEXCLUSIVE]", nullable = false)
    private Integer maxNonExclusive;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public LockType getTypeAsEnum() {
        return LockType.fromValue(type);
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(LockType val) {
        setType(val == null ? null : val.intValue());
    }

    public Integer getMaxNonExclusive() {
        return maxNonExclusive;
    }

    public void setMaxNonExclusive(Integer val) {
        if (val == null) {
            val = 0;
        }
        maxNonExclusive = val;
    }

}
