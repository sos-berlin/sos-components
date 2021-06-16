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
@Table(name = DBLayer.DAILY_PLAN_SUBMISSIONS_TABLE)
@SequenceGenerator(name = DBLayer.DAILY_PLAN_SUBMISSIONS_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_SUBMISSIONS_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanSubmissions extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_SUBMISSIONS_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[USER_ACCOUNT]", nullable = false)
    private String userAccount;
 

    @Temporal(TemporalType.DATE)
    @Column(name = "[SUBMISSION_FOR_DATE]", nullable = false)
    private Date submissionForDate;

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

    public Date getSubmissionForDate() {
        return submissionForDate;
    }

    public void setSubmissionForDate(Date submissionForDate) {
        this.submissionForDate = submissionForDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

 

  

}