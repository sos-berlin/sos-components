package com.sos.joc.db.inventory;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryMeta.JunctionType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_WORKFLOW]", "[CID_JUNCTION]",
        "[TYPE]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryWorkflowJunction extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Column(name = "[CID_JUNCTION]", nullable = false)
    private Long cidJunction;

    @Column(name = "[TYPE]", nullable = false)
    private Long type;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getCidWorkflow() {
        return cidWorkflow;
    }

    public void setCidWorkflow(Long val) {
        cidWorkflow = val;
    }

    public Long getCidJunction() {
        return cidJunction;
    }

    public void setCidJunction(Long val) {
        cidJunction = val;
    }

    public Long getType() {
        return type;
    }

    @Transient
    public JunctionType getTypeAsEnum() {
        return JunctionType.fromValue(type);
    }

    public void setType(Long val) {
        type = val;
    }

    @Transient
    public void setType(JunctionType val) {
        setType(val == null ? null : val.value());
    }
}
