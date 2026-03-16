package com.sos.joc.db.deployment;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_DEP_CONFIGURATIONS_JOIN)
@Proxy(lazy = false)
public class DBItemDepConfigurationJoin extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Id
    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Id
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[MAX_DATE]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date maxDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

}
