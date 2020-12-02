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
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.classes.PlannedOrder;

import js7.data.order.OrderId;

public class FilterDailyPlannedOrders extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlannedOrders.class);
    private String dailyPlanDate;
    private Date orderPlannedStartFrom;
    private Date orderPlannedStartTo;
    private Date submitTime;

    private String orderId;
    private Boolean submitted;
    private Set<PlannedOrder> setOfPlannedOrder;

    private Set<OrderId> setOfOrders;
    private List<String> listOfOrders;

    private List<OrderStateText> states;
    private Set<Folder> setOfFolders;
    private Date plannedStart;
    private Boolean isLate;
    private String controllerId;
    private List<String> listOfWorkflowPaths;
    private Long submissionHistoryId;
    private Long calendarId;
    private List<String> listOfSchedules;

    public List<String> getListOfOrders() {
        return listOfOrders;
    }

    public void setListOfOrders(List<String> listOfOrders) {
        this.listOfOrders = listOfOrders;
    }

    public void addOrderId(OrderId orderId) {
        if (setOfOrders == null) {
            setOfOrders = new HashSet<OrderId>();
        }
        setOfOrders.add(orderId);
    }

    public void addOrderId(String orderId) {
        if (listOfOrders == null) {
            listOfOrders = new ArrayList<String>();
        }
        listOfOrders.add(orderId);
    }
    
    public List<String> getListOfWorkflowPaths() {
        return listOfWorkflowPaths;
    }

    
    public void setListOfWorkflowPaths(List<String> listOfWorkflowPaths) {
        this.listOfWorkflowPaths = listOfWorkflowPaths;
    }    

    public void setDailyPlanDate(String dailyPlanDate) {
        if (dailyPlanDate != null) {
            this.dailyPlanDate = dailyPlanDate;
            setOrderPlanDateInterval();
        }
    }


    
    public void setListOfSchedules(List<String> listOfSchedules) {
        this.listOfSchedules = listOfSchedules;
    }
    
    public List<String> getListOfSchedules() {
        return listOfSchedules;
    }

    private void setOrderPlanDateInterval() {
        String timeZone = Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN);
        String periodBegin = Globals.sosCockpitProperties.getProperty("daily_plan_period_begin", Globals.DEFAULT_PERIOD_DAILY_PLAN);
        String dateInString = String.format("%s %s", dailyPlanDate, periodBegin);

        Optional<Instant> oInstant = JobSchedulerDate.getScheduledForInUTC(dateInString, timeZone);
        if (!oInstant.isPresent()) {
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

    public List<OrderStateText> getStates() {
        return states;
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

    public void addState(OrderStateText state) {
        if (states == null) {
            states = new ArrayList<OrderStateText>();
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

    
    public Long getCalendarId() {
        return calendarId;
    }

    
    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderKey) {
        this.orderId = orderKey;
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

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Set<PlannedOrder> getSetOfPlannedOrder() {
        return setOfPlannedOrder;
    }

    public void setSetOfPlannedOrder(Set<PlannedOrder> setOfPlannedOrder) {
        this.setOfPlannedOrder = setOfPlannedOrder;
    }

    public void addWorkflowPath(String workflowPath) {
        if (this.listOfWorkflowPaths == null) {
            this.listOfWorkflowPaths = new ArrayList<String>();
        }
        this.listOfWorkflowPaths.add(workflowPath);
    }

}