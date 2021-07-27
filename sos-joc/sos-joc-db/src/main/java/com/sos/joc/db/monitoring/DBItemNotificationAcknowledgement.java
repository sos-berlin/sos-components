package com.sos.joc.db.monitoring;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_NOTIFICATION_ACKNOWLEDGEMENTS)
public class DBItemNotificationAcknowledgement extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NOT_ID]", nullable = false)
    private Long notificationId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemNotificationAcknowledgement() {
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long val) {
        notificationId = val;
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
        comment = normalizeValue(val, 4_000);
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

}
