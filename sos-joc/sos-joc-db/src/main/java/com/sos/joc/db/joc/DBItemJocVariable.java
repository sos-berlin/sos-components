package com.sos.joc.db.joc;

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_JOC_VARIABLES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]" }) })
public class DBItemJocVariable extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[NUMERIC_VALUE]", nullable = true)
    private Long numericValue;

    @Column(name = "[TEXT_VALUE]", nullable = true)
    private String textValue;

    @JdbcTypeCode(Types.BINARY)
    @Column(name = "[BINARY_VALUE]", nullable = true)
    private byte[] binaryValue;

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

    public byte[] getBinaryValue() {
        return binaryValue;
    }

    public void setBinaryValue(byte[] val) {
        binaryValue = val;
    }
}
