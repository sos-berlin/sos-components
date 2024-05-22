package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_TAGGINGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID]", "[TAG_ID]" }) })
@Proxy(lazy = false)
public class DBItemInventoryTagging extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_TAGGINGS_SEQUENCE)
    private Long id;

    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TAG_ID]", nullable = false)
    private Long tagId;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public ConfigurationType getTypeAsEnum() {
        try {
            return ConfigurationType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(ConfigurationType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long val) {
        tagId = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

}
