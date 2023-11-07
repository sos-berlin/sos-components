package com.sos.joc.db.inventory.items;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class InventoryTagItem extends ResponseBaseSearchItem {
    
    private Long tagId;
    private String folder;
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long val) {
        this.tagId = val;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String val) {
        this.folder = val;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InventoryTagItem) == false) {
            return false;
        }
        InventoryTagItem rhs = ((InventoryTagItem) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }
    
}
