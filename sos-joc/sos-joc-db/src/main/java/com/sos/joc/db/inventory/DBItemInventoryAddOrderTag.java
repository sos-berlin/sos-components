package com.sos.joc.db.inventory;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_ADD_ORDER_TAGS)
@Proxy(lazy = false)
public class DBItemInventoryAddOrderTag extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ORDER_ID_PATTERN]", nullable = false)
    private Long orderIdPattern;

    @Column(name = "[ORDER_TAGS]", nullable = false)
    private String orderTags;

    public Long getOrderIdPattern() {
        return orderIdPattern;
    }

    public void setOrderIdPattern(Long val) {
        orderIdPattern = val;
    }

    public String getOrderTags() {
        return orderTags;
    }

    public void setOrderTags(String val) {
        orderTags = val;
    }

}
