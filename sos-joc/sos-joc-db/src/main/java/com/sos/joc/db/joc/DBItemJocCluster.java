package com.sos.joc.db.joc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

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

    @Version
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

    public Date getSwitchHeartBeat() {
        return switchHeartBeat;
    }

    public void setHeartBeat(Date val) {
        heartBeat = val;
    }

    public Date getHeartBeat() {
        return heartBeat;
    }

    public void setStartupMode(String val) {
        startupMode = val;
    }

    public String getStartupMode() {
        return startupMode;
    }
}