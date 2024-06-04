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
@Table(name = DBLayer.TABLE_HISTORY_ORDER_TAGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[MAIN_ORDER_ID]", "[TAG_NAME]" }) })
@Proxy(lazy = false)
public class DBItemHistoryOrderTag extends DBItem {

    private static final long serialVersionUID = 1L;
    private static final int mainOrderIdLength = 25;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_HISTORY_ORDER_TAGS_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private String orderId;// event

    @Column(name = "[TAG_NAME]", nullable = false)
    private String tagName;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    public DBItemHistoryOrderTag() {
        //
    }
    
    public DBItemHistoryOrderTag(String controllerId, String orderId, String tagName, Integer ordering, Date created) {
        setId(null);
        setControllerId(controllerId);
        setOrderId(orderId);
        setTagName(tagName);
        setCreated(created);
        setOrdering(ordering);
    }
    
    public DBItemHistoryOrderTag(String controllerId, String orderId, String tagName, Integer ordering) {
        setId(null);
        setControllerId(controllerId);
        setOrderId(orderId);
        setTagName(tagName);
        setOrdering(ordering);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
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
        if (val != null && val.length() > mainOrderIdLength) {
            orderId = val.substring(0, mainOrderIdLength);
        } else {
            orderId = val;
        }
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String val) {
        tagName = val;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}
