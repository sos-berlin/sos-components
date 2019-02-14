package com.sos.webservices.order.initiator.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.general.SOSFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderPath;

public class FilterDailyPlan extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlan.class);
    private Date plannedStart;
    private Date plannedStartFrom;
    private Date plannedStartTo;
    private Boolean isLate;
    private String masterId;
    private String workflow;
    private Long calendarId;
    private String orderName;
    private String orderKey;
    private Boolean submitted;
    private List<OrderPath> listOfOrders;
    private List<String> states;
    private Set<Folder> listOfFolders;

    public Set<Folder> getListOfFolders() {
        return listOfFolders;
    }

    public void setListOfFolders(Set<Folder> listOfFolders) {
        this.listOfFolders = listOfFolders;
    }

    public FilterDailyPlan() {
        super();
        this.setSortMode("DESC");
        this.setOrderCriteria("plannedStart");
    }

    public void addFolderPaths(Set<Folder> folders) {
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }
        if (folders != null) {
            listOfFolders.addAll(folders);
        }
    }

    public void addFolderPath(String folder, boolean recursive) {
        LOGGER.debug("Add folder: " + folder);
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(folder);
        filterFolder.setRecursive(recursive);
        listOfFolders.add(filterFolder);
    }

    public List<String> getStates() {
        return states;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Boolean isLate() {
        return isLate != null && isLate;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        this.isLate = late;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public void addState(String state) {
        if (states == null) {
            states = new ArrayList<String>();
        }
        states.add(state);
    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    public Date getPlannedStart() {
        return this.plannedStart;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public boolean containsFolder(String path) {
        if (listOfFolders == null || listOfFolders.size() == 0) {
            return true;
        } else {
            Path p = Paths.get(path).getParent();
            String parent = "";
            if (p != null) {
                parent = p.toString().replace('\\', '/');
            }
            for (Folder folder : listOfFolders) {
                if ((folder.getRecursive() && (parent + "/").startsWith(folder.getFolder())) || folder.getFolder().equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Date getPlannedStartTo() {
        return plannedStartTo;
    }

    public void setPlannedStartTo(Date plannedStartTo) {
        this.plannedStartTo = plannedStartTo;
    }

    public Date getPlannedStartFrom() {
        return plannedStartFrom;
    }

    public void setPlannedStartFrom(Date plannedStartFrom) {
        this.plannedStartFrom = plannedStartFrom;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public List<OrderPath> getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(List<OrderPath> listOfOrders) {
        this.listOfOrders = listOfOrders;
    }

}