package com.sos.joc.db.inventory.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.inventory.common.ResponseFolderItem;

public class InventoryTreeFolderItem extends ResponseFolderItem {

    @JsonIgnore
    private Integer type;

    public InventoryTreeFolderItem(Long id, Integer type, String name, String title, boolean valid, boolean deleted, boolean deployed,
            Long countDeployments) {
        setId(id);
        this.type = type;
        setName(name);
        setTitle(title);
        setValid(valid);
        setDeleted(deleted);
        setDeployed(deployed);
        setHasDeployments(long2boolean(countDeployments));
    }

    @JsonIgnore
    public Integer getType() {
        return type;
    }

    @JsonIgnore
    public void setType(Integer val) {
        type = val;
    }
    
    private static boolean long2boolean(Long val) {
        return val != null && val.longValue() > 0;
    }

}
