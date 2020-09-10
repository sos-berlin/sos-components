package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ArgumentType;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_ORDER_VARIABLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_WORKFLOW_ORDER]",
        "[NAME]" }) })
public class DBItemInventoryWorkflowOrderVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id // fake id for annotation
    @Column(name = "[CID_WORKFLOW_ORDER]", nullable = false)
    private Long cidWorkflowOrder;

    @Id // fake id for annotation
    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[VALUE]", nullable = false)
    private String value;

    public Long getCidWorkflowOrder() {
        return cidWorkflowOrder;
    }

    public void setCidWorkflowOrder(Long val) {
        cidWorkflowOrder = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public ArgumentType getTypeAsEnum() {
        return ArgumentType.fromValue(type);
    }

    public void setType(Integer val) {
        if (val == null) {
            val = ArgumentType.STRING.intValue();
        }
        type = val;
    }

    @Transient
    public void setType(ArgumentType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        value = val;
    }
}
