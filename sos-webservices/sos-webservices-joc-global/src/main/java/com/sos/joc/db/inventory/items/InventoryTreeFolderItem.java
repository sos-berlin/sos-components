package com.sos.joc.db.inventory.items;

import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    public InventoryTreeFolderItem(DBItemInventoryConfiguration conf, Long countDeployments, Long countReleases) {
        if (conf != null) {
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
    
    public InventoryTreeFolderItem(DBItemInventoryConfigurationTrash conf) {
        if (conf != null) {
            conf.setContent(null);
            setId(conf.getId());
            setName(conf.getName());
            setTitle(conf.getTitle());
            setValid(conf.getValid());
            setObjectType(JocInventory.getType(conf.getType()));
            setPath(conf.getPath());
        }
        setHasDeployments(null);
        setHasReleases(null);
    }

    private static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }

}
