package com.sos.joc.db.inventory;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_CHANGES_MAPPINGS)
@Proxy(lazy = false)
public class DBItemInventoryChangesMapping extends DBItem {

    private static final long serialVersionUID = 8814170718256373102L;

    @Id
    @Column(name = "[CHANGE_ID]", nullable = false)
    private Long changeId;

    @Id
    @Column(name = "[INV_ID]", nullable = false)
    private Long invId;

    @Column(name ="[TYPE]", nullable = false)
    private ConfigurationType type;
    
    public Long getChangeId() {
        return changeId;
    }
    public void setChangeId(Long changeId) {
        this.changeId = changeId;
    }
    
    public Long getInvId() {
        return invId;
    }
    public void setInvId(Long invId) {
        this.invId = invId;
    }
    
    public ConfigurationType getType() {
        return type;
    }
    public void setType(ConfigurationType type) {
        this.type = type;
    }

}
