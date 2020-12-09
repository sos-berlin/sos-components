package com.sos.js7.order.initiator.db;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.common.Folder;

public class FilterSchedules extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterSchedules.class);
    private List<String> listOfControllerIds;
    private List<Folder> listOfFolders;
    private Boolean released;
    private Boolean deleted;
    private List<String> listOfSchedules;

    public void addSchedulePath(String schedulePath) {
        if (listOfSchedules == null) {
            listOfSchedules = new ArrayList<String>();
        }
        listOfSchedules.add(schedulePath);
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

    public List<String> getListOfSchedules() {
        return listOfSchedules;
    }

    public void setListOfSchedules(List<String> listOfSchedules) {
        this.listOfSchedules = listOfSchedules;
    }

}