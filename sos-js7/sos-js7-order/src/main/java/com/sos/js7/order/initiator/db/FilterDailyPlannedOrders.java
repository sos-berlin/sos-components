package com.sos.js7.order.initiator.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.common.Folder;

public class FilterDailyPlannedOrders extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlannedOrders.class);
    private String dailyPlanDate;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;

    private String orderKey;
    private Boolean submitted;
    private List<String> listOfOrders;
    private List<String> states;
    private Set<Folder> listOfFolders;
    private Date plannedStart;
    private Boolean isLate;
    private String controllerId;
    private String workflow;
    private Long submissionHistoryId;
    private String orderTemplateName;


    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
        setOrderPlanDateInterval();
    }

    public String getOrderTemplateName() {
        return orderTemplateName;
    }

    public void setOrderTemplateName(String orderTemplateName) {
        this.orderTemplateName = orderTemplateName;
    }

    private void setOrderPlanDateInterval() {
        String timeZone = Globals.sosCockpitProperties.getProperty("daily_plan_timezone");
        String periodBegin = Globals.sosCockpitProperties.getProperty("daily_plan_period_begin");
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Instant instant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone).get();
        orderPlannedStartFrom = Date.from(instant);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(orderPlannedStartFrom);
        calendar.add(java.util.Calendar.HOUR, 24);
        orderPlannedStartTo = calendar.getTime();
    }

    public Date getOrderPlannedStartFrom() {
        return orderPlannedStartFrom;
    }

    public Date getOrderPlannedStartTo() {
        return orderPlannedStartTo;
    }

    public Set<Folder> getListOfFolders() {
        return listOfFolders;
    }

    public void setListOfFolders(Set<Folder> listOfFolders) {
        this.listOfFolders = listOfFolders;
    }

    public FilterDailyPlannedOrders() {
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

    public Boolean isLate() {
        return isLate != null && isLate;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setLate(Boolean late) {
        this.isLate = late;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
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

    public List<String> getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(List<String> listOfOrders) {
        this.listOfOrders = listOfOrders;
    }

}