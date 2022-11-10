package com.sos.joc.db.inventory.items;

import java.util.Collections;
import java.util.List;

import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

public class InventoryTreeFolderItem {

    private Long id;
    private Integer type;
    private String path;
    private String name;
    private String title;
    private String json;
    private boolean valid;
    private boolean deleted;
    private boolean deployed;
    private boolean released;
    private Number countDeployed;
    private Number countReleased;

    public ResponseFolderItem toResponseFolderItem() {
        try {
            ResponseFolderItem item = new ResponseFolderItem();
            item.setId(id);
            item.setPath(path);
            item.setName(name);
            item.setObjectType(ConfigurationType.fromValue(type));
            if (ConfigurationType.SCHEDULE.intValue() == type) { //JOC-1415
                Schedule schedule = Globals.objectMapper.readValue(json, Schedule.class);
                List<String> workflows = schedule.getWorkflowNames();
                if (workflows == null && schedule.getWorkflowName() != null) {
                    workflows = Collections.singletonList(schedule.getWorkflowName());
                }
                item.setWorkflowNames(workflows);
            } else {
                item.setWorkflowNames(null);
            }
            item.setTitle(title);
            item.setValid(valid);
            item.setDeleted(deleted);
            item.setDeployed(deployed);
            item.setReleased(released);
            item.setHasDeployments(countDeployed.intValue() > 0);
            item.setHasReleases(countReleased.intValue() > 0);
            return item;
        } catch (Throwable e) {// e.g. unknown type
            return null;
        }
    }
}
