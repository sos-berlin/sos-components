package com.sos.joc.db.inventory;

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

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[WORKFLOW_JOB_ID]",
        "[WORKFLOW_POSITION]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryWorkflowJobNode extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[WORKFLOW_JOB_ID]", nullable = false)
    private Long workflowJobId;

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition;

    @Column(name = "[LABEL]", nullable = false)
    private String label;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getWorkflowJobId() {
        return workflowJobId;
    }

    public void setWorkflowJobId(Long val) {
        workflowJobId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String val) {
        label = val;
    }

}
