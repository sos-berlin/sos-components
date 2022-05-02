package com.sos.jitl.jobs.checkhistory.classes;

import java.util.List;

import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderStateText;

public class HistoryFilter {

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    private String controllerId;
    private String job;
    private String workflow;
    private List<OrderStateText> states;
    private String dateFrom;
    private String dateTo;
    private String endDateFrom;
    private String endDateTo;
    private String timeZone;
    private List<Folder> folders;
    private List<HistoryStateText> historyStates;

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public List<OrderStateText> getStates() {
        return states;
    }

    public void setStates(List<OrderStateText> states) {
        this.states = states;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getEndDateFrom() {
        return endDateFrom;
    }

    public void setEndDateFrom(String endDateFrom) {
        this.endDateFrom = endDateFrom;
    }

    public String getEndDateTo() {
        return endDateTo;
    }

    public void setEndDateTo(String endDateTo) {
        this.endDateTo = endDateTo;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<HistoryStateText> getHistoryStates() {
        return historyStates;
    }

    public void setHistoryStates(List<HistoryStateText> historyStates) {
        this.historyStates = historyStates;
    }

}
