package com.sos.joc.db.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_TEMP_LOGS)
public class DBItemHistoryTempLog extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private Long mainOrderId;

    @Column(name = "[MEMBER_ID]", nullable = false)
    private String memberId;

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "[CONTENT]", nullable = false)
    private byte[] content;

    @Column(name = "[MOST_RECENT_FILE]", nullable = false)
    private Long mostRecentFile;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public DBItemHistoryTempLog() {
    }

    public Long getMainOrdertId() {
        return mainOrderId;
    }

    public void setMainOrderId(Long val) {
        mainOrderId = val;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String val) {
        memberId = val;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] val) {
        content = val;
    }

    public void setMostRecentFile(Long val) {
        mostRecentFile = val;
    }

    public Long getMostRecentFile() {
        return mostRecentFile;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }
}
