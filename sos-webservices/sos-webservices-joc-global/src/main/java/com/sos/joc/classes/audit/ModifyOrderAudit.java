package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.order.ModifyOrder;
import com.sos.joc.model.order.ModifyOrders;

public class ModifyOrderAudit extends ModifyOrder implements IAuditLog {

    @JsonIgnore
    private String folder;

    @JsonIgnore
    private String workflow;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    // @JsonIgnore
    private String jobschedulerId;

    public ModifyOrderAudit(ModifyOrder modifyOrder, String workflowPath, ModifyOrders modifyOrders) {
        if (modifyOrder != null) {
            this.workflow = workflowPath;
            setOrderId(modifyOrder.getOrderId());
            setArguments(modifyOrder.getArguments());
            setOrderType(modifyOrder.getOrderType());
            setPosition(modifyOrder.getPosition());
            setKill(modifyOrder.getKill());
            if (workflowPath != null) {
                Path p = Paths.get(workflowPath);
                this.folder = p.getParent().toString().replace('\\', '/');
            }
        }
        if (modifyOrders != null) {
            setAuditParams(modifyOrders.getAuditLog());
            this.jobschedulerId = modifyOrders.getJobschedulerId();
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
    @JsonIgnore
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
