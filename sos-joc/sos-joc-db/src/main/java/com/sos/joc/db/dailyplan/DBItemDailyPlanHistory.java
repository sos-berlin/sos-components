package com.sos.joc.db.dailyplan;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_DPL_HISTORY)
@Proxy(lazy = false)
public class DBItemDailyPlanHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DPL_HISTORY_SEQUENCE)
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
    @Convert(converter = NumericBooleanConverter.class)
    private boolean submitted;

    @Column(name = "[MESSAGE]", nullable = false)
    private String message;

    @Temporal(TemporalType.DATE)
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

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String val) {
        userAccount = val;
    }

    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }

    public void setDailyPlanDate(Date val) {
        dailyPlanDate = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        if (val != null && val.length() > 2000) {
            message = val.substring(0, 2000);
        } else {
            message = val;
        }
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date val) {
        submissionTime = val;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean val) {
        submitted = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public Date getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Date val) {
        scheduledFor = val;
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

}