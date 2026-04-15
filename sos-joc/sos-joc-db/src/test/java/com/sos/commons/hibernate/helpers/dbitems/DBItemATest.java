package com.sos.commons.hibernate.helpers.dbitems;

import java.time.Instant;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "A_TEST", uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
public class DBItemATest extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = "SEQ_A_TEST")
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[CREATED]", nullable = false, updatable = false, insertable = true)
    private Instant created;

    @Column(name = "[MODIFIED]", nullable = false, updatable = true)
    private Instant modified;

    public DBItemATest() {
    }

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

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant v) {
        created = v;
    }

    public void setModified(Instant v) {
        modified = v;
    }

    public Instant getModified() {
        return modified;
    }

}
