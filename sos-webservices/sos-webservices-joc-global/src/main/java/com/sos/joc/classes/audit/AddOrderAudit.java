package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.AddOrders;

public class AddOrderAudit extends AddOrder implements IAuditLog {

    @JsonIgnore
    private String folder;

    private String workflow;
    
    @JsonIgnore
    private String orderId;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    // @JsonIgnore
    private String controllerId;

    public AddOrderAudit(AddOrder addOrder, AddOrders addOrders, String orderId) {
        if (addOrder != null) {
            setScheduledFor(addOrder.getScheduledFor());
            this.workflow = addOrder.getWorkflowPath();
            this.orderId = orderId;
            setOrderName(addOrder.getOrderName());
            setArguments(addOrder.getArguments());
            setTimeZone(addOrder.getTimeZone());
            if (addOrder.getWorkflowPath() != null) {
                Path p = Paths.get(addOrder.getWorkflowPath());
                this.folder = p.getParent().toString().replace('\\', '/');
            }
        }
        if (addOrders != null) {
            setAuditParams(addOrders.getAuditLog());
            this.controllerId = addOrders.getControllerId();
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
    public String getControllerId() {
        return controllerId;
    }

    @Override
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

}
