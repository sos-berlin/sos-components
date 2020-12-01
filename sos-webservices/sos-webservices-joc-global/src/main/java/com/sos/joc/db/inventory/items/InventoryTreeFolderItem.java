package com.sos.joc.db.inventory.items;

import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.webservices.order.initiator.model.Schedule;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    private String workflowPath;

    public InventoryTreeFolderItem(DBItemInventoryConfiguration conf, Long countDeployments, Long countReleases) {
        if (conf != null) {
            if (conf.getContent() != null && ConfigurationType.SCHEDULE.intValue() == conf.getType()) {
                try {
                    Schedule ot = Globals.objectMapper.readValue(conf.getContent(), Schedule.class);
                    workflowPath = ot.getWorkflowPath();
                } catch (Exception e) {
                }
            }
            conf.setContent(null);
            setId(conf.getId());
            setName(conf.getName());
            setTitle(conf.getTitle());
            setValid(conf.getValid());
            setDeleted(conf.getDeleted());
            setDeployed(conf.getDeployed());
            setReleased(conf.getReleased());
            setObjectType(JocInventory.getType(conf.getType()));
            setPath(conf.getPath());
        }
        setHasDeployments(long2boolean(countDeployments));
        setHasReleases(long2boolean(countReleases));
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    private static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }

}
