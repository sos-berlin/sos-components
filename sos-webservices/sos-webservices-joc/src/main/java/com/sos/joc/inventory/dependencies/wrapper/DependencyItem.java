package com.sos.joc.inventory.dependencies.wrapper;

import java.util.HashSet;
import java.util.Set;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class DependencyItem {

    private DBItemInventoryConfiguration requestedItem;
    private Set<Long> referencedByIds = new HashSet<Long>();
    private Set<Long> referencesIds = new HashSet<Long>();
    
    public DependencyItem(DBItemInventoryConfiguration requestedItem) {
        this.requestedItem = requestedItem;;
    }
    
    public Set<Long> getReferencedByIds() {
        return referencedByIds;
    }
    
    public void setReferencedByIds(Set<Long> referencedByIds) {
        this.referencedByIds = referencedByIds;
    }
    
    public Set<Long> getReferencesIds() {
        return referencesIds;
    }
    
    public void setReferencesIds(Set<Long> referencesIds) {
        this.referencesIds = referencesIds;
    }

    
    public DBItemInventoryConfiguration getRequestedItem() {
        return requestedItem;
    }
    
    public void setRequestedItem(DBItemInventoryConfiguration requestedItem) {
        this.requestedItem = requestedItem;
    }
    
}
