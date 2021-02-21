package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;

import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;

public class ModifyOrderAudit implements IAuditLog {

    @JsonIgnore
    private String folder;

    @JsonIgnore
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

    public ModifyOrderAudit(JOrder jOrders, String controllerId, AuditParams auditParams, Map<String, String> nameToPath) {
        this.controllerId = controllerId;
        this.orderId = jOrders.id().string();
        this.workflow = jOrders.workflowId().path().string();
        this.workflow = nameToPath.getOrDefault(this.workflow, this.workflow);
        Path f = Paths.get(this.workflow).getParent();
        this.folder = f == null ? "/" : f.toString().replace('\\', '/');
        setAuditParams(auditParams);
    }
    
    public ModifyOrderAudit(JFreshOrder jOrders, String controllerId, AuditParams auditParams, Map<String, String> nameToPath) {
        this.controllerId = controllerId;
        this.orderId = jOrders.id().string();
        this.workflow = jOrders.asScala().workflowPath().string();
        this.workflow = nameToPath.getOrDefault(this.workflow, this.workflow);
        Path f = Paths.get(this.workflow).getParent();
        this.folder = f == null ? "/" : f.toString().replace('\\', '/');
        setAuditParams(auditParams);
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
