package com.sos.joc.db.joc;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.audit.CategoryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_JOC_AUDIT_LOG)
@Proxy(lazy = false)
public class DBItemJocAuditLog extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_AUDIT_LOG_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[REQUEST]", nullable = false)
    private String request;

    @Column(name = "[PARAMETERS]", nullable = true)
    private String parameters;

    @Column(name = "[CATEGORY]", nullable = false)
    private Integer category;

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
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getControllerId() {
        return controllerId;
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
        request = val;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String val) {
        parameters = val;
    }

    public Integer getCategory() {
        return category;
    }

    @Transient
    public CategoryType getTypeAsEnum() {
        try {
            return CategoryType.fromValue(category);
        } catch (Exception e) {
            return CategoryType.UNKNOWN;
        }
    }

    public void setCategory(Integer val) {
        category = val;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String val) {
        comment = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public String getTicketLink() {
        return ticketLink;
    }

    public void setTicketLink(String val) {
        ticketLink = val;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer val) {
        timeSpent = val;
    }
}