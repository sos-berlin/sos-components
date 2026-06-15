package com.sos.joc.db.reporting;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_REPORT_MAPPINGS)
public class DBItemReportMapping extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[REPORT_ID]", nullable = false)
    private Long reportId;

    @Id
    @Column(name = "[RUN_ID]", nullable = false)
    private Long runId;

    public Long getReportId() {
        return reportId;
    }
    public void setReportId(Long changeId) {
        this.reportId = changeId;
    }
    
    public Long getRunId() {
        return runId;
    }
    public void setRunId(Long invId) {
        this.runId = invId;
    }

}
