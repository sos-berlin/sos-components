package com.sos.joc.db.inventory.items;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    public InventoryTreeFolderItem(Long id, Integer type, String path, String name, String title, boolean valid, boolean deleted, boolean deployed,
            Long countDeployments) {
        setId(id);
        setName(name);
        setTitle(title);
        setValid(valid);
        setDeleted(deleted);
        setDeployed(deployed);
        setObjectType(getType(type));
        setPath(path);
        setHasDeployments(long2boolean(countDeployments));
    }

    private static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }
    
    private static ConfigurationType getType(Integer type) {
        ConfigurationType result = null;
        try {
            result = ConfigurationType.fromValue(type);
        } catch (Exception e) {
        }
        return result;
    }

}
