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
import com.sos.joc.db.inventory.InventoryMeta.ArgumentType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_WORKFLOW_ORDER]",
        "[NAME]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryWorkflowOrderVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Column(name = "[CID_WORKFLOW_ORDER]", nullable = false)
    private Long cidWorkflowOrder;

    @Column(name = "[TYPE]", nullable = false)
    private Long type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[VALUE]", nullable = false)
    private String value;

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

    public Long getCidWorkflowOrder() {
        return cidWorkflowOrder;
    }

    public void setCidWorkflowOrder(Long val) {
        cidWorkflowOrder = val;
    }

    public Long getType() {
        return type;
    }

    @Transient
    public ArgumentType getTypeAsEnum() {
        return ArgumentType.fromValue(type);
    }

    public void setType(Long val) {
        if (val == null) {
            val = ArgumentType.STRING.value();
        }
        type = val;
    }

    @Transient
    public void setType(ArgumentType val) {
        setType(val == null ? null : val.value());
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        value = val;
    }
}
