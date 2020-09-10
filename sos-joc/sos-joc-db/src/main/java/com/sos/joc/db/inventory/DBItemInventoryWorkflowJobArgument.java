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
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JOB_ARGUMENTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[WORKFLOW_JOB_ID]", "[NAME]" }) })
public class DBItemInventoryWorkflowJobArgument extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id // fake id for annotation
    @Column(name = "[WORKFLOW_JOB_ID]", nullable = false)
    private Long workflowJobId;

    @Id // fake id for annotation
    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[VALUE]", nullable = false)
    private String value;

    public Long getWorkflowJobId() {
        return workflowJobId;
    }

    public void setWorkflowJobId(Long val) {
        workflowJobId = val;
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
