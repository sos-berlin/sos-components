package com.sos.js7.order.initiator.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBFilter;
import com.sos.joc.model.common.Folder;

public class FilterSchedules extends DBFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterSchedules.class);
    private List<String> listOfControllerIds;
    private List<String> listOfWorkflowNames;
    private List<Folder> listOfFolders;
    private Boolean released;
    private Boolean deleted;
    private List<String> listOfScheduleNames;

    public void addScheduleName(String scheduleName) {
        if (listOfScheduleNames == null) {
            listOfScheduleNames = new ArrayList<String>();
        }
        listOfScheduleNames.add(scheduleName);
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<String> getListOfControllerIds() {
        return listOfControllerIds;
    }

    public void setListOfControllerIds(List<String> listOfControllerIds) {
        this.listOfControllerIds = listOfControllerIds;
    }

    public void addListOfControllerIds(Collection<String> listOfControllerIds) {
        if (listOfControllerIds == null) {
            listOfControllerIds = new ArrayList<String>();
        }
        listOfControllerIds.addAll(listOfControllerIds);
    }

    public List<Folder> getListOfFolders() {
        return listOfFolders;
    }

    public void setListOfFolders(List<Folder> listOfFolders) {
        this.listOfFolders = listOfFolders;
    }

    public void addControllerId(String controllerId) {
        if (listOfControllerIds == null) {
            listOfControllerIds = new ArrayList<String>();
        }
        if (!listOfControllerIds.contains(controllerId)) {
            listOfControllerIds.add(controllerId);
        }
    }

    public List<String> getListOfWorkflowNames() {
        return listOfWorkflowNames;
    }

    public void setListOfWorkflowNames(List<String> listOfWorkflowNames) {
        this.listOfWorkflowNames = listOfWorkflowNames;
    }

    public List<String> getListOfScheduleNames() {
        return listOfScheduleNames;
    }

    public void setListOfScheduleNames(List<String> listOfScheduleNames) {
        this.listOfScheduleNames = listOfScheduleNames;
    }
    
    

}