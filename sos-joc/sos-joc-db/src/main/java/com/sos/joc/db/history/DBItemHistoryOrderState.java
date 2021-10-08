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
import com.sos.joc.db.common.HistoryConstants;
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

    /** Foreign key - TABLE_HISTORY_ORDERS.MAIN_PARENT_ID */
    @Column(name = "[HO_MAIN_PARENT_ID]", nullable = false)
    private Long historyOrderMainParentId;// db

    @Column(name = "[HO_PARENT_ID]", nullable = false)
    private Long historyOrderParentId;// db

    @Column(name = "[HO_ID]", nullable = false)
    private Long historyOrderId;// TABLE_HISTORY_ORDERS.ID

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

    public Long hetHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public void setHistoryOrderMainParentId(Long val) {
        historyOrderMainParentId = val;
    }

    public Long getHistoryOrderParentId() {
        return historyOrderParentId;
    }

    public void setHistoryOrderParentId(Long val) {
        historyOrderParentId = val;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public void setHistoryOrderId(Long val) {
        historyOrderId = val;
    }

    public Integer getState() {
        return state;
    }

    @Transient
    public OrderStateText getStateAsEnum() {
        try {
            return OrderStateText.fromValue(state);
        } catch (Throwable e) {
            return OrderStateText.UNKNOWN;
        }
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
        stateCode = normalizeValue(val, HistoryConstants.MAX_LEN_STATE_CODE);
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String val) {
        stateText = normalizeValue(val, HistoryConstants.MAX_LEN_STATE_TEXT);
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

}
