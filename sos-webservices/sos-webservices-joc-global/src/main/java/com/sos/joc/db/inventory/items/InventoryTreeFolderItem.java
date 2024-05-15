package com.sos.joc.db.inventory.items;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    public void setCountDeployed(Long val) {
        setHasDeployments(val.intValue() > 0);
    }
    
    public void setCountReleased(Long val) {
        setHasReleases(val.intValue() > 0);
    }
    
    public void setType(Integer val) {
        try {
            setObjectType(ConfigurationType.fromValue(val));
        } catch (Throwable e) {// e.g. unknown type
        }
    }

    public ResponseFolderItem toResponseFolderItem() {
        if (getObjectType() == null) {
            return null;
        }
        return this;
    }
}
