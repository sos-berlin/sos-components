package com.sos.joc.db.inventory;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryMeta.CalendarType;

@Entity
@Table(name = DBLayer.TABLE_INV_CALENDARS)
public class DBItemInventoryCalendar extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[TYPE]", nullable = false)
    private Long type;

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
    public CalendarType getTypeAsEnum() {
        return CalendarType.fromValue(type);
    }

    public void setType(Long val) {
        type = val;
    }

    @Transient
    public void setType(CalendarType val) {
        setType(val == null ? null : val.value());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }
}
