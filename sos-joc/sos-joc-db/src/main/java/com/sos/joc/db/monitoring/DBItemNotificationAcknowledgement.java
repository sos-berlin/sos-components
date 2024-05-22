package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.MonitoringConstants;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_MON_NOT_ACKNOWLEDGEMENTS)
@Proxy(lazy = false)
public class DBItemNotificationAcknowledgement extends DBItem {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DBItemNotificationAcknowledgementId id;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemNotificationAcknowledgement() {
    }

    public DBItemNotificationAcknowledgementId getId() {
        return id;
    }

    public void setId(DBItemNotificationAcknowledgementId val) {
        if (val == null) {
            val = new DBItemNotificationAcknowledgementId(0L, MonitoringConstants.NOTIFICATION_DEFAULT_APPLICATION.intValue());
        }
        id = val;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        account = val;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String val) {
        comment = normalizeValue(val, MonitoringConstants.MAX_LEN_COMMENT);
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

}
