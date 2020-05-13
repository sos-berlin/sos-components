package com.sos.jobscheduler.db.cluster;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_CLUSTER)
public class DBItemJocCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[MEMBER_ID]", nullable = false)
    private String memberId;// host:appData

    @Version
    @Column(name = "[HEART_BEAT]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date heartBeat;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String val) {
        memberId = val;
    }

    public void setHeartBeat(Date val) {
        heartBeat = val;
    }

    public Date getHeartBeat() {
        return heartBeat;
    }

}