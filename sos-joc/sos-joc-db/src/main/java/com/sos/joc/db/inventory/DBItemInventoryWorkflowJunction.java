package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.JunctionType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JUNCTIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_WORKFLOW]", "[CID_JUNCTION]",
        "[TYPE]" }) })
public class DBItemInventoryWorkflowJunction extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id // fake id for annotation
    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Id // fake id for annotation
    @Column(name = "[CID_JUNCTION]", nullable = false)
    private Long cidJunction;

    @Id // fake id for annotation
    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

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

    public Integer getType() {
        return type;
    }

    @Transient
    public JunctionType getTypeAsEnum() {
        return JunctionType.fromValue(type);
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(JunctionType val) {
        setType(val == null ? null : val.intValue());
    }
}
