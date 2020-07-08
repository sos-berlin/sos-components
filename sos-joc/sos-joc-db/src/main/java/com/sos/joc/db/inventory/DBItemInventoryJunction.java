package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_JUNCTIONS)
public class DBItemInventoryJunction extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CONFIG_ID]", nullable = false)
    private Long configId;

    @Column(name = "[LIFETIME]", nullable = false)
    private String lifetime;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long val) {
        configId = val;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String val) {
        lifetime = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }
}
