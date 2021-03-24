package com.sos.joc.db.yade;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_YADE_TRANSFERS)
@SequenceGenerator(name = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE, sequenceName = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE, allocationSize = 1)
public class DBItemYadeTransfer extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_YADE_TRANSFERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[JOB]", nullable = false)
    private String job;

    @Column(name = "[JOB_POSITION]", nullable = false)
    private String jobPosition;

    @Column(name = "[HOS_ID]", nullable = false)
    private Long historyOrderStepId;

    @Column(name = "[SOURCE_PROTOCOL_ID]", nullable = false)
    private Long sourceProtocolId;

    @Column(name = "[TARGET_PROTOCOL_ID]", nullable = true)
    private Long targetProtocolId;

    @Column(name = "[JUMP_PROTOCOL_ID]", nullable = true)
    private Long jumpProtocolId;

    @Column(name = "[OPERATION]", nullable = false)
    private Integer operation;

    @Column(name = "[PROFILE_NAME]", nullable = true)
    private String profileName;

    @Column(name = "[START]", nullable = false)
    private Date start;

    @Column(name = "[END]", nullable = true)
    private Date end;

    @Column(name = "[NUM_OF_FILES]", nullable = true)
    private Long numOfFiles;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[ERROR_MESSAGE]", nullable = true)
    private String errorMessage;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemYadeTransfer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String val) {
        job = val;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String val) {
        jobPosition = val;
    }

    public Long getHistoryOrderStepId() {
        return historyOrderStepId;
    }

    public void setHistoryOrderStepId(Long val) {
        historyOrderStepId = val;
    }

    public Long getSourceProtocolId() {
        return sourceProtocolId;
    }

    public void setSourceProtocolId(Long val) {
        sourceProtocolId = val;
    }

    public Long getTargetProtocolId() {
        return targetProtocolId;
    }

    public void setTargetProtocolId(Long val) {
        targetProtocolId = val;
    }

    public Long getJumpProtocolId() {
        return jumpProtocolId;
    }

    public void setJumpProtocolId(Long val) {
        jumpProtocolId = val;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer val) {
        operation = val;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String val) {
        profileName = val;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date val) {
        start = val;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date val) {
        end = val;
    }

    public Long getNumOfFiles() {
        return numOfFiles;
    }

    public void setNumOfFiles(Long val) {
        numOfFiles = val;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer val) {
        state = val;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String val) {
        errorMessage = normalizeErrorText(val);
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, 4000);
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}