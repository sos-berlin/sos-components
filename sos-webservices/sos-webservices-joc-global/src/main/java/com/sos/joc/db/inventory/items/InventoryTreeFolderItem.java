package com.sos.joc.db.inventory.items;

import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolderItem;
import com.sos.webservices.order.initiator.model.OrderTemplate;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    private String workflowPath;

    public InventoryTreeFolderItem(DBItemInventoryConfiguration conf, Long countDeployments) {
        if (conf != null) {
            if (conf.getContent() != null && ConfigurationType.ORDER.intValue() == conf.getType()) {
                try {
                    OrderTemplate ot = Globals.objectMapper.readValue(conf.getContent(), OrderTemplate.class);
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
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    private static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }

}
