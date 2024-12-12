package com.sos.joc.db.inventory;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_DEPENDENCIES)
@Proxy(lazy = false)
public class DBItemInventoryDependency extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[INV_ID]", nullable = false)
    private Long invId;

    @Id
    @Column(name = "[INV_DEP_ID]", nullable = false)
    private Long invDependencyId;

    @Column(name = "[DEP_TYPE]", nullable = false)
    private Integer dependencyType;

    @Column(name = "[DEP_DEP_ID]", nullable = false)
    private Long depDependencyId;

    @Column(name = "[PUBLISHED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean published = false;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    public Integer getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(Integer type) {
        dependencyType = type;
    }

    @Transient
    public ConfigurationType getDependencyTypeAsEnum() {
        try {
            return ConfigurationType.fromValue(dependencyType);
        } catch (Exception e) {
            return null;
        }
    }

    @Transient
    public void setDependencyType(ConfigurationType type) {
        setDependencyType(type == null ? null : type.intValue());
    }

    public Long getInvId() {
        return invId;
    }

    public void setInvId(Long invId) {
        this.invId = invId;
    }

    public Long getInvDependencyId() {
        return invDependencyId;
    }

    public void setInvDependencyId(Long invDependencyId) {
        this.invDependencyId = invDependencyId;
    }

    public Long getDepDependencyId() {
        return depDependencyId;
    }

    public void setDepDependencyId(Long depDependencyId) {
        this.depDependencyId = depDependencyId;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        if (published == null) {
            this.published = false;
        }
        this.published = published;
    }

}
