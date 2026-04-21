package com.sos.joc.db.dailyplan;

import java.time.LocalDateTime;

import com.sos.commons.hibernate.annotations.SOSCreationTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_DPL_ORDER_VARIABLES)
public class DBItemDailyPlanVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]")
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[VARIABLE_VALUE]", nullable = false)
    private String variableValue;

    @Column(name = "[CREATED]", nullable = false)
    @SOSCreationTimestampUtc
    private LocalDateTime created;

    @Column(name = "[MODIFIED]", nullable = true)
    @SOSCurrentTimestampUtc
    private LocalDateTime modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String val) {
        variableValue = val;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

}