package com.sos.joc.db.dailyplan;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DPL_ORDER_VARIABLES)
@SequenceGenerator(name = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE, sequenceName = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[VARIABLE_VALUE]", nullable = false)
    private String variableValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = true)
    private Date modified;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

}