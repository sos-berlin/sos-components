package com.sos.joc.db.joc;

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
@Table(name = DBLayer.TABLE_JOC_AUDIT_LOG)
@SequenceGenerator(name = DBLayer.TABLE_JOC_AUDIT_LOG_SEQUENCE, sequenceName = DBLayer.TABLE_JOC_AUDIT_LOG_SEQUENCE, allocationSize = 1)
public class DBItemJocAuditLog extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_AUDIT_LOG_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[REQUEST]", nullable = false)
    private String request;

    @Column(name = "[PARAMETERS]", nullable = true)
    private String parameters;

    @Column(name = "[JOB]", nullable = true)
    private String job;

    @Column(name = "[WORKFLOW]", nullable = true)
    private String workflow;

    @Column(name = "[ORDER_ID]", nullable = true)
    private String orderId;

    @Column(name = "[CALENDAR]", nullable = true)
    private String calendar;

    @Column(name = "[FOLDER]", nullable = true)
    private String folder;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;

    @Column(name = "[CREATED]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "[TICKET_LINK]", nullable = true)
    private String ticketLink;

    @Column(name = "[TIME_SPENT]", nullable = true)
    private Integer timeSpent;

    public Long getId() {
        return this.id;
    }

    public void setId(Long val) {
        this.id = val;
    }

    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    public String getSchedulerId() {
        return this.schedulerId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        this.account = val;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String val) {
        this.request = val;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String val) {
        this.parameters = val;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String val) {
        this.job = val;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String val) {
        this.workflow = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        this.orderId = val;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String val) {
        this.calendar = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        this.folder = val;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String val) {
        this.comment = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        this.created = val;
    }

    public String getTicketLink() {
        return ticketLink;
    }

    public void setTicketLink(String val) {
        this.ticketLink = val;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer val) {
        this.timeSpent = val;
    }

}