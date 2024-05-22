package com.sos.joc.db.dailyplan;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_DPL_SUBMISSIONS)
@Proxy(lazy = false)
public class DBItemDailyPlanSubmission extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]")
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DPL_SUBMISSIONS_SEQUENCE)
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