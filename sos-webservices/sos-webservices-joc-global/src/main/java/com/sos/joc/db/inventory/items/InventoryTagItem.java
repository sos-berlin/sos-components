package com.sos.joc.db.inventory.items;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class InventoryTagItem extends ResponseBaseSearchItem {
    
    private Long tagId;
    private Long taggingId;
    private Long cId;
    private String folder;
    private Integer type;
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long val) {
        this.tagId = val;
    }
    
    public Long getTaggingId() {
        return taggingId;
    }
    
    public void setTaggingId(Long val) {
        this.taggingId = val;
    }
    
    public Long getCId() {
        return cId;
    }
    
    public void setCId(Long val) {
        this.cId = val;
    }
    
    public void setType(Integer val) {
        this.type = val;
    }
    
    public Integer getType() {
        return type;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String val) {
        this.folder = val;
    }
    
    public String getNullableName() {
        String nullableName = getName();
        if (nullableName == null) {
            return "";
        }
        return nullableName;
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
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }
    
}
