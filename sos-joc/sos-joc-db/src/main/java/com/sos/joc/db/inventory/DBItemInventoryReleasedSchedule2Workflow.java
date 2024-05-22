package com.sos.joc.db.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.VIEW_INV_RELEASED_SCHEDULE2WORKFLOWS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULE_NAME]",
        "[WORKFLOW_NAME]" }) })
@Proxy(lazy = false)
public class DBItemInventoryReleasedSchedule2Workflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Id
    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Column(name = "[SCHEDULE_FOLDER]", nullable = false)
    private String scheduleFolder;

    @Column(name = "[SCHEDULE_CONTENT]", nullable = false)
    private String scheduleContent;

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

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

    public String getScheduleFolder() {
        return scheduleFolder;
    }

    public void setScheduleFolder(String val) {
        scheduleFolder = val;
    }

    public String getScheduleContent() {
        return scheduleContent;
    }

    public void setScheduleContent(String val) {
        scheduleContent = val;
    }
}
