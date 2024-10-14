package com.sos.joc.db.history;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_HISTORY_ORDER_TAGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[ORDER_ID]", "[TAG_NAME]" }) })
@Proxy(lazy = false)
public class DBItemHistoryOrderTag extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_HISTORY_ORDER_TAGS_SEQUENCE)
    private Long id;

    @Column(name = "[HO_ID]", nullable = false)
    private Long historyId;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[TAG_NAME]", nullable = false)
    private String tagName;
    
    @Column(name = "[GROUP_ID]", nullable = false)
    private Long groupId;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[START_TIME]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    public DBItemHistoryOrderTag() {
        //
    }
    
    public DBItemHistoryOrderTag(String controllerId, String orderId, String tagName, Long groupId, Integer ordering, Date startTime) {
        setId(null);
        setControllerId(controllerId);
        setOrderId(orderId);
        setTagName(tagName);
        setGroupId(groupId);
        setStartTime(startTime);
        setOrdering(ordering);
        setHistoryId(0L);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long val) {
        if (val == null) {
            val = 0L;
        }
        historyId = val;
    }
    
    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String val) {
        tagName = val;
    }
    
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long val) {
        groupId = val;
    }
    
    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        if (val == null) {
            val = 0;
        }
        ordering = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

}
