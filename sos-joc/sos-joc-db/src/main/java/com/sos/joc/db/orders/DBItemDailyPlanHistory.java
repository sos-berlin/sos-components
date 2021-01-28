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

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[CATEGORY]", nullable = false)
    private String category;

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

    
    public String getCategory() {
        return category;
    }

    
    public void setCategory(String category) {
        this.category = category;
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

}