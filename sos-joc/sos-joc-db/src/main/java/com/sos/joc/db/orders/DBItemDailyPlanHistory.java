package com.sos.joc.db.orders;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.DAILY_PLAN_HISTORY_TABLE)
@SequenceGenerator(name = DBLayer.DAILY_PLAN_HISTORY_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_HISTORY_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_HISTORY_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[USER_ACCOUNT]", nullable = false)
    private String userAccount;

    @Column(name = "[ORDER_ID]", nullable = true)
    private String orderId;

    @Column(name = "[WORKFLOW_PATH]", nullable = true)
    private String workflowPath;
    
    @Column(name = "[WORKFLOW_FOLDER]", nullable = true)
    private String workflowFolder;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SCHEDULED_FOR]", nullable = true)
    private Date scheduledFor;

    @Column(name = "[SUBMITTED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean submitted;

    @Column(name = "[MESSAGE]", nullable = false)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[DAILY_PLAN_DATE]", nullable = false)
    private Date dailyPlanDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBMISSION_TIME]", nullable = false)
    private Date submissionTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String jobschedulerId) {
        this.controllerId = jobschedulerId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }

    public void setDailyPlanDate(Date dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    
    public String getWorkflowPath() {
        return workflowPath;
    }

    
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    
    public Date getScheduledFor() {
        return scheduledFor;
    }

    
    public void setScheduledFor(Date scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    
    public String getWorkflowFolder() {
        return workflowFolder;
    }

    
    public void setWorkflowFolder(String workflowFolder) {
        this.workflowFolder = workflowFolder;
    }

}