package com.sos.joc.db.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.order.OrderStateText;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_ORDER_STATES)
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_ORDER_STATES_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_ORDER_STATES_SEQUENCE, allocationSize = 1)
public class DBItemHistoryOrderState extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_ORDER_STATES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /** Foreign key - TABLE_HISTORY_ORDERS.ID, KEY */
    @Column(name = "[MAIN_PARENT_ID]", nullable = false)
    private Long mainParentId;// db

    @Column(name = "[PARENT_ID]", nullable = false)
    private Long parentId;// db

    @Column(name = "[ORDER_ID]", nullable = false)
    private Long orderId;// HISTORY_ORDERS.ID

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[STATE_TIME]", nullable = false)
    private Date stateTime;

    @Column(name = "[STATE_EVENT_ID]", nullable = false)
    private String stateEventId;

    @Column(name = "[STATE_CODE]", nullable = true)
    private String stateCode;

    @Column(name = "[STATE_TEXT]", nullable = true)
    private String stateText;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryOrderState() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getMainParentId() {
        return mainParentId;
    }

    public void setMainParentId(Long val) {
        mainParentId = val;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long val) {
        parentId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
    }

    public Integer getState() {
        return state;
    }

    @Transient
    public OrderStateText getStateAsEnum() {
        return OrderStateText.fromValue(state);
    }

    public void setState(Integer val) {
        state = val;
    }

    @Transient
    public void setState(OrderStateText val) {
        setState(val == null ? null : val.intValue());
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date val) {
        stateTime = val;
    }

    public String getStateEventId() {
        return stateEventId;
    }

    public void setStateEventId(String val) {
        stateEventId = val;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String val) {
        stateCode = normalizeValue(val, 50);
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String val) {
        stateText = normalizeValue(val, 255);
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

}
