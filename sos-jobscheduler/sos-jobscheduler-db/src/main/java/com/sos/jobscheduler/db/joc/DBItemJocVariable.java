package com.sos.jobscheduler.db.joc;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_VARIABLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemJocVariable implements Serializable {

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

    public DBItemJocVariable() {
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
        if (o == null || !(o instanceof DBItemJocVariable)) {
            return false;
        }
        DBItemJocVariable item = (DBItemJocVariable) o;
        if (!getName().equals(item.getName())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getName() == null ? "".hashCode() : getName().hashCode();
    }

}
