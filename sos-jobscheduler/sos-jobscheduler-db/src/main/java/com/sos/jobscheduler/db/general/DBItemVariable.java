package com.sos.jobscheduler.db.general;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.GENERAL_TABLE_VARIABLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemVariable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[NUMERIC_VALUE]", nullable = true)
    private Long numericValue;

    @Column(name = "[TEXT_VALUE]", nullable = true)
    private String textValue;

    @Version
    @Column(name = "[LOCK_VERSION]", nullable = false)
    private Long lockVersion;

    public DBItemVariable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public void setNumericValue(Long val) {
        numericValue = val;
    }

    public Long getNumericValue() {
        return numericValue;
    }

    public void setTextValue(String val) {
        textValue = val;
    }

    public String getTextValue() {
        return textValue;
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Long val) {
        lockVersion = val;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemVariable)) {
            return false;
        }
        DBItemVariable item = (DBItemVariable) o;
        if (!getName().equals(item.getName())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getName() == null ? "".hashCode() : getName().hashCode();
    }

}
