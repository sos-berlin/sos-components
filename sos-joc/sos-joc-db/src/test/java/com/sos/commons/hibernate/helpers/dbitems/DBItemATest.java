package com.sos.commons.hibernate.helpers.dbitems;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.SourceType;

import com.sos.commons.hibernate.annotations.SOSIdGenerator;
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
    @SOSIdGenerator(sequenceName = "SEQ_A_TEST")
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[JAVA_DATE_MANUAL]", nullable = false, updatable = false, insertable = true)
    private Date javaDateManual;

    /** @CreationTimestamp is a synonym for {@link CurrentTimestamp @CurrentTimestamp(timing=INSERT,source=VM)}. */
    @CreationTimestamp
    @Column(name = "[JAVA_DATE_AUTO]", nullable = false, updatable = false, insertable = true)
    private Date javaDateAuto;

    @CurrentTimestamp(source = SourceType.DB)
    @Column(name = "[DB_CURRENT_TIMESTAMP_AUTO]", nullable = false, updatable = true)
    private Date dbCurrentTimestampAuto;

    @Column(name = "[DB_CURRENT_TIMESTAMP_UTC_AUTO]", nullable = false, updatable = true)
    private Date dbCurrentTimestampUtcAuto;

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

    public Date getJavaDateManual() {
        return javaDateManual;
    }

    public void setJavaDateManual(Date v) {
        javaDateManual = v;
    }

    public Date getJavaDateAuto() {
        return javaDateAuto;
    }

    public Date getDbCurrentTimestampAuto() {
        return dbCurrentTimestampAuto;
    }

    public Date getDbCurrentTimestampUtcAuto() {
        return dbCurrentTimestampUtcAuto;
    }
}
