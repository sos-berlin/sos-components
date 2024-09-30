package com.sos.joc.db.inventory.items;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.joc.db.inventory.DBItemInventoryJobTagging;

public class InventoryJobTagItem extends DBItemInventoryJobTagging {
    
    private static final long serialVersionUID = 1L;
    private String tagName;
    private String group;
    private Integer ordering;
    
    public String getGroupedTagName() {
        if (group == null) {
            return tagName;
        } else {
            return group + ":" + tagName;
        }
    }
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String val) {
        this.tagName = val;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String val) {
        this.group = val;
    }
    
    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        this.ordering = val;
    }
    
//    @JsonIgnore
//    public String getNullableName() {
//        String nullableName = getName();
//        if (nullableName == null) {
//            return "";
//        }
//        return nullableName;
//    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(tagName).append(group).toString();
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
