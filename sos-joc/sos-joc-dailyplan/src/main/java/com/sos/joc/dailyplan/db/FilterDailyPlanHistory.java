package com.sos.joc.dailyplan.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.joc.db.DBFilter;
import com.sos.joc.model.common.Folder;

public class FilterDailyPlanHistory extends DBFilter {

    private List<String> listOfControllerIds;
    private List<String> listOfOrderIds;
    private Boolean submitted;
    private String orderId;
    private Date dailyPlanDate;
    private Date dailyPlanDateFrom;
    private Date dailyPlanDateTo;
    private Set<Folder> setOfWorkflowFolders;

    public void setControllerId(String controllerId) {
        this.addControllerId(controllerId);
    }

    public void addControllerId(String controllerId) {
        if (controllerId != null) {
            if (listOfControllerIds == null) {
                listOfControllerIds = new ArrayList<String>();
            }
            listOfControllerIds.add(controllerId);
        }
    }

    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }

    public void setDailyPlanDate(Date dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    public Date getDailyPlanDateFrom() {
        return dailyPlanDateFrom;
    }

    public void setDailyPlanDateFrom(Date dailyPlanDateFrom) {
        this.dailyPlanDateFrom = dailyPlanDateFrom;
    }

    public Date getDailyPlanDateTo() {
        return dailyPlanDateTo;
    }

    public void setDailyPlanDateTo(Date dailyPlanDateTo) {
        this.dailyPlanDateTo = dailyPlanDateTo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public List<String> getListOfControllerIds() {
        return listOfControllerIds;
    }

    public void setListOfControllerIds(List<String> listOfControllerIds) {
        this.listOfControllerIds = listOfControllerIds;
    }

    public void addListOfControllerIds(Set<String> allowedControllers) {
        if (allowedControllers != null) {
            if (listOfControllerIds == null) {
                listOfControllerIds = new ArrayList<String>();
            }
            listOfControllerIds.addAll(allowedControllers);
        }
    }

    public void addListOfOrderIds(List<String> orderIds) {
        if (orderIds != null) {
            if (listOfOrderIds == null) {
                listOfOrderIds = new ArrayList<String>();
            }
            listOfOrderIds.addAll(orderIds);
        }
    }

    public Set<Folder> getSetOfWorkflowFolders() {
        return setOfWorkflowFolders;
    }

    public void setSetOfWorkflowFolders(Set<Folder> setOfWorkflowFolders) {
        this.setOfWorkflowFolders = setOfWorkflowFolders;
    }

    public void addFolder(Set<Folder> listOfFolders) {
        if (setOfWorkflowFolders == null) {
            setOfWorkflowFolders = new HashSet<Folder>();
        }
        setOfWorkflowFolders.addAll(listOfFolders);

    }

    public List<String> getListOfOrderIds() {
        return listOfOrderIds;
    }

    public void setListOfOrderIds(List<String> listOfOrderIds) {
        this.listOfOrderIds = listOfOrderIds;
    }

}