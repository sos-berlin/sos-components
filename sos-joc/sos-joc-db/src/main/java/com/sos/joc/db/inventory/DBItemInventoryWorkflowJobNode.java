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
@Table(name = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONFIG_ID_JOB]",
        "[WORKFLOW_POSITION]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryWorkflowJobNode extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONFIG_ID_JOB]", nullable = false)
    private Long configIdJob;

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

    public Long getConfigIdJob() {
        return configIdJob;
    }

    public void getConfigIdJob(Long val) {
        configIdJob = val;
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
