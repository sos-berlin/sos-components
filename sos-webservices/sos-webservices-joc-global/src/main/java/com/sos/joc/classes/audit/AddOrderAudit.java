package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.order.StartOrder;
import com.sos.joc.model.order.StartOrders;

public class AddOrderAudit extends StartOrder implements IAuditLog {

    @JsonIgnore
    private String folder;

    @JsonIgnore
    private String workflow;

    @JsonIgnore
    private Date startTime;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    // @JsonIgnore
    private String jobschedulerId;

    public AddOrderAudit(StartOrder startOrder, StartOrders startOrders) {
        if (startOrder != null) {
            setScheduledFor(startOrder.getScheduledFor());
            this.workflow = startOrder.getWorkflowPath();
            setOrderId(startOrder.getOrderId());
            setArguments(startOrder.getArguments());
            setTimeZone(startOrder.getTimeZone());
            if (startOrder.getWorkflowPath() != null) {
                Path p = Paths.get(startOrder.getWorkflowPath());
                this.folder = p.getParent().toString().replace('\\', '/');
            }
        }
        if (startOrders != null) {
            setAuditParams(startOrders.getAuditLog());
            this.jobschedulerId = startOrders.getJobschedulerId();
        }
    }

    private void setAuditParams(AuditParams auditParams) {
        if (auditParams != null) {
            this.comment = auditParams.getComment();
            this.timeSpent = auditParams.getTimeSpent();
            this.ticketLink = auditParams.getTicketLink();
        }
    }

    @Override
    @JsonIgnore
    public String getComment() {
        return comment;
    }

    @Override
    @JsonIgnore
    public Integer getTimeSpent() {
        return timeSpent;
    }

    @Override
    @JsonIgnore
    public String getTicketLink() {
        return ticketLink;
    }

    @Override
    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    @Override
    @JsonIgnore
    public String getJob() {
        return null;
    }

    @Override
    public String getWorkflow() {
        return workflow;
    }

    @Override
    @JsonIgnore
    public String getCalendar() {
        return null;
    }

    @Override
    // @JsonIgnore
    public String getJobschedulerId() {
        return jobschedulerId;
    }
}
