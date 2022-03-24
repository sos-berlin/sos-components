package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.VIEW_INV_SCHEDULE2WORKFLOWS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULE_NAME]", "[WORKFLOW_NAME]" }) })
public class DBItemInventorySchedule2Workflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Id
    /** can be null if draft */
    @Column(name = "[WORKFLOW_NAME]", nullable = true)
    private String workflowName;

    @Column(name = "[RELEASED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean released;

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setSchedulePath(String val) {
        schedulePath = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public boolean getReleased() {
        return released;
    }

    public void setReleased(boolean val) {
        released = val;
    }

}
