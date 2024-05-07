package com.sos.joc.db.joc;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_CLUSTER, uniqueConstraints = { @UniqueConstraint(columnNames = { "[MEMBER_ID]" }) })
public class DBItemJocCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    private String id;// host:appData

    @Column(name = "[MEMBER_ID]", nullable = false)
    private String memberId;// host:appData

    @Column(name = "[SWITCH_MEMBER_ID]", nullable = true)
    private String switchMemberId;

    @Column(name = "[SWITCH_HEART_BEAT]", nullable = true)
    private Date switchHeartBeat;

    @Column(name = "[NOTIFICATION_MEMBER_ID]", nullable = true)
    private String notificationMemberId;

    @Column(name = "[NOTIFICATION]", nullable = true)
    private String notification;

    // @Version because problems with MS SQL Server
    @Column(name = "[HEART_BEAT]", nullable = false)
    private Date heartBeat;

    @Transient
    private String startupMode;

    public String getId() {
        return id;
    }

    public void setId(String val) {
        id = val;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String val) {
        memberId = val;
    }

    public String getSwitchMemberId() {
        return switchMemberId;
    }

    public void setSwitchMemberId(String val) {
        switchMemberId = val;
    }

    public void setSwitchHeartBeat(Date val) {
        switchHeartBeat = val;
    }

    public String getNotificationMemberId() {
        return notificationMemberId;
    }

    public void setNotificationMemberId(String val) {
        notificationMemberId = val;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String val) {
        notification = val;
    }

    public Date getSwitchHeartBeat() {
        return switchHeartBeat;
    }

    public void setHeartBeat(Date val) {
        heartBeat = val;
    }

    public Date getHeartBeat() {
        return heartBeat;
    }

    @Transient
    public void setStartupMode(String val) {
        startupMode = val;
    }

    @Transient
    public String getStartupMode() {
        return startupMode;
    }
}