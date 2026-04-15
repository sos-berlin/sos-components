package com.sos.joc.db.inventory;

import java.util.Date;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_JOB_TAGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemInventoryJobTag extends DBItem implements IDBItemTag {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_JOB_TAGS_SEQUENCE)
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[GROUP_ID]", nullable = false)
    private Long groupId;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long val) {
        groupId = val;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        if (val == null) {
            val = 0;
        }
        ordering = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

}
