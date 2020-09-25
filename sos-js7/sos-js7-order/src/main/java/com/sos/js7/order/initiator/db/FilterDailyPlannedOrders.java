package com.sos.js7.order.initiator.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.SOSFilter;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;

import js7.data.order.OrderId;

public class FilterDailyPlannedOrders extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlannedOrders.class);
    private String dailyPlanDate;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;

    private String orderKey;
    private Boolean submitted;
    private Set<OrderId> setOfOrders;
    private List<String> listOfOrders;
    
    private List<String> states;
    private Set<Folder> setOfFolders;
    private Date plannedStart;
    private Boolean isLate;
    private String controllerId;
    private String workflow;
    private Long submissionHistoryId;
    private Set<String> orderTemplates;

  
    public List<String> getListOfOrders() {
        return listOfOrders;
    }

    
    public void setListOfOrders(List<String> listOfOrders) {
        this.listOfOrders = listOfOrders;
    }
    
    public void addOrderKey(OrderId orderKey) {
        if (setOfOrders == null) {
            setOfOrders = new HashSet<OrderId>();
        }
        setOfOrders.add(orderKey);
    }
    
    public void addOrderKey(String orderKey) {
        if (listOfOrders == null) {
            listOfOrders = new ArrayList<String>();
        }
        listOfOrders.add(orderKey);
    }

    
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
        setOrderPlanDateInterval();
    }

    public void addOrderTemplatePath(String orderTemplatePath) {
        if (orderTemplates == null) {
            orderTemplates = new HashSet<String>();
        }
        orderTemplates.add(orderTemplatePath);
    }

    public Set<String> getOrderTemplates() {
        return orderTemplates;
    }

    private void setOrderPlanDateInterval() {
        String timeZone = Globals.sosCockpitProperties.getProperty("daily_plan_timezone",Globals.DEFAULT_TIMEZONE_DAILY_PLAN);
        String periodBegin = Globals.sosCockpitProperties.getProperty("daily_plan_period_begin",Globals.DEFAULT_PERIOD_DAILY_PLAN);
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Optional<Instant>  oInstant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone);
        if (!oInstant.isPresent()){
            throw new JocMissingRequiredParameterException("wrong parameter (dailyPlanDate periodBegin -->" + periodBegin + " " + dateInString);
        }
        Instant instant = oInstant.get();
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
        return setOfFolders;
    }

    public void setListOfFolders(Set<Folder> listOfFolders) {
        this.setOfFolders = listOfFolders;
    }

    public FilterDailyPlannedOrders() {
        super();
        this.setSortMode("DESC");
        this.setOrderCriteria("plannedStart");
    }

    public void addFolderPaths(Set<Folder> folders) {
        if (setOfFolders == null) {
            setOfFolders = new HashSet<Folder>();
        }
        if (folders != null) {
            setOfFolders.addAll(folders);
        }
    }

    public void addFolderPath(String folder, boolean recursive) {
        LOGGER.debug("Add folder: " + folder);
        if (setOfFolders == null) {
            setOfFolders = new HashSet<Folder>();
        }
        Folder filterFolder = new Folder();
        filterFolder.setFolder(folder);
        filterFolder.setRecursive(recursive);
        setOfFolders.add(filterFolder);
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
        if (setOfFolders == null || setOfFolders.size() == 0) {
            return true;
        } else {
            Path p = Paths.get(path).getParent();
            String parent = "";
            if (p != null) {
                parent = p.toString().replace('\\', '/');
            }
            for (Folder folder : setOfFolders) {
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

    public Set<OrderId> getSetOfOrders() {
        return setOfOrders;
    }

    public void setSetOfOrders(Set<OrderId> setOfOrders) {
        this.setOfOrders = setOfOrders;
    }

}