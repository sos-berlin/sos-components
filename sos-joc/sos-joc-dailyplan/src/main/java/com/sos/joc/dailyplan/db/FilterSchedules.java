package com.sos.joc.dailyplan.db;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.db.DBFilter;
import com.sos.joc.model.common.Folder;

public class FilterSchedules extends DBFilter {

    private List<Folder> folders;
    private List<String> controllerIds;
    private List<String> workflowNames;
    private List<String> scheduleNames;

    private Boolean released;
    private Boolean deleted;

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> val) {
        folders = val;
    }

    public List<String> getControllerIds() {
        return controllerIds;
    }

    public void setControllerIds(List<String> val) {
        controllerIds = val;
    }

    public void addControllerId(String controllerId) {
        if (controllerIds == null) {
            controllerIds = new ArrayList<String>();
        }
        if (!controllerIds.contains(controllerId)) {
            controllerIds.add(controllerId);
        }
    }

    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    public void setWorkflowNames(List<String> val) {
        workflowNames = val;
    }

    public List<String> getScheduleNames() {
        return scheduleNames;
    }

    public void setScheduleNames(List<String> val) {
        scheduleNames = val;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean val) {
        released = val;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean val) {
        deleted = val;
    }

}