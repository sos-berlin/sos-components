package com.sos.jitl.jobs.db.hibernateProxy;

import java.io.Serializable;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST_HIBERNATE_PROXY")
public class DBItemHibernateProxy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = "test")
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[VALUE]", nullable = false)
    private String value;

    public DBItemHibernateProxy() {
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

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        value = val;
    }

}
