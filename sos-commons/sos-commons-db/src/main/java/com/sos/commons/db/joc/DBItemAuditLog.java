package com.sos.commons.db.joc;

import java.io.Serializable;
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

@Entity
@Table(name = JocDBItemConstants.TABLE_AUDIT_LOG)
@SequenceGenerator(
		name = JocDBItemConstants.TABLE_AUDIT_LOG_SEQUENCE,
		sequenceName = JocDBItemConstants.TABLE_AUDIT_LOG_SEQUENCE,
		allocationSize = 1)
public class DBItemAuditLog implements Serializable {

    private static final long serialVersionUID = -2054646245027196877L;
    private Long id;
    private String schedulerId;
    private String account;
    private String request;
    private String parameters;
    private String job;
    private String jobChain;
    private String orderId;
    private String folder;
    private String comment;
    private Date created;
    private String ticketLink;
    private Integer timeSpent;
    private String calendar;

     /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_AUDIT_LOG_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_AUDIT_LOG_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    @Column(name = "`ACCOUNT`", nullable = false)
    public String getAccount() {
        return account;
    }
    
    @Column(name = "`ACCOUNT`", nullable = false)
    public void setAccount(String account) {
        this.account = account;
    }
    
    @Column(name = "`REQUEST`", nullable = false)
    public String getRequest() {
        return request;
    }
    
    @Column(name = "`REQUEST`", nullable = false)
    public void setRequest(String request) {
        this.request = request;
    }
    
    @Column(name = "`PARAMETERS`", nullable = true)
    public String getParameters() {
        return parameters;
    }
    
    @Column(name = "`PARAMETERS`", nullable = true)
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    @Column(name = "`JOB`", nullable = true)
    public String getJob() {
        return job;
    }
    
    @Column(name = "`JOB`", nullable = true)
    public void setJob(String job) {
        this.job = job;
    }
    
    @Column(name = "`JOB_CHAIN`", nullable = true)
    public String getJobChain() {
        return jobChain;
    }
    
    @Column(name = "`JOB_CHAIN`", nullable = true)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }
    
    @Column(name = "`ORDER_ID`", nullable = true)
    public String getOrderId() {
        return orderId;
    }
    
    @Column(name = "`ORDER_ID`", nullable = true)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    @Column(name = "`FOLDER`", nullable = true)
    public String getFolder() {
        return folder;
    }
    
    @Column(name = "`FOLDER`", nullable = true)
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    @Column(name = "`COMMENT`", nullable = true)
    public String getComment() {
        return comment;
    }
    
    @Column(name = "`COMMENT`", nullable = true)
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = true)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = true)
    public void setCreated(Date created) {
        this.created = created;
    }

    @Column(name = "`TICKET_LINK`", nullable = true)
    public String getTicketLink() {
        return ticketLink;
    }
    
    @Column(name = "`TICKET_LINK`", nullable = true)
    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }
    
    @Column(name = "`TIME_SPENT`", nullable = true)
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    @Column(name = "`TIME_SPENT`", nullable = true)
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    @Column(name = "`CALENDAR`", nullable = true)
    public String getCalendar() {
        return calendar;
    }
    
    @Column(name = "`CALENDAR`", nullable = true)
    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }
    
}