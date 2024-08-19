
package com.sos.joc.classes.dependencies.items;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class ReferencedDbItem {

    private DBItemInventoryConfiguration referencedItem;
    private List<DBItemInventoryConfiguration> references = new ArrayList<DBItemInventoryConfiguration>();
    private List<DBItemInventoryConfiguration> referencedBy = new ArrayList<DBItemInventoryConfiguration>();

    protected ReferencedDbItem() {
    }
    
    public ReferencedDbItem(DBItemInventoryConfiguration inventoryDbItem) {
        this.referencedItem = inventoryDbItem;
    }
    
    public Long getId() {
        if (referencedItem != null) {
            return referencedItem.getId();
        }
        return null;
    }

    public String getName() {
        if (referencedItem != null) {
            return referencedItem.getName();
        }
        return null;
    }

    public ConfigurationType getType() {
        if (referencedItem != null) {
            return referencedItem.getTypeAsEnum();
        }
        return null;
    }
    
    public DBItemInventoryConfiguration getReferencedItem() {
        return referencedItem;
    }
    public void setReferencedItem(DBItemInventoryConfiguration referencedItem) {
        this.referencedItem = referencedItem;
    }

    public List<DBItemInventoryConfiguration> getReferences() {
        return references;
    }
    public void setReferences(List<DBItemInventoryConfiguration> references) {
        this.references = references;
    }

    public List<DBItemInventoryConfiguration> getReferencedBy() {
        return referencedBy;
    }
    public void setReferencedBy(List<DBItemInventoryConfiguration> referencedBy) {
        this.referencedBy = referencedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("referencedItem", referencedItem).append("referencedBy", referencedBy).
                append("references", references).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(referencedItem).append(referencedBy).append(references).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReferencedDbItem) == false) {
            return false;
        }
        ReferencedDbItem rhs = ((ReferencedDbItem) other);
        return new EqualsBuilder().append(referencedItem, rhs.referencedItem).append(referencedBy, rhs.referencedBy).
                append(references, rhs.references).isEquals();
    }

}
