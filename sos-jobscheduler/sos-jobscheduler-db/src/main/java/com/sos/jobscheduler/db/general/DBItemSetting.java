package com.sos.jobscheduler.db.general;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.GENERAL_TABLE_SETTINGS)
public class DBItemSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long numericValue;
    private String textValue;
    private Long lockVersion;

    public DBItemSetting() {
    }

    @Id
    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    @Version
    @Column(name = "`LOCK_VERSION`", nullable = false)
    public Long getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Long val) {
        lockVersion = val;
    }

    @Column(name = "`NUMERIC_VALUE`", nullable = true)
    public void setNumericValue(Long val) {
        numericValue = val;
    }

    @Column(name = "`NUMERIC_VALUE`", nullable = true)
    public Long getNumericValue() {
        return numericValue;
    }

    @Column(name = "`TEXT_VALUE`", nullable = true)
    public void setTextValue(String val) {
        textValue = val;
    }

    @Column(name = "`TEXT_VALUE`", nullable = true)
    public String getTextValue() {
        return textValue;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemSetting)) {
            return false;
        }
        DBItemSetting item = (DBItemSetting) o;
        if (!getName().equals(item.getName())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getName() == null ? "".hashCode() : getName().hashCode();
    }

}
