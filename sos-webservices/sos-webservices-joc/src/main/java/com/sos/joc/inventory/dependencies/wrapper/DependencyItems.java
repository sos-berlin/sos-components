package com.sos.joc.inventory.dependencies.wrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class DependencyItems {

    private Set<DependencyItem> requesteditems = new HashSet<DependencyItem>();
    private Map<Long, DBItemInventoryConfiguration> allUniqueItems = new HashMap<Long, DBItemInventoryConfiguration>();
    
    public DependencyItems (Map<Long, DBItemInventoryConfiguration> allUniqueItems) {
        this.allUniqueItems = this.allUniqueItems;
    }
    
    public Set<DependencyItem> getRequesteditems() {
        return requesteditems;
    }
    
    public void setRequesteditems(Set<DependencyItem> requesteditems) {
        this.requesteditems = requesteditems;
    }
    
    public Map<Long, DBItemInventoryConfiguration> getAllUniqueItems() {
        return allUniqueItems;
    }
    
    public void setAllUniqueItems(Map<Long, DBItemInventoryConfiguration> allUniqueItems) {
        this.allUniqueItems = allUniqueItems;
    }
    
}
