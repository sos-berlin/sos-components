package com.sos.joc.db.inventory;

import java.time.LocalDateTime;

import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_JOB_TAGGINGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID]", "[JOB_NAME]", "[TAG_ID]" }) })
public class DBItemInventoryJobTagging extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_INV_JOB_TAGGINGS_SEQUENCE)
    private Long id;

    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;

    @Column(name = "[TAG_ID]", nullable = false)
    private Long tagId;

    @Column(name = "[MODIFIED]", nullable = false)
    @SOSCurrentTimestampUtc
    private LocalDateTime modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long val) {
        tagId = val;
    }

    public LocalDateTime getModified() {
        return modified;
    }

}
