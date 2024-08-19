package com.sos.joc.db.inventory;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class InventoryDependencyPK implements Serializable {

    private static final long serialVersionUID = 1L;
    protected Long invId;
    protected Long invDependencyId;
    
    public InventoryDependencyPK() {}
    
    public InventoryDependencyPK(Long invId, Long invDependencyId) {
        this.invId = invId;
        this.invDependencyId = invDependencyId;
        
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("invId", invId).append("invDependencyId", invDependencyId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(invId).append(invDependencyId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InventoryDependencyPK) == false) {
            return false;
        }
        InventoryDependencyPK rhs = ((InventoryDependencyPK) other);
        return new EqualsBuilder().append(invId, rhs.invId).append(invDependencyId, rhs.invDependencyId).isEquals();
    }

}
