package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.order.ModifyOrders;

import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;

public class ModifyOrderAudit extends ModifyOrders implements IAuditLog {

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


    public ModifyOrderAudit(JOrder jOrders, String controllerId, AuditParams auditParams, Map<String, String> nameToPath) {
        setControllerId(controllerId);
        this.orderId = jOrders.id().string();
        setOrderIds(Collections.singleton(this.orderId));
        this.workflow = jOrders.workflowId().path().string();
        this.workflow = nameToPath.getOrDefault(this.workflow, this.workflow);
        Path f = Paths.get(this.workflow).getParent();
        this.folder = f == null ? "/" : f.toString().replace('\\', '/');
        setAuditParams(auditParams);
    }
    
    public ModifyOrderAudit(JFreshOrder jOrders, String controllerId, AuditParams auditParams, Map<String, String> nameToPath) {
        setControllerId(controllerId);
        this.orderId = jOrders.id().string();
        setOrderIds(Collections.singleton(this.orderId));
        this.workflow = jOrders.asScala().workflowPath().string();
        this.workflow = nameToPath.getOrDefault(this.workflow, this.workflow);
        Path f = Paths.get(this.workflow).getParent();
        this.folder = f == null ? "/" : f.toString().replace('\\', '/');
        setAuditParams(auditParams);
    }
    
    public ModifyOrderAudit(JOrder jOrders, String controllerId, ModifyOrders modifyOrders, Map<String, String> nameToPath) {
        setControllerId(controllerId);
        this.orderId = jOrders.id().string();
        setOrderIds(Collections.singleton(this.orderId));
        setKill(modifyOrders.getKill());
        setArguments(modifyOrders.getArguments());
        setOrderType(modifyOrders.getOrderType());
        this.workflow = jOrders.workflowId().path().string();
        this.workflow = nameToPath.getOrDefault(this.workflow, this.workflow);
        Path f = Paths.get(this.workflow).getParent();
        this.folder = f == null ? "/" : f.toString().replace('\\', '/');
        setAuditParams(modifyOrders.getAuditLog());
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
    @JsonIgnore
    public Long getDepHistoryId() {
        return null;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

}
