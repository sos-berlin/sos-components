package com.sos.joc.db.dailyplan;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DPL_SUBMISSIONS)
@SequenceGenerator(name = DBLayer.TABLE_DPL_SUBMISSIONS_SEQUENCE, sequenceName = DBLayer.TABLE_DPL_SUBMISSIONS_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanSubmission extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DPL_SUBMISSIONS_SEQUENCE)
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

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String val) {
        userAccount = val;
    }

    public Date getSubmissionForDate() {
        return submissionForDate;
    }

    public void setSubmissionForDate(Date val) {
        submissionForDate = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}