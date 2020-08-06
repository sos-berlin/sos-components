package com.sos.joc.classes.audit;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.joc.model.common.JobSchedulerObjectType;

public class InventoryAudit implements IAuditLog {

    @JsonProperty("objectType")
    private JobSchedulerObjectType objectType;

    @JsonProperty("name")
    private String name;

    @JsonIgnore
    private String folder;

    @JsonIgnore
    private String comment;

    @JsonIgnore
    private Integer timeSpent;

    @JsonIgnore
    private String ticketLink;

    @JsonIgnore
    private Date startTime;

    public InventoryAudit(JobSchedulerObjectType type, String name) {
        objectType = type;
        this.name = name;
        folder = "";
    }

    @Override
    @JsonIgnore
    public String getComment() {
        return comment;
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
    public String getOrderId() {
        return null;
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
    public String getCalendar() {
        return null;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    @Override
    public String getJobschedulerId() {
        return "-";
    }

    public JobSchedulerObjectType getObjectType() {
        return objectType;
    }

    @Override
    public String getWorkflow() {
        // TODO Auto-generated method stub
        return null;
    }

}
