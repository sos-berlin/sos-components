package com.sos.joc.db.dailyplan;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DPL_ORDER_VARIABLES)
@SequenceGenerator(name = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE, sequenceName = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DPL_ORDER_VARIABLES_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[PLANNED_ORDER_ID]", nullable = false)
    private Long plannedOrderId;

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

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long val) {
        plannedOrderId = val;
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