package com.sos.jobscheduler.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_PARAMETER_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_PARAMETER_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_PARAMETER_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerParameterHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db
    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID, TABLE_SCHEDULER_ORDER_STEP_HISTORY.ID */
    private Long orderHistoryId;// db
    private Long orderStepHistoryId;// db
    /** Others */
    private Long paramType; // 0-order, 1- step start, 2-step end
    private String paramName;// event
    private String paramValue;// event

    private Date created;

    public DBItemSchedulerParameterHistory() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_PARAMETER_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_PARAMETER_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** Foreign key */
    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    @Column(name = "`ORDER_STEP_HISTORY_ID`", nullable = false)
    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    @Column(name = "`ORDER_STEP_HISTORY_ID`", nullable = false)
    public void setOrderStepHistoryId(Long val) {
        orderStepHistoryId = val;
    }

    /** Others */
    @Column(name = "`PARAM_TYPE`", nullable = false)
    public Long getParamType() {
        return paramType;
    }

    @Column(name = "`PARAM_TYPE`", nullable = false)
    public void setParamType(Long val) {
        paramType = val;
    }

    @Column(name = "`PARAM_NAME`", nullable = false)
    public String getParamName() {
        return paramName;
    }

    @Column(name = "`PARAM_NAME`", nullable = false)
    public void setParamName(String val) {
        paramName = val;
    }

    @Column(name = "`PARAM_VALUE`", nullable = false)
    public String getParamValue() {
        return paramValue;
    }

    @Column(name = "`PARAM_VALUE`", nullable = false)
    public void setParamValue(String val) {
        paramValue = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemSchedulerParameterHistory)) {
            return false;
        }
        DBItemSchedulerParameterHistory item = (DBItemSchedulerParameterHistory) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
