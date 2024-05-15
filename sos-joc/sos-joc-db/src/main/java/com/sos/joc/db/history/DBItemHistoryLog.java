package com.sos.joc.db.history;

import java.sql.Types;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_LOGS)
public class DBItemHistoryLog extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_HISTORY_LOGS_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[HO_MAIN_PARENT_ID]", nullable = false)
    private Long historyOrderMainParentId;

    @Column(name = "[HO_ID]", nullable = false)
    private Long historyOrderId;

    @Column(name = "[HOS_ID]", nullable = false)
    private Long historyOrderStepId;

    @Column(name = "[COMPRESSED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean compressed;

    @Column(name = "[FILE_BASENAME]", nullable = false)
    private String fileBasename;

    @Column(name = "[FILE_SIZE_UNCOMPRESSED]", nullable = false)
    private Long fileSizeUncompressed;

    @Column(name = "[FILE_LINES_UNCOMPRESSED]", nullable = false)
    private Long fileLinesUncompressed;

    // TODO 6.4.5.Final
    // @Type(type = "org.hibernate.type.BinaryType")
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "[FILE_CONTENT]", nullable = false)
    private byte[] fileContent;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public void setHistoryOrderMainParentId(Long val) {
        historyOrderMainParentId = val;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public void setHistoryOrderId(Long val) {
        historyOrderId = val;
    }

    public Long getHistoryOrderStepId() {
        return historyOrderStepId;
    }

    public void setHistoryOrderStepId(Long val) {
        historyOrderStepId = val;
    }

    public void setCompressed(boolean val) {
        compressed = val;
    }

    public boolean getCompressed() {
        return compressed;
    }

    public String getFileBasename() {
        return fileBasename;
    }

    public void setFileBasename(String val) {
        fileBasename = val;
    }

    public Long getFileSizeUncomressed() {
        return fileSizeUncompressed;
    }

    public void setFileSizeUncomressed(Long val) {
        fileSizeUncompressed = val;
    }

    public Long getFileLinesUncomressed() {
        return fileLinesUncompressed;
    }

    public void setFileLinesUncomressed(Long val) {
        fileLinesUncompressed = val;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] val) {
        fileContent = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    @Transient
    public boolean fileContentIsNull() {
        return fileContent == null;
    }
}
